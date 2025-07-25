package com.spookymvlder.formtools.datatypes;

import java.util.Objects;

public class FieldData implements FormStructure {
    private String value;
    private String dataType;
    private int position;

    public FieldData(String value, String dataType, int position) {
        this.value = value;
        this.dataType = dataType;
        this.position = position;
    }

    public String getValue() {
        return value;
    }

    public String getDataType() {
        return dataType;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "FieldData{" +
                "value='" + value + '\'' +
                ", dataType='" + dataType + '\'' +
                ", position=" + position +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FieldData fieldData = (FieldData) o;
        return getPosition() == fieldData.getPosition() && Objects.equals(getValue(), fieldData.getValue()) && Objects.equals(getDataType(), fieldData.getDataType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue(), getDataType(), getPosition());
    }
}
