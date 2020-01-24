package nl.tno.imb;

// TODO: add Google protocol buffers implementation  
// TODO: starting Java 1.7 the DirectByteBuffer seems quick enough, for now implement own buffer (backwards compatible <= 1.6)

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * General buffer for use within the IMB framework to standardize byte order and data exchange.<br>
 * IMB is always little-endian within the framework (and over the network). IMB defines the following types: byte, boolean,
 * int32, int64, single, double, string. string is defined as a byte array with UTF-8 characters prefixed with length as
 * int32.<br>
 * Class is to be extended with Google Protocol Buffers to store data compressed and compatible with Protocol Buffers.<br>
 * Standard Java ByteBuffers are not used because of performance issues.<br>
 * Java 1.7 defines DirectByteBuffer which looks promising but for now is not widely used.<br>
 * All reads advance the read cursor. All qreads advance the read cursor and read without any checking. All writes advance the
 * write cursor. All qwrites advance the write cursor and write without any checking. All prepares advance the prepare
 * cursor.<br>
 * Prepares are to be used to optimize the buffer memory allocations. prepareApply closes the prepares and allocates the newly
 * needed buffer size.
 * @author hans.cornelissen@tno.nl
 */
public class TByteBuffer
{

    // constructors

    /**
     * Constructor: create byte buffer of the specified length
     * @param aLength int; length in bytes of the buffer to create
     */
    public TByteBuffer(int aLength)
    {
        this.fBuffer = new byte[aLength];
    }

    /**
     * Constructor: create empty byte buffer
     */
    public TByteBuffer()
    {
        this.fBuffer = new byte[0];
    }

    /**
     * Constructor: create byte buffer as copy of the specified byte array
     * @param aBuffer byte[];
     */
    public TByteBuffer(byte[] aBuffer)
    {
        this.fBuffer = aBuffer;
    }

    private byte[] fBuffer = null;

    private int fReadCursor = 0;

    private int fWriteCursor = 0;

    private int fPrepareCursor = 0;

    /**
     * length of the byte buffer in bytes
     * @return length of the allocated buffer in bytes
     */
    public int getLength()
    {
        return this.fBuffer.length;
    }

    /**
     * adjust the length of the buffer retaining buffer contents (less when new size is less)
     * @param aLength int; new size of buffer in bytes
     */
    private void setLength(int aLength)
    {
        if (aLength != this.fBuffer.length)
        {
            // create new buffer
            byte[] NewBuffer = new byte[aLength];
            // check how much data to copy from old buffer
            int len = this.fBuffer.length;
            if (len > aLength)
                len = aLength;
            // copy old data to new buffer
            for (int i = 0; i < len; i++)
                NewBuffer[i] = this.fBuffer[i];
            this.fBuffer = NewBuffer;
        }
    }

    // uncompressed size of variables within the framework
    /** uncompressed size of boolean within IMB framework */
    public static final int SIZE_OF_BOOLEAN = Byte.SIZE / 8;

    /** uncompressed size of byte within IMB framework */
    public static final int SIZE_OF_BYTE = Byte.SIZE / 8;

    /** uncompressed size of 32 bit integer within IMB framework */
    public static final int SIZE_OF_INT32 = Integer.SIZE / 8;

    /** uncompressed size of 64 bit integer within IMB framework */
    public static final int SIZE_OF_INT64 = Long.SIZE / 8;

    /** uncompressed size of single within IMB framework */
    public static final int SIZE_OF_SINGLE = Float.SIZE / 8;

    /** uncompressed size of double within IMB framework */
    public static final int SIZE_OF_DOUBLE = Double.SIZE / 8;

    /**
     * Retrieve reference to internal byte buffer
     * @return reference to internal byte buffer
     */
    public byte[] getBuffer()
    {
        return this.fBuffer;
    }

    /** Clear the byte buffer to zero length and reset all cursors */
    public void clear()
    {
        setLength(0);
        this.fReadCursor = 0;
        this.fPrepareCursor = 0;
        this.fWriteCursor = 0;
    }

    /** Clear the byte buffer to the specified length and reset all cursors. */
    public void clear(int aLength)
    {
        setLength(aLength);
        this.fReadCursor = 0;
        this.fWriteCursor = 0;
        this.fPrepareCursor = 0;
    }

    /**
     * Check if the byte buffer is empty
     * @return true if the byte buffer is empty
     */
    public boolean isEmpty()
    {
        return this.fBuffer.length == 0;
    }

    /**
     * Retrieve the current reading (cursor) position
     * @return cursor position for reading: index into byte buffer in bytes starting at 0
     */
    public int getReadCursor()
    {
        return this.fReadCursor;
    }

    /**
     * Retrieve the current writing (cursor) position
     * @return cursor position for writing: index into byte buffer in bytes starting at 0
     */
    public int getWriteCursor()
    {
        return this.fWriteCursor;
    }

    /** Reset the reading cursor */
    public void ReadStart()
    {
        this.fReadCursor = 0;
    }

    /**
     * Retrieve the bytes that still can be read from the byte buffer
     * @return bytes available for reading
     */
    public int getReadAvailable()
    {
        return this.fBuffer.length - this.fReadCursor;
    }

    /**
     * Read a boolean from the byte buffer
     * @return boolean read from byte buffer (default is false)
     */
    public boolean readBoolean()
    {
        return readBoolean(false);
    }

    /**
     * Read a boolean from the byte buffer
     * @param aDefaultValue boolean; in case of a read error this value is returned
     * @return boolean read from byte buffer
     */
    public boolean readBoolean(boolean aDefaultValue)
    {
        if (SIZE_OF_BOOLEAN <= getReadAvailable())
            return this.fBuffer[this.fReadCursor++] != 0;
        else
            return aDefaultValue;
    }

