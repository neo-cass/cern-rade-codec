package cern.rade.services.codec.labview;

import cern.japc.value.EnumItem;
import cern.japc.value.EnumType;
import cern.japc.value.EnumTypeBitSize;
import cern.japc.value.SimpleValueStandardMeaning;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Test only implementation of the public JAPC {@link EnumType} interface (the JAPC implementations are internal).
 */
final class TestEnumType implements EnumType {
    private final String name;
    private final boolean applicableToEnumItemSet;
    private final Map<Long, TestEnumItem> itemsByCode = new LinkedHashMap<>();

    /**
     * @param symbolsToCodes the items; the codes are sparse and unordered on purpose: LV only sees positions 0..n-1
     */
    TestEnumType(String name, boolean applicableToEnumItemSet, Map<String, Long> symbolsToCodes) {
        this.name = name;
        this.applicableToEnumItemSet = applicableToEnumItemSet;
        symbolsToCodes.forEach((symbol, code) -> itemsByCode.put(code, new TestEnumItem(this, symbol, code)));
    }

    /** An enum whose items are LV positions OFF=0 (code 1), ON=1 (code 5), STANDBY=2 (code 10). */
    static TestEnumType mode() {
        Map<String, Long> items = new LinkedHashMap<>();
        items.put("STANDBY", 10L);
        items.put("OFF", 1L);
        items.put("ON", 5L);
        return new TestEnumType("LVCodecTestMode", false, items);
    }

    /** An enum usable in an enum item set: the codes are the bit masks of the items, in non alphabetical order. */
    static TestEnumType flags() {
        Map<String, Long> items = new LinkedHashMap<>();
        items.put("READY", 4L);
        items.put("BUSY", 1L);
        items.put("FAULT", 2L);
        return new TestEnumType("LVCodecTestFlags", true, items);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public EnumTypeBitSize getBitSize() {
        return EnumTypeBitSize.INT;
    }

    @Override
    public boolean isApplicableToEnumItemSet() {
        return applicableToEnumItemSet;
    }

    @Override
    public EnumItem valueOf(String symbol) {
        return itemsByCode.values().stream().filter(item -> item.getSymbol().equals(symbol)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown symbol " + symbol));
    }

    @Override
    public EnumItem valueOf(long code) {
        EnumItem item = itemsByCode.get(code);
        if (item == null) {
            throw new IllegalArgumentException("Unknown code " + code);
        }
        return item;
    }

    @Override
    public Set<EnumItem> values() {
        return Set.copyOf(itemsByCode.values());
    }

    @Override
    public Collection<String> symbols() {
        return itemsByCode.values().stream().map(TestEnumItem::getSymbol).toList();
    }

    /**
     * Test only implementation of the public JAPC {@link EnumItem} interface.
     */
    private static final class TestEnumItem implements EnumItem {
        private final EnumType enumType;
        private final String symbol;
        private final long code;

        TestEnumItem(EnumType enumType, String symbol, long code) {
            this.enumType = enumType;
            this.symbol = symbol;
            this.code = code;
        }

        @Override
        public EnumType getEnumType() {
            return enumType;
        }

        @Override
        public long getCode() {
            return code;
        }

        /** Abstract in the interface (which deprecates it), so it has to be implemented. Not used by the codec. */
        @Deprecated
        @Override
        public String getString() {
            return symbol;
        }

        @Override
        public String getSymbol() {
            return symbol;
        }

        @Override
        public SimpleValueStandardMeaning getStandardMeaning() {
            return null;
        }

        @Override
        public boolean isSettable() {
            return true;
        }

        @Override
        public int compareTo(EnumItem other) {
            return Long.compare(code, other.getCode());
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof EnumItem other && other.getEnumType() == enumType && other.getCode() == code;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(code);
        }

        @Override
        public String toString() {
            return symbol;
        }
    }
}
