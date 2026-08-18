package com.spookymvlder.formtools.processsor;

import com.spookymvlder.formtools.datatypes.ConditionalBool;
import com.spookymvlder.formtools.datatypes.GroupingType;
import com.spookymvlder.formtools.datatypes.Usage;
import com.spookymvlder.formtools.datatypes.fieldproperties.*;
import com.spookymvlder.formtools.settings.ProcessingOverrides;

import static com.spookymvlder.formtools.processsor.FieldPropertyConverter.*;

public class FieldPropertyFactory {

    public static ResolvedFieldPropertyRow parseRow(FieldPropertyRow row) {
        return new ResolvedFieldPropertyRow(
                normalizeBlank(row.getFieldId()),
                normalizeBlank(row.getFieldName()),
                normalizeBlank(row.getDisplayName()),

                normalizeBlank(row.getParentId()),

                row.getActive(),
                row.getExpireDate(),

                parseConditionalBool(row.getFieldRepeated()),
                row.getMinOccurs(),
                row.getMaxOccurs(),
                normalizeList(row.getCondRepeatedSourceIds()),
                normalizeList(row.getCondRepeatedValues()),
                parseLinkLogic(row.getCondRepeatedLogic()),

                parseUsage(row.getRequired()),
                normalizeList(row.getCondRequiredSourceIds()),
                normalizeList(row.getCondRequiredValues()),
                parseLinkLogic(row.getCondRequiredLogic()),

                normalizeBlank(row.getCodeSystemName()),
                normalizeBlank(row.getCodeSystemId()),
                normalizeBlank(row.getCodeSystemVersion()),
                normalizeBlank(row.getCodeSystemCode()),

                parseDataType(row.getDataType()),
                normalizeBlank(row.getRangeStart()),
                normalizeBlank(row.getRangeEnd()),
                row.getRangeStartInclusive(),
                row.getRangeEndInclusive(),
                row.getMinLength(),
                row.getMaxLength(),
                normalizeList(row.getEnumerations()),
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


    // Note that any updates to defaults must also update ValidateFormExtract
    public static ResolvedFieldPropertyRow applyDefaults(ResolvedFieldPropertyRow row) {
        Boolean active = row.active() != null ? row.active() : true;
        ConditionalBool fieldRepeated = row.fieldRepeated() != null ? row.fieldRepeated() : ConditionalBool.FALSE;
        Usage required = row.required() != null ? row.required() : Usage.O;

        // Deliberately NOT derived from required. Usage is the sole authority on required-presence
        // (validateRequiredFields), including the conditional CR/CE cases that a static minOccurs
        // cannot express. minOccurs enforces only a count the form author declared outright, so the
        // two validation stages stay disjoint and neither relies on the other to catch a missing field.
        Integer minOccurs = row.minOccurs() != null ? row.minOccurs() : 0;

        Integer maxOccurs = row.maxOccurs() != null ? row.maxOccurs()
                : (fieldRepeated == ConditionalBool.FALSE ? 1 : null);

        Boolean rangeStartInclusive = (row.rangeStartInclusive() != null || row.rangeStart() == null)
                ? row.rangeStartInclusive() : Boolean.TRUE;
        Boolean rangeEndInclusive = (row.rangeEndInclusive() != null || row.rangeEnd() == null)
                ? row.rangeEndInclusive() : Boolean.TRUE;

        return new ResolvedFieldPropertyRow(
                row.fieldId(),
                row.fieldName(),
                row.displayName(),

                row.parentId(),

                active,
                row.expireDate(),

                fieldRepeated,
                minOccurs,
                maxOccurs,
                row.condRepeatedSourceIds(),
                row.condRepeatedValues(),
                row.condRepeatedLogic(),

                required,
                row.condRequiredSourceIds(),
                row.condRequiredValues(),
                row.condRequiredLogic(),

                row.codeSystemName(),
                row.codeSystemId(),
                row.codeSystemVersion(),
                row.codeSystemCode(),

                row.dataType(),
                row.rangeStart(),
                row.rangeEnd(),
                rangeStartInclusive,
                rangeEndInclusive,
                row.minLength(),
                row.maxLength(),
                row.enumerations(),
                row.regexPattern(),

                row.isAttribute(),
                row.attributeName(),

                row.uiControl(),
                row.unit(),
                row.displayOrder(),
                row.validationMessage(),

                row.skipImport(),
                row.groupingType() != null ? row.groupingType() : GroupingType.NONE,
                row.componentOf()
        );
    }

    public static FieldProperty buildFieldProperty(FieldPropertyRow row) {
        return buildFieldProperty(parseRow(row));
    }

    public static ResolvedFieldPropertyRow applyOverrides(ResolvedFieldPropertyRow row) {
        ProcessingOverrides overrides = ProcessingOverrides.getInstance();
        String displayName = overrides.getDisplayNameOverride(row.fieldId());
        Boolean active = overrides.getActiveOverride(row.fieldId());
        Boolean skipImport = overrides.getSkipImportOverride(row.fieldId());

        if (displayName == null && active == null && skipImport == null) {
            return row;
        }

        return new ResolvedFieldPropertyRow(
                row.fieldId(),
                row.fieldName(),
                displayName != null ? displayName : row.displayName(),

                row.parentId(),

                active != null ? active : row.active(),
                row.expireDate(),

                row.fieldRepeated(),
                row.minOccurs(),
                row.maxOccurs(),
                row.condRepeatedSourceIds(),
                row.condRepeatedValues(),
                row.condRepeatedLogic(),

                row.required(),
                row.condRequiredSourceIds(),
                row.condRequiredValues(),
                row.condRequiredLogic(),

                row.codeSystemName(),
                row.codeSystemId(),
                row.codeSystemVersion(),
                row.codeSystemCode(),

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
                row.attributeName(),

                row.uiControl(),
                row.unit(),
                row.displayOrder(),
                row.validationMessage(),

                skipImport != null ? skipImport : row.skipImport(),
                row.groupingType(),
                row.componentOf()
        );
    }

    public static FieldProperty buildFieldProperty(ResolvedFieldPropertyRow row) {
        ResolvedFieldPropertyRow resolved = applyOverrides(applyDefaults(row));

        FieldIdentity identity = buildIdentity(resolved);
        FieldRelation relation = buildRelation(resolved);
        FieldCardinality cardinality = buildCardinality(resolved);
        FieldValueRestriction restriction = buildRestriction(resolved);
        FieldCodeSpec codeSpec = buildCodeSpec(resolved);
        FieldMetadataUI metadataUI = buildMetadataUI(resolved);

        return new FieldProperty(
                identity,
                relation,
                cardinality,
                restriction,
                codeSpec,
                metadataUI
        );
    }

    public static FieldIdentity buildIdentity(ResolvedFieldPropertyRow row) {
        return new FieldIdentity(
                row.fieldId(),
                row.fieldName(),
                row.displayName(),
                row.active(),
                row.expireDate(),
                row.skipImport()
        );
    }

    public static FieldRelation buildRelation(ResolvedFieldPropertyRow row) {
        return new FieldRelation(
                row.parentId(),
                row.groupingType(),
                row.componentOf()
        );
    }

    public static FieldCardinality buildCardinality(ResolvedFieldPropertyRow row) {
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

    public static FieldValueRestriction buildRestriction(ResolvedFieldPropertyRow row) {
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

    public static FieldCodeSpec buildCodeSpec(ResolvedFieldPropertyRow row) {
        return new FieldCodeSpec(
                row.codeSystemName(),
                row.codeSystemId(),
                row.codeSystemVersion(),
                row.codeSystemCode()
        );
    }

    public static FieldMetadataUI buildMetadataUI(ResolvedFieldPropertyRow row) {
        return new FieldMetadataUI(
                row.uiControl(),
                row.unit(),
                row.displayOrder(),
                row.validationMessage()
        );
    }
}