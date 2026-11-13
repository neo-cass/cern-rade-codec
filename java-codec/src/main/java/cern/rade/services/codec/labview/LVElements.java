package cern.rade.services.codec.labview;

import cern.japc.value.DiscreteFunction;
import cern.japc.value.EnumItem;
import cern.japc.value.EnumItemSet;
import cern.japc.value.EnumType;
import cern.japc.value.SimpleDescriptor;
import cern.japc.value.factory.DomainValueFactory;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.labview.LVEnumDescriptor;
import cern.rade.services.common.containers.labview.LVEnumSetDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Creates the {@link LVElement} matching a LabVIEW type descriptor. It is the extension point of the data codec: a
 * new LV type is supported by adding it here (and in the type descriptor codec).
 * <p>
 * Flattened formats (big endian): numbers see {@link LVNumber}; BOOL one byte (1 = true); STRING an i32 length then the
 * bytes; enum the item index as an unsigned 8, 16 or 32 bit integer (ENUM_8, ENUM_16, ENUM_32); enum set one byte per
 * item; discrete function the data of a 2D array of DBL: i32 2, i32 N, the N X values then the N Y values.
 */
final class LVElements {
    private static final LVElement BOOL = new BoolElement();
    private static final LVElement STRING = new StringElement();
    private static final LVElement DISCRETE_FUNCTION = new DiscreteFunctionElement();

    private LVElements() {
    }

    /**
     * @return the element to encode a JAPC value as the given LabVIEW type.
     */
    static LVElement forEncoding(LVTypeDescriptor desc) throws CodecException {
        return create(desc, null);
    }

    /**
     * @param desc      the LabVIEW type of the element
     * @param field     the JAPC descriptor of the field holding the element, only consulted for enums and enum sets, may be null
     * @param fieldName the LV label of the field, for the error messages
     * @return the element to decode LabVIEW data as a JAPC value.
     * @throws CodecException if the LV type is not supported, or an enum type is needed and the descriptor has none.
     */
    static LVElement forDecoding(LVTypeDescriptor desc, SimpleDescriptor field, String fieldName) throws CodecException {
        boolean needsEnumType = LVEnumDescriptor.isEnum(desc.getType()) || desc.getType() == LVType.ENUM_SET;
        return create(desc, needsEnumType ? EnumTypes.require(field, fieldName) : null);
    }

    /**
     * @param enumType the JAPC enum type, needed to decode enums and enum sets only
     */
    private static LVElement create(LVTypeDescriptor desc, EnumType enumType) throws CodecException {
        LVType type = desc.getType();
        return switch (type) {
            case BOOL -> BOOL;
            case STRING -> STRING;
            case ENUM_8, ENUM_16, ENUM_32 -> new EnumElement((LVEnumDescriptor) desc, enumType);
            case ENUM_SET -> new EnumSetElement((LVEnumSetDescriptor) desc, enumType);
            case DISCRETE_FUNCTION -> DISCRETE_FUNCTION;
            default -> new NumberElement(LVNumber.of(type)
                    .orElseThrow(() -> new CodecException("LabVIEW type " + type + " is not supported in data (field '" + desc.getName() + "')")));
        };
    }

    /**
     * An element whose JAPC counterpart has a known class, checked when writing.
     */
    private abstract static class TypedElement<T> implements LVElement {
        private final Class<T> valueClass;
        private final Class<?> javaType;
        private final String lvDescription;

        TypedElement(Class<T> valueClass, Class<?> javaType, String lvDescription) {
            this.valueClass = valueClass;
            this.javaType = javaType;
            this.lvDescription = lvDescription;
        }

        @Override
        public final Class<?> javaType() {
            return javaType;
        }

        @Override
        public final void write(Object value, DataOutputStream out) throws IOException, CodecException {
            if (!valueClass.isInstance(value)) {
                throw new CodecException("Cannot encode a JAPC " + (value == null ? "null" : value.getClass().getSimpleName()) + " as " + lvDescription);
            }
            writeValue(valueClass.cast(value), out);
        }

