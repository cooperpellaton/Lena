package com.example.benlalah.lena;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by benlalah on 19/01/17.
 */

public class Picture {

    private List<String> keywords;
    private Bitmap image;
    private String filename;
    private boolean loaded;

    public Picture(Bitmap image){
        this.image = image;
        loaded = true;
    }
    public Picture(String filename) {
        this.filename = filename;
        loaded = false;
    }

    public void addKeyword(String keyword){
        keywords.add(keyword);
    }

    public Bitmap getImage(){ return image; }
}
