package com.vtb.parkingmap.CommentRating;


/**
 * Created by BACHMAP
 */
public class Product {
    private long id;
    private String username;
    private int rating;
    private String comment;
    private String lastupdated;

    //Constructor

    public Product(long id, String username, int rating, String comment, String lastupdated) {
        this.id = id;
        this.username = username;
        this.rating = rating;
        this.comment = comment;
        this.lastupdated = lastupdated;
    }

    //Setter, getter

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getLastUpdated() {
        return lastupdated;
    }

    public void getLastupdated(String lastupdated) {
        this.lastupdated = lastupdated;
    }
}
