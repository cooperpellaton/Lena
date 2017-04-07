package com.example.benlalah.lena;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.bitmap;

/**
 * Created by benlalah on 21/01/17.
 */

public class DeviceVideoManager {

    public static List<PhoneVideo> getVideos(Context context) {
        List<PhoneVideo> videos = new ArrayList<>();

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Video.VideoColumns.DATA };
        Cursor c = context.getContentResolver().query(uri, projection, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                PhoneVideo video = new PhoneVideo();
                video.setUri(c.getString(0));
                video.setDuration(getDuration(context, video.getUri()));
                videos.add(video);
            }
            c.close();
        }
        Log.i("Videos", ""+videos.size());
        return videos;
    }

    public static int getDuration(Context context, String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(context, Uri.parse(uri));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);
        return (int)(timeInMillisec/1000);
    }

    public static Bitmap getThumbnailVideo(String uri){
        return ThumbnailUtils.createVideoThumbnail(uri, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
    }
}
