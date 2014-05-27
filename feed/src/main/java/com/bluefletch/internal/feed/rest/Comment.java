package com.bluefletch.internal.feed.rest;

import java.io.Serializable;

/**
 * Created by mattmehalso on 4/3/14.
 * o	_id: comment id
 o	createdDate: created date time
 o	commentText: text of comment
 o	commentUser:
 •	_id: user id
 •	imageUrl: profile pic URL
 •	username: username
 o	newCommentForUser: boolean indicating whether this is a new post based on when the user last logged in and/or refreshed the page (note: may not be present if false)
 o	numUpdates: number of times the comment has been update

 */
@SuppressWarnings("serial")
public class Comment implements Serializable {
    private String _id;
    private String createdDate;
    private String commentText;
    private User commentUser;
    private boolean newCommentForUser;
    private int numUpdates;

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

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public User getCommentUser() {
        return commentUser;
    }

    public void setCommentUser(User commentUser) {
        this.commentUser = commentUser;
    }

    public boolean isNewCommentForUser() {
        return newCommentForUser;
    }

    public void setNewCommentForUser(boolean newCommentForUser) {
        this.newCommentForUser = newCommentForUser;
    }

    public int getNumUpdates() {
        return numUpdates;
    }

    public void setNumUpdates(int numUpdates) {
        this.numUpdates = numUpdates;
    }
}
