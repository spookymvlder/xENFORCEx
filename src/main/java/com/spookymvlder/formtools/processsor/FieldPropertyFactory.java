package com.spookymvlder.formtools.processsor;

import com.spookymvlder.formtools.datatypes.fieldproperties.*;

import static com.spookymvlder.formtools.processsor.FieldPropertyConverter.*;

public class FieldPropertyFactory {

    public static NormalizedFieldPropertyRow normalizeRow(FieldPropertyRow row) {
        return new NormalizedFieldPropertyRow(
                normalizeBlank(row.getFieldId()),
                normalizeBlank(row.getFieldName()),
                normalizeBlank(row.getDisplayName()),

                normalizeBlank(row.getParentId()),

                row.getActive(),
                row.getExpireDate(),

                parseConditionalBool(row.getFieldRepeated()),
                row.getMinOccurs(),
                row.getMaxOccurs(),
                row.getCondRepeatedSourceIds(),
                row.getCondRepeatedValues(),
                parseLinkLogic(row.getCondRepeatedLogic()),

                parseConditionalBool(row.getRequired()),
                row.getCondRequiredSourceIds(),
                row.getCondRequiredValues(),
                parseLinkLogic(row.getCondRequiredLogic()),

                normalizeBlank(row.getCodeSystemName()),
                normalizeBlank(row.getCodeSystemId()),
                normalizeBlank(row.getCodeSystemVersion()),
                normalizeBlank(row.getCodeSystemCode()),

                parseDataType(row.getDataType()),
                row.getRangeStart(),
                row.getRangeEnd(),
                row.getRangeStartInclusive(),
                row.getRangeEndInclusive(),
                row.getMinLength(),
                row.getMaxLength(),
                row.getEnumerations(),
                normalizeBlank(row.getRegexPattern()),

                row.getAttribute(),
                normalizeBlank(row.getAttributeName()),

                normalizeBlank(row.getUiControl()),
                normalizeBlank(row.getUnit()),
                row.getDisplayOrder(),
                normalizeBlank(row.getValidationMessage()),

                row.getSkipImport(),
                parseGroupingType(row.getGroupingType()),
                normalizeBlank(row.getComponentOf())
        );
    }

    public static FieldProperty buildFieldProperty(FieldPropertyRow row) {
        return buildFieldProperty(normalizeRow(row));
    }

    public static FieldProperty buildFieldProperty(NormalizedFieldPropertyRow row) {
        FieldIdentity identity = buildIdentity(row);
        FieldRelation relation = buildRelation(row);
        FieldCardinality cardinality = buildCardinality(row);
        FieldValueRestriction restriction = buildRestriction(row);
        FieldCodeSpec codeSpec = buildCodeSpec(row);
        FieldMetadataUI metadataUI = buildMetadataUI(row);

        return new FieldProperty(
                identity,
                relation,
                cardinality,
                restriction,
                codeSpec,
                metadataUI
        );
    }



    public static FieldIdentity buildIdentity(NormalizedFieldPropertyRow row) {
        return new FieldIdentity(
                row.fieldId(),
                row.fieldName(),
                row.displayName(),
                row.active(),
                row.expireDate(),
                row.skipImport()
        );
    }

    public static FieldRelation buildRelation(NormalizedFieldPropertyRow row) {
        return new FieldRelation(
                row.parentId(),
                row.groupingType(),
                row.componentOf()
        );
    }

    public static FieldCardinality buildCardinality(NormalizedFieldPropertyRow row) {
        return new FieldCardinality(
                row.fieldRepeated(),
                row.minOccurs(),
                row.maxOccurs(),
                row.condRepeatedSourceIds(),
                row.condRepeatedValues(),
                row.condRepeatedLogic(),
                row.required(),
                row.condRequiredSourceIds(),
                row.condRequiredValues(),
                row.condRequiredLogic()
        );
    }



    public static FieldValueRestriction buildRestriction(NormalizedFieldPropertyRow row) {
        return new FieldValueRestriction(
                row.dataType(),
                row.rangeStart(),
                row.rangeEnd(),
                row.rangeStartInclusive(),
                row.rangeEndInclusive(),
                row.minLength(),
                row.maxLength(),
                row.enumerations(),
                row.regexPattern(),
                row.isAttribute(),
                row.attributeName()
        );
    }

    public static FieldCodeSpec buildCodeSpec(NormalizedFieldPropertyRow row) {
        return new FieldCodeSpec(
                row.codeSystemName(),
                row.codeSystemId(),
                row.codeSystemVersion(),
                row.codeSystemCode()
        );
    }

    public static FieldMetadataUI buildMetadataUI(NormalizedFieldPropertyRow row) {
        return new FieldMetadataUI(
                row.uiControl(),
                row.unit(),
                row.displayOrder(),
                row.validationMessage()
        );
    }


}
