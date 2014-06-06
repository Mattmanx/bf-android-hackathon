package com.bluefletch.internal.feed;


import android.app.Application;
import android.content.Intent;

import com.bluefletch.internal.feed.rest.FeedAPI;
import com.bluefletch.internal.feed.rest.FeedRequestInterceptor;
import com.bluefletch.internal.feed.service.BusProvider;
import com.bluefletch.internal.feed.service.FeedService;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.converter.GsonConverter;
import timber.log.Timber;

import static com.bluefletch.internal.feed.service.AppEvents.NeedsAuthenticationEvent;
import static timber.log.Timber.DebugTree;
/**
 * Created by blakebyrnes on 6/3/14.
 */
public class FeedApp extends Application {
    private FeedService mFeedService;
    private Bus mBus = BusProvider.getInstance();


    @Override public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }

        SessionManager manager = new SessionManager(this);
        mFeedService = new FeedService(buildApi(manager), mBus, manager);
        mBus.register(this);

        UAirship.takeOff(this);
        PushManager.shared().setIntentReceiver(IntentReceiver.class);
        PushManager.enablePush();

        Crashlytics.start(this);
    }

    private FeedAPI buildApi(SessionManager sessionManager) {
        Gson gsonConv = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();

        return new RestAdapter.Builder()
                .setEndpoint(getString(R.string.base_url))
                .setRequestInterceptor(new FeedRequestInterceptor(sessionManager))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(new AndroidLog("FeedAPI"))
                .setConverter(new GsonConverter(gsonConv))
                .build()
                .create(FeedAPI.class);

    }

    @Subscribe
    public void needsAuthentication(NeedsAuthenticationEvent ev){
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    /** A tree which logs important information for crash reporting. */
    private static class CrashReportingTree extends Timber.HollowTree {
        @Override public void i(String message, Object... args) {
            Crashlytics.log(String.format(message, args));
        }

        @Override public void i(Throwable t, String message, Object... args) {
            i(message, args); // Just add to the log.
        }

        @Override public void e(String message, Object... args) {
            i("ERROR: " + message, args); // Just add to the log.
        }

        @Override public void e(Throwable t, String message, Object... args) {
            e(message, args);

            Crashlytics.logException(t);
        }
    }
}