    /**
     * Read a byte from the byte buffer
     * @return byte read from the byte buffer
     */
    public byte readByte()
    {
        return readByte((byte) 0);
    }

    /**
     * Read a byte from the byte buffer
     * @param aDefaultValue byte; in case of a read error this value is returned
     * @return byte read from the byte buffer
     */
    public byte readByte(byte aDefaultValue)
    {
        if (SIZE_OF_BYTE <= getReadAvailable())
            return this.fBuffer[this.fReadCursor++];
        else
            return aDefaultValue;
    }

    /**
     * Read an integer (32 bit) from the byte buffer
     * @return integer (32 bit) read from the byte buffer
     */
    public int readInt32()
    {
        return readInt32(0);
    }

    /**
     * Read an integer (32 bit) from the byte buffer
     * @param aDefaultValue int; in case of a read error this value is returned
     * @return integer (32 bit) read from the byte buffer
     */
    public int readInt32(int aDefaultValue)
    {
        if (SIZE_OF_INT32 <= getReadAvailable())
        {
            this.fReadCursor += SIZE_OF_INT32;
            return (int) (this.fBuffer[this.fReadCursor - 4] & 0xFF) + ((int) (this.fBuffer[this.fReadCursor - 3] & 0xFF) << 8)
                    + ((int) (this.fBuffer[this.fReadCursor - 2] & 0xFF) << 16)
                    + ((int) (this.fBuffer[this.fReadCursor - 1] & 0xFF) << 24);
        }
        else
            return aDefaultValue;
    }

    /**
     * Read an integer (64 bit) from the byte buffer
     * @return integer (64 bit) read from the byte buffer
     */
    public long readInt64()
    {
        return readInt64(0);
    }

    /**
     * Read an integer (64 bit) from the byte buffer
     * @param aDefaultValue long; in case of a read error this value is returned
     * @return integer (64 bit) read from the byte buffer
     */
    public long readInt64(long aDefaultValue)
    {
        if (SIZE_OF_INT64 <= getReadAvailable())
        {
            this.fReadCursor += SIZE_OF_INT64;
            return (long) (this.fBuffer[this.fReadCursor - 8] & 0xFF)
                    + ((long) (this.fBuffer[this.fReadCursor - 7] & 0xFF) << 8)
                    + ((long) (this.fBuffer[this.fReadCursor - 6] & 0xFF) << 16)
                    + ((long) (this.fBuffer[this.fReadCursor - 5] & 0xFF) << 24)
                    + ((long) (this.fBuffer[this.fReadCursor - 4] & 0xFF) << 32)
                    + ((long) (this.fBuffer[this.fReadCursor - 3] & 0xFF) << 40)
                    + ((long) (this.fBuffer[this.fReadCursor - 2] & 0xFF) << 48)
                    + ((long) (this.fBuffer[this.fReadCursor - 1] & 0xFF) << 56);
        }
        else
            return aDefaultValue;
    }

    /**
     * Read a single float from the byte buffer
     * @return single float read from the byte buffer
     */
    public float readSingle()
    {
        return readSingle(Float.NaN);
    }

    /**
     * Read a single float from the byte buffer
     * @param aDefaultValue float; in case of a read error this value is returned
     * @return single float read from the byte buffer
     */
    public float readSingle(float aDefaultValue)
    {
        if (SIZE_OF_SINGLE <= getReadAvailable())
        {
            return Float.intBitsToFloat(readInt32(0));
        }
        else
            return aDefaultValue;
    }

    /**
     * Read a double float from the byte buffer
     * @return double float read from the byte buffer
     */
    public double readDouble()
    {
        return readDouble(Double.NaN);
    }

    /**
     * Read a double float from the byte buffer
     * @param aDefaultValue double; in case of a read error this value is returned
     * @return double float read from the byte buffer
     */
    public double readDouble(double aDefaultValue)
    {
        if (SIZE_OF_DOUBLE <= getReadAvailable())
        {
            long res = readInt64(0);
            return Double.longBitsToDouble(res);
        }
        else
            return aDefaultValue;
    }

    /**
     * Read a string from the byte buffer. The string is converted from UTF-8 to a standard string.
     * @return the string read from the byte buffer
     */
    public String readString()
    {
        return readString("");
    }

    /**
     * Read a string from the byte buffer. The string is converted from UTF-8 to a standard string.
     * @param aDefaultValue String; in case of a read error this value is returned
     * @return the string read from the byte buffer
     */
    public String readString(String aDefaultValue)
    {
        int len = readInt32(-1);
        if ((len != -1) && (len <= getReadAvailable()))
        {
            if (len > 0)
            {
                this.fReadCursor += len;
                return new String(this.fBuffer, this.fReadCursor - len, len, Charset.forName("UTF-8"));
            }
            else
                return "";
        }
        else
            return aDefaultValue;
    }

    // read size and data and store as a whole WITHOUT size (size=length buffer)

    /**
     * Read new byte buffer contents from this byte buffer. Decode a size prefixed number of bytes from the byte buffer into a
     * newly created byte buffer. The size part is not transfered to the new byte buffer but used as the length.
     * @return a new byte buffer containing the data read from this byte buffer
     */
    public TByteBuffer readByteBuffer()
    {
        int len = readInt32(-1);
        if ((len != -1) && (len <= getReadAvailable()))
        {
            this.fReadCursor += len;
            return new TByteBuffer(Arrays.copyOfRange(this.fBuffer, this.fReadCursor - len, this.fReadCursor));
        }
        else
            return null;
    }

