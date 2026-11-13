package cern.rade.services.codec.labview;

import cern.japc.value.BooleanType;
import cern.japc.value.EnumType;
import cern.japc.value.MapDescriptor;
import cern.japc.value.SimpleDescriptor;
import cern.japc.value.SimpleValueStandardMeaning;
import cern.japc.value.Type;
import cern.japc.value.ValueType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Test only implementations of the public JAPC descriptor interfaces, with the minimum the codec looks at: the enum
 * type of the fields. (JAPC creates its descriptors from the device metadata, there is no public factory.)
 */
final class TestDescriptors {
    private TestDescriptors() {
    }

    /** The descriptor of a single field, with an enum type if it is an enum or an enum set (null otherwise). */
    static SimpleDescriptor simple(EnumType enumType) {
        return new TestSimpleDescriptor(enumType);
    }

    /** The descriptor of a property, from the descriptors of its fields: name, descriptor, name, descriptor... */
    static MapDescriptor property(Object... fieldNamesAndDescriptors) {
        Map<String, SimpleDescriptor> fields = new LinkedHashMap<>();
        for (int i = 0; i < fieldNamesAndDescriptors.length; i += 2) {
            fields.put((String) fieldNamesAndDescriptors[i], (SimpleDescriptor) fieldNamesAndDescriptors[i + 1]);
        }
        return new TestMapDescriptor(fields);
    }

    private static final class TestMapDescriptor implements MapDescriptor {
        private final Map<String, SimpleDescriptor> fields;

        TestMapDescriptor(Map<String, SimpleDescriptor> fields) {
            this.fields = fields;
        }

        @Override
        public Type getType() {
            return Type.MAP;
        }

        @Override
        public boolean isConstant() {
            return false;
        }

        @Override
        public int size() {
            return fields.size();
        }

        @Override
        public String[] getNames() {
            return fields.keySet().toArray(String[]::new);
        }

        @Override
        public SimpleDescriptor get(String name) {
            return fields.get(name);
        }
    }

    private static final class TestSimpleDescriptor implements SimpleDescriptor {
        private final EnumType enumType;

        TestSimpleDescriptor(EnumType enumType) {
            this.enumType = enumType;
        }

        @Override
        public EnumType getEnumType() {
            return enumType;
        }

        @Override
        public Type getType() {
            return Type.SIMPLE;
        }

        @Override
        public ValueType getValueType() {
            return enumType == null ? ValueType.UNDEFINED : ValueType.ENUM;
        }

        @Override
        public boolean isConstant() {
            return false;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public String[] getNames() {
            return new String[0];
        }

        @Override
        public SimpleDescriptor get(String name) {
            return null;
        }

        @Override
        public String getUnit() {
            return null;
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public double getMinValue() {
            return Double.NaN;
        }

        @Override
        public double getMaxValue() {
            return Double.NaN;
        }

        @Deprecated
        @Override
        public boolean isFilterable() {
            return false;
        }

        @Deprecated
        @Override
        public boolean isDiscrete() {
            return false;
        }

        @Override
        public int getLength() {
            return UNDEFINED_DIMENSION;
        }

        @Override
        public int getRowCount() {
            return UNDEFINED_DIMENSION;
        }

        @Override
        public int getColumnCount() {
            return UNDEFINED_DIMENSION;
        }

        @Override
        public String getFormatPattern() {
            return null;
        }

        @Override
        public String[] getExtraCharacteristicNames() {
            return new String[0];
        }

        @Override
        public String getExtraCharacteristic(String name) {
            return null;
        }

        @Override
        public BooleanType getBooleanType() {
            return null;
        }

        @Override
        public SimpleValueStandardMeaning getStandardMeaning(Object value) {
            return null;
        }

        @Override
        public boolean isSettable(Object value) {
            return true;
        }
    }
}
