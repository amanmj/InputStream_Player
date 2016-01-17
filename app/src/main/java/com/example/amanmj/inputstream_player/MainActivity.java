package com.example.amanmj.inputstream_player;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.exoplayer.ExoPlayer;

public class MainActivity extends AppCompatActivity {
    private ExoPlayer exoPlayer;
    private int rendererCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        rendererCount=1;
        exoPlayer= ExoPlayer.Factory.newInstance(rendererCount);




    }
    @Override
    public long getAvailableBytes() {
        if(counter.get() > file.length())
            return file.length();
        else
            return counter.get();
    }
}