        abstract void writeValue(T value, DataOutputStream out) throws IOException, CodecException;
    }

    private static final class NumberElement extends TypedElement<Number> {
        private final LVNumber number;

        NumberElement(LVNumber number) {
            super(Number.class, number.javaType(), "a LabVIEW " + number);
            this.number = number;
        }

        @Override
        public Object read(ByteBuffer buffer) throws CodecException {
            return number.read(buffer);
        }

        @Override
        void writeValue(Number value, DataOutputStream out) throws IOException, CodecException {
            number.write(value, out);
        }
    }

    private static final class BoolElement extends TypedElement<Boolean> {
        BoolElement() {
            super(Boolean.class, boolean.class, "a LabVIEW BOOL");
        }

        @Override
        public Object read(ByteBuffer buffer) {
            return buffer.get() == 1;
        }

        @Override
        void writeValue(Boolean value, DataOutputStream out) throws IOException {
            out.writeByte(value ? 1 : 0);
        }
    }

    private static final class StringElement extends TypedElement<String> {
        StringElement() {
            super(String.class, String.class, "a LabVIEW STRING");
        }

        @Override
        public Object read(ByteBuffer buffer) throws CodecException {
            int length = buffer.getInt();
            if (length < 0 || length > buffer.remaining()) {
                throw new CodecException("LabVIEW string length " + length + " exceeds the available data");
            }
            byte[] raw = new byte[length];
            buffer.get(raw);
            return new String(raw);
        }

        @Override
        void writeValue(String value, DataOutputStream out) throws IOException {
            byte[] raw = value.getBytes();
            out.writeInt(raw.length);
            out.write(raw);
        }
    }

    /**
     * A JAPC enum item is mapped to the LV enum item with the same symbol; the flattened value is the position of that
     * item in the LV enum.
     */
    private static final class EnumElement extends TypedElement<EnumItem> {
        private final LVEnumDescriptor desc;
        private final LVNumber index;
        private final Map<String, EnumItem> japcItems;

        /**
         * @param enumType the JAPC enum type, null if the element is only used for encoding
         */
        EnumElement(LVEnumDescriptor desc, EnumType enumType) {
            super(EnumItem.class, EnumItem.class, "the LabVIEW enum '" + desc.getName() + "'");
            this.desc = desc;
            this.index = switch (desc.getType()) {
                case ENUM_8 -> LVNumber.UINT_8;
                case ENUM_16 -> LVNumber.UINT_16;
                default -> LVNumber.UINT_32;
            };
            this.japcItems = enumType == null ? null : EnumTypes.itemsBySymbol(enumType);
        }

        @Override
        public Object read(ByteBuffer buffer) throws CodecException {
            long position = ((Number) index.read(buffer)).longValue();
            if (position >= desc.size()) {
                throw new CodecException("LabVIEW enum '" + desc.getName() + "' value " + position + " is out of range (" + desc.size() + " items)");
            }
            String symbol = desc.getItem((int) position);
            EnumItem item = japcItems.get(symbol);
            if (item == null) {
                throw new CodecException("LabVIEW enum '" + desc.getName() + "' item '" + symbol + "' does not exist in the JAPC enum");
            }
            return item;
        }

        @Override
        void writeValue(EnumItem item, DataOutputStream out) throws IOException, CodecException {
            int position = desc.indexOf(item.getSymbol());
            if (position < 0) {
                throw new CodecException("Enum item '" + item.getSymbol() + "' is not part of the LabVIEW enum '" + desc.getName() + "'");
            }
            index.write(position, out);
        }
    }

    /**
     * A JAPC enum item set is a cluster of booleans labelled with the symbols of the items: true if the set contains
     * the item. When decoding, the cluster may hold a subset of the items of the JAPC enum type (the missing ones are
     * not in the set) but all its labels must be items of the JAPC enum type.
     */
    private static final class EnumSetElement extends TypedElement<EnumItemSet> {
        private final LVEnumSetDescriptor desc;
        private final EnumType enumType;
        /** the JAPC item of each LV item, null if the element is only used for encoding */
        private final EnumItem[] japcItems;

