package com.example.benlalah.lena;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

public class DeviceImageManager {

    public static String endpoint = "http://54.89.244.147/api";

    public static List<PhonePhoto> getPhoneAlbums(Context context){
        // Creating vectors to hold the final albums objects and albums names
        ArrayList<PhonePhoto> listPhotos = new ArrayList<>();
        // which image properties are we querying
        String[] projection = new String[] {
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID
        };

        // content: style URI for the "primary" external storage volume
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        // Make the query.
        Cursor cur = context.getContentResolver().query(images,
                projection, // Which columns to return
                null,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                null        // Ordering
        );

        if ( cur != null && cur.getCount() > 0 ) {
            Log.i("DeviceImageManager"," query count=" + cur.getCount());

            if (cur.moveToFirst()) {
                String bucketName;
                String data;
                String imageId;
                int bucketNameColumn = cur.getColumnIndex(
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

                int imageUriColumn = cur.getColumnIndex(
                        MediaStore.Images.Media.DATA);

                int imageIdColumn = cur.getColumnIndex(
                        MediaStore.Images.Media._ID );

                do {
                    // Get the field values
                    bucketName = cur.getString( bucketNameColumn );
                    data = cur.getString( imageUriColumn );
                    imageId = cur.getString( imageIdColumn );

                    // Adding a new PhonePhoto object to phonePhotos vector
                    PhonePhoto phonePhoto = new PhonePhoto();
                    phonePhoto.setAlbumName( bucketName );
                    phonePhoto.setPhotoUri( data );
                    phonePhoto.setId( Integer.valueOf( imageId ) );


                    listPhotos.add(phonePhoto);
                } while (cur.moveToNext());
            }

            cur.close();
        }

        return listPhotos;
    }


    public static void processPhoto(Context context, PhonePhoto photo){
        final ClarifaiClient client =
                new ClarifaiBuilder("LB2LQK4Bf8XC1v9vhNoqwrWmBbwWY6io3PWLAgQG", "L6N-mwjGI94XSdNqLsHDA2Y2LSG6DoIlcEoSLZTz").buildSync();
        try {
            final List<ClarifaiOutput<Concept>> predictionResults =
                    client.getDefaultModels().generalModel()
                            .predict()
                            .withInputs(
                                    ClarifaiInput.forImage(ClarifaiImage.of(photo.getBytes()))
                            )
                            .executeSync()
                            .get();

            for(ClarifaiOutput<Concept> concept: predictionResults){
                for(Concept c: concept.data()) {
                    photo.addTag(c.name(), c.value());
                    Log.e("Concept", c.name());
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    public static Bitmap getImage(String url){
            Bitmap bm = null;
            InputStream is = null;
            BufferedInputStream bis = null;
            try{
                URLConnection conn = new URL(url).openConnection();
                conn.connect();
                is = conn.getInputStream();
                bis = new BufferedInputStream(is, 8192);
                bm = BitmapFactory.decodeStream(bis);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally {
                if (bis != null){
                    try{
                        bis.close();
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }
                if (is != null){
                    try{
                        is.close();
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
            return bm;

    }

    public static List<PhonePhoto> readPhotoTags(Context context){
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = appSharedPrefs.getString("photos", "[]");
        Log.e("JSON", json);
        Type type = new TypeToken<List<PhonePhoto>>(){}.getType();
        List<PhonePhoto> mStudentObject = gson.fromJson(json, type);
        return mStudentObject;
    }

    public static void writePhotoTags(Context context, List<PhonePhoto> photos){
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(photos);
        prefsEditor.putString("photos", json);
        prefsEditor.commit();
    }

    public static String sendData(String function, Map<String, String> params){
        String results = "-1";
        try {
            String url = endpoint;

            URL obj = new URL(url+"/"+function);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Content-Type","application/json; charset=utf-8");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");


            JSONObject urlParams = new JSONObject(params);
            Log.e("ARR", "OK");

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParams.toString());
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            Log.i("ARR", ""+responseCode);
            if(responseCode != 200)
                return "-1";
            Log.e("ARR", "OK1");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);

            in.close();
            Log.e("ARR", "OK2");
            results = response.toString();

        } catch(Exception e){
            e.printStackTrace();
            Log.e("ARR", "ERROR");
        }  finally{
            return results;
        }
    }
}