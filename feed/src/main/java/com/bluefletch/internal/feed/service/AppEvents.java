package com.bluefletch.internal.feed.service;

import com.bluefletch.internal.feed.rest.Post;

import org.joda.time.DateTime;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by blakebyrnes on 6/5/14.
 */
public class AppEvents {

    public static class NeedsAuthenticationEvent { }
    public static class LogoutEvent { }
    public static class OnAuthenticatedEvent {}
    public static class ValidateAuthenticationEvent{
        private boolean broadcastAuthenticatedEvent = false;

        public ValidateAuthenticationEvent(boolean broadcastAuthenticatedEvent) {
            this.broadcastAuthenticatedEvent = broadcastAuthenticatedEvent;
        }

        public boolean isBroadcastAuthenticatedEvent() {
            return broadcastAuthenticatedEvent;
        }

        public void setBroadcastAuthenticatedEvent(boolean broadcastAuthenticatedEvent) {
            this.broadcastAuthenticatedEvent = broadcastAuthenticatedEvent;
        }
    }

    public static class AuthenticationErrorEvent {
        private boolean unableToReadCookies;
        private RetrofitError authError;

        public AuthenticationErrorEvent(boolean unableToReadCookies) {
            this.unableToReadCookies = unableToReadCookies;
        }

        public AuthenticationErrorEvent(RetrofitError authError) {
            this.authError = authError;
        }

        public boolean isUnableToReadCookies() {
            return unableToReadCookies;
        }

        public void setUnableToReadCookies(boolean unableToReadCookies) {
            this.unableToReadCookies = unableToReadCookies;
        }

        public RetrofitError getAuthError() {
            return authError;
        }

        public void setAuthError(RetrofitError authError) {
            this.authError = authError;
        }
    }
    public static class AuthenticateUserEvent{
        private String username;
        private String password;


        public AuthenticateUserEvent(String username, String pwd) {
            this.username = username;
            this.password = pwd;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    public static class LoadFeedEvent {
        private DateTime asOfDate;

        public LoadFeedEvent(DateTime asOfDate) {
            this.asOfDate = asOfDate;
        }

        public DateTime getAsOfDate() {
            return asOfDate;
        }

        public void setAsOfDate(DateTime asOfDate) {
            this.asOfDate = asOfDate;
        }
    }

    public static class FeedLoadedError   {
        private RetrofitError error;

        public FeedLoadedError(RetrofitError error) {
            this.error = error;
        }
        public RetrofitError getError() {
            return error;
        }

        public void setError(RetrofitError error) {
            this.error = error;
        }


    }
    public static class FeedLoadedEvent {
        private List<Post> posts;

        public FeedLoadedEvent(List<Post> posts) {
            setPosts(posts);
        }

        public List<Post> getPosts() {
            return posts;
        }

        public void setPosts(List<Post> posts) {
            this.posts = posts;
        }

    }

    public static class CreatePostEvent {
        private String text;

        public CreatePostEvent(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
    public static class PostCreatedEvent {
        private Post post;

        public PostCreatedEvent(Post post) {
            this.post = post;
        }

        public Post getPost() {
            return post;
        }

        public void setPost(Post post) {
            this.post = post;
        }
    }

    public static class PostCreateError extends FeedLoadedError {
        public PostCreateError(RetrofitError err) {
            super(err);
        }
    }
}
