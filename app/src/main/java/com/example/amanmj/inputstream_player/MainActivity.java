package com.example.amanmj.inputstream_player;

import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.SampleSource;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private ExoPlayer exoPlayer;
    private int rendererCount;
    File file;
    SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rendererCount=1;
        exoPlayer= ExoPlayer.Factory.newInstance(rendererCount);
        /* location of file in the root directory of SD Card named "song.mp3" */
        file=new File(Environment.getExternalStorageDirectory(),"song.mp3");
        /*check if file is present or not*/
        try
        {
            if(file.exists()==false)
            {
                throw new FileNotFoundException("File not present in root of SD Card");
            }
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }










        /*
        seekbar Listener to seek exoplayer to different duration
         */
        seekBar=(SeekBar)findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    long newPos = (progress * exoPlayer.getDuration()) / 100;
                    exoPlayer.seekTo(newPos);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
    /*@Override
    public long getAvailableBytes() {
        if(counter.get() > file.length())
            return file.length();
        else
            return counter.get();
    }*/
    Runnable runnable=new Runnable() {
        @Override
        public void run() {

        }
    };
}
