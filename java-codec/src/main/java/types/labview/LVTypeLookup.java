package types.labview;
import cern.cmw.data.DataType;

import java.util.HashMap;
import java.util.Map;
import static java.util.Map.entry;
public final class LVTypeLookup {
    private static final HashMap<Byte, LVType> lookupByCodeMap = new HashMap<Byte, LVType>( Map.ofEntries(
            entry((byte)0x01, LVType.INT_8),
            entry((byte)0x02, LVType.INT_16),
            entry((byte)0x03, LVType.INT_32),
            entry((byte)0x04, LVType.INT_64),
            entry((byte)0x05, LVType.UINT_8),
            entry((byte)0x06, LVType.UINT_16),
            entry((byte)0x07, LVType.UINT_32),
            entry((byte)0x08, LVType.UINT_64),
            entry((byte)0x09, LVType.SGL),
            entry((byte)0x0A, LVType.DBL),
            entry((byte)0x0B, LVType.EXT),
            entry((byte)0x0C, LVType.SGL_CPX),
            entry((byte)0x0D, LVType.DBL_CPX),
            entry((byte)0x0E, LVType.EXT_CPX),
            entry((byte)0x15, LVType.ENUM_8),
            entry((byte)0x16, LVType.ENUM_16),
            entry((byte)0x17, LVType.ENUM_32),
            entry((byte)0x19, LVType.SGL_PHS),
            entry((byte)0x1A, LVType.DBL_PHS),
            entry((byte)0x1B, LVType.EXT_PHS),
            entry((byte)0x1C, LVType.SGL_PHS_CPX),
            entry((byte)0x1D, LVType.DBL_PHS_CPX),
            entry((byte)0x1E, LVType.EXT_PHS_CPX),
            entry((byte)0x21, LVType.BOOL),
            entry((byte)0x30, LVType.STRING),
            entry((byte)0x32, LVType.PATH),
            entry((byte)0x33, LVType.PICTURE),
            entry((byte)0x40, LVType.ARRAY),
            entry((byte)0x50, LVType.CLUSTER),
            entry((byte)0x54, LVType.WAVE_FORM),
            entry((byte)0x70, LVType.REFNUM),
            entry((byte)0x53, LVType.VARIANT),
            entry((byte)-1, LVType.UNKNOWN)
    ));
    private static final HashMap<LVType, Byte> lookupByTypeMap = new HashMap<LVType, Byte>( Map.ofEntries(
            entry(LVType.INT_8, (byte)0x01),
            entry(LVType.INT_16, (byte)0x02),
            entry(LVType.INT_32, (byte)0x03),
            entry(LVType.INT_64, (byte)0x04),
            entry(LVType.UINT_8, (byte)0x05),
            entry(LVType.UINT_16, (byte)0x06),
            entry(LVType.UINT_32, (byte)0x07),
            entry(LVType.UINT_64, (byte)0x08),
            entry(LVType.SGL, (byte)0x09),
            entry(LVType.DBL, (byte)0x0A),
            entry(LVType.EXT, (byte)0x0B),
            entry(LVType.SGL_CPX, (byte)0x0C),
            entry(LVType.DBL_CPX, (byte)0x0D),
            entry(LVType.EXT_CPX, (byte)0x0E),
            entry(LVType.ENUM_8, (byte)0x15),
            entry(LVType.ENUM_16, (byte)0x16),
            entry(LVType.ENUM_32, (byte)0x17),
            entry(LVType.SGL_PHS, (byte)0x19),
            entry(LVType.DBL_PHS, (byte)0x1A),
            entry(LVType.EXT_PHS, (byte)0x1B),
            entry(LVType.SGL_PHS_CPX, (byte)0x1C),
            entry(LVType.DBL_PHS_CPX, (byte)0x1D),
            entry(LVType.EXT_PHS_CPX, (byte)0x1E),
            entry(LVType.BOOL, (byte)0x21),
            entry(LVType.STRING, (byte)0x30),
            entry(LVType.PATH, (byte)0x32),
            entry(LVType.PICTURE, (byte)0x33),
            entry(LVType.ARRAY, (byte)0x40),
            entry(LVType.CLUSTER, (byte)0x50),
            entry(LVType.WAVE_FORM, (byte)0x54),
            entry(LVType.REFNUM, (byte)0x70),
            entry(LVType.VARIANT, (byte)0x53),
            entry(LVType.UNKNOWN, (byte)-1)

    ));

    private static final HashMap<DataType, Byte> lookupUpByCMWDataType = new HashMap<DataType, Byte>( Map.ofEntries(
            entry(DataType.DT_BYTE, (byte)0x01),
            entry(DataType.DT_BOOL, (byte)0x21),
            entry(DataType.DT_SHORT, (byte)0x02),
            entry(DataType.DT_INT, (byte)0x03),
            entry(DataType.DT_LONG, (byte)0x04),
            entry(DataType.DT_FLOAT, (byte)0x09),
            entry(DataType.DT_DOUBLE, (byte)0x0A),
            entry(DataType.DT_STRING, (byte)0x30),
            entry(DataType.DT_BYTE_ARRAY, (byte)0x40),
            entry(DataType.DT_BOOL_ARRAY, (byte)0x40),
            entry(DataType.DT_SHORT_ARRAY, (byte)0x40),
            entry(DataType.DT_INT_ARRAY, (byte)0x40),
            entry(DataType.DT_LONG_ARRAY, (byte)0x40),
            entry(DataType.DT_FLOAT_ARRAY, (byte)0x40),
            entry(DataType.DT_DOUBLE_ARRAY, (byte)0x40),
            entry(DataType.DT_STRING_ARRAY, (byte)0x40),
            entry(DataType.DT_DATA, (byte)0x50)

    ));
    private LVTypeLookup() {

    }
    public static LVType getType(byte typeCode){
        return lookupByCodeMap.get(typeCode);
    }
    public static byte getType(LVType typeDefinition){
        return lookupByTypeMap.get(typeDefinition);
    }

}
