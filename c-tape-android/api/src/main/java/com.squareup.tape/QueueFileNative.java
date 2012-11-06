package com.squareup.tape;

import com.squareup.tape.QueueFile;
import com.squareup.tape.QueueFile.ElementReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.String;
import java.lang.System;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/*-[
  // Header for objectiveC code generated by j2objC
  #include "queuefile.h"
]-*/

public class QueueFileNative implements QueueFile {
  
  // NOTE: this object is accessed from native code.
  private ByteBuffer nativeObj;

  /**
   * Make SURE you call close() on every object, otherwise the native code will not free
   * associated resources (memory and file handle).
   * @param filename
   * @throws IOException
   */
  public QueueFileNative(String filename) throws IOException {
    // Stash value for native code to use later.
    nativeObj = nativeNew(filename);
  }

  private native ByteBuffer nativeNew(String filename) /*-[
    // objC code here
  ]-*/;

  public native int getFileLength() /*-[
    // objC code here
  ]-*/;

  @Override
  public void add(byte[] data) throws IOException {
    add(data, 0, data.length);
  }

  @Override
  public void add(byte[] data, int offset, int count)
      throws IOException {
    if (count + offset > data.length) {
      throw new IOException("Add attempted with count exceeding length" +
                            " of data array: " + (count + offset) +
                            " exceeds length " + data.length);
    }
    addUnchecked(data, offset, count);
  }

  private native void addUnchecked(byte[] data, int offset, int count);

  public native boolean isEmpty() /*-[
    // objC code here
  ]-*/;

  @Override
  public native byte[] peek() throws IOException /*-[
    // objC code here
  ]-*/;

  private native void nativePeekWithReader(NativeCallback ncb) throws
      IOException /*-[
    // objC code here
  ]-*/;

  @Override
  public void peek(ElementReader reader) throws IOException {
    nativePeekWithReader(new NativeCallback(reader));
  }

  private native void nativeForEach(NativeCallback ncb) throws IOException /*-[
    // objC code here
  ]-*/;

  @Override
  public void forEach(ElementReader reader) throws IOException {
    nativeForEach(new NativeCallback(reader));
  }

  @Override
  public native int size() /*-[
    // objC code here
  ]-*/;

  @Override
  public native void remove() throws IOException /*-[
    // objC code here
  ]-*/;

  @Override
  public native void clear() throws IOException /*-[
    // objC code here
  ]-*/;

  @Override
  public native void close() throws IOException /*-[
    // objC code here
  ]-*/;

  @Override
  public native String toString() /*-[
    // objC code here
  ]-*/;

  private static native void initIDs();
  
  static {
    System.loadLibrary("c-tape-android-native");
    initIDs();
  }

  private static native int nativeReadElementStream(ByteBuffer streamHandle, byte[] buffer, int offset, int length)
      throws  IOException /*-[
    // objC code here
  ]-*/;

  private static native int nativeReadElementStreamNextByte(ByteBuffer streamHandle)  throws  IOException /*-[
    // objC code here
  ]-*/;

  /** Reads a single element. */
  private static final class NativeCallback {
    ElementReader reader;
    
    private NativeCallback(ElementReader reader) {
      this.reader = reader;
    }

    /**
     * Callback is called from native code to inform reader data is ready.
     *
     * IMPORTANT: THE CONTRACT WITH THE C CODE IS SUCH THAT THE READER MUST
     * CALL BACK READ() - IF ANY OTHER CALLS TO THE QUEUE ARE DONE THERE
     * IS THE POTENTIAL FOR DEADLOCK OR OTHER UNDEFINED BEHAVIOUR ***.
     */
    private void callback(final ByteBuffer streamHandle, int length) throws IOException {
      reader.read(new InputStream() {

        /** @return number of bytes actually read or -1 if end of the stream has been reached. */
        @Override public int read(byte[] buffer, int offset, int length) throws IOException {
          if (buffer.length < offset + length) {
            throw new ArrayIndexOutOfBoundsException("read buffer won't fit length");
          }
          int bytesRead = nativeReadElementStream(streamHandle, buffer, offset, length);
          return bytesRead;
        }

        @Override public int read() throws IOException {
          return nativeReadElementStreamNextByte(streamHandle);
        }
      }, length);
    }
    
    private static native void initIDs();
    static {
      initIDs();
    }
  }
  
}
