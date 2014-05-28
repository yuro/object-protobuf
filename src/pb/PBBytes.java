// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
// http://code.google.com/p/protobuf/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//     * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package pb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Immutable array of bytes.
 *
 * @author crazybob@google.com Bob Lee
 * @author kenton@google.com Kenton Varda
 */
public final class PBBytes {
    /**
     * Empty PBBytes.
     */
    public static final PBBytes EMPTY = new PBBytes(new byte[0]);
    private final byte[] bytes;
    private volatile int hash = 0;

    private PBBytes(final byte[] bytes) {
        this.bytes = bytes == null ? new byte[0] : bytes;
    }

    /**
     * Copies the given bytes into a {@code PBBytes}.
     */
    public static PBBytes copyFrom(final byte[] bytes, final int offset,
                                   final int size) {
        final byte[] copy = new byte[size];
        System.arraycopy(bytes, offset, copy, 0, size);
        return new PBBytes(copy);
    }

    // =================================================================
    // byte[] -> PBBytes

    public static PBBytes copyFrom(final InputStream inputStream) {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        int ch;
        try {
            while ((ch = inputStream.read()) != -1) {
                bytestream.write(ch);
            }
            return new PBBytes(bytestream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Copies the given bytes into a {@code PBBytes}.
     */
    public static PBBytes copyFrom(final byte[] bytes) {
        return copyFrom(bytes, 0, bytes.length);
    }

    /**
     * Copies {@code size} bytes from a {@code java.nio.ByteBuffer} into
     * a {@code PBBytes}.
     */
    public static PBBytes copyFrom(final ByteBuffer bytes, final int size) {
        final byte[] copy = new byte[size];
        bytes.get(copy);
        return new PBBytes(copy);
    }

    /**
     * Copies the remaining bytes from a {@code java.nio.ByteBuffer} into
     * a {@code PBBytes}.
     */
    public static PBBytes copyFrom(final ByteBuffer bytes) {
        return copyFrom(bytes, bytes.remaining());
    }

    /**
     * Encodes {@code text} into a sequence of bytes using the named charset
     * and returns the result as a {@code PBBytes}.
     */
    public static PBBytes copyFrom(final String text, final String charsetName)
            throws UnsupportedEncodingException {
        return new PBBytes(text.getBytes(charsetName));
    }

    /**
     * Encodes {@code text} into a sequence of UTF-8 bytes and returns the
     * result as a {@code PBBytes}.
     */
    public static PBBytes copyFromUtf8(final String text) {
        try {
            return new PBBytes(text.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported?", e);
        }
    }

    /**
     * Concatenates all byte strings in the list and returns the result.
     * <p/>
     * <p>The returned {@code PBBytes} is not necessarily a unique object.
     * If the list is empty, the returned object is the singleton empty
     * {@code PBBytes}.  If the list has only one element, that
     * {@code PBBytes} will be returned without copying.
     */
    public static PBBytes copyFrom(List<PBBytes> list) {
        if (list.size() == 0) {
            return EMPTY;
        } else if (list.size() == 1) {
            return list.get(0);
        }

        int size = 0;
        for (PBBytes str : list) {
            size += str.size();
        }
        byte[] bytes = new byte[size];
        int pos = 0;
        for (PBBytes str : list) {
            System.arraycopy(str.bytes, 0, bytes, pos, str.size());
            pos += str.size();
        }
        return new PBBytes(bytes);
    }

    /**
     * Creates a new {@link Output} with the given initial capacity.
     */
    public static Output newOutput(final int initialCapacity) {
        return new Output(new ByteArrayOutputStream(initialCapacity));
    }

    /**
     * Creates a new {@link Output}.
     */
    public static Output newOutput() {
        return newOutput(32);
    }

    // =================================================================
    // PBBytes -> byte[]

    /**
     * Constructs a new PBBytes builder, which allows you to efficiently
     * construct a {@code PBBytes} by writing to a {@link PBCodedOutputStream}.
     * Using this is much more efficient than calling {@code newOutput()} and
     * wrapping that in a {@code CodedOutputStream}.
     * <p/>
     * <p>This is package-private because it's a somewhat confusing interface.
     * Users can call {@link PBMessage#toByteString()} instead of calling this
     * directly.
     *
     * @param size The target byte size of the {@code PBBytes}.  You must
     *             write exactly this many bytes before building the result.
     */
    static CodedBuilder newCodedBuilder(final int size) {
        return new CodedBuilder(size);
    }

    /**
     * Gets the byte at the given index.
     *
     * @throws ArrayIndexOutOfBoundsException {@code index} is < 0 or >= size
     */
    public byte byteAt(final int index) {
        return bytes[index];
    }

    /**
     * Gets the number of bytes.
     */
    public int size() {
        return bytes.length;
    }

    /**
     * Returns {@code true} if the size is {@code 0}, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return bytes.length == 0;
    }

    /**
     * Copies bytes into a buffer at the given offset.
     *
     * @param target buffer to copy into
     * @param offset in the target buffer
     */
    public void copyTo(final byte[] target, final int offset) {
        System.arraycopy(bytes, 0, target, offset, bytes.length);
    }

    /**
     * Copies bytes into a buffer.
     *
     * @param target       buffer to copy into
     * @param sourceOffset offset within these bytes
     * @param targetOffset offset within the target buffer
     * @param size         number of bytes to copy
     */
    public void copyTo(final byte[] target, final int sourceOffset,
                       final int targetOffset,
                       final int size) {
        System.arraycopy(bytes, sourceOffset, target, targetOffset, size);
    }

    // =================================================================
    // equals() and hashCode()

    /**
     * Copies bytes to a {@code byte[]}.
     */
    public byte[] toByteArray() {
        final int size = bytes.length;
        final byte[] copy = new byte[size];
        System.arraycopy(bytes, 0, copy, 0, size);
        return copy;
    }

    /**
     * Constructs a new read-only {@code java.nio.ByteBuffer} with the
     * same backing byte array.
     */
    public ByteBuffer asReadOnlyByteBuffer() {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.asReadOnlyBuffer();
    }

    /**
     * Constructs a new {@code String} by decoding the bytes using the
     * specified charset.
     */
    public String toString(final String charsetName)
            throws UnsupportedEncodingException {
        return new String(bytes, charsetName);
    }

    // =================================================================
    // Input stream

    /**
     * Constructs a new {@code String} by decoding the bytes as UTF-8.
     */
    public String toStringUtf8() {
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported?", e);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof PBBytes)) {
            return false;
        }

        final PBBytes other = (PBBytes) o;
        final int size = bytes.length;
        if (size != other.bytes.length) {
            return false;
        }

        final byte[] thisBytes = bytes;
        final byte[] otherBytes = other.bytes;
        for (int i = 0; i < size; i++) {
            if (thisBytes[i] != otherBytes[i]) {
                return false;
            }
        }

        return true;
    }

    // =================================================================
    // Output stream

    @Override
    public int hashCode() {
        int h = hash;

        if (h == 0) {
            final byte[] thisBytes = bytes;
            final int size = bytes.length;

            h = size;
            for (int i = 0; i < size; i++) {
                h = h * 31 + thisBytes[i];
            }
            if (h == 0) {
                h = 1;
            }

            hash = h;
        }

        return h;
    }

    /**
     * Creates an {@code InputStream} which can be used to read the bytes.
     */
    public InputStream newInput() {
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Creates a {@link PBCodedInputStream} which can be used to read the bytes.
     * Using this is more efficient than creating a {@link PBCodedInputStream}
     * wrapping the result of {@link #newInput()}.
     */
    public PBCodedInputStream newCodedInput() {
        // We trust CodedInputStream not to modify the bytes, or to give anyone
        // else access to them.
        return PBCodedInputStream.newInstance(bytes);
    }

    /**
     * Outputs to a {@code PBBytes} instance. Call {@link #toByteString()} to
     * create the {@code PBBytes} instance.
     */
    public static final class Output extends FilterOutputStream {
        private final ByteArrayOutputStream bout;

        /**
         * Constructs a new output with the given initial capacity.
         */
        private Output(final ByteArrayOutputStream bout) {
            super(bout);
            this.bout = bout;
        }

        /**
         * Creates a {@code PBBytes} instance from this {@code Output}.
         */
        public PBBytes toByteString() {
            final byte[] byteArray = bout.toByteArray();
            return new PBBytes(byteArray);
        }
    }

    /**
     * See {@link PBBytes#newCodedBuilder(int)}.
     */
    static final class CodedBuilder {
        private final PBCodedOutputStream output;
        private final byte[] buffer;

        private CodedBuilder(final int size) {
            buffer = new byte[size];
            output = PBCodedOutputStream.newInstance(buffer);
        }

        public PBBytes build() {
            output.checkNoSpaceLeft();

            // We can be confident that the CodedOutputStream will not modify the
            // underlying bytes anymore because it already wrote all of them.  So,
            // no need to make a copy.
            return new PBBytes(buffer);
        }

        public PBCodedOutputStream getCodedOutput() {
            return output;
        }
    }
}
