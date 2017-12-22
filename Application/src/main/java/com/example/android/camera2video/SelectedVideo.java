package com.example.android.camera2video;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.adeel.library.easyFTP;

import java.io.File;
import java.io.InputStream;

/**
 * Created by manoj on 13-11-2017.
 */

public class SelectedVideo extends Activity {
    VideoView view;AudioManager audioManager;
    private ProgressDialog prg;
    Button upLoad;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selected_video);
        view = (VideoView)findViewById(R.id.videoView1);
        Bundle bundle = getIntent().getExtras();
        String path=bundle.getString("path");
        String itemValue = bundle.getString("itemValue");
        view.setVideoURI(Uri.parse(path+"/"+itemValue ));
        final VideoView videoView = (VideoView) findViewById(R.id.videoView1);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        Uri uri = Uri.parse(path+"/"+itemValue);
        upLoad = (Button)findViewById(R.id.upload);
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        int width = dm.widthPixels;
        videoView.setMinimumWidth(width);
        videoView.setMinimumHeight(height);
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int maxVolume =    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        SeekBar volControl = (SeekBar)findViewById(R.id.volbar);
        volControl.setMax(maxVolume);
        volControl.setProgress(curVolume);
        volControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2)    {
                // TODO Auto-generated method stub
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, arg1, 0);
            }
        });

        Button stopbutton = (Button) findViewById(R.id.btn_stop);
        stopbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.pause();

            }
        });

        Button playbutton = (Button) findViewById(R.id.btn_play);
        playbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle bundle = getIntent().getExtras();
                String itemValue = bundle.getString("itemValue");
                Toast.makeText(getApplicationContext(),
                        itemValue, Toast.LENGTH_LONG)
                        .show();
                view.start();
            }
        });

        Button backbutton = (Button) findViewById(R.id.btn_back);
        backbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                view.stopPlayback();
                startActivity(new Intent(SelectedVideo.this, VideoList.class));
            }
        });

        upLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                up(view);
            }
        });
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public  void up(View v){
        final File dir = SelectedVideo.this.getExternalFilesDir(null);
        String address="139.59.24.226",u="",p="",directory=dir.getAbsolutePath();
        uploadTask async=new uploadTask();
        async.execute(address,u,p,directory);//Passing arguments to AsyncThread
    }

    class uploadTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            prg = new ProgressDialog(SelectedVideo.this);
            prg.setMessage("Uploading...");
            prg.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                easyFTP ftp = new easyFTP();
                InputStream is = getResources().openRawResource(+R.drawable.easyftptest);
                ftp.connect(params[0], params[1], params[2]);
                boolean status = false;
                if (!params[3].isEmpty()) {
                    status = ftp.setWorkingDirectory(params[3]); // if User say provided any Destination then Set it , otherwise
                }
                // Upload will be stored on Default /root level on server
                Bundle bundle = getIntent().getExtras();
                String itemValue = bundle.getString("itemValue");
                ftp.uploadFile(is, itemValue);
                //Toast.makeText(SelectedVideo.this,"Successfull",Toast.LENGTH_LONG).show();
                return new String("Upload Successful");
            } catch (Exception e) {
                String t = "Failure : " + e.getLocalizedMessage();
                Log.d("Failures",t);
                //Toast.makeText(SelectedVideo.this,"Failure",Toast.LENGTH_LONG).show();
                return t;
            }
        }
        @Override
        protected void onPostExecute(String str) {
            prg.dismiss();
            Toast.makeText(SelectedVideo.this,str,Toast.LENGTH_LONG).show();
        }
    }


}


