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

 updated on 6/3 to make it an extension of the post class

 */
@SuppressWarnings("serial")
public class Comment extends Post {

    private String commentText;
    private User commentUser;
    private boolean newCommentForUser;

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
}
