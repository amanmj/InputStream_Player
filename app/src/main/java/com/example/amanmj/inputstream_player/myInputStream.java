package com.example.amanmj.inputstream_player;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/* My custom built InputStream */
public class myInputStream extends InputStream {

    /*keeping track of bytesRead so that we have to call getAvailableBytes() whenever bytesRead is <= 0 */
    private long numberOfBytesCanBeRead = 0;

    private final GetAvailableBytes getAvailableBytes;
    private final RandomAccessFile randomAccessFile;

    public myInputStream(GetAvailableBytes getAvailableBytes, RandomAccessFile randomAccessFile) {
        this.getAvailableBytes = getAvailableBytes;
        this.randomAccessFile = randomAccessFile;
    }

    @Override
    public int available() throws IOException
    {
        if(numberOfBytesCanBeRead > 0)
            return (int) numberOfBytesCanBeRead;

        /*calculate number of bytes that can be read from myInputStream without blocking it */

        numberOfBytesCanBeRead = getAvailableBytes.getAvailableBytes()-randomAccessFile.getFilePointer();

        /*if bytes are not available return -1 */
        if(numberOfBytesCanBeRead < 0)
            return -1;

        /*keep running loop until we get a non-zero value of numberOfBytesCanBeRead*/
        while(numberOfBytesCanBeRead == 0)
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

        if(availableBytes < 0) {
            return -1;
        }

        int cnt = (availableBytes < byteCount) ? availableBytes : byteCount;

        /*read from randomAccessFile*/

        int read = randomAccessFile.read(buffer, byteOffset, cnt);
        numberOfBytesCanBeRead -= read;
        return read;
    }

    /*method to skip bytes in a the myInputStream*/
    @Override
    public long skip(long byteCount) throws IOException {
        if(byteCount < 0)
            return -1;

        long availableBytes = available();

        long skipped = (availableBytes < byteCount) ? availableBytes : byteCount;

        /*return number of bytes skipped*/
        int actualSkipped = randomAccessFile.skipBytes( (int)skipped );
        numberOfBytesCanBeRead -= actualSkipped;
        return actualSkipped;
    }

    /*an interface that gives us number of available bytes that we are increasing from a new thread in MainActivity*/
    public interface GetAvailableBytes {
        long getAvailableBytes();
    }
}