package com.vtb.parkingmap.models;

public class Cardcomment {
    String Username, Comment, Date;
    private int Rating;

    public Cardcomment(String username, String comment, String date, int rating) {
        Username = username;
        Comment = comment;
        Date = date;
        Rating = rating;
    }

    public int getRating() {
        return Rating;
    }

    public void setRating(int rating) {
        Rating = rating;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }
}
