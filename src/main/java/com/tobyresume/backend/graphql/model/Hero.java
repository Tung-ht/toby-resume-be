package com.tobyresume.backend.graphql.model;

/**
 * GraphQL type Hero — single-locale view for public API.
 */
public class Hero {

    private String tagline;
    private String bio;
    private String fullName;
    private String title;
    private String profilePhotoUrl;

    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
}