    // class version of simple read functions (auto type detect by overloading)

    /**
     * Read a Boolean object from the this byte buffer.
     * @param aValue the Boolean to be read from this byte buffer
     * @throws IndexOutOfBoundsException when the value could not be read from this byte buffer
     */
    // public void read(Boolean aValue) throws IndexOutOfBoundsException {
    // if (getReadAvailable() < SIZE_OF_BOOLEAN)
    // throw new IndexOutOfBoundsException();
    // aValue = fBuffer[fReadCursor++] != 0;
    // }

    /**
     * Read a Byte object from the this byte buffer.
     * @param aValue the Byte to be read from this byte buffer
     * @throws IndexOutOfBoundsException when the value could not be read from this byte buffer
     */
    // public void read(Byte aValue) throws IndexOutOfBoundsException {
    // if (getReadAvailable() < SIZE_OF_BYTE)
    // throw new IndexOutOfBoundsException();
    // aValue = fBuffer[fReadCursor++];
    // }

    /**
     * Read an Integer object from the this byte buffer.
     * @param aValue the Integer to be read from this byte buffer
     * @throws IndexOutOfBoundsException when the value could not be read from this byte buffer
     */
    // public void read(Integer aValue) throws IndexOutOfBoundsException {
    // if (getReadAvailable() < SIZE_OF_INT32)
    // throw new IndexOutOfBoundsException();
    // fReadCursor += SIZE_OF_INT32;
    // aValue = (fBuffer[fReadCursor - 4] & 0xFF) + ((fBuffer[fReadCursor - 3] & 0xFF) << 8)
    // + ((fBuffer[fReadCursor - 2] & 0xFF) << 16) + ((fBuffer[fReadCursor - 1] & 0xFF) << 24);
    // }

    /**
     * Read a Long object from the this byte buffer.
     * @param aValue the Long to be read from this byte buffer
     * @throws IndexOutOfBoundsException when the value could not be read from this byte buffer
     */
    // public void read(Long aValue) throws IndexOutOfBoundsException {
    // if (getReadAvailable() < SIZE_OF_INT64)
    // throw new IndexOutOfBoundsException();
    // fReadCursor += SIZE_OF_INT64;
    // aValue = (long)(fBuffer[fReadCursor - 8] & 0xFF) + ((fBuffer[fReadCursor - 7] & 0xFF) << 8)
    // + ((fBuffer[fReadCursor - 6] & 0xFF) << 16) + ((fBuffer[fReadCursor - 5] & 0xFF) << 24)
    // + ((fBuffer[fReadCursor - 4] & 0xFF) << 32) + ((fBuffer[fReadCursor - 3] & 0xFF) << 40)
    // + ((fBuffer[fReadCursor - 2] & 0xFF) << 48) + ((fBuffer[fReadCursor - 1] & 0xFF) << 56);
    // }

    /**
     * Read a Float object from the this byte buffer.
     * @param aValue the Float to be read from this byte buffer
     * @throws IndexOutOfBoundsException when the value could not be read from this byte buffer
     */
    // public void read(Float aValue) throws IndexOutOfBoundsException {
    // if (getReadAvailable() < SIZE_OF_SINGLE)
    // throw new IndexOutOfBoundsException();
    // aValue = Float.intBitsToFloat(readInt32(0));
    // }

    /**
     * Read a Double object from the this byte buffer.
     * @param aValue the Double to be read from this byte buffer
     * @throws IndexOutOfBoundsException when the value could not be read from this byte buffer
     */
    // public void read(Double aValue) throws IndexOutOfBoundsException {
    // if (getReadAvailable() < SIZE_OF_DOUBLE)
    // throw new IndexOutOfBoundsException();
    // aValue = Double.longBitsToDouble(readInt64(0));
    // }

    /**
     * Read a String object from the this byte buffer.
     * @param aValue the String to be read from this byte buffer
     * @throws IndexOutOfBoundsException when the value could not be read from this byte buffer
     */
    // public void read(String aValue) throws IndexOutOfBoundsException {
    // Integer len = new Integer(0);
    // read(len);
    // if (len > 0)
    // {
    // if (getReadAvailable() < len)
    // throw new IndexOutOfBoundsException();
    // fReadCursor += len;
    // aValue = new String(fBuffer, fReadCursor - len, len, Charset.forName("UTF-8"));
    // }
    // else
    // aValue = "";
    // }

    /**
     * Read size and data and store as a whole WITHOUT size (size=length buffer)
     * @param aValue TByteBuffer; byte buffer to store the read data in
     */
    public void readByteBuffer(TByteBuffer aValue)
    {
        Integer len = new Integer(0);
        len = readInt32();
        if (len > 0)
        {
            aValue.setLength(len);
            this.fReadCursor += len;
            for (int i = 0; i < len; i++)
                aValue.fBuffer[i] = this.fBuffer[this.fReadCursor + i];
        }
        else
            aValue.clear();
    }

    // QRead (no checking)
    // Read a boolean from the byte buffer aDefaultValue in case of a read error this value is returned return boolean read from
    // byte buffer

    /**
     * Read a boolean from the byte buffer without any checks
     * @return the boolean read from the byte buffer
     */
    public boolean qReadBoolean()
    {
        return this.fBuffer[this.fReadCursor++] != 0;
    }

    /**
     * Read a byte from the byte buffer without any checks
     * @return the byte read from the byte buffer
     */
    public byte qReadByte()
    {
        return this.fBuffer[this.fReadCursor++];
    }

