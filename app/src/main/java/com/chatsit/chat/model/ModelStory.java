package com.chatsit.chat.model;

public class ModelStory {

    public String imageUri;
    public long timestart;
    public long timeend;
    public String storyid;
    public String userid;

    public ModelStory(String imageUri, long timestart, long timeend, String storyid, String userid) {
        this.imageUri = imageUri;
        this.timestart = timestart;
        this.timeend = timeend;
        this.storyid = storyid;
        this.userid = userid;
    }

    public ModelStory() {
    }


    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {

        this.imageUri = imageUri;
    }

    public long getTimestart() {
        return timestart;


    }

    public void setTimestart(long timestart) {


        this.timestart = timestart;
    }



    public long getTimeend() {
        return timeend;
    }

    public void setTimeend(long timeend) {

        this.timeend = timeend;
    }

    public String getStoryid() {
        return storyid;
    }

    public void setStoryid(String storyid) {
        this.storyid = storyid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
