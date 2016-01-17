package com.example.amanmj.inputstream_player;

import android.os.Environment;
import android.util.Log;

import com.google.android.exoplayer.C;
import com.google.android.exoplayer.upstream.DataSpec;
import com.google.android.exoplayer.upstream.UriDataSource;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class myDataSource implements UriDataSource {
    private myInputStream.GetAvailableBytes getAvailableBytes;
    //private long getAvailableBytes;
    private String uriString;
    private myInputStream inputStream;
    private long bytesRemaining;
    private boolean opened;

    public myDataSource(myInputStream.GetAvailableBytes getAvailableBytes) {
        this.getAvailableBytes =getAvailableBytes;
        Log.i("Aman", "Constructor called");
    }

    @Override
    public long open(DataSpec dataspec)
    {
        try
        {
            Log.i("Aman","uristring= "+uriString);
            uriString = dataspec.uri.toString();
            String path = dataspec.uri.getPath();

            File file = new File(Environment.getExternalStorageDirectory(),uriString);
            Log.i("Aman","length myDatasource = "+file.length());

            RandomAccessFile randomAccessFile=new RandomAccessFile(file,"r");
            Log.i("Aman","length "+randomAccessFile.length());
            //FileInputStream inputStream2=new FileInputStream(file);
            inputStream=new myInputStream(getAvailableBytes,randomAccessFile);
            //inputStream = assetManager.open(path, AssetManager.ACCESS_RANDOM);


            Log.i("path", path);
            Log.i("path2", uriString);

            long skipped = inputStream.skip(dataspec.position);
            if (skipped < dataspec.position) {
                // assetManager.open() returns an AssetInputStream, whose skip() implementation only skips
                // fewer bytes than requested if the skip is beyond the end of the asset's data.
                throw new EOFException();
            }
            if (dataspec.length != C.LENGTH_UNBOUNDED) {
                bytesRemaining = dataspec.length-skipped;
            } else {
                bytesRemaining=file.length();
                //bytesRemaining = inputStream.available();
                if (bytesRemaining == Integer.MAX_VALUE) {
                    // assetManager.open() returns an AssetInputStream, whose available() implementation
                    // returns Integer.MAX_VALUE if the remaining length is greater than (or equal to)
                    // Integer.MAX_VALUE. We don't know the true length in this case, so treat as unbounded.
                    bytesRemaining = C.LENGTH_UNBOUNDED;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        opened = true;
        Log.i("Aman","bytesremaining"+String.valueOf(bytesRemaining));

        return bytesRemaining;
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength)
    {
        if (bytesRemaining == 0) {
            Log.i("Aman","read returning -1");
            return -1;
        } else {
            int bytesRead = 0;
            try {
                int bytesToRead = bytesRemaining == C.LENGTH_UNBOUNDED ? readLength
                        : (int) Math.min(bytesRemaining, readLength);
                bytesRead = inputStream.read(buffer, offset, bytesToRead);
                //   Log.i("Aman","bytesToread="+bytesToRead+" offset= "+offset+" bytesRead= "+bytesRead+" readLength= "+readLength);
            }
            catch (IOException e)
            {

            }

            if (bytesRead > 0) {
                if (bytesRemaining != C.LENGTH_UNBOUNDED) {
                    bytesRemaining -= bytesRead;
                }

            }
            return bytesRead;
        }
    }

    @Override
    public String getUri() {
        return uriString;
    }

    @Override
    public void close()
    {
        uriString = null;
        if (inputStream != null) {
            try {
                inputStream.close();
            }
            catch (IOException e)
            {

            }
            finally
            {
                inputStream = null;
                if (opened) {
                    opened = false;

                }
            }
        }
    }
}

