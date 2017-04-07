package com.example.benlalah.lena;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


public class TakePicFragment extends Fragment {
    static final int PERMISSION_CAMERA = 0;
    static final String[] CAMERA_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    private Button takepic;

    private FrameLayout camera_view;
    private ImageButton imgClose;

    public TakePicFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_take_pic, container, false);
        //camera_view = (FrameLayout)view.findViewById(R.id.camera_view);
        //imgClose = (ImageButton) view.findViewById(R.id.imgClose);
        displayCamera();
        takepic = (Button) view.findViewById(R.id.takepic);
        takepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "The picture is being processed.", Toast.LENGTH_SHORT).show();
                CustomPreview.takeAPicture(getActivity().getContentResolver());
                Toast.makeText(getActivity(), "The picture has been saved to the gallery.", Toast.LENGTH_SHORT).show();

            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle saved) {
        super.onActivityCreated(saved);

    }

    public void displayCamera(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(CAMERA_PERMISSIONS, PERMISSION_CAMERA);
        } else {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (PERMISSION_CAMERA == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                displayCamera();
            else
                Toast.makeText(getActivity(), "Unable to start camera me unless permissions are granted.", Toast.LENGTH_LONG).show();
        }
    }

}
