package com.example.benlalah.lena;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhonePhoto {

    private int id;
    private String albumName;
    private String photoUri;
    public HashMap<String, Float> tags;
    private boolean showInGallery;

    public PhonePhoto(){
        showInGallery = true;
    }
    public int getId() {
        return id;
    }

    public void setId( int id ) {
        this.id = id;
    }


    public void setAlbumName( String name ) {
        this.albumName = name;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri( String photoUri ) {
        this.photoUri = photoUri;
    }

    public boolean isShowInGallery(){
        return showInGallery;
    }

    public void filter(String search){
        if(search.equals("")){
            showInGallery = true;
            return;
        }
        if(tags == null) {
            showInGallery = false;
            return;
        }
        for(Map.Entry<String, Float> e: tags.entrySet()){
            if(e.getKey().equals(search)) {
                showInGallery = true;
                return;
            }
        }
        showInGallery = false;
    }

    public void addTag(String tag, float p){
        if(tags == null)
            tags = new HashMap<>();
        tags.put(tag, p);
    }

    public byte[] getBytes(){
        Bitmap image = DeviceImageManager.getImage("file://"+photoUri);
        return getBytesFromBitmap(image);
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }
}