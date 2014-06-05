package com.bluefletch.internal.feed;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.bluefletch.internal.feed.service.BusProvider;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.bluefletch.internal.feed.service.AppEvents.AuthenticateUserEvent;
import static com.bluefletch.internal.feed.service.AppEvents.AuthenticationErrorEvent;
import static com.bluefletch.internal.feed.service.AppEvents.OnAuthenticatedEvent;
import static com.bluefletch.internal.feed.service.AppEvents.ValidateAuthenticationEvent;

public class MainActivity extends Activity implements TextView.OnEditorActionListener {
     //ui elements
    @InjectView(R.id.username) EditText username;
    @InjectView(R.id.password) EditText password;

    private Bus mBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        username.setOnEditorActionListener(this);
        password.setOnEditorActionListener(this);

        getBus().register(this);
        getBus().post(new ValidateAuthenticationEvent(true));
    }

    @Override
    protected void onResume() {
        super.onResume();
        getBus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getBus().unregister(this);
    }

    @Subscribe
    public void onAuthenticated(OnAuthenticatedEvent event){
        if (progressDialog != null)
            progressDialog.dismiss();

        startActivity(new Intent(this, FeedActivity.class));
    }


    @Subscribe
    public void loginFailed(AuthenticationErrorEvent err){
        if (progressDialog != null)
            progressDialog.dismiss();

        String message = err.isUnableToReadCookies() ?
                "Unable to retrieve cookie from response headers."
                : "Unable to login";

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Error")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    @Override
    public boolean onEditorAction(TextView textView, int keyCode, KeyEvent event) {
        if (keyCode == EditorInfo.IME_ACTION_NEXT ||
                keyCode == EditorInfo.IME_ACTION_GO ||
                keyCode == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.getAction() == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_ENTER))
        {
            if (textView == username) {
                password.requestFocus();
            } else {
                password.clearFocus();
                handleLoginClick(findViewById(R.id.login));
            }
            return true;
        }
        // Returning false allows other listeners to react to the press.
        return false;
    }


    private ProgressDialog progressDialog;

    @OnClick(R.id.login)
    protected void handleLoginClick(View v) {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Authenticating");
        progressDialog.setMessage("Logging you into the BlueFletch feed...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        getBus().post(new AuthenticateUserEvent(
                username.getText().toString(),
                password.getText().toString()));
    }

    // Use some kind of injection, so that we can swap in a mock for tests.
    // Here we just use simple getter/setter injection for simplicity.
    private Bus getBus() {
        if (mBus == null) {
            mBus = BusProvider.getInstance();
        }
        return mBus;
    }

    public void setBus(Bus bus) {
        mBus = bus;
    }

}
