package com.example.amanmj.inputstream_player;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;


public class myInputStream extends InputStream {

    //private long getAvailableBytes;
    private final GetAvailableBytes getAvailableBytes;
    private final RandomAccessFile randomAccessFile;

    public myInputStream(GetAvailableBytes getAvailableBytes, RandomAccessFile randomAccessFile) {
        this.getAvailableBytes = getAvailableBytes;
        this.randomAccessFile = randomAccessFile;
    }

    @Override
    public int available() throws IOException
    {
        long n = getAvailableBytes.getAvailableBytes()-randomAccessFile.getFilePointer();

        while(n<=0)
        {
            Log.i("Aman", "sleeping available method");
            try {
                Thread.sleep(4000L);
            } catch (InterruptedException e) {

                e.printStackTrace();
                return -1;
            }
            n = this.getAvailableBytes.getAvailableBytes()-randomAccessFile.getFilePointer();

        }
        //Log.i("Aman","available method finished");
        Log.i("Aman","avaiable = "+n);
        return (int) n;

    }

    @Override
    public void close() throws IOException {
        randomAccessFile.close();
        super.close();
        Log.i("Aman","closed");
    }

    @Override
    public int read() throws IOException {
        throw new IOException("fuck off");
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        throw new IOException("fuck off");
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {

        int available = available();

        if(available==-1)
        {
            return -1;
        }

        int cnt = (available < byteCount) ? available : byteCount;
        int x= randomAccessFile.read(buffer, byteOffset, cnt);
        Log.i("Aman","read= "+x);
        return x;
    }

    @Override
    public long skip(long byteCount) throws IOException {
        if(byteCount<=0)
            return 0;
        long available = this.getAvailableBytes.getAvailableBytes()-randomAccessFile.getFilePointer();
        while(available < byteCount) {
            Log.i("Aman","sleeping skip");
            try {
                Thread.sleep(4000L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                return -1;
            }
            available=this.getAvailableBytes.getAvailableBytes()-randomAccessFile.getFilePointer();
        }

        long skipped = (available < byteCount) ? available : byteCount;
        Log.i("Aman","skipped="+skipped);
        int x=randomAccessFile.skipBytes((int)skipped);
        Log.i("Aman","skipBytes in file= "+x);
        return x;
    }

    public interface GetAvailableBytes {
        long getAvailableBytes();
    }
}