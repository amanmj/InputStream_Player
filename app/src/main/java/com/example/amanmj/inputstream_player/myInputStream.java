package com.example.amanmj.inputstream_player;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/* My custom built InputStream */
public class myInputStream extends InputStream {

    private final GetAvailableBytes getAvailableBytes;
    private final RandomAccessFile randomAccessFile;

    public myInputStream(GetAvailableBytes getAvailableBytes, RandomAccessFile randomAccessFile) {
        this.getAvailableBytes = getAvailableBytes;
        this.randomAccessFile = randomAccessFile;
    }

    @Override
    public int available() throws IOException
    {
        /*calculate number of bytes that can be read from myInputStream without blocking it */
        long numberOfBytesCanBeRead = getAvailableBytes.getAvailableBytes()-randomAccessFile.getFilePointer();

        /*keep running loop until we get a non-zero value of numberOfBytesCanBeRead*/
        while(numberOfBytesCanBeRead <= 0)
        {
            try {
                /*sleep for 4 seconds*/
                Thread.sleep(4000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return -1;
            }
            numberOfBytesCanBeRead = this.getAvailableBytes.getAvailableBytes()-randomAccessFile.getFilePointer();
        }
        return (int) numberOfBytesCanBeRead;
    }

    @Override
    public void close() throws IOException {
        randomAccessFile.close();
        super.close();
    }

    @Override
    public int read() throws IOException {
        throw new IOException("Illegal To Enter Here");
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        throw new IOException("Illegal To Enter Here");
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {

        int availableBytes = available();

        if(availableBytes==-1) {
            return -1;
        }

        int cnt = (availableBytes < byteCount) ? availableBytes : byteCount;

        /*read from randomAccessFile*/
        return randomAccessFile.read(buffer, byteOffset, cnt);
    }

    /*method to skip bytes in a the myInputStream*/
    @Override
    public long skip(long byteCount) throws IOException {
        if(byteCount <= 0)
            return 0;

        long availableBytes = this.getAvailableBytes.getAvailableBytes()-randomAccessFile.getFilePointer();

        /*keep running loop until we get the appropriate availableBytes so that we can read from the position to which we skipped*/
        while(availableBytes < byteCount) {
            try {
                Thread.sleep(4000L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                return -1;
            }
            availableBytes=this.getAvailableBytes.getAvailableBytes()-randomAccessFile.getFilePointer();
        }

        long skipped = (availableBytes < byteCount) ? availableBytes : byteCount;

        /*return number of bytes skipped*/
        return randomAccessFile.skipBytes( (int)skipped );
    }

    /*an interface that gives us number of available bytes that we are increasing from a new thread in MainActivity*/
    public interface GetAvailableBytes {
        long getAvailableBytes();
    }
}