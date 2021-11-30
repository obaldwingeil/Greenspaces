package com.example.greenspaces;

import java.io.Serializable;
import java.util.ArrayList;

public class Location implements Serializable {

    private String location_id;
    private String name;
    private String address;
    private String type;
    private String description;
    private Float rating;
    private ArrayList<Image> images;
    private Boolean saved;

    public Location(String location_id, String name, String address, String type, String description, Float rating, Boolean saved){
        this.location_id = location_id;
        this.name = name;
        this.address = address;
        this.type = type;
        this.description = description;
        this.rating = rating;
        this.saved = saved;
        this.images = new ArrayList<>();
    }


    public String getLocation_id() {
        return location_id;
    }

    public void setLocation_id(String location_id) {
        this.location_id = location_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public ArrayList<Image> getImages() {
        return images;
    }

    public void setImages(ArrayList<Image> images) {
        this.images = images;
    }

    public void addImage(Image image){
        this.images.add(image);
    }

    public Boolean isSaved() {
        return saved;
    }

    public void setSaved(Boolean saved) {
        this.saved = saved;
    }
}
