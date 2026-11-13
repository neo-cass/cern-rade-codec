package cern.rade.services.codec.labview;

import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.labview.LVType;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.LongFunction;

/**
 * The LabVIEW numeric types, and their conversion from and to JAPC numbers. This is the only place where numbers are
 * converted.
 * <p>
 * JAPC has no unsigned types: when decoding, an unsigned LV integer becomes the next larger signed JAPC type (U8 to
 * short, U16 to int, U32 to long), U64 becomes a long (values above {@link Long#MAX_VALUE} are rejected).
 * When encoding, any JAPC number can be written as any LV type of the same family (integer or floating point) if its
 * value is in the range of that type.
 */
enum LVNumber {
    INT_8(LVType.INT_8, 1, false, byte.class, v -> (byte) v),
    INT_16(LVType.INT_16, 2, false, short.class, v -> (short) v),
    INT_32(LVType.INT_32, 4, false, int.class, v -> (int) v),
    INT_64(LVType.INT_64, 8, false, long.class, v -> v),
    UINT_8(LVType.UINT_8, 1, true, short.class, v -> (short) v),
    UINT_16(LVType.UINT_16, 2, true, int.class, v -> (int) v),
    UINT_32(LVType.UINT_32, 4, true, long.class, v -> v),
    UINT_64(LVType.UINT_64, 8, true, long.class, v -> v),
    SGL(LVType.SGL, 4, false, float.class, null),
    DBL(LVType.DBL, 8, false, double.class, null);

    private final LVType lvType;
    private final int bytes;
    private final boolean unsigned;
    private final Class<?> javaType;
    /** Boxes an integer into the JAPC type, null for the floating point types. */
    private final LongFunction<Object> integerBoxer;

    LVNumber(LVType lvType, int bytes, boolean unsigned, Class<?> javaType, LongFunction<Object> integerBoxer) {
        this.lvType = lvType;
        this.bytes = bytes;
        this.unsigned = unsigned;
        this.javaType = javaType;
        this.integerBoxer = integerBoxer;
    }

    /**
     * @return the numeric type for the LV type, empty if the LV type is not numeric (or not supported).
     */
    static Optional<LVNumber> of(LVType lvType) {
        for (LVNumber number : values()) {
            if (number.lvType == lvType) {
                return Optional.of(number);
            }
        }
        return Optional.empty();
    }

    /**
     * @return the primitive type of the JAPC values (and JAPC arrays) of this type.
     */
    Class<?> javaType() {
        return javaType;
    }

    /**
     * @return the JAPC (boxed) value of the next number in the buffer.
     */
    Object read(ByteBuffer buffer) throws CodecException {
        if (isFloatingPoint()) {
            return bytes == 4 ? (Object) buffer.getFloat() : (Object) buffer.getDouble();
        }
        long value = switch (bytes) {
            case 1 -> buffer.get();
            case 2 -> buffer.getShort();
            case 4 -> buffer.getInt();
            default -> buffer.getLong();
        };
        if (unsigned) {
            if (bytes == 8 && value < 0) {
                throw new CodecException("LabVIEW " + lvType + " value " + Long.toUnsignedString(value) + " is above the maximum JAPC long value " + Long.MAX_VALUE);
            }
            if (bytes < 8) {
                value &= (1L << (8 * bytes)) - 1;
            }
        }
        return integerBoxer.apply(value);
    }

    /**
     * Writes the JAPC number as this type.
     *
     * @throws CodecException if the value is out of the range of this type, or is not an integer for an integer type.
     */
    void write(Number number, DataOutputStream out) throws IOException, CodecException {
        if (isFloatingPoint()) {
            writeFloatingPoint(number.doubleValue(), out);
            return;
        }
        if (number instanceof Float || number instanceof Double) {
            throw new CodecException("Cannot convert the floating point value " + number + " to the LabVIEW integer type " + lvType);
        }
        long value = number.longValue();
        if (value < min() || value > max()) {
            throw new CodecException("Value " + number + " is out of the range of the LabVIEW type " + lvType + " [" + min() + ", " + max() + "]");
        }
        switch (bytes) {
            case 1 -> out.writeByte((int) value);
            case 2 -> out.writeShort((int) value);
            case 4 -> out.writeInt((int) value);
            default -> out.writeLong(value);
        }
    }

    private void writeFloatingPoint(double value, DataOutputStream out) throws IOException, CodecException {
        if (bytes == 8) {
            out.writeDouble(value);
            return;
        }
        float single = (float) value;
        if (Float.isInfinite(single) && !Double.isInfinite(value)) {
            throw new CodecException("Value " + value + " is out of the range of the LabVIEW type " + lvType);
        }
        out.writeFloat(single);
    }

    /** The smallest value of this integer type. */
    private long min() {
        return unsigned ? 0 : -(1L << (8 * bytes - 1));
    }

    /** The largest value of this integer type which a JAPC value can hold (a JAPC long is signed). */
    private long max() {
        if (bytes == 8) {
            return Long.MAX_VALUE;
        }
        return unsigned ? (1L << (8 * bytes)) - 1 : (1L << (8 * bytes - 1)) - 1;
    }

    private boolean isFloatingPoint() {
        return integerBoxer == null;
    }
}
