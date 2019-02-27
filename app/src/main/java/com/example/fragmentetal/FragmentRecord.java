package com.example.fragmentetal;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FragmentRecord extends Fragment {

    private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 222;
    String mCurrentAudioPath;
    ArrayList<File> list;
    ListView mlistView;
    Button record,stopRecording,playAudio,stopPlaying;
    EditText mFailName;
    MediaRecorder mMediaRecorder;
    MediaPlayer mMediaPlayer;

    public FragmentRecord() {

    }

    public static FragmentRecord newInstance() {
        FragmentRecord fragment = new FragmentRecord();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View audio = inflater.inflate(R.layout.record_fragment, container, false);
        mlistView = audio.findViewById(R.id.listView);
        record = audio.findViewById(R.id.btnRecord);
        stopRecording = audio.findViewById(R.id.btnStopRecording);
        playAudio = audio.findViewById(R.id.playAudio);
        stopPlaying = audio.findViewById(R.id.stopPlaying);
        mFailName = audio.findViewById(R.id.editText);

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    startRecording();
                    stopRecording.setEnabled(true);
                    record.setEnabled(false);
                    playAudio.setEnabled(false);
                    stopPlaying.setEnabled(false);
                } else {

                    String[] permissinRequest = {Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permissinRequest, RECORD_AUDIO_PERMISSION_REQUEST_CODE);
                }
            }
        });

        stopRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaRecorder.stop();
                stopRecording.setEnabled(false);
                record.setEnabled(true);
                playAudio.setEnabled(true);
                stopPlaying.setEnabled(false);
            }
        });

        playAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                stopPlaying.setEnabled(true);
                stopRecording.setEnabled(false);
                record.setEnabled(false);
                mMediaPlayer = new MediaPlayer();
                try {
                    mMediaPlayer.setDataSource(mCurrentAudioPath);
                    mMediaPlayer.prepare();
                }catch (Exception e){
                    e.printStackTrace();
                }
                mMediaPlayer.start();
                Toast.makeText(getActivity(),getString(R.string.playing_btn),Toast.LENGTH_SHORT).show();
            }
        });

        stopPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlaying.setEnabled(false);
                record.setEnabled(true);
                playAudio.setEnabled(true);
                stopRecording.setEnabled(false);

                if(mMediaPlayer != null){
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    setupMediaRecorder();
                }
            }
        });

        return audio;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            // we have heard back from audio recorder
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {

            }
        }
    }
    private void startRecording() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        mCurrentAudioPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/"+timestamp+"_audio_record.3gp";
        setupMediaRecorder();
        try{
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        }catch (Exception e){
            e.printStackTrace();
        }

        mFailName.setText(mCurrentAudioPath);
        list = audioReader( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        mlistView.setAdapter( new RecorderAdapter());

    }

    private ArrayList<File> audioReader(File externalStoragePublicDirectory) {
        ArrayList<File> mlist = new ArrayList<>();

        File[] files = externalStoragePublicDirectory.listFiles();
        for (int i = 0;i<files.length;i++){
            if(files[i].isDirectory()){
                mlist.addAll(audioReader(files[i]));
            }else{
                if(files[i].getName().endsWith(".3gp")){
                    mlist.add(files[i]);
                }
            }
        }
        return  mlist;
    }
    private void setupMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mMediaRecorder.setOutputFile(mCurrentAudioPath);
    }
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        audioDeleter(file);
    }

    private void audioDeleter(File file) {
        if(file.exists()){
            File[] theData = file.listFiles();
            for (int i = 0; i < theData.length; i++) {
                File oneFile = theData[i];
                if (oneFile.isDirectory()) {
                } else {
                    if (oneFile.getName().endsWith(".3gp")) {
                        oneFile.delete();
                    }
                }
            }
        }
    }

    class RecorderAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.list_items,parent ,false);
            TextView audio = convertView.findViewById(R.id.textView);
            audio.setText(list.get(position).toString());

            return convertView;
        }
    }
}