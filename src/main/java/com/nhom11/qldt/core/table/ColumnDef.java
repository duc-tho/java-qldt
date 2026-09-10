package com.nhom11.qldt.core.table;

import java.util.function.Function;

public class ColumnDef {
    public final String field;
    public final String label;
    private final Function<Object, String> valueExtractor;

    public ColumnDef(String field, String label, Function<Object, String> valueExtractor) {
        this.field = field;
        this.label = label;
        this.valueExtractor = valueExtractor;
    }

    public String getValue(Object item) {
        return valueExtractor.apply(item);
    }
}
