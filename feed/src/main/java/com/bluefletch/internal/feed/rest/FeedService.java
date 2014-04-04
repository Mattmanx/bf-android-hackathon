package com.bluefletch.internal.feed.rest;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by mattmehalso on 4/3/14.
 */
public interface FeedService {

    /**
     * Logs in the user, executing the provided callback after the call is made.
     *
     * @param username
     * @param password
     * @param cb
     */
    @FormUrlEncoded
    @POST("/login")
    void login(@Field("username") String username, @Field("password") String password,
               Callback<User> cb);

    /**
     * Returns the feed for the user.
     * @param asOfDt
     * @param cb
     */
    @GET("/feed")
    void feed(@Query("asOfDt") String asOfDt, Callback<List<Post>> cb);
}