        EnumSetElement(LVEnumSetDescriptor desc, EnumType enumType) throws CodecException {
            super(EnumItemSet.class, EnumItemSet.class, "the LabVIEW enum set '" + desc.getName() + "'");
            this.desc = desc;
            this.enumType = enumType;
            this.japcItems = enumType == null ? null : resolveItems(desc, enumType);
        }

        private static EnumItem[] resolveItems(LVEnumSetDescriptor desc, EnumType enumType) throws CodecException {
            Map<String, EnumItem> bySymbol = EnumTypes.itemsBySymbol(enumType);
            EnumItem[] items = new EnumItem[desc.size()];
            for (int i = 0; i < items.length; i++) {
                items[i] = bySymbol.get(desc.getItem(i));
                if (items[i] == null) {
                    throw new CodecException("LabVIEW enum set '" + desc.getName() + "' item '" + desc.getItem(i) + "' does not exist in the JAPC enum " + enumType.getName());
                }
            }
            return items;
        }

        @Override
        public Object read(ByteBuffer buffer) {
            List<EnumItem> members = new ArrayList<>();
            for (EnumItem item : japcItems) {
                if (buffer.get() == 1) {
                    members.add(item);
                }
            }
            return DomainValueFactory.newEnumItemSet(enumType, members.toArray(EnumItem[]::new));
        }

        @Override
        void writeValue(EnumItemSet set, DataOutputStream out) throws IOException, CodecException {
            boolean[] members = new boolean[desc.size()];
            for (EnumItem item : set) {
                int position = desc.indexOf(item.getSymbol());
                if (position < 0) {
                    throw new CodecException("Enum item '" + item.getSymbol() + "' is not part of the LabVIEW enum set '" + desc.getName() + "'");
                }
                members[position] = true;
            }
            for (boolean member : members) {
                out.writeByte(member ? 1 : 0);
            }
        }
    }

    /**
     * A JAPC discrete function is flattened like a 2D array of DBL: dim0 = 2, dim1 = N, then row 0 (X) and row 1 (Y).
     * An array without any element (LabVIEW may flatten an empty 2D array as 0 x 0) is an empty function.
     */
    private static final class DiscreteFunctionElement extends TypedElement<DiscreteFunction> {
        DiscreteFunctionElement() {
            super(DiscreteFunction.class, DiscreteFunction.class, "a LabVIEW discrete function");
        }

        @Override
        public Object read(ByteBuffer buffer) throws CodecException {
            int rows = buffer.getInt();
            int columns = buffer.getInt();
            if (rows < 0 || columns < 0) {
                throw new CodecException("Discrete function has negative dimensions: " + rows + " x " + columns);
            }
            if (rows == 0 || columns == 0) {
                return DomainValueFactory.newDiscreteFunction(new double[0], new double[0]);
            }
            if (rows != 2) {
                throw new CodecException("Discrete function must have 2 rows (X, Y), got " + rows + " rows of " + columns + " columns");
            }
            if (columns > buffer.remaining() / (2 * Double.BYTES)) {
                throw new CodecException("Discrete function with " + columns + " points exceeds the available data");
            }
            return DomainValueFactory.newDiscreteFunction(readDoubles(buffer, columns), readDoubles(buffer, columns));
        }

        private static double[] readDoubles(ByteBuffer buffer, int length) {
            double[] values = new double[length];
            for (int i = 0; i < length; i++) {
                values[i] = buffer.getDouble();
            }
            return values;
        }

        @Override
        void writeValue(DiscreteFunction function, DataOutputStream out) throws IOException, CodecException {
            double[] x = function.toXArray();
            double[] y = function.toYArray();
            if (x.length != y.length) {
                throw new CodecException("Discrete function X and Y arrays have different lengths: " + x.length + " != " + y.length);
            }
            out.writeInt(2);
            out.writeInt(x.length);
            for (double value : x) {
                out.writeDouble(value);
            }
            for (double value : y) {
                out.writeDouble(value);
            }
        }
    }
}
