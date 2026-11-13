package cern.rade.services.codec.labview;

import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.japc.value.Array2D;
import cern.japc.value.DiscreteFunction;
import cern.japc.value.DiscreteFunctionList;
import cern.japc.value.EnumItem;
import cern.japc.value.EnumItemSet;
import cern.japc.value.EnumType;
import cern.japc.value.MapParameterValue;
import cern.japc.value.ParameterValue;
import cern.japc.value.SimpleParameterValue;
import cern.japc.value.Type;
import cern.japc.value.ValueConversionException;
import cern.japc.value.ValueType;
import cern.japc.value.factory.DomainValueFactory;
import cern.rade.services.codec.CodecException;

import java.lang.reflect.Array;

/**
 * Access to the JAPC values as the codec needs them: scalars as one object, arrays as dimensions and a flat Java array.
 * It also is the one place deciding which JAPC types are not representable in LabVIEW (OBJECT and UNDEFINED).
 */
final class JapcValues {
    private JapcValues() {
    }

    static CodecException notRepresentable(Object japcType) {
        return new CodecException("JAPC type " + japcType + " is not representable in LabVIEW");
    }

    /**
     * @return the value as a map (the fields of a property).
     * @throws CodecException if it is not a map, or is not representable in LabVIEW (OBJECT)
     */
    static MapParameterValue asMap(ParameterValue value) throws CodecException {
        checkRepresentable(value);
        if (Type.MAP.equals(value.getType())) {
            return value.castAsMap();
        }
        throw new CodecException("A LabVIEW cluster requires a JAPC map, got a " + value.getType() + " value");
    }

    /**
     * @return the value as a simple value (one field).
     * @throws CodecException if it is not a simple value, or is not representable in LabVIEW (OBJECT)
     */
    static SimpleParameterValue asSimple(ParameterValue value) throws CodecException {
        checkRepresentable(value);
        if (Type.SIMPLE.equals(value.getType())) {
            return value.castAsSimple();
        }
        throw new CodecException("A single LabVIEW field requires a JAPC simple value, got a " + value.getType() + " value");
    }

    private static void checkRepresentable(ParameterValue value) throws CodecException {
        if (!Type.MAP.equals(value.getType()) && !Type.SIMPLE.equals(value.getType())) {
            throw notRepresentable(value.getType());
        }
    }

    /**
     * @return the value type of the value, which is never UNDEFINED.
     * @throws CodecException if the type is UNDEFINED, which is the case of a value which has not been set: JAPC throws
     *                        when asking for its type
     */
    static ValueType valueTypeOf(SimpleParameterValue value) throws CodecException {
        ValueType type;
        try {
            type = value.getValueType();
        } catch (ValueConversionException e) {
            throw new CodecException("JAPC type " + ValueType.UNDEFINED + " is not representable in LabVIEW (the value has not been set)", e);
        }
        if (ValueType.UNDEFINED.equals(type)) {
            throw notRepresentable(type);
        }
        return type;
    }

    /**
     * @return true if the value is an array on the LabVIEW side: a JAPC array, 2D array or discrete function list.
     */
    static boolean isArray(ValueType type) {
        return type.isArray() || type.isArray2d() || ValueType.DISCRETE_FUNCTION_LIST.equals(type);
    }

    /**
     * @return the JAPC counterpart of the LV element ({@link LVElement}) of the scalar value.
     * @throws CodecException if the value is not a scalar, or of a type not representable in LabVIEW
     */
    static Object scalar(SimpleParameterValue value) throws CodecException {
        ValueType type = valueTypeOf(value);
        if (ValueType.ENUM.equals(type)) {
            return value.getEnumItem();
        }
        if (ValueType.ENUM_SET.equals(type)) {
            return value.getEnumItemSet();
        }
        if (ValueType.DISCRETE_FUNCTION.equals(type)) {
            return value.getDiscreteFunction();
        }
        if (type.isScalar()) {
            return value.getObject();
        }
        throw notRepresentable(type);
    }

    /**
     * @param element the JAPC counterpart of an LV element, see {@link LVElement}
     */
    static SimpleParameterValue ofScalar(Object element) {
        return SimpleParameterValueFactory.newSimpleParameterValue(element);
    }

    /**
     * @return the dimensions and the elements of the JAPC array, 2D array or discrete function list.
     */
    static LVArrays.Data flatten(SimpleParameterValue value) {
        ValueType type = value.getValueType();
        if (ValueType.DISCRETE_FUNCTION_LIST.equals(type)) {
            DiscreteFunctionList list = value.getDiscreteFunctionList();
            DiscreteFunction[] functions = new DiscreteFunction[list.size()];
            for (int i = 0; i < functions.length; i++) {
                functions[i] = list.getFunction(i);
            }
            return new LVArrays.Data(new int[]{functions.length}, functions);
        }
        if (type.isArray2d()) {
            Array2D array = value.getArray2D();
            return new LVArrays.Data(new int[]{array.getRowCount(), array.getColumnCount()}, array.getArray1D());
        }
        return new LVArrays.Data(new int[]{value.getLength()}, value.getObject());
    }

    /**
     * @return the JAPC array, 2D array or discrete function list (for discrete functions) holding the data.
     * @throws CodecException if the array has more than 2 dimensions, which JAPC cannot hold
     */
    static SimpleParameterValue ofArray(LVArrays.Data data) throws CodecException {
        if (data.flat() instanceof DiscreteFunction[] functions) {
            return SimpleParameterValueFactory.newSimpleParameterValue(DomainValueFactory.newDiscreteFunctionList(functions));
        }
        return switch (data.dimensions().length) {
            case 1 -> SimpleParameterValueFactory.newSimpleParameterValue(data.flat());
            case 2 -> SimpleParameterValueFactory.newSimpleParameterValue(data.flat(), data.dimensions());
            default -> throw new CodecException("Unsupported array dimension (>2): " + data.dimensions().length);
        };
    }

    /**
     * @return the JAPC enum type of an enum or enum set value (scalar or array); null if it is an empty array, which
     * does not tell the enum type.
     */
    static EnumType enumTypeOf(SimpleParameterValue value) throws CodecException {
        Object first;
        if (isArray(value.getValueType())) {
            LVArrays.Data data = flatten(value);
            first = data.length() == 0 ? null : Array.get(data.flat(), 0);
        } else {
            first = scalar(value);
        }
        if (first instanceof EnumItem item) {
            return item.getEnumType();
        }
        return first instanceof EnumItemSet set ? set.getEnumType() : null;
    }
}
