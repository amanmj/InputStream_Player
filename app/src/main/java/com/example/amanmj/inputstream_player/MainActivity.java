package com.example.amanmj.inputstream_player;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private int rendererCount;
    private File file;
    private TextView textView,textView5,textView4;
    private long song_duration;
    private int BUFFER_BYTES;  /* equals number of kilo bytes to read per second from file*/
    private AtomicLong avaialableBytes; //atomicLong used because it's Thread safe
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
        seekBar=(SeekBar)findViewById(R.id.seekBar);



        /*initialise number of available bytes for the inputstream to read = 100 */
        avaialableBytes=new AtomicLong(100);

        exoPlayer= ExoPlayer.Factory.newInstance(rendererCount);

        /*check if file is present or not*/
        try
        {
            /* location of file in the root directory of SD Card named "song.mp3" */
            file=new File(Environment.getExternalStorageDirectory(),"song.mp3");
        }
        catch(Exception e)
        {
            Toast.makeText(getApplicationContext(),"song.mp3 not found in root of SD Card",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        }

        /*instantiate myDataSource*/
        DataSource dataSource=new myDataSource(this);

        ExtractorSampleSource extractorSampleSource=new ExtractorSampleSource(Uri.parse("song.mp3"),dataSource,new DefaultAllocator(64*1024),64*1024*256);
        TrackRenderer audio=new MediaCodecAudioTrackRenderer(extractorSampleSource,null,true);

        /*prepare ExoPlayer*/
        exoPlayer.prepare(audio);
        exoPlayer.setPlayWhenReady(true);


        /*start a new Thread that will determine the buffering speed of the song.
        available bytes will increase by BUFFER_BYTES*1024 after each second thus providing a buffering of
        "BUFFER_BYTES" KBPS
        The bytes will enter myInputStream from "song.mp3" at a rate of BYFFER_BYTES KBPS.
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
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser==true) {
                    exoPlayer.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    /*Override method in getAvailableBytes Interface in MyInputStream*/
    @Override
    public long getAvailableBytes() {
        /*a check to handle if available bytes exceed the total bytes in file*/
        if(avaialableBytes.get() > file.length())
            return file.length();
        else
            return avaialableBytes.get();
    }
    Runnable updateAvailableBytes = new Runnable() {

        @Override
        public void run() {

            /*loop to update the availableBytes by BUFFER_BYTES*1024 per second which determines the
            buffer speed/available bytes to be read */

            while(true)
            {
                try {
                    avaialableBytes.addAndGet(BUFFER_BYTES*1024);
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
                            if(exoPlayer.getDuration()>0) {
                                seekBar.setProgress((int) exoPlayer.getCurrentPosition());

                                long currentPos=exoPlayer.getCurrentPosition()*file.length()/exoPlayer.getDuration();
                                textView4.setText("Number of bytes to be read up till here = " + currentPos);
                                textView.setText("         Number of bytes read up till here = "+avaialableBytes.get());

                                if(avaialableBytes.get()+150000>currentPos)
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
