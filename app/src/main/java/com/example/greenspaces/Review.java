package com.example.greenspaces;

import android.provider.BlockedNumberContract;

import java.util.ArrayList;

public class Review {

    private String review_id;
    private String user_id;
    private String location_id;
    private String description;
    private double rating;
    private String user_name;
    private String location_name;
    private String type;
    private ArrayList<Image> images = new ArrayList<>();

    public Review(String review_id, String user_id, String location_id, String description, double rating, String user_name, String location_name, String type, ArrayList<Image> images){
        this.review_id = review_id;
        this.user_id = user_id;
        this.location_id = location_id;
        this.description = description;
        this.rating = rating;
        this.type = type;
        this.user_name = user_name;
        this.location_name = location_name;
        this.images = images;

    }

    public Review(String review_id, String user_id, String location_id, String description, double rating, String user_name, String location_name, String type){
        this.review_id = review_id;
        this.user_id = user_id;
        this.location_id = location_id;
        this.description = description;
        this.rating = rating;
        this.user_name = user_name;
        this.location_name = location_name;
        this.type = type;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public ArrayList<Image> getImages() {
        return images;
    }

    public void setImages(ArrayList<Image> images) {
        this.images = images;
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

    public String getLocation_id() {
        return location_id;
    }

    public void setLocation_id(String location_id) {
        this.location_id = location_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public void addImage(Image image){
        this.images.add(image);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation_name() {
        return location_name;
    }

    public void setLocation_name(String location_name) {
        this.location_name = location_name;
    }
}
