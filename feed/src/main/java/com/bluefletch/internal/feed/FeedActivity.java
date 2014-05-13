package com.bluefletch.internal.feed;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ListView;
import android.widget.Toast;

import com.bluefletch.internal.feed.rest.FeedRequestInterceptor;
import com.bluefletch.internal.feed.rest.FeedService;
import com.bluefletch.internal.feed.rest.Post;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Skeleton feed activity - contains an action bar with a logout and refresh icon (currently do nothing),
 * performs a feed lookup on creation.  Also handles situations where the user becomes logged out but
 * somehow gets back to this view.
 *
 * TODO: Present the feed, implement the action bar buttons, etc.  Consider extending ListActivity for easy presentation!!!
 */
public class FeedActivity extends Activity {

    private static final String TAG = FeedActivity.class.getSimpleName();

    private SessionManager sessionManager;

    private FeedService feedService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        sessionManager = new SessionManager(this);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(getString(R.string.base_url))
                .setRequestInterceptor(new FeedRequestInterceptor(sessionManager))
                .build();

        feedService = restAdapter.create(FeedService.class);

        refreshFeed();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //every time we come back to feed, make sure we're still logged in.  if not, let's present the
        //user with an error and go back to the login activity.
        if(sessionManager.getUser() == null) {
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Logged Out")
                    .setMessage("You have been logged out. Please tap OK to return to the login screen " +
                            "and log in again.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).create().show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.feed, menu);

        menu.findItem(R.id.action_logout).setIcon(
                new IconDrawable(this, Iconify.IconValue.fa_sign_out)
                        .colorRes(android.R.color.holo_blue_light)
                        .actionBarSize());

        menu.findItem(R.id.action_refresh).setIcon(
                new IconDrawable(this, Iconify.IconValue.fa_refresh)
                        .colorRes(android.R.color.holo_blue_light)
                        .actionBarSize());

        menu.findItem(R.id.action_settings).setIcon(
                new IconDrawable(this, Iconify.IconValue.fa_wrench)
                        .colorRes(android.R.color.holo_blue_light)
                        .actionBarSize());
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
        } else if (id == R.id.action_refresh) {
            Toast.makeText(this, "Refreshing feed...", Toast.LENGTH_SHORT).show();
            refreshFeed();
        } else if(id == R.id.action_logout) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Confirm Logout?")
                    .setMessage("Are you sure you want to log out from the BlueFletch Feed?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            sessionManager.terminateSession();

                                            dialog.dismiss();

                                            Intent i = new Intent();
                                            i.setClass(FeedActivity.this, MainActivity.class);
                                            startActivity(i);

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

    /**
     * Helper method to refresh the feed.  Assumes a service object has already been instantiated.
     */
    private void refreshFeed() {
        feedService.feed(null, new Callback<List<Post>>() {
            @Override
            public void success(List<Post> posts, Response response) {
                Log.i(TAG, "Received " + posts.size() + " posts from the feed service.");

                Context context = getApplicationContext();
                try {
                    ListView listView = (ListView) findViewById(R.id.listViewPostFeed);
                    CustomListViewAdapter adapter = new CustomListViewAdapter(context,
                            R.layout.post_item, posts);
                    listView.setAdapter(adapter);
                }
                catch(Exception e)
                {
                    Toast toastError = Toast.makeText(context, "Feed load failed.", Toast.LENGTH_LONG);
                    toastError.show();
                }

            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e(TAG, "There was an error retrieving posts from the feed.");
            }
        });
    }

}
