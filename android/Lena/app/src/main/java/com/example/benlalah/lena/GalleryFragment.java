package com.example.benlalah.lena;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GalleryFragment extends Fragment {
    private GridView gridView;
    private EditText query;
    private TextView noresults;
    private GridViewAdapter gridAdapter;
    static final String[] GALLERY_PERMISSIONS = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    static final int PERMISSION_GALLERY = 1;

    // List pics
    public static List<PhonePhoto> data;
    public static boolean dataLoaded = false;
    public GalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        gridView = (GridView) view.findViewById(R.id.gridView);
        noresults = (TextView) view.findViewById(R.id.no_results);
        gridAdapter = new GridViewAdapter(getActivity());
        gridView.setAdapter(gridAdapter);

        query = (EditText) view.findViewById(R.id.search_q);
        query.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() > 0 && s.charAt(s.length()-1) == ' ')
                    gridAdapter.filter(s.toString().trim());
            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Dialog settingsDialog = new Dialog(getActivity());
                settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                View image_display = getActivity().getLayoutInflater().inflate(R.layout.reinforcement_layout, null);
                ImageView buttyes = (ImageView) image_display.findViewById(R.id.buttyes);
                ImageView buttno = (ImageView) image_display.findViewById(R.id.buttno);
                View mess = image_display.findViewById(R.id.message);
                mess.setVisibility(View.INVISIBLE);
                buttyes.setVisibility(View.INVISIBLE);
                buttno.setVisibility(View.INVISIBLE);
                settingsDialog.setContentView(image_display);
                ImageView image = (ImageView) image_display.findViewById(R.id.image_dialog);
                Picasso.with(getActivity())
                        .load( "file:" + gridAdapter.dataToShow.get(position).getPhotoUri() )
                        .centerCrop()
                        .fit()
                        .into((ImageView) image);
                Log.i("FILE",  "file:" + gridAdapter.dataToShow.get(position).getPhotoUri());
                settingsDialog.show();
            }
        });

        if(data == null) {
            data = new ArrayList<>();
            showGallery();
        }
        return view;
    }

    private void showGallery(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(GALLERY_PERMISSIONS, PERMISSION_GALLERY);
        else
            new ShowGallery().execute();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_GALLERY) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                showGallery();
            else
                Toast.makeText(getActivity(), "Unable to show gallery unless permissions are granted.", Toast.LENGTH_LONG).show();
        }
    }

    private class ShowGallery extends AsyncTask<Void, PhonePhoto, Void>{
        private ProgressDialog progress;
        private int prog = 0;

        @Override
        protected Void doInBackground(Void... params) {
            int max_photos = 50;
            List<PhonePhoto> photos = DeviceImageManager.readPhotoTags(getActivity());
            data = new ArrayList<>();
            if (photos.isEmpty()){
                Log.e("PHOTOPROC", "EMPTY...");
                photos = DeviceImageManager.getPhoneAlbums(getActivity());
                int i = 0;
                for(PhonePhoto photo: photos) {
                    if(i < max_photos) {
                        Log.e("PHOTOPROC", "PROCESSING...");
                        DeviceImageManager.processPhoto(getActivity(), photo);
                        publishProgress(photo);
                    }
                    i++;
                }
                DeviceImageManager.writePhotoTags(getActivity(), data);
            }else {
                Log.e("PHOTOPROC", "ALREADY CACHED...");

                PhonePhoto[] pics = new PhonePhoto[Math.min(photos.size(), max_photos)];
                for(int i = 0; i < photos.size(); i++){
                    if(i < max_photos)
                        pics[i] = photos.get(i);
                }
                publishProgress(pics);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void pictures){
            dataLoaded = true;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(PhonePhoto... values) {
            for(int i = 0; i <values.length; i++)
                data.add(values[i]);
            gridAdapter.filter("");
            gridAdapter.notifyDataSetChanged();
        }
    }

    public class FilterTask extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... params0) {
            JSONArray arr = new JSONArray();
            Map<String, String> params = new HashMap<>();
            for(PhonePhoto photo: data) {
                JSONArray tags = new JSONArray();

                for(Map.Entry<String, Float> e: photo.tags.entrySet()){
                    tags.put(e.getKey());
                }
                arr.put(tags);
            }
            params.put("query", params0[0]);
            params.put("tags", arr.toString());
            Log.i("ARR", arr.toString());
            String jsonStr = DeviceImageManager.sendData("gallery", params);
            if(!jsonStr.equals("-1")){
                try{
                    JSONArray obj = new JSONArray(jsonStr);
                    for(int i = 0; i < obj.length(); i++){
                        gridAdapter.dataToShow.add(data.get(obj.getInt(i)));
                    }

                }catch(Exception e) {
                    Log.e("ARR", "ERROR");
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void pictures){
            if(gridAdapter.dataToShow.size() == 0)
                noresults.setVisibility(View.VISIBLE);
            else
                noresults.setVisibility(View.INVISIBLE);
            gridAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

    }

    public class GridViewAdapter extends BaseAdapter {
        private Context context;
        private List<PhonePhoto> dataToShow;

        public GridViewAdapter(Context context) {
            this.context = context;
            dataToShow = new ArrayList<>();
            if(data != null)
                dataToShow = new ArrayList<>(data);
        }

        public void filter(String text){
            if(text.length() < 3) {
                dataToShow = new ArrayList<>(data);
                return;
            }
            dataToShow = new ArrayList();
            new FilterTask().execute(text);
        }

        @Override
        public int getCount() {
            return dataToShow.size();
        }

        @Override
        public Object getItem(int arg0) {
            return dataToShow.get(arg0);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(R.layout.grid_item_layout, parent, false);
            }

            ImageView image = (ImageView) row.findViewById(R.id.image_album);
            Picasso.with(context)
                    .load( "file:" + dataToShow.get(position).getPhotoUri() )
                    .centerCrop()
                    .fit()
                    .into((ImageView) image);

            return row;
        }
    }

}
