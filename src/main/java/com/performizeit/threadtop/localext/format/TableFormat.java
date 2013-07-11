package com.performizeit.threadtop.localext.format;

import java.util.*;

/**
 * Represent formatted table
 * User: lyanm
 */
public class TableFormat {
    private Map<String,ColumnFormat> columnsByName;
    private ArrayList<String> columnsOrder;

    StringBuilder sb;
    Formatter formatter;

    /**
     * Ctor
     */
    public TableFormat() {
        sb = new StringBuilder();
        formatter = new Formatter(sb, Locale.US);
        columnsByName = new HashMap<>();
        columnsOrder = new ArrayList<>();
    }

    /**
     * Add column according to the insert order
     */
    public void addColumn(ColumnFormat columnFormat) {
        columnsByName.put(columnFormat.getColumnName(), columnFormat);
        columnsOrder.add(columnFormat.getColumnName());
    }

    public String getFormatHeader() {
        return getFormatHeader(getColumnNo());
    }

    public String[] getColumnHeaders() {
        String[] result = new String[columnsByName.size()];
        int i=0;
        for(String columnName : columnsOrder) {
            result[i] = columnsByName.get(columnName).getHeaderName();
            i++;
        }
        return result;
    }

    public ColumnFormat getColumnFormat(String name) {
        return columnsByName.get(name);
    }

    public int getColumnNo() {
        return columnsOrder.size();
    }

    public String getFormatHeader(int lastColumn) {
        StringBuilder result = new StringBuilder();
        // 1. build format
        int i=0;
        for(String columnName : columnsOrder) {
            if(i<lastColumn) {
                result.append(columnsByName.get(columnName).getHeaderFormat());
                i++;
            }
        }
        return result.toString();
    }

    public void printHeader() {
        formatter.format(getFormatHeader(), getColumnHeaders());
        System.out.println(formatter.toString());
    }

    public void clean() {
        sb.setLength(0);
    }

    public void format(String columnName, double value) {
        ColumnFormat columnFormat = getColumnFormat(columnName);
        if(columnFormat != null) {
            formatter.format(columnFormat.getValueFormat(), value);
        }
    }
    public void format(String columnName, long value) {
        ColumnFormat columnFormat = getColumnFormat(columnName);
        if(columnFormat != null) {
            formatter.format(columnFormat.getValueFormat(), value);
        }
    }

    public void format(String columnName, String value) {
        ColumnFormat columnFormat = getColumnFormat(columnName);
        if(columnFormat != null) {
            formatter.format(columnFormat.getValueFormat(), value);
        }
    }

    public void formatEmptyLine(int columnNo) {
        formatter.format(getFormatHeader(columnNo), generateEmptyStringArrays(columnNo));
    }

    /**
     *
     * @return array of empty strings
     */
    private String[] generateEmptyStringArrays(int size) {
        String result[] = new String[size];
        for(int i =0; i<result.length; i++) {
            result[i] = "".intern();
        }
        return result;
    }

    public void printRaw() {
        System.out.println(formatter.toString());
        clean();
    }
}