    /**
     * Read an 32 bit integer from the byte buffer without any checks
     * @return the 32 bit integer read from the byte buffer
     */
    public int qReadInt32()
    {
        this.fReadCursor += SIZE_OF_INT32;
        return (int) (this.fBuffer[this.fReadCursor - 4] & 0xFF) + ((int) (this.fBuffer[this.fReadCursor - 3] & 0xFF) << 8)
                + ((int) (this.fBuffer[this.fReadCursor - 1] & 0xFF) << 16)
                + ((int) (this.fBuffer[this.fReadCursor - 1] & 0xFF) << 24);
    }

    /**
     * Read an 64 bit integer from the byte buffer without any checks
     * @return the 64 bit integer read from the byte buffer
     */
    public long qReadInt64()
    {
        this.fReadCursor += SIZE_OF_INT64;
        return (long) (this.fBuffer[this.fReadCursor - 8] & 0xFF) + ((long) (this.fBuffer[this.fReadCursor - 7] & 0xFF) << 8)
                + ((long) (this.fBuffer[this.fReadCursor - 6] & 0xFF) << 16)
                + ((long) (this.fBuffer[this.fReadCursor - 5] & 0xFF) << 24)
                + ((long) (this.fBuffer[this.fReadCursor - 4] & 0xFF) << 32)
                + ((long) (this.fBuffer[this.fReadCursor - 3] & 0xFF) << 40)
                + ((long) (this.fBuffer[this.fReadCursor - 2] & 0xFF) << 48)
                + ((long) (this.fBuffer[this.fReadCursor - 1] & 0xFF) << 56);
    }

    /**
     * Read a single (32 bit float) from the byte buffer without any checks
     * @return the single (32 bit float) read from the byte buffer
     */
    public float qReadSingle()
    {
        // size of float = size of int (int32)
        return Float.intBitsToFloat(readInt32(0));
    }

    /**
     * Read a double from the byte buffer without any checks
     * @return the double read from the byte buffer
     */
    public double qReadDouble(double aDefaultValue)
    {
        // size of double = size of long (int64)
        return Double.longBitsToDouble(readInt64(0));
    }

    /**
     * Read a string from the byte buffer without any checks
     * @return the string read from the byte buffer
     */
    public String qReadString(String aDefaultValue)
    {
        int len = qReadInt32();
        if (len > 0)
        {
            this.fReadCursor += len;
            return new String(this.fBuffer, this.fReadCursor - len, len, Charset.forName("UTF-8"));
        }
        else
            return "";
    }

    /**
     * Read a byte buffer this byte buffer without any checks Read size and data and store as a whole WITHOUT size (size=length
     * buffer)
     * @return the new byte buffer read from the byte buffer
     */
    public TByteBuffer qReadByteBuffer()
    {
        int len = qReadInt32();
        this.fReadCursor += len;
        return new TByteBuffer(Arrays.copyOfRange(this.fBuffer, this.fReadCursor - len, this.fReadCursor));
    }

    /**
     * Read all data available from the read cursor in this byte buffer to a newly created byte buffer
     * @return a newly created byte buffer that contains all data read
     */
    public TByteBuffer readRestToByteBuffer()
    {
        return new TByteBuffer(readRest());
    }

    /**
     * Read all data available from the read cursor
     * @return a byte array containing all data read
     */
    public byte[] readRest()
    {
        return Arrays.copyOfRange(this.fBuffer, this.fReadCursor, this.fReadCursor + getReadAvailable());
    }

    /**
     * Skip the specified amount of bytes for reading Advances the read cursor the specified amount of bytes
     * @param aValueSize int; number of bytes to skip for reading
     */
    public void skipReading(int aValueSize)
    {
        this.fReadCursor += aValueSize;
    }

    // peek type result
    /**
     * Read a boolean from the byte buffer at an offset to the read cursor without advancing the read cursor. If the value could
     * not be read the default 'false' is returned.
     * @param aOffset int; 0-based offset to the read cursor to peek at for the boolean
     * @return the value read from the byte buffer at the specified offset
     */
    public boolean peekBoolean(int aOffset)
    {
        return peekBoolean(aOffset, false);
    }

    /**
     * Read a boolean from the byte buffer at an offset to the read cursor without advancing the read cursor.
     * @param aOffset int; 0-based offset to the read cursor to peek at for the boolean
     * @param aDefaultValue boolean; if the value could not be read this default is returned
     * @return the value read from the byte buffer at the specified offset
     */
    public boolean peekBoolean(int aOffset, boolean aDefaultValue)
    {
        if (SIZE_OF_BOOLEAN + aOffset <= getReadAvailable())
            return this.fBuffer[this.fReadCursor + aOffset] != 0;
        else
            return aDefaultValue;
    }

    /**
     * Read a byte from the byte buffer at an offset to the read cursor without advancing the read cursor. If the value could
     * not be read the default '0' is returned.
     * @param aOffset int; 0-based offset to the read cursor to peek at for the byte
     * @return the value read from the byte buffer at the specified offset
     */
    public byte peekByte(int aOffset)
    {
        return peekByte(aOffset, (byte) 0);
    }

    /**
     * Read a byte from the byte buffer at an offset to the read cursor without advancing the read cursor.
     * @param aOffset int; 0-based offset to the read cursor to peek at for the byte
     * @param aDefaultValue byte; if the value could not be read this default is returned
     * @return the value read from the byte buffer at the specified offset
     */
    public byte peekByte(int aOffset, byte aDefaultValue)
    {
        if (SIZE_OF_BYTE + aOffset <= getReadAvailable())
            return this.fBuffer[this.fReadCursor + aOffset];
        else
            return aDefaultValue;
    }

