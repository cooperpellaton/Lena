package com.example.benlalah.lena;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class SnapchatFragment extends Fragment {

    static final String[] GALLERY_PERMISSIONS = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    static final int PERMISSION_GALLERY = 1;

    private ListView listView;
    private ListVideoAdapter adapter;
    private List<PhoneVideo> videos;
    private Timer timer;
    private View progressBar;
    private int passedTime;
    public SnapchatFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        timer = new Timer();
        View view  = inflater.inflate(R.layout.fragment_snapchat, container, false);
        listView = (ListView) view.findViewById(R.id.listVideos);
        progressBar = view.findViewById(R.id.progressVideo);
        progressBar.setVisibility(View.VISIBLE);
        adapter = new ListVideoAdapter();
        videos = new ArrayList<>();
        listView.setAdapter(adapter);
        listView.setDivider(null);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Log.e("ERROR", "Video click");
                final Dialog settingsDialog = new Dialog(getActivity());
                settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                View image_display = getActivity().getLayoutInflater().inflate(R.layout.video_player_layout, null);
                final TextView time = (TextView) image_display.findViewById(R.id.time);
                settingsDialog.setContentView(image_display);
                settingsDialog.show();
                try {
                    Runtime.getRuntime().exec("chmod -R 777 " + videos.get(position).getUri());
                    VideoView player = (VideoView) image_display.findViewById(R.id.videoplayer);
                    player.setZOrderOnTop(true);
                    player.requestFocus();
                    player.setMediaController(new MediaController(getActivity()));
                    player.setVideoURI(Uri.parse(videos.get(position).getUri()));
                    player.seekTo(videos.get(position).getStartTime() * 1000);
                    player.start();
                    passedTime = 0;

                    final Handler mHandler = new Handler() {
                        public void handleMessage(NotificationCompat.MessagingStyle.Message msg) {
                            time.setText(Integer.toString(Math.max(videos.get(position).getDuration()-passedTime, 0)));
                        }
                    };

                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if(passedTime > videos.get(position).getDuration()) {
                                settingsDialog.dismiss();
                                this.cancel();
                            }
                            passedTime++;
                            mHandler.obtainMessage(1).sendToTarget();
                        }
                    }, 0,1000);



                    settingsDialog.show();
                    settingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            timer.cancel();
                        }
                    });
                } catch(Exception e){
                    Log.e("ERROR", "Video error");
                    e.printStackTrace();
                }
            }
        });
        showVideos();
        return view;
    }

    private void showVideos(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(GALLERY_PERMISSIONS, PERMISSION_GALLERY);
        else
            new GetVideos().execute();
    }


    private class ListVideoAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return videos.size();
        }

        @Override
        public Object getItem(int i) {
            return videos.get(i);
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
            score.setImageBitmap(videos.get(i).getBitmap());

            TextView dur = (TextView) view.findViewById(R.id.duration);
            dur.setVisibility(View.VISIBLE);
            dur.setText(videos.get(i).getDuration()+"s");
            return view;
        }
    }

    private class GetVideos extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params0) {
            // Get the highlights from the internet
            String js = DeviceImageManager.sendData("videos", new HashMap<String, String>());
            try {
                JSONArray json = new JSONArray(new JSONArray(js).getString(0));
                Log.i("JSON", "OK+");
                List<PhoneVideo> elements  = DeviceVideoManager.getVideos(getActivity());
                for(int i = 0; i < elements.size(); i++) {
                    elements.get(i).setThumbnail(DeviceVideoManager.getThumbnailVideo(elements.get(i).getUri()));
                    videos.add(elements.get(i));
                    Log.i("Video", elements.get(i).getUri());
                    for(int j = 0; j  < json.length(); j++){
                        JSONObject a = json.getJSONObject(j);
                        String filename = a.getString("filename");
                        JSONArray data = a.getJSONArray("data");
                        if(videos.get(i).getUri().contains(filename) && videos.get(i).getUri().contains("DCIM/Camera")){
                            int startTime = 0;
                            int durationTime = 7;
                            for(int k = 0; k < data.length(); k++){
                                startTime = (int)data.getJSONObject(k).getDouble("start");
                                durationTime = (int)data.getJSONObject(k).getDouble("duration");
                            }
                            videos.get(i).setStartTime(startTime);
                            videos.get(i).setDuration(durationTime);
                        }
                    }
                    publishProgress();
                }
            }catch(Exception e) {
                Log.e("JSON", ":/");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void pictures){
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_GALLERY) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                showVideos();
            else
                Toast.makeText(getActivity(), "Unable to show videos unless permissions are granted.", Toast.LENGTH_LONG).show();
        }
    }
}
