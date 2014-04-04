package com.bluefletch.internal.feed.rest;

import java.util.List;

/**
 * Created by mattmehalso on 4/3/14.
 *
 *  _id: post id
 •	comments: array of comments containing
 o	_id: comment id
 o	createdDate: created date time
 o	commentText: text of comment
 o	commentUser:
 •	_id: user id
 •	imageUrl: profile pic URL
 •	username: username
 o	newCommentForUser: boolean indicating whether this is a new post based on when the user last logged in and/or refreshed the page (note: may not be present if false)
 o	numUpdates: number of times the comment has been update
 •	createdDate: created date time
 •	lastUpdatedDate: last updated date time (will get updated if new comment is submitted)
 •	postText: text of the post
 •	originalPostText: the original text of the post (will be same as postText unless post is updated)
 •	numUpdates: number of times the post has been updated
 •	postUser:
 o	_id: user id
 o	imageUrl: profile pic URL
 o	username: username
 •	newPostForUser: boolean indicating whether this post will show up as “new” for the user based on when the user last logged in and/or refreshed the page  (note: may not be present if false)

 */
public class Post {

    private String _id;
    private String createdDate;
    private String lastUpdatedDate;
    private String postText;
    private String originalPostText;
    private int numUpdates;
    private User postUser;
    private boolean newPostForUser;
    List<Comment> comments;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(String lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public String getOriginalPostText() {
        return originalPostText;
    }

    public void setOriginalPostText(String originalPostText) {
        this.originalPostText = originalPostText;
    }

    public int getNumUpdates() {
        return numUpdates;
    }

    public void setNumUpdates(int numUpdates) {
        this.numUpdates = numUpdates;
    }

    public User getPostUser() {
        return postUser;
    }

    public void setPostUser(User postUser) {
        this.postUser = postUser;
    }

    public boolean isNewPostForUser() {
        return newPostForUser;
    }

    public void setNewPostForUser(boolean newPostForUser) {
        this.newPostForUser = newPostForUser;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
