package com.bluefletch.internal.feed.rest;

import java.io.Serializable;

/**
 * Created by mattmehalso on 4/3/14.
 */
public class User implements Serializable {

    private String _id;
    private String imageUrl;
    private String username;

    //these last two are optional, as the User object will be reused in areas where only the top 3
    //elements are provided.  We are also leaving out the password element.
    private String lastActionDate;
    private String createdDate;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLastActionDate() {
        return lastActionDate;
    }

    public void setLastActionDate(String lastActionDate) {
        this.lastActionDate = lastActionDate;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
