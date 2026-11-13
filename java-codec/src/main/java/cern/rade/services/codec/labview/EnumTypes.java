package cern.rade.services.codec.labview;

import cern.japc.value.EnumItem;
import cern.japc.value.EnumType;
import cern.japc.value.MapDescriptor;
import cern.japc.value.SimpleDescriptor;
import cern.japc.value.ValueDescriptor;
import cern.rade.services.codec.CodecException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Everything the codec needs to know about JAPC enum types.
 * <p>
 * A LabVIEW enum (or enum set) only carries item names, while JAPC enum items have their own codes defined by the
 * device. The codec therefore maps the two by item name, and needs the JAPC {@link EnumType} to decode. As CMW has no
 * enum types, it can only come from the JAPC {@link ValueDescriptor} of the parameter. The descriptor is consulted for
 * enum and enum set fields only, everything else is decided from the LV type descriptor alone.
 */
final class EnumTypes {
    private EnumTypes() {
    }

    /**
     * @param descriptor the JAPC descriptor of a property (cluster), may be null
     * @return the descriptor of the field with the given name, null if the descriptor is not that of a property or has no such field.
     */
    static SimpleDescriptor fieldDescriptor(ValueDescriptor descriptor, String fieldName) {
        // a SimpleDescriptor is also a MapDescriptor, but it does not describe the fields of a property
        if (descriptor instanceof MapDescriptor map && !(descriptor instanceof SimpleDescriptor)) {
            return map.get(fieldName);
        }
        return null;
    }

    /**
     * @param descriptor the JAPC descriptor of a single field, may be null
     * @return the descriptor as a {@link SimpleDescriptor}, null if it is not one.
     */
    static SimpleDescriptor singleDescriptor(ValueDescriptor descriptor) {
        return descriptor instanceof SimpleDescriptor simple ? simple : null;
    }

    /**
     * @param field     the JAPC descriptor of the field, may be null
     * @param fieldName the LV label of the field, for the error message
     * @return the enum type of the field.
     * @throws CodecException if the descriptor is missing or has no enum type.
     */
    static EnumType require(SimpleDescriptor field, String fieldName) throws CodecException {
        EnumType enumType = field == null ? null : field.getEnumType();
        if (enumType == null) {
            throw new CodecException("JAPC descriptor with enum type required for field " + fieldName);
        }
        return enumType;
    }

    /**
     * @return the symbols of all the items of the enum type, ordered by JAPC code.
     */
    static String[] symbolsByCode(EnumType enumType) {
        return enumType.values().stream()
                .sorted(Comparator.comparingLong(EnumItem::getCode))
                .map(EnumItem::getSymbol)
                .toArray(String[]::new);
    }

    /**
     * @return the items of the enum type by symbol.
     */
    static Map<String, EnumItem> itemsBySymbol(EnumType enumType) {
        Map<String, EnumItem> items = new HashMap<>();
        for (EnumItem item : enumType.values()) {
            items.put(item.getSymbol(), item);
        }
        return items;
    }
}
