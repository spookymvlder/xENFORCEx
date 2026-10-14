package com.spookymvlder.formtools.datatypes.fieldproperties;

import com.spookymvlder.formtools.datatypes.ConditionalBool;
import com.spookymvlder.formtools.datatypes.FieldDataType;
import com.spookymvlder.formtools.datatypes.GroupingType;
import com.spookymvlder.formtools.datatypes.LinkLogic;
import com.spookymvlder.formtools.datatypes.Usage;

import java.time.LocalDate;
import java.util.List;

// Represents a parsed, typed row with blanks normalized to null and cross-field defaults applied.
public record ResolvedFieldPropertyRow (
    String fieldId,
    String fieldName,
    String namespaceUri,
    String displayName,

    String parentId,

    Boolean active,
    LocalDate expireDate,

    ConditionalBool fieldRepeated,
    Integer minOccurs,
    Integer maxOccurs,
    List<String> condRepeatedSourceIds,
    List<String> condRepeatedValues,
    LinkLogic condRepeatedLogic,

    Usage required,
    List<String> condRequiredSourceIds,
    List<String> condRequiredValues,
    LinkLogic condRequiredLogic,

    String codeSystemName,
    String codeSystemId,
    String codeSystemVersion,
    String codeSystemCode,

    FieldDataType dataType,
    String rangeStart,
    String rangeEnd,
    Boolean rangeStartInclusive,
    Boolean rangeEndInclusive,
    Integer minLength,
    Integer maxLength,
    List<String> enumerations,
    String regexPattern,

    Boolean isAttribute,
    String attributeName,

    String uiControl,
    String unit,
    Integer displayOrder,
    String validationMessage,

    Boolean skipImport,
    GroupingType groupingType,
    String componentOf
        ){

}
