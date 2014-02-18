package com.performizeit.threadtop.localext.format;

/**
 * Represent formatted table column
 * User: lyanm
 */
public class ColumnFormat {
    private String columnName;
    private boolean isSorted = false;   // default param
    private String headerFormat;
    private String valueFormat = null;   // default param

    /**
     * Ctor
     */
    public ColumnFormat(String columnName, boolean isSorted, String format, String valueFormat) {
        this.columnName = columnName;
        this.isSorted = isSorted;
        this.headerFormat = format;
        this.valueFormat = valueFormat;
    }
    /**
     * Ctor
     */
    public ColumnFormat(String columnName, String format,String valueFormat) {
        this(columnName, false, format,valueFormat);
    }
    /**
     * Ctor
     */
    public ColumnFormat(String columnName, String format) {
        this(columnName, false, format,format);
    }
    /**
     * Ctor
     */
    public ColumnFormat(String columnName, boolean sorted, String format) {
        this(columnName, sorted, format,format);
    }


    /**
     * @return formatted name of the column's header
     */
    public String getHeaderName() {
        if(isSorted) {
            return ">"+ columnName;
        }
        return columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getHeaderFormat() {
        return headerFormat;
    }

    public String getValueFormat() {
        if(valueFormat !=null)
            return valueFormat;
        return headerFormat;
    }
}