    /**
     * Read an 32 bit integer from the byte buffer at an offset to the read cursor without advancing the read cursor. If the
     * value could not be read the default '0' is returned.
     * @param aOffset int; 0-based offset to the read cursor to peek at for the integer
     * @return the value read from the byte buffer at the specified offset
     */
    public int peekInt32(int aOffset)
    {
        return peekInt32(aOffset, 0);
    }

    /**
     * Read an 32 bit integer from the byte buffer at an offset to the read cursor without advancing the read cursor.
     * @param aOffset int; 0-based offset to the read cursor to peek at for the integer
     * @param aDefaultValue int; if the value could not be read this default is returned
     * @return the value read from the byte buffer at the specified offset
     */
    public int peekInt32(int aOffset, int aDefaultValue)
    {
        if (SIZE_OF_INT32 + aOffset <= getReadAvailable())
        {
            return (int) (this.fBuffer[this.fReadCursor + aOffset] & 0xFF)
                    + ((int) (this.fBuffer[this.fReadCursor + aOffset + 1] & 0xFF) << 8)
                    + ((int) (this.fBuffer[this.fReadCursor + aOffset + 2] & 0xFF) << 16)
                    + ((int) (this.fBuffer[this.fReadCursor + aOffset + 3] & 0xFF) << 24);
        }
        else
            return aDefaultValue;
    }

    /**
     * Read an 64 bit integer (long) from the byte buffer at an offset to the read cursor without advancing the read cursor. If
     * the value could not be read the default '0' is returned.
     * @param aOffset int; 0-based offset to the read cursor to peek at for the long
     * @return the value read from the byte buffer at the specified offset
     */
    public long peekInt64(int aOffset)
    {
        return peekInt64(aOffset, 0);
    }

    /**
     * Read an 64 bit integer (long) from the byte buffer at an offset to the read cursor without advancing the read cursor.
     * @param aOffset int; 0-based offset to the read cursor to peek at for the long
     * @param aDefaultValue long; if the value could not be read this default is returned
     * @return the value read from the byte buffer at the specified offset
     */
    public long peekInt64(int aOffset, long aDefaultValue)
    {
        if (SIZE_OF_INT64 + aOffset <= getReadAvailable())
            return (long) (this.fBuffer[this.fReadCursor + aOffset] & 0xFF)
                    + ((long) (this.fBuffer[this.fReadCursor + aOffset + 1] & 0xFF) << 8)
                    + ((long) (this.fBuffer[this.fReadCursor + aOffset + 2] & 0xFF) << 16)
                    + ((long) (this.fBuffer[this.fReadCursor + aOffset + 3] & 0xFF) << 24)
                    + ((long) (this.fBuffer[this.fReadCursor + aOffset + 4] & 0xFF) << 32)
                    + ((long) (this.fBuffer[this.fReadCursor + aOffset + 5] & 0xFF) << 40)
                    + ((long) (this.fBuffer[this.fReadCursor + aOffset + 6] & 0xFF) << 48)
                    + ((long) (this.fBuffer[this.fReadCursor + aOffset + 7] & 0xFF) << 56);
        else
            return aDefaultValue;
    }

    /**
     * Read a single (32 bit float) from the byte buffer at an offset to the read cursor without advancing the read cursor. If
     * the value could not be read the default 'NaN' is returned.
     * @param aOffset int; 0-based offset to the read cursor to peek at for the float
     * @return the value read from the byte buffer at the specified offset
     */
    public float peekSingle(int aOffset)
    {
        return peekSingle(aOffset, Float.NaN);
    }

    /**
     * Read a single (32 bit float) from the byte buffer at an offset to the read cursor without advancing the read cursor.
     * @param aOffset int; 0-based offset to the read cursor to peek at for the float
     * @param aDefaultValue float; if the value could not be read this default is returned
     * @return the value read from the byte buffer at the specified offset
     */
    public float peekSingle(int aOffset, float aDefaultValue)
    {
        if (SIZE_OF_SINGLE + aOffset <= getReadAvailable())
            return Float.intBitsToFloat(peekInt32(aOffset, 0));
        else
            return aDefaultValue;
    }

    /**
     * Read a double from the byte buffer at an offset to the read cursor without advancing the read cursor. If the value could
     * not be read the default 'NaN' is returned.
     * @param aOffset int; 0-based offset to the read cursor to peek at for the double
     * @return the value read from the byte buffer at the specified offset
     */
    public double peekDouble(int aOffset)
    {
        return peekDouble(aOffset, Double.NaN);
    }

    /**
     * Read a double from the byte buffer at an offset to the read cursor without advancing the read cursor.
     * @param aOffset int; 0-based offset to the read cursor to peek at for the double
     * @param aDefaultValue double; if the value could not be read this default is returned
     * @return the value read from the byte buffer at the specified offset
     */
    public double peekDouble(int aOffset, double aDefaultValue)
    {
        if (SIZE_OF_DOUBLE + aOffset <= getReadAvailable())
            return Double.longBitsToDouble(peekInt64(aOffset, 0));
        else
            return aDefaultValue;
    }

    /**
     * Read a string from the byte buffer at an offset to the read cursor without advancing the read cursor. If the value could
     * not be read the default "" is returned.
     * @param aOffset int; 0-based offset to the read cursor to peek at for the string
     * @return the value read from the byte buffer at the specified offset
     */
    public String peekString(int aOffset)
    {
        return peekString(aOffset, "");
    }

