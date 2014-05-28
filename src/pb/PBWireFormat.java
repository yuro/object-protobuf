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

/**
 * This class is used internally by the Protocol Buffer library and generated
 * message implementations.  It is public only because those generated messages
 * do not reside in the {@code protobuf} package.  Others should not use this
 * class directly.
 * <p/>
 * This class contains constants and helper functions useful for dealing with
 * the Protocol Buffer wire format.
 *
 * @author kenton@google.com Kenton Varda
 */
public final class PBWireFormat {
    public static final int WIRETYPE_VARINT = 0;
    public static final int WIRETYPE_FIXED64 = 1;
    public static final int WIRETYPE_LENGTH_DELIMITED = 2;
    public static final int WIRETYPE_START_GROUP = 3;
    public static final int WIRETYPE_END_GROUP = 4;
    public static final int WIRETYPE_FIXED32 = 5;
    static final int TAG_TYPE_BITS = 3;
    static final int TAG_TYPE_MASK = (1 << TAG_TYPE_BITS) - 1;

    // Do not allow instantiation.
    private PBWireFormat() {
    }

    /**
     * Given a tag tag, determines the wire type (the lower 3 bits).
     */
    static int getTagWireType(final int tag) {
        return tag & TAG_TYPE_MASK;
    }

    /**
     * Given a tag tag, determines the field number (the upper 29 bits).
     */
    public static int getTagFieldNumber(final int tag) {
        return tag >>> TAG_TYPE_BITS;
    }

    /**
     * Makes a tag tag given a field number and wire type.
     */
    static int makeTag(final int fieldNumber, final int wireType) {
        return (fieldNumber << TAG_TYPE_BITS) | wireType;
    }

    /**
     * This is only here to support the lite runtime and should not be used by users.
     */
    public enum FieldType {
        DOUBLE(WIRETYPE_FIXED64),
        FLOAT(WIRETYPE_FIXED32),
        INT64(WIRETYPE_VARINT),
        UINT64(WIRETYPE_VARINT),
        INT32(WIRETYPE_VARINT),
        FIXED64(WIRETYPE_FIXED64),
        FIXED32(WIRETYPE_FIXED32),
        BOOL(WIRETYPE_VARINT),
        STRING(WIRETYPE_LENGTH_DELIMITED),
        GROUP(WIRETYPE_START_GROUP),
        MESSAGE(WIRETYPE_LENGTH_DELIMITED),
        BYTES(WIRETYPE_LENGTH_DELIMITED),
        UINT32(WIRETYPE_VARINT),
        ENUM(WIRETYPE_VARINT),
        SFIXED32(WIRETYPE_FIXED32),
        SFIXED64(WIRETYPE_FIXED64),
        SINT32(WIRETYPE_VARINT),
        SINT64(WIRETYPE_VARINT),
        UNKNOWN(WIRETYPE_VARINT);
        private final int wireType;

        FieldType(final int wireType) {
            this.wireType = wireType;
        }

        public int getWireType() {
            return wireType;
        }
    }

    public enum FieldLabel {
        OPTIONAL,    // optional
        REQUIRED,    // required
        REPEATED,    // repeated
        // indexed by Label.
    }
}
