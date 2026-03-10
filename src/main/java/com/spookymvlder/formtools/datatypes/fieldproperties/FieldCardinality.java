package com.spookymvlder.formtools.datatypes.fieldproperties;

import com.spookymvlder.formtools.datatypes.ConditionalBool;
import com.spookymvlder.formtools.datatypes.LinkLogic;

import java.util.List;

public class FieldCardinality {
    private ConditionalBool fieldRepeated;
    private Integer minOccurs;
    private Integer maxOccurs;
    private List<String> condRepeatedSourceIds;
    //TODO update logic here to split list level here and condRequiredValues on curly braces and array level via pipe
    private List<String[]> condRepeatedValues;
    private LinkLogic condRepeatedLogic;

    private ConditionalBool required;
    //TODO import separate conditionally mandatory logic if complicated.
    private List<String> condRequiredSourceIds;
    private List<String[]> condRequiredValues;
    private LinkLogic condRequiredLogic;

    public FieldCardinality(ConditionalBool fieldRepeated,
                            Integer minOccurs,
                            Integer maxOccurs,
                            List<String> condRepeatedSourceIds,
                            List<String> condRepeatedValues,
                            LinkLogic condRepeatedLogic,
                            ConditionalBool required,
                            List<String> condRequiredSourceIds,
                            List<String> condRequiredValues,
                            LinkLogic condRequiredLogic) {
        this.fieldRepeated = fieldRepeated;
        this.condRepeatedSourceIds = condRepeatedSourceIds;
        this.condRepeatedValues = getProcessedCondRepeatedValues(condRepeatedValues);
        this.condRepeatedLogic = condRepeatedLogic;
        this.required = required;
        this.condRequiredSourceIds = condRequiredSourceIds;
        this.condRequiredValues = getProcessedCondRequiredValues(condRequiredValues);
        this.condRequiredLogic = condRequiredLogic;
        setOccurs(minOccurs, maxOccurs, fieldRepeated, required);
    }

    private void setOccurs(Integer minOccurs, Integer maxOccurs, ConditionalBool fieldRepeated, ConditionalBool required) {
        if (minOccurs == null) {
            if (required == ConditionalBool.FALSE) {
                this.minOccurs = 0;
            }
        }
        if (maxOccurs == null) {
            if (fieldRepeated == ConditionalBool.FALSE) {
                this.maxOccurs = 1;
            }
        }
    }

    private List<String[]> getProcessedCondRepeatedValues(List<String> condRepeatedValues) {
        return condRepeatedValues.stream()
                .map(s -> s.split(","))
                .toList();
    }

    public ConditionalBool getFieldRepeated() {
        return fieldRepeated;
    }

    public Integer getMinOccurs() {
        return minOccurs;
    }

    public Integer getMaxOccurs() {
        return maxOccurs;
    }

    public List<String> getCondRepeatedSourceIds() {
        return condRepeatedSourceIds;
    }

    public List<String[]> getCondRepeatedValues() {
        return condRepeatedValues;
    }

    public LinkLogic getCondRepeatedLogic() {
        return condRepeatedLogic;
    }

    private List<String[]> getProcessedCondRequiredValues(List<String> condRequiredValues) {
        return condRequiredValues.stream()
                .map(s -> s.split(","))
                .toList();
    }

    public ConditionalBool getRequired() {
        return required;
    }

    public List<String> getCondRequiredSourceIds() {
        return condRequiredSourceIds;
    }

    public List<String[]> getCondRequiredValues() {
        return condRequiredValues;
    }

    public LinkLogic getCondRequiredLogic() {
        return condRequiredLogic;
    }
}
