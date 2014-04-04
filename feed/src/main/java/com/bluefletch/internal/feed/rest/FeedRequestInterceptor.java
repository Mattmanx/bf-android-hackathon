package com.bluefletch.internal.feed.rest;

import android.util.Log;

import com.bluefletch.internal.feed.SessionManager;

import retrofit.RequestInterceptor;

/**
 * Created by mattmehalso on 4/3/14.
 */
public class FeedRequestInterceptor implements RequestInterceptor {

    private static final String TAG = FeedRequestInterceptor.class.getSimpleName();

    private SessionManager sessionManager;

    public FeedRequestInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void intercept(RequestFacade requestFacade) {
        if(sessionManager.isSessionActive()) {
            String cookie = sessionManager.getSessionToken();
            requestFacade.addHeader("Cookie", "connect.sid=" + cookie);

            Log.i(TAG, "Set cookie on request to " + cookie);
        }
    }
}
