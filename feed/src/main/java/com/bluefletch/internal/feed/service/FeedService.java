package com.bluefletch.internal.feed.service;

import com.bluefletch.internal.feed.SessionManager;
import com.bluefletch.internal.feed.rest.Comment;
import com.bluefletch.internal.feed.rest.FeedAPI;
import com.bluefletch.internal.feed.rest.Post;
import com.bluefletch.internal.feed.rest.User;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.urbanairship.push.PushManager;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;
import timber.log.Timber;

import static com.bluefletch.internal.feed.service.AppEvents.*;
import static timber.log.Timber.i;

/**
 * Created by blakebyrnes on 6/5/14.
 */
public class FeedService {
    private FeedAPI mApi;
    private Bus mBus;
    // services / helpers
    private SessionManager sessionManager;


    public FeedService(FeedAPI api, Bus bus, SessionManager sessionManager) {
        this.mApi = api;
        this.mBus = bus;
        this.sessionManager = sessionManager;

        mBus.register(this);
    }


    @Subscribe
    public void loadFeed(LoadFeedEvent event) {
        DateTime ago = event.getAsOfDate();
        String asOfDate = ISODateTimeFormat.dateTime().print(ago);

        mApi.feed(asOfDate, new Callback<List<Post>>() {
            @Override
            public void success(List<Post> posts, Response response) {
                Timber.i("Received %s posts from the feed service.", posts.size());
                mBus.post(new FeedLoadedEvent(posts));
            }

            @Override
            public void failure(RetrofitError error) {
                Timber.e("Error retrieving posts from the feed: %s", error.getCause());
                if (!error.isNetworkError()) {
                    mBus.post(new LogoutEvent());
                } else {
                    mBus.post(new FeedLoadedError(error));
                }
            }
        });
    }

    @Subscribe
    public void validateLogon(ValidateAuthenticationEvent ev){

        //every time we come back to feed, make sure we're still logged in.  if not, let's present the
        //user with an error and go back to the login activity.
        if(!sessionManager.isSessionActive()) {
            mBus.post(new NeedsAuthenticationEvent());
        } else if (ev.isBroadcastAuthenticatedEvent()){
            mBus.post(new OnAuthenticatedEvent());
        }
    }

    @Subscribe
    public void authenticated(AuthenticateUserEvent event){

        //Call the feed service with the provided user and pass
        mApi.login(event.getUsername(), event.getPassword(), new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                Set<String> tags = new HashSet<String>();
                tags.add("BFUsers");
                PushManager.shared().setAliasAndTags(user.getUsername(), tags);

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
                        i("Cookie token found: %s", cookie);
                    }
                }

                if (cookie != null) {
                    sessionManager.initializeSession(cookie, user);
                    mBus.post(new OnAuthenticatedEvent());
                } else {
                    Timber.e("Unable to get cookie off of response. Unable to log in.");
                    mBus.post(new AuthenticationErrorEvent(true));
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Timber.e("Login failure: %s", retrofitError.getMessage());
                mBus.post(new AuthenticationErrorEvent(retrofitError));
            }
        });
    }

    @Subscribe
    public void terminateCurrentSession(LogoutEvent event){
        getSessionManager().terminateSession();
        mBus.post(new NeedsAuthenticationEvent());
    }
    @Subscribe
    public void createPost(CreatePostEvent event){
        if (event.getReplyToId() != null) {
            mApi.createComment(event.getText(), event.getReplyToId(),
                    sessionManager.getUser().getUsername(), new Callback<Comment>() {
                @Override
                public void success(Comment post, Response response) {
                    Timber.i("Saved Comment.");
                    mBus.post(new PostCreatedEvent(post));
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    Timber.e("Error saving post to the feed: %s", retrofitError.getCause());
                    mBus.post(new PostCreateError(retrofitError));
                }
            });
        } else {
            mApi.createPost(event.getText(), sessionManager.getUser().getUsername(), new Callback<Post>() {
                @Override
                public void success(Post post, Response response) {
                    Timber.i("Saved Post.");
                    mBus.post(new PostCreatedEvent(post));
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    Timber.e("Error saving post to the feed: %s", retrofitError.getCause());
                    mBus.post(new PostCreateError(retrofitError));
                }
            });
        }
    }

    @Subscribe
    public void uploadProfilePicture(UploadProfilePictureEvent event){
        TypedFile file = new TypedFile("image/jpeg", new File(event.getImagePath()));

        mApi.uploadProfilePicture(sessionManager.getUser().getUsername(), file, new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                Timber.i("Saved profile picture.");
                mBus.post(new ProfilePictureUpdated());
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Timber.e("Error saving a users profile picture: %s", retrofitError);
                mBus.post(new ProfilePictureError(retrofitError));
            }
        });
    }


    public SessionManager getSessionManager(){
        return sessionManager;
    }
}