    /**
     * Read a string from the byte buffer at an offset to the read cursor without advancing the read cursor.
     * @param aOffset int; 0-based offset to the read cursor to peek at for the string
     * @param aDefaultValue String; if the value could not be read this default is returned
     * @return the value read from the byte buffer at the specified offset
     */
    public String peekString(int aOffset, String aDefaultValue)
    {
        int len = peekInt32(aOffset, -1);
        if (len >= 0)
        {
            if (len + aOffset <= getReadAvailable())
            {
                return new String(this.fBuffer, this.fReadCursor + aOffset, len, Charset.forName("UTF-8"));
            }
            else
                return aDefaultValue;
        }
        else
            return aDefaultValue;
    }

    /**
     * Compare the contents of this byte buffer at the read cursor to a given byte array
     * @param aValue byte[]; byte array to compare with this byte buffer
     * @param aOffset int; offset to the read cursor to start comparing
     * @return if the byte buffer data equals the byte array contents true is returned
     */
    public boolean compare(byte[] aValue, int aOffset)
    {
        if (aOffset + aValue.length <= getReadAvailable())
        {
            for (int i = 0; i < aValue.length; i++)
            {
                if (this.fBuffer[aOffset + this.fReadCursor + i] != aValue[i])
                    return false;
            }
            return true;
        }
        else
            return false;
    }

    /**
     * Shift all bytes in the byte buffer to the left and insert a new byte to the right (end of byte buffer)
     * @param aRightByte byte; the byte to insert at the right side of the byte buffer after the shift
     */
    public void shiftLeftOneByte(byte aRightByte)
    {
        for (int i = 0; i < this.fBuffer.length - 1; i++)
            this.fBuffer[i] = this.fBuffer[i + 1];
        this.fBuffer[this.fBuffer.length - 1] = aRightByte;
    }

    /**
     * Start prepare sequence. This should be called before prepare calls. Resets the prepare cursor to the current writing
     * cursor position.
     * @return the new prepare cursor position. This can be used to calls of writeStart to reuse a byte buffer with partial
     *         deviating data.
     */
    public int prepareStart()
    {
        this.fPrepareCursor = this.fWriteCursor;
        return this.fPrepareCursor;
    }

    /**
     * Prepares the byte buffer for later writing of a boolean. Advances the prepare cursor for the correct amount of bytes to
     * write a boolean.
     * @param aValue boolean; the boolean to be written later in a call to qWrite
     */
    public void prepare(boolean aValue)
    {
        this.fPrepareCursor += SIZE_OF_BOOLEAN;
    }

    /**
     * Prepares the byte buffer for later writing of a byte. Advances the prepare cursor for the correct amount of bytes to
     * write a byte.
     * @param aValue byte; the byte to be written later in a call to qWrite
     */
    public void prepare(byte aValue)
    {
        this.fPrepareCursor += SIZE_OF_BYTE;
    }

    /**
     * Prepares the byte buffer for later writing of an 32 bit integer. Advances the prepare cursor for the correct amount of
     * bytes to write an 32 bit integer.
     * @param aValue int; the 32 bit integer to be written later in a call to qWrite
     */
    public void prepare(int aValue)
    {
        this.fPrepareCursor += SIZE_OF_INT32;
    }

    /**
     * Prepares the byte buffer for later writing of an 64 bit integer (long). Advances the prepare cursor for the correct
     * amount of bytes to write an 64 bit integer (long).
     * @param aValue long; the 64 bit integer (long) to be written later in a call to qWrite
     */
    public void prepare(long aValue)
    {
        this.fPrepareCursor += SIZE_OF_INT64;
    }

    /**
     * Prepares the byte buffer for later writing of a 32 bit single (float). Advances the prepare cursor for the correct amount
     * of bytes to write a 32 bit single (float).
     * @param aValue float; the 32 bit single (float) to be written later in a call to qWrite
     */
    public void prepare(float aValue)
    {
        this.fPrepareCursor += SIZE_OF_SINGLE;
    }

    /**
     * Prepares the byte buffer for later writing of a double. Advances the prepare cursor for the correct amount of bytes to
     * write a double.
     * @param aValue double; the double to be written later in a call to qWrite
     */
    public void prepare(double aValue)
    {
        this.fPrepareCursor += SIZE_OF_DOUBLE;
    }

    /**
     * Prepares the byte buffer for later writing of a string. Advances the prepare cursor for the correct amount of bytes to
     * write a string (including its size).
     * @param aValue String; the string to be written later in a call to qWrite
     */
    public void prepare(String aValue)
    {
        this.fPrepareCursor += SIZE_OF_INT32 + aValue.getBytes(Charset.forName("UTF-8")).length;
    }

    /**
     * Prepares the byte buffer for later writing of a byte array (without size). Advances the prepare cursor for the correct
     * amount of bytes to write a byte array (without size).
     * @param aValue byte[]; the byte array to be written later in a call to qWrite
     */
    public void prepare(byte[] aValue)
    {
        this.fPrepareCursor += aValue.length;
    }

    /**
     * Prepares the byte buffer for later writing of an other byte buffers readable data. Advances the prepare cursor for the
     * correct amount of bytes to write a byte buffers readable data.
     * @param aValue TByteBuffer; the byte buffers readable data to be written later in a call to qWrite
     */
    public void prepare(TByteBuffer aValue)
    {
        this.fPrepareCursor += SIZE_OF_INT32 + aValue.getReadAvailable();
    }

    /**
     * Prepares the byte buffer for later writing of the specified number of bytes. Advances the prepare cursor for the correct
     * amount of bytes to write the specified number of bytes.
     * @param aValueSize int; the number of bytes to be written later in a call to qWrite
     */
    public int prepareSize(int aValueSize)
    {
        int res = this.fPrepareCursor;
        this.fPrepareCursor += aValueSize;
        return res;
    }

