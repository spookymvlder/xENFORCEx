# HL7 C-CDA Allergies Section example

Exercises `CODED_VALUE` (see `ImportSpec.txt`'s CODED VALUE section) against a real, publicly
published HL7 document, rather than invented data — and specifically the **whole Allergies and
Intolerances Section**, not a single isolated field. This matters: a schema that only validates one
cherry-picked element doesn't say much about whether the engine scales to a real clinical document's
actual nesting and repetition. This one does: two complete, repeating allergy entries, each with its
full real Allergy Concern Act → Allergy Intolerance Observation → Reaction Observation → Severity
Observation chain, with every real multi-attribute element (`classCode`/`moodCode`, `code`/`codeSystem`,
`typeCode`/`inversionInd`) checked via `COMPOSITE`/`CONSTITUENT` fields, not just one attribute picked
per element.

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
`id`/`templateId` lines, the human-readable `<text>` table, and `author`/`assignedAuthor` blocks —
left out for focus (they'd just repeat the same `COMPOSITE`/attribute patterns already shown several
times over), not because they're out of reach.

## Why there's an XSD and Schematron here too

A CSV schema that only checks itself against is making a claim nobody can verify. The base CDA XSD
leaves almost everything about an act or observation optional (see `reference/`'s XSD excerpt, which
covers both the `Act` and `Observation` generic types this fixture's four templates are declared
against); the real conformance rules — fixed codes, required cardinalities, nested structure — live
entirely in HL7's Schematron instead (`reference/`'s rules file). The CSV schema here maps onto those
real rules for **all four** templates this fixture covers (Allergy Concern Act, Allergy - Intolerance
Observation, Reaction Observation, Severity Observation): of their real rules combined, all but the four
bound to an external, dynamic value set (SNOMED CT / RxNorm, plus one HL7-internal dynamic status-code
value set on the Concern Act — all too large or too open-ended to enumerate in a CSV) have a direct CSV
equivalent. See
[`reference/schematron_rules_allergy_observation.md`](reference/schematron_rules_allergy_observation.md)
for the full, per-template rule-by-rule comparison.

## Where the real limitations are (and one that got fixed)

Three genuine engine findings surfaced while building this - one turned out to be a fixable bug, not
a wall, once pointed at the mechanism actually meant for it; two remain real boundaries:

1. **Multi-attribute elements: use `COMPOSITE`/`CONSTITUENT`, not one row per element.** A real element
   like `<code code="ASSERTION" codeSystem="2.16.840.1.113883.5.4"/>` carries two attributes worth
   checking. A first pass at this fixture picked one attribute per element as a workaround - but
   `groupingType=COMPOSITE` plus `CONSTITUENT` children was specifically built for exactly this:
   `FormParser.parseConstituentsFromNode` already read every constituent's attribute off the
   *same* composite element correctly. What didn't work yet was validation: `ValidateFormExtract`'s
   required/cardinality checks iterated constituents as if they were separate `FormField` tree nodes
   (which they never are - their value lives on the composite's own data map), producing false
   "missing" errors, and `SingleFormFieldValidator`'s `CompositeField` branch was a literal
   `//TODO composite/constituent value validation` no-op, so even a wrong constituent value went
   unchecked. Both are now fixed (constituent presence/populated-ness checked against the composite's
   own data; each constituent validated against its own `FieldProperty`, including `ENUMERATION`
   normalization and `CODED_VALUE`), and this schema uses `COMPOSITE` throughout - every `classCode`/
   `moodCode` pair, every `code`/`codeSystem` pair, and the `typeCode`/`inversionInd` pair on the
   reaction/severity `entryRelationship` wrappers.
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
  — the CSV schema (64 rows), anchored at `section` itself (the real document's actual top-level
  element is `ClinicalDocument`, not modeled here). `entry` is `fieldRepeated=T`, covering both real
  allergy entries with one schema; every multi-attribute element is a `COMPOSITE` with `CONSTITUENT`
  children for each attribute.
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
The three `value` constituents (allergy type, reaction, severity - fieldIds `26`, `49`, `62`) are
`CODED_VALUE` in open mode (identity + presence only — real external vocabularies like SNOMED CT are
far too large to enumerate in a CSV). The RxNorm substance `code` constituent (fieldId `32`) instead
uses `codeSystemCode="70618,2670"`, a schema-declared literal list covering exactly the two real
substances in this document — no import needed.

To see full membership enforcement instead: copy both `sampleCodeSystem_*.json` files into
`codesystems/` (named by their `codeSystemId`, e.g. `codesystems/2.16.840.1.113883.6.96.json`), set
`codeSystemEnforceValues=T` on fieldIds `26`, `32`, `49`, and `62`, and re-run Review — it validates
cleanly against the real codes in those files (across *both* entries' different real codes), and fails
with a clear message if you change any value to a real code that isn't in the imported list.
