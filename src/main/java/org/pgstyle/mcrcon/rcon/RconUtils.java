package org.pgstyle.mcrcon.rcon;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

/**
 * @since since
 * @version version
 * @author PGKan
 */
public class RconUtils {

    public static final Charset UTF8 = StandardCharsets.UTF_8;

    private static final SecureRandom random;

    static {
        SecureRandom ran;
        try {
            ran = SecureRandom.getInstanceStrong();
        }
        catch (NoSuchAlgorithmException e) {
            ran = new SecureRandom();
        }
        random = ran;
    }

    /**
     * Convert {@code int} data into an array of {@code byte}.
     *
     * @param i
     *        the {@code int} data
     * @param be
     *        use big-dendian
     * @return an array of {@code byte}
     */
    public static byte[] fromInt(int i, boolean be) {
        byte bytes[] = new byte[4];
        for (int j = 0; j < 4; j++) {
            int li = be ? 3 - j : j;
            bytes[li] = (byte) (i >> j * 8);
        }
        return bytes;
    }

    /**
     * Merge the arguments {@code array1} and {@code array2} together with
     * {@code array1}
     * in the front.
     *
     * @param array1
     *        the first array
     * @param array2
     *        the second array
     * @return a new array with the merged data stored
     * @throws NullPointerException
     *         if the argument {@code array1} or {@code array2} is
     *         {@code null}.
     */
    public static byte[] merge(byte[] array1, byte[] array2) {
        Objects.requireNonNull(array1, "array1 == null");
        Objects.requireNonNull(array2, "array2 == null");
        byte[] array = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, array, 0, array1.length);
        System.arraycopy(array2, 0, array, array1.length, array2.length);
        return array;
    }

    /**
     * Get a partition of the argument {@code array} with specified start index
     * and length. Without extending the size of array if the length is greater
     * than the count of available elements.
     *
     * @param array
     *        the array
     * @param start
     *        the starting index
     * @param length
     *        the targeted count of element
     * @return a new array with the specified element stored
     * @throws IllegalArgumentException
     *         if the argument {@code array} is {@code null}; or argument
     *         {@code start} or {@code length} is negative.
     */
    public static byte[] partition(byte array[], int start, int length) {
        return RconUtils.partition(array, start, length, false);
    }

    /**
     * Get a partition of the argument {@code array} with specified start index
     * and length.
     *
     * @param array
     *        the array
     * @param start
     *        the starting index
     * @param length
     *        the targeted count of element
     * @param padding
     *        the length of return array is strictly defined by the argument
     *        {@code length} if set to {@code true}
     * @return a new array with the specified element stored
     * @throws IllegalArgumentException
     *         if the argument {@code array} is {@code null}; or argument
     *         {@code start} or {@code length} is negative.
     */
    public static byte[] partition(byte array[], int start, int length, boolean padding) {
        Objects.requireNonNull(array, "argument array must not null");
        return padding ? Arrays.copyOfRange(array, start, start + length)
            : Arrays.copyOfRange(array, start, Math.min(start + length, array.length));
    }

    public static int randi() {
        return RconUtils.random.nextInt(Integer.MAX_VALUE);
    }

    /**
     * Heavy buffered {@link InputStream} read, read all available content in
     * an {@code InputStream} into a byte array. This method is faster and more
     * robust than {@link InputStream#read(byte[])}. Using the default byte
     * buffer size of 2 <sup>16</sup>.
     *
     * @param inputStream
     *        the source {@code InputStream}
     * @return a byte array contains all data available in the
     *         {@code InputStream}
     * @throws IOException
     *         if the input stream is readable
     * @throws IllegalArgumentException
     *         if the argument inputStream is null.
     */
    public static byte[] read(InputStream inputStream) throws IOException {
        return RconUtils.read(inputStream, Short.MAX_VALUE + 1 << 1);
    }

    /**
     * Read the content of an {@link InputStream} into the buffer array. This
     * method work the same way as {@link InputStream#read(byte[])} but more
     * robust with concurrent stream reading.
     *
     * @param inputStream
     *        the source {@code InputStream}
     * @param buffer
     *        the destination of read content
     * @return the count of byte read from the {@code InputStream}
     * @throws IOException
     */
    public static int read(InputStream inputStream, byte[] buffer) throws IOException {
        int i = 0;
        inputStream = RconUtils.bufferedWrapper(inputStream);
        int b;
        while (i < buffer.length && (b = inputStream.read()) != -1) {
            buffer[i++] = (byte) b;
        }
        return i;
    }

    /**
     * Heavy buffered {@link InputStream} read, read all available content in
     * an {@code InputStream} into a byte array. This method is faster and more
     * robust than {@link InputStream#read(byte[])}. The size of byte buffer can
     * affect the speed, higher the buffer size faster the read speed up to the
     * size of available content length. The default buffer size is 2
     * <sup>16</sup>. This is recommended to use a byte buffer size larger than
     * 1024.
     *
     * @param inputStream
     *        the source {@code InputStream}
     * @param bufferSize
     *        the size of byte buffer
     * @return a byte array contains all data available in the
     *         {@code InputStream}
     * @throws IOException
     *         if the input stream is readable
     * @throws IllegalArgumentException
     *         if the argument inputStream is null; or bufferSize is
     *         negative or zero.
     */
    public static byte[] read(InputStream inputStream, int bufferSize) throws IOException {
        byte[] bytes = new byte[0];
        byte[] buffer = new byte[bufferSize];
        inputStream = RconUtils.bufferedWrapper(inputStream);
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            bytes = RconUtils.merge(bytes, RconUtils.partition(buffer, 0, length));
        }
        return bytes;
    }

    public static int readInt(InputStream inputStream, boolean be, int timeout) throws IOException {
        RconUtils.waitForLength(inputStream, 4, timeout);
        byte[] bytes = new byte[4];
        inputStream.read(bytes, 0, 4);
        return RconUtils.toInt(bytes, be);
    }

    public static String readString(InputStream inputStream, int length, int timeout) throws IOException {
        RconUtils.waitForLength(inputStream, length, timeout);
        byte[] bytes = new byte[length];
        inputStream.read(bytes, 0, length);
        return new String(bytes, RconUtils.UTF8);
    }

    public static void readTerminater(InputStream inputStream, int timeout) throws IOException {
        RconUtils.waitForLength(inputStream, 2, timeout);
        byte[] bytes = new byte[2];
        inputStream.read(bytes, 0, 2);
    }

    /**
     * Retrieve {@code int} data in an array of {@code byte}.
     *
     * @param bytes
     *        the array of {@code byte}
     * @param be
     *        use big-dendian
     * @return the {@code int} value
     */
    public static int toInt(byte[] bytes, boolean be) {
        Objects.requireNonNull(bytes, "argument bytes must not null");
        int i = 0;
        for (int j = 0; j < 4; j++) {
            int li = be ? 1 - j : j;
            i |= (bytes[li] & 0xff) << j * 8;
        }
        return i;
    }

    public static void waitForLength(InputStream inputStream, int length, int timeout) throws IOException {
        int wait = 0;
        while (inputStream.available() < length) {
            try {
                Thread.sleep(5);
            }
            catch (InterruptedException e) {}
            wait += 5;
            if (wait >= timeout) {
                throw new IOException("timeout");
            }
        }
    }

    /**
     * Wrap the {@code InputStream} in a {@code BufferedInputStream} if needed.
     *
     * @param inputStream
     *        the origin {@code InputStream}
     * @return a wrapped {@code BufferedInputStream}; or {@code inputStream}
     *         itself if it has already been a {@code BufferedInputStream}.
     */
    private static InputStream bufferedWrapper(InputStream inputStream) {
        if (inputStream instanceof BufferedInputStream) {
            return inputStream;
        }
        return new BufferedInputStream(inputStream);
    }

    /** The class {@code StreamUtils} is unnewable. */
    private RconUtils() {}

}
