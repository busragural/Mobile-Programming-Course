package com.example.courseassistantapp_auth;

import java.util.ArrayList;

public class UserDetails {
    private String name, surname, personId, email, phone, education, level, instagram, twitter;
    private ArrayList<Boolean> settings;

    public ArrayList<Boolean> getSettings() {
        return settings;
    }

    public void setSettings(ArrayList<Boolean> settings) {
        this.settings = settings;
    }

    public UserDetails() {
    }


    public UserDetails(String name, String surname, String personId, String email, String phone, String education, String level, String instagram, String twitter) {
        this.name = name;
        this.surname = surname;
        this.personId = personId;
        this.email = email;
        this.phone = phone;
        this.education = education;
        this.level = level;
        this.instagram = instagram;
        this.twitter = twitter;
    }

    public UserDetails(String name, String surname, String personId, String email, String phone, String education, String level, String instagram, String twitter, ArrayList<Boolean> settings) {
        this.name = name;
        this.surname = surname;
        this.personId = personId;
        this.email = email;
        this.phone = phone;
        this.education = education;
        this.level = level;
        this.instagram = instagram;
        this.twitter = twitter;
        this.settings = settings;
    }

    public UserDetails(String name, String surname, String personId, String phone, String education, String instagram, String twitter) {
        this.name = name;
        this.surname = surname;
        this.personId = personId;
        this.phone = phone;
        this.education = education;
        this.instagram = instagram;
        this.twitter = twitter;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }


    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }
}
