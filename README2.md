# xml-auto-validator
## Glossary
The terminology used  by this project may differ from established norms. The terms used throughout the project are 
listed here.\
\
**Form** - A structured document of collected information that may repeat.\
\
**Form Instance** - One complete, distinct occurrence of form information within the document. If one document contains 
seven completed occurrences of a form, each of those seven occurrences is a separate form instance.
>e.g., If seven job applications have been exported as an XML document, then each application is a separate instance of
a single form. Each applicant has completed the form differently, and the applications are independent of each other.
> 
**Element** - An XML tag representing a structured component of the document. Elements may contain attributes, text 
values, or child elements.\
\
**Field** - The parsed representation of a form element for a form instance. If an element is repeated in a form 
multiple times, then each repetition receives its own field. Fields may hold data or act as containers for other fields. 
See [Field Types](#field-types) for more information.\
\
**Field Property** - A template for how the parser should process and validate form field information. Field properties 
are currently imported via a CSV import spec. See [Field Property Information](#field-property-information) for more 
details.\
\
**Anchor Element** - The top-level element of the structured document. It is not required that the start of the 
structured document correspond with the beginning of a form instance. The anchor element ***CANNOT*** repeat. It is 
expected that the first element in the document is the anchor element.\
\
**Form Root** - The element that defines the beginning of a form instance. This element ***CAN*** repeat within the 
document, but ***CANNOT*** repeat *within* the form.
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
**Hierarchy** - as the document is structured, it is expected to be in a nested format. This is typically represented by 
visually by indentation, although the parser ignores that type of white space. A **parent** has **children**. A parent 
can have multiple children, but a child can only have a single parent. Both elements and fields are expected to have a 
parent/child relationship. The only element not expected to have a parent is the anchor element. A circular hierarchy is 
not permitted.


<a name="field-type-definitions"></a>
### Field Types
**Simple Field** - Typically a one-to-one mapping between an element and a field. A simple field has a value.\
\
**Composite Field** - Typically a many-to-one mapping between elements and a field. The values from multiple elements
are stored on a field. In the example below, a composite field for a person's name could be made up of both the given
and family elements.
```
<name>
    <given></given>
    <family></family>
</name>
```
**Constituent** - The individual pieces of a composite field. In the composite field example, given and family are the
constituents for a name composite field.\
\
**Container Field** - A field that stores no value, but is important for the hierarchy of the form. In the composite
field example, if given and family were instead imported as simple fields, name would exist as a container field to
reference the two simple fields. The two simple fields would be children of the container field.

<a name="field-property-information"></a>
### Field Property Information
Field properties are primarily populated through the import spec CSV.

- As mappings are based on column names, the column names from the CSV will be used within this section (fieldId instead 
of Field ID).
- The data type and whether the column is required will appear after the column name in parentheses.
- The listed data type is what Java will convert the specified column value to and is only associated with the field 
property. It is not associated with the final value of the field. *Note that Integer may be stored as int.*
- A required column is listed as 'Required', while an optional column is listed as 'Optional'.
- Any column that is not required can be removed from the CSV being imported without issue.

*fieldId* (String, Required) - A unique ID corresponding to an element in the structured document. Every element must 
have an ID, even if it is not a part of the form. If the form is associated with a specification, the ID provided by 
that specification should be used. The ID ***SHOULD NOT*** contain commas.\
*fieldName* (String, Required) - The name of an element as it appears within the structured document.\
*displayName* (String, Optional) - A longer, more descriptive name for the field. Display name will be used by the 
parser as the name for any parsed field. Defaults to fieldName if left blank.\
*displayOrder* (Integer, Optional) - The order that fields should appear in. Will be respected if possible. Order should
be ascending from the anchor element, with children having a higher value than their parents. Currently not used by the 
utility after parsing.\
*parentId* (String, Required) - The fieldId of this field's direct parent. This may only be left blank for the anchor 
element.\
*hierarchyLevel* (Integer, Optional) - The expected hierarchy level for this element in the form. The anchor element 
should have a hierarchy level of 0. Currently not used by the utility after parsing.\
*active* (Boolean, Optional) - Whether this field is in use. Allows for specifications to version their import specs and 
deprecate fields, or to prepare for new fields. Defaults to active if left blank.\
*expireDate* (LocalDate, Required if "active" == False) - The last date when this field should be considered active. 
Required if active is set to False. Currently only used to validate that the active flag is accurate at time of 
parsing.\
*fieldRepeated* (Character, Optional) - Determines if the element is allowed to appear more than once. If a block of 
elements should be repeated, the container for those elements should indicate that repetition is allowed. In the example 
below, the person element would be considered repeating, but name, given, and family would not. If name was set to 
repeat, each person would be allowed to have multiple names (which likely isn't what you'd expect). Allows T, F, and C, 
for True, False, and Conditionally Recurring. Defaults to F if left blank.
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
*minOccurs* (Integer, Optional) - The minimum number of times the field must appear. Only referenced if fieldRepeated is 
set to something other than F. Defaults to 1 if left blank.\
*maxOccurs* (Integer, Optional) - The maximum number of times the field may appear. Only referenced if fieldRepeated is 
set to something other than F. Defaults to 1 if left blank.\
*condRepeatedSourceIds* (String[], Required if "fieldRepeated" == C) - Indicates the field IDs of the source fields that 
should be referenced to determine if this field is conditionally repeated. Expects a comma-delimited list of fieldId 
values. Ignored if fieldRepeated set to 'F'.\
*condRepeatedValues* (List<String[]>, Required if "fieldRepeated" == C) - Which values in a source field determine that 
this field is necessary. If more than one source field, each value set should be piped-delimited and encased in curly 
braces. Only referenced if fieldRepeated set to 'C'.
- If any value in the source field would cause this field to be repeated, enter "**ANY"
- If the source field is numeric, a requirement threshold can be indicated via <, >, <=, >= followed by a numeric value, 
such as {<4}.
- An inclusive range can be indicated by a hyphen separating two numeric values. Inclusivity can be indicated by the 
presence of greater-than or less-than symbols. The condition {>2-5} would indicate values of 2.01 up to and including 5 
would conditionally repeat the field being evaluated.
- To specify any value except for the value indicated, use **!. Any value for the source field other than the specified 
value will trigger this field to be conditionally repeated.
- If the source field had specified enumerations, numeric values will correspond to the enumeration at the specified 
position, 0-indexed.


*condRepeatedLogic* (Enum [AND, OR], Required if condRepeatedSourceIds indicates more than one field and those 
fields can be present at the same time) - logic to connect multiple conditional requirements. Only referenced if 
fieldRepeated set to 'C'.\
*required* (Character, Required) - Allows T, F, and C, for True, False, and Conditionally Required. Defaults to F if 
left blank. If a field is marked as required, 'T', it will only trigger an error if the parent it belongs to is 
populated and the field is not present, or present and empty. If a parent element is listed as required,
the parent element must appear but each of its children will need to be marked individually.\
This makes it possible to have a field such as "startDate" be required if its parent element "job" is populated, but if
there is no job to provide information for, then there would be no need to provide a start date. Similarly, "job" could 
be marked as a required element so that it would appear in the form, and "startDate" could be set to conditionally 
required based on whether "job" was populated.\
*condRequiredSourceIds* (String[], Required if "required" == C) - Indicates the fieldIds of the source elements that 
should be referenced to determine if this field is conditionally required. Field Ids should be comma-delimited.\
*condRequiredValues* (List<String[]>, Required if "required" == C) - Which values in a source field determine that this 
field is necessary. If more than one source field, each value set should be piped-delimited and encased in curly braces.
- If any value in the source field would cause this field to be required, enter **ANY
- If the source field is numeric, a requirement threshold can be indicated via <, >, <=, >= followed by a numeric value, 
such as {<4}.
- An inclusive range can be indicated by a hyphen separating two numeric values. Inclusivity can be indicated by the 
presence of greater-than or less-than symbols. The condition {>2-5} would indicate values of 2.01 up to and including 
5 would conditionally require the field being evaluated.
- To specify any value except for the value indicated, use **!. Any value for the source field other than the 
specified value will trigger this field to be conditionally required.
- If the source field had specified enumerations, numeric values will correspond to the enumeration at the specified 
position, 0-indexed.

*condRequiredLogic* (Enum [AND, OR], Required if condRequiredSourceIds indicates more than one field and those fields 
can be present at the same time.) - logic to connect multiple conditional requirements. Only referenced if fieldRequired
set to 'C'.\
*codeSystemName* (String, Optional) - The name of the code system as it would appear in the structured document. If 
populated, the name that should be stored to metadata.\
*codeSystemId* (String, Optional) - If the value type corresponds with a specific code system, provide the code 
system's ID.\
*codeSystemVersion* (String, Optional) - The version of the code system to expect.\
*codeSystemCode* (String, Optional) - If the fieldId does not correspond with a corresponding code in the code system, 
provide the field's code.\

*dataType* (String, Required) - Data type of field. Supports label, instance identifier, boolean, date, time, string, 
character, coded value, custom enumeration, integer, double, telephone, email, duration, decimal, all the 
numeric data types.\
*rangeStart* (Double, Optional) - If value should be limited to a range, start of that value range.\
*rangeEnd* (Double, Optional) - If value should be limited to a range, end of that value range.\
*rangeInclusive* (Boolean, Required if rangeStart or rangeEnd) - Are the range values provided inclusive or exclusive? 
T for inclusive, F for exclusive.\
*minLength* (Integer, Optional) - If string field, minimal length of field, inclusive.\
*maxLength* (Integer, Optional) - If string field, maximum length of field, inclusive.\
*enumeration* (String[], Optional) - If only specific values should be accepted for the field, list those values 
separated by |.\
*unit* (String, Optional) - The unit corresponding to the field, used for display.\
*regexPattern* (String, Optional) - If the field should match a pattern, provide that pattern.\


*isAttribute* (Boolean, Optional) - If the value for the field is retrieved from an element's attribute rather than as 
a text or element value.\
*attributeName* (String, Required if "isAttribute" == T) - Required if the value is found in an attribute tag, list 
the name of the appropriate tag.\

*uiControl* (String, Optional) - Helper for downstream user for how to render data.\
*validationMessage* (String, Optional) - A custom error message to log if this field fails to be parsed successfully, 
in addition to any system errors logged.\
*skipImport* (Boolean, Optional) - Indicates that the field should be skipped during import, for example if the field 
corresponds to user instructions not part of the form. This differs from an inactive field in that the skipped field's 
relationships will be bridged (the parent of this field will receive this record's children and vice versa). Providing 
active children to a skipped field may lead to an unintended hierarchy. Not recommended for use with label fields.\





### Self Notes
- Consider changing fieldId to elementId.
- Consider removing displayOrder, or specifying that it is provided for UI to implement downstream.
- Consider changing fieldName to elementName.
- Consider removing hierarchy level or specifying that it is provided for UI to implement downstream. Should root be 0
or anchor?
- Handle fields that are active but have an expiration date set.
- Confirm default of fieldRepeated is F, and min/max is 1 if blank.
- Add full list of dataTypes supported.
- Add grouping type