package com.spookymvlder.formtools.datatypes.fieldproperties;

import com.spookymvlder.formtools.datatypes.ConditionalBool;
import com.spookymvlder.formtools.datatypes.LinkLogic;

import java.util.List;

public class FieldRequirement {

    private ConditionalBool required;
    //TODO import separate conditionally mandatory logic if complicated.
    private List<String> condRequiredSourceIds;
    private List<String[]> condRequiredValues;
    private LinkLogic condRequiredLogic;

    public FieldRequirement(ConditionalBool required,
                            List<String> condRequiredSourceIds,
                            List<String> condRequiredValues,
                            LinkLogic condRequiredLogic) {
        this.required = required;
        this.condRequiredSourceIds = condRequiredSourceIds;
        this.condRequiredValues = getProcessedCondRequiredValues(condRequiredValues);
        this.condRequiredLogic = condRequiredLogic;
    }

    public List<String[]> getProcessedCondRequiredValues(List<String> condRequiredValues) {
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
