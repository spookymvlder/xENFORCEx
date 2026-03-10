# xml-auto-validator
* * * 
# Glossary
The terminology used by this project may differ from established norms. The terms used throughout the project are 
listed here.

#### Form
A **form** is a structured document of collected information that may repeat.

#### From Instance
A **form instance** is one complete, distinct occurrence of form information within the document. If one document 
contains seven completed occurrences of a form, each of those seven occurrences is a separate form instance.
>e.g., If seven job applications have been exported as an XML document, then each application is a separate instance of
a single form. Each applicant has completed the form differently, and the applications are independent of each other.
> 

#### Element
An **element** is an XML tag representing a structured component of the document. Elements may contain attributes, text 
values, or child elements.

#### Field
A **field** is the parsed representation of a form element for a form instance. If an element is repeated in a form 
multiple times, then each repetition receives its own field. Fields may hold data or act as containers for other fields.
See [Field Types](#field-types) for more information.
#### Field Property
A **field property** is a template that defines how the parser should process and validate form field information. Field 
properties are currently imported via a CSV import specification. See [Field Property Information](#field-property-information) for more 
details.

#### Anchor Element
The **anchor element** is the top-level element of the structured document. It is not required that the start of the 
structured document correspond with the beginning of a form instance. The anchor element ***CANNOT*** repeat. It is 
expected that the first element in the document is the anchor element.

#### Form Root
The **form root** is the element that defines the beginning of a form instance. This element ***CAN*** repeat within the 
document, but ***CANNOT*** repeat *within* the form. It may be the anchor element as long as there is only one instance 
of the form in the parsed document.
```
<hrExport>                                  //This is the anchor element. It is shared by each instance of the form.
    <application>                           //This is the form root and indicates the beginning of a new form instance.
        <personalInfo></personalInfo>
        <jobHistory></jobHistory>
    </application>
    <application>
        <personalInfo></personalInfo>
        <jobHistory></jobHistory>
    </application>
</hrExport>
```

#### Hierarchy
**Hierarchy** is the document's structure, which is expected to be in a nested format. This is typically represented 
visually by indentation, although the parser ignores that type of white space.\
\
A **parent** has **children**. A parent can have multiple children, but a child can only have a single parent.\
\
Fields will always have a parent except for the form root. Elements follow XML rules, so only the anchor element is 
expected to not have a parent. The only element not expected to have a parent is the anchor element.\
\
A circular hierarchy is not permitted.

* * * 
<a name="field-type-definitions"></a>
## Field Types

#### Simple Field
A **simple field** typically represents a one-to-one mapping between an element and a field. A simple field has a single 
value.

#### Composite Field
A **composite field** represents a many-to-one mapping between elements and a field. The values from multiple elements
are stored on a field.\
\
In the example below, a composite field for a person's name could be made up of both the `given`
and `family` elements.
```
<name>
    <given></given>
    <family></family>
</name>
```

#### Constituent
A **constituent** is an individual element that makes up part of a composite field.\
\
In the composite field example, `given` and `family` are the constituents for a `name` composite field.

#### Container Field
A **container field** stores no value, but maintains the hierarchy of the form.\
\
In the composite field example, if `given` and `family` were instead imported as simple fields, `name` would exist as a 
container field to reference the two simple fields. The two simple fields would be children of the container field.

* * * 

<a name="field-property-information"></a>
## Field Property Information
Field properties are primarily populated through the import specification CSV.

- Column names referenced here match the CSV column names.
- The data type listed in the definition refers to the Java type used to store the field property.
- Each field property will have a data type column, which defines how the parsed field value is stored.
- Required columns must appear in the CSV.
- Optional columns may be omitted.
- Default behavior for blank column values is listed when relevant.
- The data type and whether the column is required will appear after the column name in parentheses.
- A required column not being present will prevent the import from processing.
- Unless otherwise noted, a missing value for a required column will cause an error to be logged.

> [!NOTE]\
> The field property data type may be boxed or primitive.
* * * 

### Field Identifier Columns

#### fieldId
(String, Required)\
A unique identifier corresponding to an element in the structured document. Every element must 
have an ID, even if it is not a part of the form as a field.\
\
If the form is associated with a specification, the ID provided by that specification should be used.

>[!IMPORTANT]\
> The ID ***SHOULD NOT*** contain commas.

#### fieldName
(String, Required)\
The name of an element as it appears within the structured document.\
\
Duplicate names are allowed as long as the `fieldId` is unique.

#### displayName
(String, Optional)\
A longer, more descriptive name for the field. Display name is used for the name of the parsed field.\
\
Defaults to `fieldName`.
* * * 

### Field Hierarchy Columns
> [!NOTE]\
> `hierarchyLevel` is included in the import specification as it makes reviewing the actual CSV file easier. The 
> column itself is not used by the parser.


#### parentId
(String, Required)\
The `fieldId` of this field's direct parent.\
\
This may only be left blank for the anchor element.

#### hierarchyLevel
(Integer, Optional)\
The expected hierarchy level for this element in the form. The anchor element 
should have a hierarchy level of 0.\
\
Currently not used by the parser.

* * * 
### Versioning Columns

#### active
(Boolean, Optional)\
Whether this field is in use. Allows for specifications to version their import specifications.\
\
Defaults to `T`.

#### expireDate
(LocalDate, Required if `active == F`)\
The last date when this field should be considered active.\
\
Currently only used to validate that the `active` flag is accurate at time of parsing.
* * * 

### Cardinality Columns

#### fieldRepeated
(Character, Optional)\
Determines if the element is allowed to appear more than once. \
\
Allowed values:
>T → repeating\
>F → non-repeating\
>C → conditionally repeating
>
Defaults to `F`.\
\
If a block of elements should be repeated, the container for those elements should indicate that repetition is allowed.\
\
In the example below, the `person` element would be considered repeating.
```
<person>
    <name>
        <given></given>
        <family></family>
    </name>
</person>
<person>
    <name>
        <given></given>
        <family></family>
    </name>
</person>
```

#### minOccurs
(Integer, Optional)\
The minimum number of times the field must appear.\
\
Only referenced if `fieldRepeated != F`.\
\
Defaults to `1`.

#### maxOccurs
(Integer, Optional)\
The maximum number of times the field may appear.\
\
Only referenced if `fieldRepeated != F`.\
\
Defaults to `1`.

#### condRepeatedSourceIds
(String[], Required if `fieldRepeated == C`)\
Comma-delimited list of other fields that if present, determine that this field must be repeated.\
\
Fields should be referenced by `fieldId`.\
\
This column is ignored when the condition requiring it is not met.

#### condRepeatedValues
(List<String[]>, Required if `fieldRepeated == C`)\
The triggering value set in `condRepeatedSourceIds` that indicate repetition should occur.\
\
If more than one source field, each value set should be piped-delimited and encased in curly braces. 
- If any value in the source field would cause this field to be repeated, enter `**ANY`
- If the source field is numeric, a requirement threshold can be indicated via `<`, `>`, `<=`, or `>=` followed by a 
numeric value, such as `{<4}`.
- An inclusive range can be indicated by a hyphen separating two numeric values. Inclusivity can be indicated by the 
presence of greater-than or less-than symbols. The condition `{>2-5}` would indicate values of 2.01 up to and including 5 
would conditionally repeat the field being evaluated.
- To specify any value except for the value indicated, use `**!`.
- If the source field had specified enumerations, numeric values will correspond to the enumeration at the specified 
position, 0-indexed.

This column is ignored when the condition requiring it is not met.

#### condRepeatedLogic 
(Enum [AND, OR], Required if `condRepeatedSourceIds` indicates more than one field and those fields can be populated at 
the same time)\
Logic to determine how multiple source fields are evaluated.\
\
This column is ignored when the condition requiring it is not met.
* * * 

### Required Field Columns

#### required
(Character, Required)\
If the field is required.\
\
Allowed values:
>T → repeating\
>F → non-repeating\
>C → conditionally repeating
>
A required field triggers an error only if its parent exists.
If a field is marked as required, 'T', it will only trigger an error if the parent it belongs to is 
populated and the field is not present, or present and empty. 
\
Defaults to `F`.

>If a parent element is listed as required, the parent element must appear but each of its children will need to have 
> their behavior individually defined.
>
> This makes it possible to have a field such as `startDate` be required if its parent element `job` is populated, but 
> if there is no job to provide information for, then there would be no need to provide a start date.
> 
> Similarly, `job` could be marked as a required element so that it would appear in the form, and `startDate` could be 
> set to conditionally required based on whether `job` was populated.


#### condRequiredSourceIds 
(String[], Required if `required == C`)\
Indicates the `fieldId` of the source elements that should be referenced to determine if this field is conditionally 
required.\
\
Field Ids should be comma-delimited.

#### condRequiredValues
(List<String[]>, Required if `required == C`)\
The triggering value set in `condRequiredSourceIds` that indicates whether the field is required.\
\
If more than one source field, each value set should be piped-delimited and encased in curly braces.
- If any value in the source field would cause this field to be required, enter `**ANY`.
- If the source field is numeric, a requirement threshold can be indicated via `<`, `>`, `<=`, or `>=` followed by a 
numeric value, such as `{<4}`.
- An inclusive range can be indicated by a hyphen separating two numeric values. Inclusivity can be indicated by the 
presence of greater-than or less-than symbols. The condition `{>2-5}` would indicate values of 2.01 up to and including 
5 would conditionally require the field being evaluated.
- To specify any value except for the value indicated, use `**!`.
- If the source field had specified enumerations, numeric values will correspond to the enumeration at the specified 
position, 0-indexed.

This column is ignored when the condition requiring it is not met.

#### condRequiredLogic
(Enum [AND, OR], Required if `condRequiredSourceIds` indicates more than one field and those fields 
can be present at the same time.)\
Logic to determine how multiple source fields are evaluated.\
\
This column is ignored when the condition requiring it is not met.

* * * 
### Code System Metadata Columns
Values for these columns are ignored when `codeSystemId` is blank.

#### codeSystemName
(String, Optional)\
The name of the code system as it would appear in the structured document. If populated, the name that should be stored 
to metadata.

#### codeSystemId
(String, Optional)\
If the value type corresponds with a specific code system, provide the code system's ID.

#### codeSystemVersion
(String, Optional)\
The version of the code system to expect.

#### codeSystemCode
(String, Optional)\
If the `fieldId` does not correspond with the corresponding code in the code system, provide the field's code.
* * * 

### Field Value Restriction Columns
For columns such as `rangeStart` and `rangeEnd` that are obviously paired, the behavior is as follows:
- When both columns are empty, no restrictions are set for the value.
- When only one column is empty, the populated column acts as a one-sided boundary on the data.

#### dataType
(String, Required if `groupingType != (COMPOSITE or CONTAINER`))\
Data type of the field's value. If there is a boxed Java data type, that is the data type that will be used.\
\
Many data types may simply be stored as Strings with validation rules applied.\
\
Currently supported data types:
> string\
> integer\
> decimal\
> double\
> float\
> boolean\
> date\
> time\
> datetime\
> character\
> long

Planned support for:
> duration\
> anyuri\
> idref\
> entity\
> id\
> gyearmonth\
> gmonthday\
> gday\
> gmonth\
> gyear\
> coded_value\
> enumeration\
> email\
> telephone\
> url\
> postal_code\
> country_code\
> currency\
> percentage\
> custom\
> raw_xml



#### rangeStart
(Double, Optional)\
If value should be limited to a range, start of that value range.

#### rangeEnd
(Double, Optional)\
If value should be limited to a range, end of that value range.

#### rangeStartInclusive
(Boolean, Optional)\
Determines if `rangeStart` is inclusive.\
\
Behavior:
> `T` -> inclusive\
> `F` -> exclusive
>
Defaults to `T`. Ignored when `rangeStart` is not populated.

#### rangeEndInclusive
(Boolean, Optional)\
Determines if `rangeEnd` is inclusive.\
\
Behavior:
> `T` -> inclusive\
> `F` -> exclusive
>
Defaults to `T`. Ignored when `rangeEnd` is not populated.

#### minLength
(Integer, Optional)\
If string field, minimal length of field, inclusive.

#### maxLength
(Integer, Optional)\
If string field, maximum length of field, inclusive.

#### enumeration
(String[], Optional)\
Pipe-delimited list of allowed values.

#### regexPattern
(String, Optional)\
If the field should match a pattern, provide that pattern.

* * *
### Attribute Information Columns

#### isAttribute
(Boolean, Optional)\
If the value for the field is retrieved from an element's attribute rather than element content.

#### attributeName
(String, Required if `isAttribute == T`)\
The name of the attribute tag to use for the field's value.
* * * 

### UI Metadata/Control Columns

#### uiControl
(String, Optional)\
Helper for downstream user for how to render data.

#### unit
(String, Optional)\
The unit corresponding to the field, used for display.

#### displayOrder
(Integer, Optional)\
The order that fields should appear in. Order should be ascending from the anchor element, with children having a higher
value than their parents.\
\
Included for the sake of downstream UI, but not this column's value is not used by the parser.


#### validationMessage
(String, Optional)\
A custom error message to log if this field fails to be parsed successfully, in addition to any system errors logged.
* * *

### Import Behavior Columns

#### skipImport
(Boolean, Optional)\
Indicates that the field should be skipped during import, for example if the field corresponds to user instructions not 
part of the form.\
\
This differs from an inactive field in that the skipped field's relationships will be bridged (the parent of this field 
will receive this record's children and vice versa). 
> [!WARNING]\
> Skip import should not be used on fields whose children are active. This may lead be unexpected tree bridging.

#### groupingType
(Enum [NONE, COMPOSITE, CONSTITUENT, CONTAINER], Optional)\
Determines the form of the parsed field.\
\
Behavior:
> `NONE` -> a simple field is created\
> `COMPOSITE` -> composite field\
> `CONSTITUENT` -> contributes its value to a composite field\
> `CONTAINTER` -> structural field with no value, may have children\

Defaults to `None`.

#### componentOf
(String, Required if `groupingType == CONSTITUENT`)

* * *
## System Behavior
The anchor element is determined by an empty parentId.\
\
The form root is set in system configuration.\



* * *
### Self Notes
- Consider removing displayOrder, or specifying that it is provided for UI to implement downstream.
- Consider removing hierarchy level or specifying that it is provided for UI to implement downstream.
- Handle fields that are active but have an expiration date set.
- Confirm default of fieldRepeated is F, and min/max is 1 if blank.
- Add grouping type
- Enter expected input format for Boolean
- Add expanded expireDate functionality to project plans.
- Ensure root data type no longer referenced
- Add details on how root derived/anchor element derived.
- Update range properties