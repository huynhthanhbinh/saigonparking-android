package com.vtb.parkingmap.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Results implements Serializable {
    private String reference;

    private String[] types;

    private String scope;

    private String icon;

    private String name;

    private Geometry geometry;

    private String vicinity;

    private String id;

    private Photos[] photos;

    private String place_id;

    @SerializedName("opening_hours")
    private OpeningHours openingHours;

    private String rating;


    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public String getRating() {
        return rating;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Photos[] getPhotos() {
        return photos;
    }

    public void setPhotos(Photos[] photos) {
        this.photos = photos;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }


    @Override
    public String toString() {
        return "ClassPojo [reference = " + reference + ", types = " + types + ", scope = " + scope + ", icon = " + icon + ", name = " + name + ", geometry = " + geometry + ", vicinity = " + vicinity + ", id = " + id + ", photos = " + photos + ", place_id = " + place_id + "]";
    }
}
