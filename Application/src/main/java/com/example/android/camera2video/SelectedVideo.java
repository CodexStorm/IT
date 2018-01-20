package com.example.android.camera2video;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.adeel.library.easyFTP;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by manoj on 13-11-2017.
 */

public class SelectedVideo extends Activity {
    VideoView view;AudioManager audioManager;
    private ProgressDialog prg;
    static int serverResponseCode;
    String path;
    Button upLoad;
    Bundle bundle;
    String itemValue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selected_video);
        view = (VideoView)findViewById(R.id.videoView1);
        Bundle bundle = getIntent().getExtras();
        String path=bundle.getString("path");
        itemValue = bundle.getString("itemValue");
        path = bundle.getString("path");
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
                showdialogue(view);
            }
        });
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void showdialogue(final View v)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SelectedVideo.this);
        /*final Context context = dialogBuilder.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.custom_dialogue, null, false);*/
        LayoutInflater inflater = SelectedVideo.this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialogue, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.edit1);

        dialogBuilder.setMessage("Enter Ip Address :");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Constants.IP =(String)edt.getText().toString();
                final File dir = SelectedVideo.this.getExternalFilesDir(null);
               // Constants.VideoFilePath = bundle.getString("path");
               // String path = bundle.getString("path");
                Log.d("FilePath",dir.getAbsolutePath()+itemValue);
                int respnse = upLoad2Server(dir.getAbsolutePath()+itemValue);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public  void up(View v){
        final File dir = SelectedVideo.this.getExternalFilesDir(null);
        String address=Constants.IP,u="",p="",directory=dir.getAbsolutePath();
        uploadTask async=new uploadTask();
        async.execute(address,u,p,directory);//Passing arguments to AsyncThread
    }

    public static int upLoad2Server(String sourceFileUri) {
        String upLoadServerUri = Constants.IP;
        // String [] string = sourceFileUri;
        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        DataInputStream inStream = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        String responseFromServer = "";

        File sourceFile = new File(sourceFileUri);
        if (!sourceFile.isFile()) {
            Log.e("Huzza", "Source File Does not exist");
            return 0;
        }
        try { // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(upLoadServerUri);
            conn = (HttpURLConnection) url.openConnection(); // Open a HTTP  connection to  the URL
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", fileName);
            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""+ fileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available(); // create a buffer of  maximum size
            Log.i("Huzza", "Initial .available : " + bytesAvailable);

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("Upload file to server", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
            // close streams
            Log.i("Upload file to server", fileName + " File is written");
            fileInputStream.close();
            dos.flush();
            dos.close();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
//this block will give the response of upload link
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn
                    .getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                Log.i("Huzza", "RES Message: " + line);
            }
            rd.close();
        } catch (IOException ioex) {
            Log.e("Huzza", "error: " + ioex.getMessage(), ioex);
        }
        return serverResponseCode;  // like 200 (Ok)

    } // end upLoad2Server
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
                Bitmap b;
                Bundle bundle = getIntent().getExtras();
                String itemValue = bundle.getString("itemValue");
                b = bitmapper();
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
    public Bitmap bitmapper()
    {
        final File dir = SelectedVideo.this.getExternalFilesDir(null);
        String SrcPath=dir.getAbsolutePath();

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

        mediaMetadataRetriever.setDataSource(SrcPath);
        String METADATA_KEY_DURATION = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        Bitmap bmpOriginal = mediaMetadataRetriever.getFrameAtTime(0);
        int bmpVideoHeight = bmpOriginal.getHeight();
        int bmpVideoWidth = bmpOriginal.getWidth();

        Log.d("LOGTAG", "bmpVideoWidth:'" + bmpVideoWidth + "'  bmpVideoHeight:'" + bmpVideoHeight + "'");

        byte [] lastSavedByteArray = new byte[0];

        float factor = 0.20f;
        int scaleWidth = (int) ( (float) bmpVideoWidth * factor );
        int scaleHeight = (int) ( (float) bmpVideoHeight * factor );
        int max = (int) Long.parseLong(METADATA_KEY_DURATION);
        for ( int index = 0 ; index < max ; index++ )
        {
            bmpOriginal = mediaMetadataRetriever.getFrameAtTime(index * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
            bmpVideoHeight = bmpOriginal == null ? -1 : bmpOriginal.getHeight();
            bmpVideoWidth = bmpOriginal == null ? -1 : bmpOriginal.getWidth();
            int byteCount = bmpOriginal.getWidth() * bmpOriginal.getHeight() * 4;
            ByteBuffer tmpByteBuffer = ByteBuffer.allocate(byteCount);
            bmpOriginal.copyPixelsToBuffer(tmpByteBuffer);
            byte [] tmpByteArray = tmpByteBuffer.array();

            if ( !Arrays.equals(tmpByteArray, lastSavedByteArray))
            {
                int quality = 100;
                String mediaStorageDir=dir.getAbsolutePath();
                File outputFile = new File(mediaStorageDir , "IMG_" + ( index + 1 )
                        + "_" + max + "_quality_" + quality + "_w" + scaleWidth + "_h" + scaleHeight + ".png");
                Log.e("Output Files::>>",""+outputFile);
                OutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(outputFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Bitmap bmpScaledSize = Bitmap.createScaledBitmap(bmpOriginal, scaleWidth, scaleHeight, false);

                bmpScaledSize.compress(Bitmap.CompressFormat.PNG, quality, outputStream);

                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                lastSavedByteArray = tmpByteArray;
            }
        }
        mediaMetadataRetriever.release();
        return bmpOriginal;
    }

}


