# HL7 C-CDA Allergies Section example

Exercises `CODED_VALUE` (see `ImportSpec.txt`'s CODED VALUE section) against a real, publicly
published HL7 document, rather than invented data — and specifically the **whole Allergies and
Intolerances Section**, not a single isolated field. This matters: a schema that only validates one
cherry-picked element doesn't say much about whether the engine scales to a real clinical document's
actual nesting and repetition. This one does: two complete, repeating allergy entries, each with its
full real Allergy Concern Act → Allergy Intolerance Observation → Reaction Observation → Severity
Observation chain.

## Source and attribution

[`condensedHl7AllergiesSection.xml`](condensedHl7AllergiesSection.xml) is a trimmed excerpt of the
entire Allergies and Intolerances Section from HL7's own public C-CDA 2.1 sample document:

- Repository: [HL7/CDA-ccda-2.1](https://github.com/HL7/CDA-ccda-2.1) (`examples/C-CDA_R2-1_CCD.xml`)
- The source file's own header credits Lantana Consulting Group, LLC and the C-CDA R2.1 editing team,
  and states the document depicts a fictional patient for illustration purposes.

Every code, OID, UUID, and `nullFlavor` use kept below is real and unmodified, including both real
allergy entries (Penicillin → Nausea → Mild; Codeine → Wheezing → Moderate) with their genuine
differences (the second entry uses `nullFlavor="UNK"` where the first has a real date, and has no
`<high>` element at all where the first does). Trimmed: `templateId`, the extension-less duplicate
`id`/`templateId` lines, the human-readable `<text>` table, and `author`/`assignedAuthor` blocks — see
"Where the real limitations are" below for exactly why `templateId` and `author`'s nested `id`
(`root` *and* `extension` on one element) were dropped.

## Why there's an XSD and Schematron here too

A CSV schema that only checks itself against is making a claim nobody can verify. The base CDA XSD
leaves almost everything about an observation optional (see `reference/`'s XSD excerpt); the real
conformance rules — fixed codes, required cardinalities, nested structure — live entirely in HL7's
Schematron instead (`reference/`'s rules file). The CSV schema here maps onto those real rules for the
Allergy Intolerance Observation template specifically (one of the four templates this fixture covers):
of its real rules, all but the two bound to an external, dynamic value set (SNOMED CT / RxNorm — too
large to enumerate in a CSV) have a direct CSV equivalent. The Concern Act, Reaction Observation, and
Severity Observation templates each have their own Schematron pattern too, not transcribed here — this
comparison is scoped to the one template worth the detail, not padded out for coverage's sake. See
[`reference/schematron_rules_allergy_observation.md`](reference/schematron_rules_allergy_observation.md).

## Where the real limitations are

Three genuine engine limitations surfaced while building this, not three ways of saying the same
thing — each has a different practical consequence:

1. **One attribute per `FieldProperty`.** A real element like `<code code="ASSERTION"
   codeSystem="2.16.840.1.113883.5.4"/>` carries two attributes worth checking, but one schema row can
   only read one. The schema here picks the one that matters most per element (usually `code`) and
   leaves the rest as descriptive metadata (`codeSystemName`), same as the existing `driverHistory`
   example already works around. This is why `author`/`assignedAuthor` (whose `id` element needs both
   `root` *and* `extension` checked to be meaningful) was left out entirely, not because it's
   structurally harder to reach.
2. **No way to pick a schema row by a nested `templateId`.** The engine matches a child by element
   name within a parent position — it has no concept of "use *this* definition when the child's
   `templateId` is X, *that* one when it's Y." Within a single section's own vertical nesting this
   never bites: `observation` appears at three different depths here (allergy, reaction, severity),
   each under a *different* parent, so each gets its own schema row with no collision — proven by this
   fixture actually validating. It **would** bite trying to model the full CCD document at once,
   because `Problems`/`Medications`/`Allergies` (and every other section) are all generically
   `<component><section>` under the *same* parent (`structuredBody`), which the engine has no way to
   tell apart. That's the real boundary: one section's full depth is fine; several sibling sections
   sharing a parent, reusing the same tag name for different templates, is not — today.
3. **HL7's native timestamp format isn't ISO-8601.** A real CDA `effectiveTime/low` value looks like
   `199805011145-0800` (`yyyyMMddHHmm±zzzz`), not `yyyy-MM-ddTHH:mm:ss`. The engine's `DATE`/`DATETIME`
   types parse ISO-8601 only, so every `low`/`high` field here is modeled as `STRING`, not `DATE` —
   a deliberate, correct workaround given the real data, not an oversight. (A `regexPattern` could
   still check the shape, just not the ordered/range semantics `DATE` gives for free.)

None of these stopped this section from validating correctly — see "Running it" below — they just
mark where a straightforward field-by-field schema stops being straightforward.

## Files

- [`field_property_template_hl7_allergies_section.csv`](field_property_template_hl7_allergies_section.csv)
  — the CSV schema (36 rows), anchored at `section` itself (the real document's actual top-level
  element is `ClinicalDocument`, not modeled here). `entry` is `fieldRepeated=T`, covering both real
  allergy entries with one schema.
- [`condensedHl7AllergiesSection.xml`](condensedHl7AllergiesSection.xml) — the real excerpt above.
- [`sampleCodeSystem_2.16.840.1.113883.6.96.json`](sampleCodeSystem_2.16.840.1.113883.6.96.json) /
  [`sampleCodeSystem_2.16.840.1.113883.6.88.json`](sampleCodeSystem_2.16.840.1.113883.6.88.json) — real,
  small SNOMED CT / RxNorm Code Systems covering every code this fixture actually uses, in the JSON
  shape the CLI's "Code Systems" feature reads/writes. Not wired in by default (`codesystems/` is
  gitignored, user-local data) — see below to try it.
- [`reference/`](reference/) — the real, published alternative this example's CSV schema can be
  checked against. See
  [`reference/xsd_excerpt_POCD_MT000040_Observation.xsd`](reference/xsd_excerpt_POCD_MT000040_Observation.xsd)
  and
  [`reference/schematron_rules_allergy_observation.md`](reference/schematron_rules_allergy_observation.md).

## Running it

```
specFilePath=src/main/resources/examples/hl7CcdaAllergy/field_property_template_hl7_allergies_section.csv
xmlFilePath=src/main/resources/examples/hl7CcdaAllergy/condensedHl7AllergiesSection.xml
rootId=0
skipXsdValidation=true
```

**Validate schema only** and **Review** both pass cleanly as shipped, across both repeating entries.
The two `value` fields (allergy type, reaction) and the severity `value` field are `CODED_VALUE` in
open mode (identity + presence only — real external vocabularies like SNOMED CT are far too large to
enumerate in a CSV). The RxNorm substance `code` field (fieldId `21`) instead uses
`codeSystemCode="70618,2670"`, a schema-declared literal list covering exactly the two real substances
in this document — no import needed.

To see full membership enforcement instead: copy both `sampleCodeSystem_*.json` files into
`codesystems/` (named by their `codeSystemId`, e.g. `codesystems/2.16.840.1.113883.6.96.json`), set
`codeSystemEnforceValues=T` on fieldIds `17`, `21`, `30`, and `35`, and re-run Review — it validates
cleanly against the real codes in those files (across *both* entries' different real codes), and fails
with a clear message if you change any value to a real code that isn't in the imported list.
