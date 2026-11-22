# HL7 C-CDA Allergy Observation example

Exercises `CODED_VALUE` (see `ImportSpec.txt`'s CODED VALUE section) against a real, publicly
published HL7 document, rather than invented data.

## Source and attribution

[`condensedHl7AllergyObservation.xml`](condensedHl7AllergyObservation.xml) is a trimmed excerpt of the
Allergy Intolerance Observation entry from HL7's own public C-CDA 2.1 sample document:

- Repository: [HL7/CDA-ccda-2.1](https://github.com/HL7/CDA-ccda-2.1) (`examples/C-CDA_R2-1_CCD.xml`)
- The source file's own header credits Lantana Consulting Group, LLC and the C-CDA R2.1 editing team,
  and states the document depicts a fictional patient for illustration purposes.

Every element and attribute kept in the excerpt is unmodified real content (`classCode`/`moodCode`,
the `ASSERTION`/`completed` codes, the real SNOMED CT value `419199007` "Allergy to substance", and
the real RxNorm substance code `70618` "Penicillin"). Only unrelated siblings were trimmed
(`templateId`, `id`, `text`/`reference`, `effectiveTime`, `author`, and the nested reaction/severity
`entryRelationship`) to keep this a focused engine test rather than the whole CCD document — this
project's engine currently matches by element name within a parent, not by a nested `templateId`, so
testing against the full, un-trimmed CCD document isn't yet practical (see Phase 3/roadmap notes on
`CodeSystem`/`FieldCodeSpec`).

## Files

- [`field_property_template_hl7_allergy.csv`](field_property_template_hl7_allergy.csv) — the CSV
  schema, scoped to just this one observation (anchor = `fragment`, a wrapper this example adds since
  the real document's actual top-level element is `ClinicalDocument`, not modeled here).
- [`condensedHl7AllergyObservation.xml`](condensedHl7AllergyObservation.xml) — the real excerpt above,
  wrapped in that same `fragment` element with the document's real default namespace
  (`xmlns="urn:hl7-org:v3"`) carried over.
- [`sampleCodeSystem_2.16.840.1.113883.6.96.json`](sampleCodeSystem_2.16.840.1.113883.6.96.json) — a
  tiny, real-content Code System (just the one SNOMED code this fixture uses), in the same JSON shape
  the CLI's "Code Systems" feature reads/writes. Not wired in by default (`codesystems/` is gitignored,
  user-local data) — see below to try it.

## Running it

```
specFilePath=src/main/resources/examples/hl7CcdaAllergy/field_property_template_hl7_allergy.csv
xmlFilePath=src/main/resources/examples/hl7CcdaAllergy/condensedHl7AllergyObservation.xml
rootId=0
skipXsdValidation=true
```

**Validate schema only** and **Review** both pass cleanly as shipped. The SNOMED `value` field is
`CODED_VALUE` in open mode (identity + presence only, no `codeSystemEnforceValues`) — real external
vocabularies like SNOMED CT are far too large to enumerate in a CSV. The RxNorm substance `code` field
instead uses `codeSystemCode=70618`, a schema-declared literal constraint that needs no import at all.

To see full membership enforcement against an imported Code System instead: copy
`sampleCodeSystem_2.16.840.1.113883.6.96.json` to `codesystems/2.16.840.1.113883.6.96.json` in your
working directory (or use the CLI's Code Systems menu to re-enter it), then set `codeSystemEnforceValues=T`
on the schema's `value` row (fieldId `4`) and re-run Review — it validates cleanly against the one real
code in that file, and fails with a clear message if you change the document's value to a different
SNOMED code.
