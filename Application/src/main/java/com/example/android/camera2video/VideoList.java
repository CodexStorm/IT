package com.example.android.camera2video;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by manoj on 13-11-2017.
 */

public class VideoList extends Activity{
    private ListView mListView;
    private List<String> fileNameList;
    public  String path;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_video);
        file = Environment.getExternalStorageDirectory();
        /*fileNameList = getFileListfromSDCard();*/
        final ListView listView = (ListView) findViewById(R.id.list);
        final ArrayList<String>alPath=new ArrayList<String>();
        final ArrayList<String> alName=new ArrayList<String>();
        final File dir = VideoList.this.getExternalFilesDir(null);
        path = dir.getAbsolutePath() + "/";
        File directory = new File(path);
        File[] file = directory.listFiles();
        for (int i = 0; i < file.length; i++) {

            alName.add(file[i].getName());
            alPath.add(file[i].getAbsolutePath());
        }

            final ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>
                    (VideoList.this,android.R.layout.simple_list_item_1,alName);
            listView.setAdapter(arrayAdapter);
        /*ArrayAdapter<String> adapter1 = new ArrayAdapter<String> (this,android.R.layout.simple_list_item_1, android.R.id.text1, fileNameList);
        listView.setAdapter(adapter1);*/
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                int itemPosition = position;
                String itemValue = (String)   listView.getItemAtPosition(position);
                Intent intent = new Intent(VideoList.this, com.example.android.camera2video.SelectedVideo.class);
                intent.putExtra("id", id);
                intent.putExtra("itemValue", itemValue);
                intent.putExtra("path", path);
                startActivity(intent);
            }

        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                int itemPosiition  = i;
                File f = new File(alPath.get(itemPosiition));
                boolean delete =  f.delete();
                alPath.remove(itemPosiition);
                alName.remove(itemPosiition);
                arrayAdapter.notifyDataSetChanged();

                return delete;

            }
        });

        Button button = (Button) findViewById(R.id.btn_Online);
        // Capture button clicks
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent myIntent = new Intent(VideoList.this,CameraActivity.class);
                startActivity(myIntent);
            }
        });

    }

    private List<String> getFileListfromSDCard() {

        File files = new File(path);
        FileFilter filter = new FileFilter() {
            private final List<String> exts = Arrays.asList("mp4","MP4");
            @Override
            public boolean accept(File pathname) {
                String ext;
                String path = pathname.getPath();
                ext = path.substring(path.lastIndexOf(".") + 1);
                return exts.contains(ext);
            }
        };

        final File [] filesFound = files.listFiles(filter);
        List<String> flLst = new ArrayList<String>();
        if (filesFound != null && filesFound.length > 0) {
            for (File file : filesFound) {
                flLst.add(file.getName());
            }
        }
        return flLst;
    }


}
