package com.example.amanmj.inputstream_player;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

public class MainActivity extends AppCompatActivity implements myInputStream.GetAvailableBytes {
    private ExoPlayer exoPlayer;
    private DataSource dataSource;
    private TrackRenderer audio;
    private ExtractorSampleSource extractorSampleSource;
    private int rendererCount;
    private File file;
    private TextView textView,speed,textView5,textView4;
    private long song_duration;
    private Button inc,dec,temp;   /*to increase/decrease the number of bytes available per second*/
    private int BUFFER_BYTES;  /* equals number of kilo bytes to read per second from file*/
    private AtomicLong availableBytes; //atomicLong used because it's Thread safe
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        rendererCount=1;

        /*set current available bytes per second = 20KB */
        BUFFER_BYTES=20;

        textView=(TextView)findViewById(R.id.textView3);
        textView4=(TextView)findViewById(R.id.textView4);
        textView5=(TextView)findViewById(R.id.textView5);
        speed=(TextView)findViewById(R.id.speed);
        seekBar=(SeekBar)findViewById(R.id.seekBar);
        inc=(Button)findViewById(R.id.inc);
        dec=(Button)findViewById(R.id.dec);
        temp=(Button)findViewById(R.id.temp);



        /*initialise number of available bytes for the inputstream to read = 100 */
        availableBytes=new AtomicLong(100);

        exoPlayer= ExoPlayer.Factory.newInstance(rendererCount);
        exoPlayer.addListener(new ExoPlayer.Listener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            }

            @Override
            public void onPlayWhenReadyCommitted() {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
            }
        });

        /*check if file is present or not*/
        try
        {
            /* location of file in the root directory of SD Card named "song.mp3" */
            file=new File(Environment.getExternalStorageDirectory(),"temp_song");
        }
        catch(Exception e)
        {
            Toast.makeText(getApplicationContext(),"song.mp3 not found in root of SD Card",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        }

        /*instantiate myDataSource*/
        dataSource=new myDataSource(this);

        extractorSampleSource=new ExtractorSampleSource(Uri.parse("temp_song"),dataSource,new DefaultAllocator(64*1024),64*1024*256);
        audio=new MediaCodecAudioTrackRenderer(extractorSampleSource,null,true);

        /*prepare ExoPlayer*/
        exoPlayer.prepare(audio);
        exoPlayer.setPlayWhenReady(true);


        /*start a new Thread that will determine the buffering speed of the song.
        available bytes will increase by BUFFER_BYTES*1024 after each second thus providing a buffering of
        "BUFFER_BYTES" KBPS
        The bytes will enter myInputStream from "song.mp3" at a rate of BUFFER_BYTES KBPS.
         */
        new Thread(updateAvailableBytes).start();

        while(exoPlayer.getDuration()<0)
        {
            /*Sleep until exoplayer.getDuration() returns -1*/
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.song_duration=exoPlayer.getDuration();

        /*
        A thread to update bytes read and number of bytes to read from myInputStream
        The mp3 will play smoothly only If ( number of bytes to read < number of bytes read )
        */
        new Thread(seekBarThread).start();


        /*
        seekbar Listener to skip bytes
         */
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar,int progress, boolean fromUser) {
                if (fromUser==true) {
                    exoPlayer.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        /*listener to increase number of available bytes per second*/
        inc.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                BUFFER_BYTES+=5;
            }
        });

        /*listener to decrease number of available bytes per second*/
        dec.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(BUFFER_BYTES==5)
                    Toast.makeText(getApplicationContext(),"AVAILABLE BYTES CANNOT BE ZERO",Toast.LENGTH_LONG).show();
                else
                    BUFFER_BYTES-=5;
            }
        });


        /*A temp button created in order to start exoplayer again if it throws exception when bytes are unavailable
         to be read
          */
        temp.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                extractorSampleSource=new ExtractorSampleSource(Uri.parse("song.mp3"),dataSource,new DefaultAllocator(64*1024),64*1024*256);
                audio=new MediaCodecAudioTrackRenderer(extractorSampleSource,null,true);
                exoPlayer.prepare(audio);
                exoPlayer.setPlayWhenReady(true);
            }
        });
    }

    /*Override method in getAvailableBytes Interface in MyInputStream*/
    @Override
    public long getAvailableBytes() {
        /*a check to handle if available bytes exceed the total bytes in file*/
        if(availableBytes.get() > file.length())
            return file.length();
        else
            return file.length();
    }
    Runnable updateAvailableBytes = new Runnable() {

        @Override
        public void run() {

            /*loop to update the availableBytes by BUFFER_BYTES*1024 per second which determines the
            buffer speed/available bytes to be read */

            while(true)
            {
                try {
                    availableBytes.addAndGet(BUFFER_BYTES*1024);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    Runnable seekBarThread=new Runnable() {
        @Override
        public void run() {
            seekBar.setMax((int) song_duration);
            while(true){
                try {
                    Thread.sleep(500);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(exoPlayer.getDuration()>0)
                            {
                                seekBar.setProgress((int) exoPlayer.getCurrentPosition());

                                long currentPos=exoPlayer.getCurrentPosition()*file.length()/exoPlayer.getDuration();
                                textView4.setText("Number of bytes to be read up till here = " + currentPos);
                                textView.setText("         Number of bytes read up till here = "+availableBytes.get());
                                speed.setText(BUFFER_BYTES+" KBPS");
                                if(availableBytes.get()-currentPos>150000)
                                    textView5.setText("PLAYING");
                                else
                                    textView5.setText("READING BYTES");
                            }
                        }
                    });
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}