    /**
     * Adjusts the length of the byte buffer to accommodate at a minimum all prepared data
     */
    public void prepareApply()
    {
        if (this.fBuffer.length < this.fPrepareCursor)
            setLength(this.fPrepareCursor);
    }

    /**
     * Adjusts the length of the byte buffer to accommodate exactly all prepared data
     */
    public void prepareApplyAndTrim()
    {
        if (this.fBuffer.length != this.fPrepareCursor)
            setLength(this.fPrepareCursor);
    }

    /**
     * Start writing at the specified 0-based index. Resets the write cursor to the specified 0-based index
     * @param aIndex int; the new value for the write cursor
     */
    public void writeStart(int aIndex)
    {
        this.fWriteCursor = aIndex;
    }

    /**
     * Returns the room still available in the byte buffer to write data to without reallocating memory.
     * @return the room still left in the byte buffer to write
     */
    public int getwriteAvailable()
    {
        return this.fBuffer.length - this.fWriteCursor;
    }

    /**
     * Write the specified boolean to the byte buffer. Buffer space is allocated if needed.
     * @param aValue boolean; the boolean to be written to the byte buffer
     */
    public void write(boolean aValue)
    {
        if (SIZE_OF_BOOLEAN > getwriteAvailable())
            setLength(this.fWriteCursor + SIZE_OF_BOOLEAN);
        this.fBuffer[this.fWriteCursor] = (aValue) ? (byte) -1 : (byte) 0;
        this.fWriteCursor += SIZE_OF_BOOLEAN;
    }

    /**
     * Write the specified byte to the byte buffer. Buffer space is allocated if needed.
     * @param aValue byte; the byte to be written to the byte buffer
     */
    public void write(byte aValue)
    {
        if (SIZE_OF_BYTE > getwriteAvailable())
            setLength(this.fWriteCursor + SIZE_OF_BYTE);
        this.fBuffer[this.fWriteCursor] = aValue;
        this.fWriteCursor += SIZE_OF_BYTE;
    }

    /**
     * Write the specified 32 bit integer to the byte buffer. Buffer space is allocated if needed.
     * @param aValue int; the 32 bit integer to be written to the byte buffer
     */
    public void write(int aValue)
    {
        if (SIZE_OF_INT32 > getwriteAvailable())
            setLength(this.fWriteCursor + SIZE_OF_INT32);
        this.fBuffer[this.fWriteCursor] = (byte) (aValue & 0xFF);
        this.fBuffer[this.fWriteCursor + 1] = (byte) ((aValue >> 8) & 0xFF);
        this.fBuffer[this.fWriteCursor + 2] = (byte) ((aValue >> 16) & 0xFF);
        this.fBuffer[this.fWriteCursor + 3] = (byte) ((aValue >> 24) & 0xFF);
        this.fWriteCursor += SIZE_OF_INT32;
    }

    /**
     * Write the specified 64 bit integer (long) to the byte buffer. Buffer space is allocated if needed.
     * @param aValue long; the 64 bit integer (long) to be written to the byte buffer
     */
    public void write(long aValue)
    {
        if (SIZE_OF_INT64 > getwriteAvailable())
            setLength(this.fWriteCursor + SIZE_OF_INT64);
        this.fBuffer[this.fWriteCursor] = (byte) (aValue & 0xFF);
        this.fBuffer[this.fWriteCursor + 1] = (byte) ((aValue >> 8) & 0xFF);
        this.fBuffer[this.fWriteCursor + 2] = (byte) ((aValue >> 16) & 0xFF);
        this.fBuffer[this.fWriteCursor + 3] = (byte) ((aValue >> 24) & 0xFF);
        this.fBuffer[this.fWriteCursor + 4] = (byte) ((aValue >> 32) & 0xFF);
        this.fBuffer[this.fWriteCursor + 5] = (byte) ((aValue >> 40) & 0xFF);
        this.fBuffer[this.fWriteCursor + 6] = (byte) ((aValue >> 48) & 0xFF);
        this.fBuffer[this.fWriteCursor + 7] = (byte) ((aValue >> 56) & 0xFF);
        this.fWriteCursor += SIZE_OF_INT64;
    }

    /**
     * Write the specified single (float) to the byte buffer. Buffer space is allocated if needed.
     * @param aValue float; the single (float) to be written to the byte buffer
     */
    public void write(float aValue)
    {
        write(Float.floatToIntBits(aValue));
    }

    /**
     * Write the specified double to the byte buffer. Buffer space is allocated if needed.
     * @param aValue double; the double to be written to the byte buffer
     */
    public void write(double aValue)
    {
        write(Double.doubleToLongBits(aValue));
    }

    /**
     * Write the specified string to the byte buffer in UTF-8 format. Buffer space is allocated if needed.
     * @param aValue String; the string to be written to the byte buffer
     */
    public void write(String aValue)
    {
        byte[] s = aValue.getBytes(Charset.forName("UTF-8"));
        int len = s.length;
        if (SIZE_OF_INT32 + len > getwriteAvailable())
            setLength(this.fWriteCursor + SIZE_OF_INT32 + len);
        // first write size
        write(len);
        // write content
        for (int i = 0; i < len; i++)
            this.fBuffer[this.fWriteCursor + i] = s[i];
        this.fWriteCursor += len;
    }

    /**
     * Write the specified byte array WITHOUT the size to the byte buffer. Buffer space is allocated if needed.
     * @param aValue byte[]; the byte buffer to be written to the byte buffer
     */
    public void write(byte[] aValue)
    {
        if (aValue.length > getwriteAvailable())
            setLength(this.fWriteCursor + aValue.length);
        for (int i = 0; i < aValue.length; i++)
            this.fBuffer[this.fWriteCursor + i] = aValue[i];
        this.fWriteCursor += aValue.length;
    }

