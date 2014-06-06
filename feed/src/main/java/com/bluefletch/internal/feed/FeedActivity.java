package com.bluefletch.internal.feed;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.IconButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluefletch.internal.feed.adapter.PostArrayAdapter;
import com.bluefletch.internal.feed.rest.Comment;
import com.bluefletch.internal.feed.rest.Post;
import com.bluefletch.internal.feed.rest.User;
import com.bluefletch.internal.feed.service.BusProvider;
import com.bluefletch.internal.feed.service.ResizeImageAsyncTask;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.nhaarman.listviewanimations.itemmanipulation.AnimateAdditionAdapter;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import timber.log.Timber;

import static com.bluefletch.internal.feed.service.AppEvents.CreatePostEvent;
import static com.bluefletch.internal.feed.service.AppEvents.FeedLoadedError;
import static com.bluefletch.internal.feed.service.AppEvents.FeedLoadedEvent;
import static com.bluefletch.internal.feed.service.AppEvents.LoadFeedEvent;
import static com.bluefletch.internal.feed.service.AppEvents.LogoutEvent;
import static com.bluefletch.internal.feed.service.AppEvents.PostCreateError;
import static com.bluefletch.internal.feed.service.AppEvents.PostCreatedEvent;
import static com.bluefletch.internal.feed.service.AppEvents.ProfilePictureUpdated;
import static com.bluefletch.internal.feed.service.AppEvents.UploadProfilePictureEvent;
import static com.bluefletch.internal.feed.service.AppEvents.ValidateAuthenticationEvent;

/**
 * Skeleton feed activity - contains an action bar with a logout and refresh icon (currently do nothing),
 * performs a feed lookup on creation.  Also handles situations where the user becomes logged out but
 * somehow gets back to this view.
 */
public class FeedActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {

    @InjectView(R.id.list) protected ListView listView;
    @InjectView(R.id.container) protected SwipeRefreshLayout swipeLayout;
    @InjectView(R.id.add_post_text_input) protected EditText postTextInput;
    @InjectView(R.id.save_post_button) protected IconButton saveButton;
    @InjectView(R.id.reply_to_post_text) protected TextView replyText;


    private Bus mBus = BusProvider.getInstance();
    private final Integer DEFAULT_DAYS_BACK = 14;

