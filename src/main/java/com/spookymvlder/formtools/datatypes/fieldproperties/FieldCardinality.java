package com.spookymvlder.formtools.datatypes.fieldproperties;

import com.spookymvlder.formtools.datatypes.ConditionalBool;
import com.spookymvlder.formtools.datatypes.LinkLogic;
import com.spookymvlder.formtools.datatypes.Usage;

import java.util.List;

public class FieldCardinality {
    private ConditionalBool fieldRepeated;
    private Integer minOccurs;
    private Integer maxOccurs;
    private List<String> condRepeatedSourceIds;
    //TODO update logic here to split list level here and condRequiredValues on curly braces and array level via pipe
    private List<String[]> condRepeatedValues;
    private LinkLogic condRepeatedLogic;

    private Usage required;
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
                            Usage required,
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
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
    }


    private List<String[]> getProcessedCondRepeatedValues(List<String> condRepeatedValues) {
        return processValueSets(condRepeatedValues);
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
        return processValueSets(condRequiredValues);
    }

    //TODO extend to recognize **!, comparators (<, >, <=, >=), ranges (a-b), enum-index references.
    private static List<String[]> processValueSets(List<String> rawValueSets) {
        if (rawValueSets == null) {
            return new java.util.ArrayList<>();
        }
        return rawValueSets.stream()
                .map(s -> s == null ? "" : s.trim())
                .map(s -> {
                    if (s.startsWith("{") && s.endsWith("}")) {
                        return s.substring(1, s.length() - 1);
                    }
                    return s;
                })
                .map(s -> s.split(","))
                .toList();
    }

    public Usage getRequired() {
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
