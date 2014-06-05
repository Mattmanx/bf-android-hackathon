package com.bluefletch.internal.feed.rest;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;

/**
 * Created by mattmehalso on 4/3/14.
 */
public interface FeedAPI {

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

    /**
     * Creates a post and returns to the callback
     * @param postText
     * @param username
     * @param cb
     */
    @FormUrlEncoded
    @POST("/post")
    void createPost(@Field("postText") String postText, @Field("username") String username
            , Callback<Post> cb);

    /**
     * Creates a post and returns to the callback
     * @param commentText
     * @param postId
     * @param username
     * @param cb
     */
    @FormUrlEncoded
    @POST("/comment")
    void createComment(@Field("commentText") String commentText, @Field("postId") String postId, @Field("username") String username
            , Callback<Comment> cb);


    /**
     * Upload a user profile picture.
     * @param photo -
     *
     *      photo = new File(selectedImageUri.getPath());
            typedFile = new TypedFile("application/octet-stream", photo);
     * @param cb
     */
    @Multipart
    @POST("/user/{username}/profilepic")
    void uploadProfilePicture(@Path("username") String username, @Part("imageFile") TypedFile photo, Callback<User> cb);
}
