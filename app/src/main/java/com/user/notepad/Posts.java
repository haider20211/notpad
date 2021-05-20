package com.user.notepad;

public class Posts
{

    public String uid, date, time, type, image, message;

    public Posts()
    {
        //for firebase Use Only
    }

    public Posts(String uid, String date, String time, String type, String image, String message) {
        this.uid = uid;
        this.date = date;
        this.time = time;
        this.type = type;
        this.image = image;
        this.message = message;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
