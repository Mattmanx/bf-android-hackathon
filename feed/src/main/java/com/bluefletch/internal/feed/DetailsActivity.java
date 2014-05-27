package com.bluefletch.internal.feed;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bluefletch.internal.feed.adapter.CommentArrayAdapter;
import com.bluefletch.internal.feed.adapter.PostArrayAdapter;
import com.bluefletch.internal.feed.rest.Comment;
import com.bluefletch.internal.feed.rest.Post;
import com.bluefletch.internal.feed.util.ISO8601;
import com.bluefletch.internal.feed.util.UrlFinder;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class DetailsActivity extends Activity {

    final Context context = this;
    private CommentArrayAdapter commentArrayAdapter;
    private ListView listView;
    private static final String TAG = DetailsActivity.class.getSimpleName();
    public static final String DATE_FORMAT_PRESENTABLE = "K:mm a MMM d";
    private static final SimpleDateFormat presentableDateFormat = new SimpleDateFormat(DATE_FORMAT_PRESENTABLE);
    private static int PRIMARY_TEXT_SIZE = 14;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent i = getIntent();
        Post post = (Post)i.getSerializableExtra(getString(R.string.intent_selected_post));

        listView = (ListView) findViewById(R.id.detail_list);

        populatePostInformation(post);

        ArrayList<Comment> c = new ArrayList<Comment>(post.getComments());
        commentArrayAdapter = new CommentArrayAdapter(context, R.layout.listitem_feed, c);
        listView.setAdapter(commentArrayAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populatePostInformation(Post post) {

        View view = findViewById(R.id.post_details);
        ISO8601 iso8601 = new ISO8601();

        TextView textviewContent = (TextView) view.findViewById(R.id.listitem_posttext);
        //String content = cursor.getString(cursor.getColumnIndex(LaterListSQLiteHelper.COLUMN_CONTENT));
        String content = post.getPostText();
        String formattedContent = UrlFinder.formatContent(content);
        textviewContent.setText(Html.fromHtml(formattedContent));
        textviewContent.setTextSize(PRIMARY_TEXT_SIZE);

        TextView textviewTime = (TextView) view.findViewById(R.id.listitem_time);
        Calendar cal = null;
        try {
            cal = iso8601.toCalendar(post.getCreatedDate());
        } catch (ParseException parseEx) {
            Log.e(TAG, "ParseException converting post createdDate to calendar: " + parseEx.getMessage());
        }

        //Date dateAddDtm = new SimpleDateFormat("MMMM d, yyyy", Locale.US).parse(post.getCreatedDate());
        //Date dateAddDtm = LaterListItem.getDateFromFormattedLong(cursor.getLong(cursor.getColumnIndex(LaterListSQLiteHelper.COLUMN_ADD_DTM)));
        //textviewTime.setText(presentableDateFormat.format(dateAddDtm));
        if (cal != null) {
            textviewTime.setText(presentableDateFormat.format(cal.getTime()));
            //PrettyTime prettyTime = new PrettyTime();
            //textviewTime.setText(prettyTime.format(cal));
        }
        else {
            textviewTime.setText("");
        }
        textviewTime.setTextSize(PRIMARY_TEXT_SIZE - 2);

        String url = getString(R.string.base_url) + post.getPostUser().getImageUrl();

        ImageView imageviewUserPic = (ImageView) view.findViewById(R.id.listitem_userimage);
        Picasso.with(context)
                .load(getString(R.string.base_url) + post.getPostUser().getImageUrl())
                .resize(50, 50)
                .into(imageviewUserPic);

        TextView textviewUsername = (TextView) view.findViewById(R.id.listitem_username);
        //String id = cursor.getString(cursor.getColumnIndex(LaterListSQLiteHelper.COLUMN_ID));
        String username = post.getPostUser().getUsername();
        textviewUsername.setText(username);

        TextView textviewPostId = (TextView) view.findViewById(R.id.listitem_postid);
        //int status = cursor.getInt(cursor.getColumnIndex(LaterListSQLiteHelper.COLUMN_STATUS));
        textviewPostId.setText(post.get_id());

        TextView textviewCommentId = (TextView) view.findViewById(R.id.listitem_commentid);
        //int status = cursor.getInt(cursor.getColumnIndex(LaterListSQLiteHelper.COLUMN_STATUS));
        textviewCommentId.setText(Integer.toString(-1));
    }

}
