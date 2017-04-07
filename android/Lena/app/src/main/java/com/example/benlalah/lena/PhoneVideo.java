package com.example.benlalah.lena;

import android.graphics.Bitmap;

/**
 * Created by benlalah on 21/01/17.
 */

public class PhoneVideo {
    private String Uri;
    private Bitmap thumbnail;
    private String videoURL;
    private int startTime;
    private int duration;

    public PhoneVideo(){
    }

    public void setUri(String uri){
        Uri = uri;
    }

    public String getUri(){
        return Uri;
    }

    public Bitmap getBitmap(){
        return thumbnail;
    }
    public void setThumbnail(Bitmap t){
        thumbnail = t;
    }
    public String getVideoURL(){ return videoURL;}
    public void setVideoURL(String v){videoURL = v;}
    public int getStartTime(){ return startTime; }
    public void setStartTime(int time){ startTime = time; }
    public void setDuration(int time){ duration = time; }
    public int getDuration(){ return duration; }

}
