package com.example.benlalah.lena;

import android.app.Dialog;
import android.content.Context;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Config;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.DetectedFaces;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.benlalah.lena.GalleryFragment.dataLoaded;


public class MediaFacebook extends Fragment {

    private Timer timer;

    // Profile Pics
    private GridView profilePicList;
    private static List<PhonePhoto> profilePics;
    private ProfilePicsAdapter profilePicsAdapter;

    // Cover Photos
    private ListView coverPicList;
    private static List<PhonePhoto> coverPics;
    private CoverPicsAdapter coverPicsAdapter;

    // Friends Photos
    private GridView friendsPicList;
    private static List<PhonePhoto> friendsPics;
    private FriendsPicsAdapter friendsPicsAdapter;
    private TextView txtProfile;
    private TextView txtCover;
    private TextView txtFriends;

    public MediaFacebook() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_facebook, container, false);

        txtProfile = (TextView) view.findViewById(R.id.txtProfile);
        txtFriends = (TextView) view.findViewById(R.id.txtFriends);
        txtCover = (TextView) view.findViewById(R.id.txtCover);

        profilePicList = (GridView) view.findViewById(R.id.profilePics);
        profilePicsAdapter = new ProfilePicsAdapter();
        if(profilePics == null || profilePics.size() == 0)
            profilePics = new ArrayList<>();
        profilePicList.setAdapter(profilePicsAdapter);

        coverPicList = (ListView) view.findViewById(R.id.coverPics);
        coverPicsAdapter = new CoverPicsAdapter();
        if(coverPics == null || coverPics.size() == 0)
            coverPics = new ArrayList<>();
        coverPicList.setAdapter(coverPicsAdapter);

        friendsPicList = (GridView) view.findViewById(R.id.friendsPics);
        friendsPicsAdapter = new FriendsPicsAdapter();
        if(friendsPics == null || friendsPics.size() == 0)
            friendsPics = new ArrayList<>();
        friendsPicList.setAdapter(friendsPicsAdapter);
        Log.e("NO", "NDSD");
        timer = new Timer();
        timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (dataLoaded && (profilePics == null || profilePics.size() == 0)) {
                        Log.e("DATA", "Data loaded");
                        new GetProfilePics().execute();
                        this.cancel();
                    }
                }
            }, 10, 50);


        profilePicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final Dialog settingsDialog = new Dialog(getActivity());
                settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                View image_display = getActivity().getLayoutInflater().inflate(R.layout.reinforcement_layout, null);

                settingsDialog.setContentView(image_display);
                ImageView image = (ImageView) image_display.findViewById(R.id.image_dialog);
                ImageView buttyes = (ImageView) image_display.findViewById(R.id.buttyes);
                ImageView buttno = (ImageView) image_display.findViewById(R.id.buttno);

                Picasso.with(getActivity())
                        .load( "file:" + profilePics.get(position).getPhotoUri() )
                        .centerCrop()
                        .fit()
                        .into((ImageView) image);

                buttno.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        profilePics.remove(position);
                        profilePicsAdapter.notifyDataSetChanged();
                        settingsDialog.dismiss();
                    }
                });

                buttyes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        settingsDialog.dismiss();
                    }
                });

                Log.i("FILE",  "file:" + profilePics.get(position).getPhotoUri());
                settingsDialog.show();
            }
        });

        coverPicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final Dialog settingsDialog = new Dialog(getActivity());
                settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                View image_display = getActivity().getLayoutInflater().inflate(R.layout.reinforcement_layout, null);

                settingsDialog.setContentView(image_display);
                ImageView image = (ImageView) image_display.findViewById(R.id.image_dialog);
                ImageView buttyes = (ImageView) image_display.findViewById(R.id.buttyes);
                ImageView buttno = (ImageView) image_display.findViewById(R.id.buttno);

                Picasso.with(getActivity())
                        .load( "file:" + coverPics.get(position).getPhotoUri() )
                        .centerCrop()
                        .fit()
                        .into((ImageView) image);

                buttno.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        coverPics.remove(position);
                        coverPicsAdapter.notifyDataSetChanged();
                        settingsDialog.dismiss();
                    }
                });

                buttyes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        settingsDialog.dismiss();
                    }
                });


                settingsDialog.show();
            }
        });

        friendsPicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final Dialog settingsDialog = new Dialog(getActivity());
                settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                View image_display = getActivity().getLayoutInflater().inflate(R.layout.reinforcement_layout, null);

                settingsDialog.setContentView(image_display);
                ImageView image = (ImageView) image_display.findViewById(R.id.image_dialog);
                ImageView buttyes = (ImageView) image_display.findViewById(R.id.buttyes);
                ImageView buttno = (ImageView) image_display.findViewById(R.id.buttno);

                Picasso.with(getActivity())
                        .load( "file:" + friendsPics.get(position).getPhotoUri() )
                        .centerCrop()
                        .fit()
                        .into((ImageView) image);

                buttno.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        friendsPics.remove(position);
                        friendsPicsAdapter.notifyDataSetChanged();
                        settingsDialog.dismiss();
                    }
                });

                buttyes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        settingsDialog.dismiss();
                    }
                });


                settingsDialog.show();
            }
        });
        return view;
    }

    private class GetCoverPics extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params0) {
            String charset = "UTF-8";
            for(int i = 0; i < GalleryFragment.data.size(); i++) {

            }
            return null;
        }



        @Override
        protected void onPostExecute(Void pictures){
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i("PUB", "PIblished");
            coverPicsAdapter.notifyDataSetChanged();
        }

    }

    private class GetProfilePics extends AsyncTask<Void, Void, Void>{
        private int updated;
        @Override
        protected Void doInBackground(Void... params0) {
            String charset = "UTF-8";
            for(int i = 0; i < Math.min(GalleryFragment.data.size(), 20); i++) {
                File uploadFile1 = new File(GalleryFragment.data.get(i).getPhotoUri());
                String requestURL = DeviceImageManager.endpoint + "/profile_picture_upload";
                try {
                    MultipartClass multipart = new MultipartClass(requestURL, charset);
                    multipart.addFilePart("pic", uploadFile1);
                    List<String> response = multipart.finish();
                    for (String line : response) {
                        try {
                            JSONArray arr = new JSONArray(line);
                            arr = new JSONArray(arr.getString(0));
                            int n = arr.getInt(0);
                            if (arr.getBoolean(1)) {
                                profilePics.add(GalleryFragment.data.get(i));
                                updated = 0;
                            } else if(n == 0) {
                                coverPics.add(GalleryFragment.data.get(i));
                                updated = 1;
                            }
                            else {
                                friendsPics.add(GalleryFragment.data.get(i));
                                updated = 2;
                            }
                            publishProgress();
                        } catch(Exception e){
                            Log.i("JSON", "json :/");
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return null;
        }



        @Override
        protected void onPostExecute(Void pictures){
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            switch(updated){
                case 0:
                    txtProfile.setVisibility(View.VISIBLE);
                    setListViewHeightBasedOnChildren(profilePicList);
                    profilePicList.deferNotifyDataSetChanged();
                    break;
                case 1:
                    txtCover.setVisibility(View.VISIBLE);
                    setListViewHeightBasedOnChildren(coverPicList);
                    break;
                case 2:
                    txtFriends.setVisibility(View.VISIBLE);
                    setListViewHeightBasedOnChildren(friendsPicList);
                    break;
            }
            updated = -1;
        }

    }

    private class ProfilePicsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return profilePics.size();
        }

        @Override
        public Object getItem(int i) {
            return profilePics.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            View view = convertView;
            if (view == null) {
                LayoutInflater li = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = li.inflate(R.layout.grid_item_layout, null);
            } else
                view = convertView;

            Log.i("LOG", "Showing pics");
            ImageView score = (ImageView) view.findViewById(R.id.image_album);
            Picasso.with(getActivity())
                    .load( "file:" + profilePics.get(i).getPhotoUri() )
                    .centerCrop()
                    .fit()
                    .into(score);

            return view;
        }
    }

    private class CoverPicsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return coverPics.size();
        }

        @Override
        public Object getItem(int i) {
            return coverPics.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            View view = convertView;
            if (view == null) {
                LayoutInflater li = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = li.inflate(R.layout.snapchat_video_layout, null);
            } else
                view = convertView;

            ImageView score = (ImageView) view.findViewById(R.id.snapvideo);
            Picasso.with(getActivity())
                    .load( "file:" + coverPics.get(i).getPhotoUri() )
                    .centerCrop()
                    .fit()
                    .into(score);

            return view;
        }
    }

    private class FriendsPicsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return friendsPics.size();
        }

        @Override
        public Object getItem(int i) {
            return friendsPics.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            View view = convertView;
            if (view == null) {
                LayoutInflater li = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = li.inflate(R.layout.grid_item_layout, null);
            } else
                view = convertView;

            ImageView score = (ImageView) view.findViewById(R.id.image_album);
            Picasso.with(getActivity())
                    .load( "file:" + friendsPics.get(i).getPhotoUri() )
                    .centerCrop()
                    .fit()
                    .into(score);

            return view;
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem instanceof ViewGroup) {
                listItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public static void setListViewHeightBasedOnChildren(GridView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();

        for (int i = 0; i < listAdapter.getCount()/3; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem instanceof ViewGroup) {
                listItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight;
        listView.setLayoutParams(params);
    }
}
