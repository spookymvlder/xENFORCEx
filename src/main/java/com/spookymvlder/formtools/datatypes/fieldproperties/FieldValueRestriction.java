package com.spookymvlder.formtools.datatypes.fieldproperties;

import com.spookymvlder.formtools.datatypes.FieldDataType;

import java.util.List;

public class FieldValueRestriction {
    private FieldDataType dataType;
    private Double rangeStart;
    private Double rangeEnd;
    private Boolean rangeStartInclusive;
    private Boolean rangeEndInclusive;
    private Integer minLength;
    private Integer maxLength;
    private List<String> enumerations;
    private String regexPattern;

    // ATTRIBUTE INFORMATION COLUMNS
    private Boolean isAttribute;
    private String attributeName;

    public FieldValueRestriction(FieldDataType dataType,
                                 Double rangeStart,
                                 Double rangeEnd,
                                 Boolean rangeStartInclusive,
                                 Boolean rangeEndInclusive,
                                 Integer minLength,
                                 Integer maxLength,
                                 List<String> enumerations,
                                 String regexPattern,
                                 Boolean isAttribute,
                                 String attributeName) {
        this.dataType = dataType;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.rangeStartInclusive = rangeStartInclusive;
        this.rangeEndInclusive = rangeEndInclusive;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.enumerations = enumerations;
        this.regexPattern = regexPattern;
        this.isAttribute = isAttribute;
        this.attributeName = attributeName;
    }



    public FieldDataType getDataType() {
        return dataType;
    }

    public Double getRangeStart() {
        return rangeStart;
    }

    public Double getRangeEnd() {
        return rangeEnd;
    }

    public Boolean getRangeStartInclusive() {
        return Boolean.TRUE.equals(this.rangeStartInclusive);
    }

    public Boolean getRangeEndInclusive() {
        return Boolean.TRUE.equals(this.rangeEndInclusive);
    }

    public Integer getMinLength() {
        return minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public List<String> getEnumerations() {
        return enumerations;
    }

    public String getRegexPattern() {
        return regexPattern;
    }

    public Boolean getAttribute() {
        return Boolean.TRUE.equals(this.isAttribute);
    }

    public String getAttributeName() {
        return attributeName;
    }
}
