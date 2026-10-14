# xml-auto-validator

A validation engine for structured XML documents whose shape, required fields, and business rules are
defined in a CSV — not in compiled Java classes or a hand-authored XSD. Edit the CSV and the engine's
understanding of the document changes with it, no recompile needed.

Point it at a CSV schema and an XML document, and it checks: which elements are required (and whether
"required" means merely present or actually populated), conditional requirements based on other
fields' values, numeric/date/time ranges and relative date offsets, enumerated value sets, ISO
durations, and composite values assembled from several elements — then reports every problem it can
find in one pass, rather than stopping at the first one. An XSD can still be layered in alongside this
for structural validation; the two are complementary, not a choice between them.

The conditional-requirement and value-restriction vocabulary draws conceptual inspiration from HL7 v2
conformance methodology (presence vs. populated-ness as separate axes, for example), expressed in this
project's own terms — it does not implement or claim conformance to any HL7 specification.

There's no fixed output format yet; see [ROADMAP.md](src/main/resources/Documentation/ROADMAP.md) for what's planned and what's
currently in progress. A JSON export of a validated document already works via the CLI's commit step.

## Getting started

Requires JDK 21+ (the Gradle wrapper pins the toolchain, so your system JDK doesn't need to match
exactly).

```bash
./gradlew build
./gradlew run
```

`./gradlew run` launches an interactive CLI. On first run it has no file paths configured yet — use
the **Edit settings** menu to point `specFilePath` at your CSV schema and `xmlFilePath` at the document
to validate, then **Apply and save** so those choices persist to `xml-auto-validator.properties` (see
[xml-auto-validator.properties.example](xml-auto-validator.properties.example) for every available
setting, documented inline). From the main menu:

- **Validate schema only** checks just the CSV for internal consistency, without touching an XML
  document at all — useful while you're still authoring the schema.
- **Review** runs the full pipeline and holds the result in memory without finalizing anything, so you
  can look at every error and warning first.
- **Commit** writes the reviewed form out (currently as JSON); declining to overwrite an existing file,
  or a write failure, leaves the form held so you can retry rather than losing the review.

## Schema reference

The full CSV column-by-column reference — every column's type, default, and validation rules — lives
in [ImportSpec.txt](src/main/resources/Documentation/ImportSpec.txt). It's kept in sync with the
importer's actual behavior as the source of truth for the CSV format, including the condition-token
grammar (`**ANY`, `**ALL`, `**!`, ranges, relative date offsets) used by conditional-requirement and
conditional-repetition columns. This README doesn't duplicate it; what follows is the conceptual model
the format is built on.

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
properties are currently imported via a CSV import specification. See [Schema reference](#schema-reference) above for
the full column reference.

#### Anchor Element
The **anchor element** is the top-level element of the structured document — identified by having no
`parentId` in the CSV schema, not by any special name. It is not required that the start of the
structured document correspond with the beginning of a form instance. The anchor element ***CANNOT*** repeat. It is 
expected that the first element in the document is the anchor element.

#### Form Root
The **form root** is the element that defines the beginning of a form instance, configured via the
`rootId` setting (see [xml-auto-validator.properties.example](xml-auto-validator.properties.example)).
This element ***CAN*** repeat within the document, but ***CANNOT*** repeat *within* the form. It may be the anchor element as long as there is only one instance 
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

## Project status and further reading

This project is under active development; development notes and open design questions that don't fit
the schema reference live in [TODO.md](TODO.md). [ROADMAP.md](src/main/resources/Documentation/ROADMAP.md) tracks the current phase of
work and what's planned next.

## License

[MIT](LICENSE)