    // write all readable data WITH size
    /**
     * Write the readable data in the specified byte buffer to this byte buffer. Buffer space is allocated if needed.
     * @param aValue TByteBuffer; the byte buffer who's readable data is to be written to this byte buffer
     */
    public void write(TByteBuffer aValue)
    {
        write(aValue.fBuffer.length);
        write(aValue.fBuffer);
    }

    // QWrite (no room checking)

    /**
     * write a boolean to the buffer; the QWrite methods do not check for room in the buffer
     * @param aValue boolean; the boolean value to be written to the buffer
     */
    public void qWrite(boolean aValue)
    {

        this.fBuffer[this.fWriteCursor] = (aValue) ? (byte) -1 : (byte) 0;
        this.fWriteCursor += SIZE_OF_BOOLEAN;
    }

    /**
     * write a single byte to the buffer; the QWrite methods do not check for room in the buffer
     * @param aValue byte; the byte value to be written to the buffer
     */
    public void qWrite(byte aValue)
    {
        this.fBuffer[this.fWriteCursor] = aValue;
        this.fWriteCursor += SIZE_OF_BYTE;
    }

    /**
     * write a single integer (32 bit) to the buffer; the QWrite methods do not check for room in the buffer
     * @param aValue int; the integer (32 bit) value to be written to the buffer
     */
    public void qWrite(int aValue)
    {
        this.fBuffer[this.fWriteCursor] = (byte) (aValue & 0xFF);
        this.fBuffer[this.fWriteCursor + 1] = (byte) ((aValue >> 8) & 0xFF);
        this.fBuffer[this.fWriteCursor + 2] = (byte) ((aValue >> 16) & 0xFF);
        this.fBuffer[this.fWriteCursor + 3] = (byte) ((aValue >> 24) & 0xFF);
        this.fWriteCursor += SIZE_OF_INT32;
    }

    /**
     * write a single integer (64 bit) to the buffer; the QWrite methods do not check for room in the buffer
     * @param aValue long; the integer (64 bit) value to be written to the buffer
     */
    public void qWrite(long aValue)
    {
        this.fBuffer[this.fWriteCursor] = (byte) (aValue & 0xFF);
        this.fBuffer[this.fWriteCursor + 1] = (byte) ((aValue >> 8) & 0xFF);
        this.fBuffer[this.fWriteCursor + 2] = (byte) ((aValue >> 16) & 0xFF);
        this.fBuffer[this.fWriteCursor + 3] = (byte) ((aValue >> 24) & 0xFF);
        this.fBuffer[this.fWriteCursor + 4] = (byte) ((aValue >> 32) & 0xFF);
        this.fBuffer[this.fWriteCursor + 5] = (byte) ((aValue >> 40) & 0xFF);
        this.fBuffer[this.fWriteCursor + 6] = (byte) ((aValue >> 48) & 0xFF);
        this.fBuffer[this.fWriteCursor + 7] = (byte) ((aValue >> 56) & 0xFF);
        this.fWriteCursor += SIZE_OF_INT64;
    }

    /**
     * write a single float (32 bit) to the buffer; the QWrite methods do not check for room in the buffer
     * @param aValue float; the float (32 bit) value to be written to the buffer
     */
    public void qWrite(float aValue)
    {
        qWrite(Float.floatToIntBits(aValue));
    }

    /**
     * write a single double (64 bit) to the buffer; the QWrite methods do not check for room in the buffer
     * @param aValue double; the float (64 bit) value to be written to the buffer
     */
    public void qWrite(double aValue)
    {
        qWrite(Double.doubleToLongBits(aValue));
    }

    /**
     * Write a string to the buffer, prefixed with the size as a 32 bit integer. The characters are UTF-8 encoded, every char is
     * 1 byte in size The QWrite methods do not check for room in the buffer
     * @param aValue String; the float (32 bit) value to be written to the buffer
     */
    public void qWrite(String aValue)
    {
        byte[] s = aValue.getBytes(Charset.forName("UTF-8"));
        int len = s.length;
        // first write size
        qWrite(len);
        // write content
        for (int i = 0; i < len; i++)
            this.fBuffer[this.fWriteCursor + i] = s[i];
        this.fWriteCursor += len;
    }

    /**
     * write array of byte WITHOUT size; the QWrite methods do not check for room in the buffer
     * @param aValue byte[]; the byte array written to the buffer (without prefixed size)
     */
    public void qWrite(byte[] aValue)
    {
        for (int i = 0; i < aValue.length; i++)
            this.fBuffer[this.fWriteCursor + i] = aValue[i];
        this.fWriteCursor += aValue.length;
    }

    /**
     * write, with no checking, all readable data from the given byte buffer to this prefixed WITH size
     * @param aValue TByteBuffer; readable data in byte buffer to be written to the buffer
     */
    public void qWrite(TByteBuffer aValue)
    {
        qWrite(aValue.fBuffer.length);
        qWrite(aValue.fBuffer);
    }

    /**
     * signal number of bytes directly written to buffer without using class methods update write cursor and return if it fitted
     * into buffer (should trigger exception ?)
     * @param aValueSize int; number of bytes directly written into buffer
     * @return true if all written data fitted into buffer
     */
    public boolean written(int aValueSize)
    {
        this.fWriteCursor += aValueSize;
        return getwriteAvailable() >= 0;
    }

    /**
     * apply written data (trim extra buffer space)
     */
    public void writeApply()
    {
        if (this.fWriteCursor != this.fBuffer.length)
            setLength(this.fWriteCursor);
    }
}
