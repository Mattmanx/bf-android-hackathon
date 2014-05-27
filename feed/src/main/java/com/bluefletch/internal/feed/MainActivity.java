package com.bluefletch.internal.feed;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bluefletch.internal.feed.rest.FeedRequestInterceptor;
import com.bluefletch.internal.feed.rest.FeedService;
import com.bluefletch.internal.feed.rest.User;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;

public class MainActivity extends Activity  {

    private static final String TAG = MainActivity.class.getSimpleName();

    //ui elements
    private EditText username;
    private EditText password;
    private Button loginButton;

    // services / helpers
    private SessionManager sessionManager;
    private FeedService feedService;


    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get references to our UI elements
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.login);

        //set up our session manager
        sessionManager = new SessionManager(this);

        //if we already have a session, skip the login view and go right to the feed.
        if(sessionManager.isSessionActive()) {
            startActivity(new Intent(this, FeedActivity.class));
        } else {
            //Set up our REST service client
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(getString(R.string.base_url))
                    .setRequestInterceptor(new FeedRequestInterceptor(sessionManager))
                    .build();

            feedService = restAdapter.create(FeedService.class);

            //Handle the login button click... we should do field validation here...
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleLoginClick(v);
                }
            });
        }
    }

    private void handleLoginClick(View v) {
        //Get the user and pass from the UI
        String user = username.getText().toString();
        String pass = password.getText().toString();
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Authenticating");
        progressDialog.setMessage("Logging you into the BlueFletch feed...");
        progressDialog.setCancelable(false);
        progressDialog.show();


        //Call the feed service with the provided user and pass
        feedService.login(user, pass, new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                progressDialog.dismiss();

                //Get cookie out of response headers
                String cookie = null;
                for (Header h : response.getHeaders()) {
                    if ("Set-Cookie".equals(h.getName()) &&
                            h.getValue() != null &&
                            h.getValue().startsWith("connect.sid")) {
                        cookie = h.getValue();
                        cookie = cookie.substring(cookie.indexOf("connect.sid="), cookie.indexOf(";"));
                        cookie = cookie.replace("connect.sid=", "");
                        cookie = cookie.trim();
                        Log.i(TAG, "Cookie token found: " + cookie);
                    }
                }

                if (cookie != null) {
                    sessionManager.initializeSession(cookie, user);
                    startActivity(new Intent(MainActivity.this, FeedActivity.class));
                } else {
                    Log.e(TAG, "Unable to get cookie off of response. Unable to log in.");

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Error")
                            .setMessage("Unable to retrieve cookie from response headers.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                progressDialog.dismiss();

                Log.e(TAG, "Login failure: " + retrofitError.getMessage());
                //We should inspect the error here and provide a useful message to the user. for now,
                //just saying we can't login.
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Error")
                        .setMessage("Unable to login")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });
    }

}
