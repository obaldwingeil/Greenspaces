package com.example.greenspaces;

import java.io.Serializable;

public class Image implements Serializable {

    private String url;
    private String location_id;
    private String review_id;
    private String user_id;

    public Image(String url, String location_id, String review_id, String user_id) {
        this.url = url;
        this.location_id = location_id;
        this.review_id = review_id;
        this.user_id = user_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocation_id() {
        return location_id;
    }

    public void setLocation_id(String location_id) {
        this.location_id = location_id;
    }

    public String getReview_id() {
        return review_id;
    }

    public void setReview_id(String review_id) {
        this.review_id = review_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