    private AnimateAdditionAdapter<String> mAnimateAdditionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);


        ButterKnife.inject(this);

        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_blue_dark,
                android.R.color.holo_blue_light,
                android.R.color.background_dark);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (listView == null || listView.getChildCount() == 0) ?
                                0 : listView.getChildAt(0).getTop();
                swipeLayout.setEnabled(topRowVerticalPosition >= 0);
            }
        });

        refreshFeed();
    }

    private void hideReplyBox(){
        replyPosition = -1;
        replyText.setVisibility(View.GONE);
        postTextInput.setHint("Add a post");

    }
    private int replyPosition = -1;
    @OnItemClick(R.id.list)
    public void onRowClick(View v, int position){
        if (position == replyPosition) {
            hideReplyBox();
            return;
        }

        replyPosition = position;
        postTextInput.setHint("Add your comment");

        Post p = (Post) listView.getAdapter().getItem(position);
        boolean isComment = p instanceof Comment;
        User user = isComment ? ((Comment)p).getCommentUser() : p.getPostUser();
        String text = isComment ? ((Comment)p).getCommentText() : p.getPostText();
        replyText.setText(Html.fromHtml(String.format("Reply to <b>@%s</b><em>: %s</em>",
                user.getUsername(), text)));
        replyText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        refreshFeed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBus.unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBus.register(this);
        mBus.post(new ValidateAuthenticationEvent(false));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.feed, menu);

        menu.findItem(R.id.action_logout).setIcon(
                new IconDrawable(this, Iconify.IconValue.fa_sign_out)
                        .colorRes(android.R.color.holo_blue_light)
                        .actionBarSize()
        );

        menu.findItem(R.id.action_settings).setIcon(
                new IconDrawable(this, Iconify.IconValue.fa_wrench)
                        .colorRes(android.R.color.holo_blue_light)
                        .actionBarSize());
        return true;
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private Uri capturedImageUri=null;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            takePicture();
        } else if(id == R.id.action_logout) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Confirm Logout?")
                    .setMessage("Are you sure you want to log out from the BlueFletch Feed?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mBus.post(new LogoutEvent());
                            dialog.dismiss();
                            FeedActivity.this.finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }
    private void takePicture() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), (DateTime.now().getMillis() + ".jpg"));
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Toast.makeText(FeedActivity.this, "Sorry, we're unable to create a file on your phone.", Toast.LENGTH_SHORT);
                return;
            }
        } else {
            file.delete();
        }
        capturedImageUri = Uri.fromFile(file);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                String path = new ResizeImageAsyncTask(getApplication().getContentResolver())
                        .execute(capturedImageUri)
                        .get();

                mBus.post(new UploadProfilePictureEvent(path));
            } catch (InterruptedException e) {
                Timber.e("ERROR resizing image: %s", e.getLocalizedMessage());
            } catch (ExecutionException e) {
                Timber.e("ERROR resizing image: %s", e.getLocalizedMessage());
            }
        }
    }

    @Subscribe
    public void onProfilePicSaved(ProfilePictureUpdated ev){
        refreshFeed();
    }

    private Toast saveToast;

    @OnClick(R.id.save_post_button)
    protected void onSaveClicked() {
        final String text = postTextInput.getText().toString();
        if (text.isEmpty()) {
            Toast.makeText(FeedActivity.this, "Please add some valid text", Toast.LENGTH_SHORT).show();
            return;
        }

        saveButton.setEnabled(false);
        saveToast = Toast.makeText(FeedActivity.this, "Saving..", Toast.LENGTH_LONG);
        saveToast.show();
        CreatePostEvent newPost = new CreatePostEvent(text);
        if (replyPosition != -1) {
            Post replyPost = (Post) listView.getAdapter().getItem(replyPosition);
            if (replyPost instanceof Comment) {
                //loop back to next non-comment
                for (int i=replyPosition;i>=0;i--){
                    Post r = (Post) listView.getAdapter().getItem(i);
                    if (!(r instanceof Comment)) {
                        replyPost = r;
                        break;
                    }
                }
            }
            newPost.setReplyToId(replyPost.get_id());
        }

        mBus.post(newPost);
    }
    @Subscribe
    public void onNewPostCreated(PostCreatedEvent ev){
        ev.getPost().setDidJustAdd(true);
        int position = 0;
        if (replyPosition != -1) position = replyPosition+1;

        ((AnimateAdditionAdapter<Post>) listView.getAdapter()).insert(position, ev.getPost());
        hideReplyBox();
        postTextInput.setText(null);
        saveButton.setEnabled(true);
        saveToast.cancel();
    }
    @Subscribe
    public void onNewPostFailed(PostCreateError ev){

        postTextInput.setText(null);
        saveButton.setEnabled(true);
        saveToast.cancel();

        if (!ev.getError().isNetworkError()) {
            Toast.makeText(FeedActivity.this, "Error saving post", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(FeedActivity.this, R.string.error_connecting, Toast.LENGTH_SHORT).show();
        }
    }

    private Toast refreshToast;
    @Subscribe
    public void feedLoaded(FeedLoadedEvent event) {
        List<Post> posts = event.getPosts();
        Timber.i("Received %s posts from the feed service.", posts.size());
        PostArrayAdapter adapter = new PostArrayAdapter(FeedActivity.this, R.layout.post_item, posts);
        AnimateAdditionAdapter<Post> animationAdapter = new AnimateAdditionAdapter<Post>(adapter);
        animationAdapter.setListView(listView);
        listView.setAdapter(animationAdapter);

        refreshToast.cancel();
        swipeLayout.setRefreshing(false);
    }
    @Subscribe
    public void feedError(FeedLoadedError event){
        refreshToast.cancel();
        swipeLayout.setRefreshing(false);
        Toast.makeText(FeedActivity.this, R.string.error_connecting, Toast.LENGTH_SHORT).show();
    }
    /**
     * Helper method to refresh the feed.  Assumes a service object has already been instantiated.
     */
    private void refreshFeed() {
        swipeLayout.setRefreshing(true);
        refreshToast = Toast.makeText(FeedActivity.this, R.string.refreshing, Toast.LENGTH_SHORT);
        refreshToast.show();

        DateTime ago = DateTime.now().withTimeAtStartOfDay().minusDays(DEFAULT_DAYS_BACK);
        BusProvider.getInstance().post(new LoadFeedEvent(ago));
    }
}
