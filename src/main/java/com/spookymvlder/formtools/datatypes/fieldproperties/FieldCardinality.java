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

    public FieldCardinality(ConditionalBool fieldRepeated,
                            Integer minOccurs,
                            Integer maxOccurs,
                            List<String> condRepeatedSourceIds,
                            List<String> condRepeatedValues,
                            LinkLogic condRepeatedLogic) {
        this.fieldRepeated = fieldRepeated;
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
        this.condRepeatedSourceIds = condRepeatedSourceIds;
        this.condRepeatedValues = getProcessedCondRepeatedValues(condRepeatedValues);
        this.condRepeatedLogic = condRepeatedLogic;
    }

    public List<String[]> getProcessedCondRepeatedValues(List<String> condRepeatedValues) {
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
}
