# Allergy Intolerance Observation — real Schematron rules

This is **not** a copy of HL7's Schematron file. The real file (one combined Schematron covering
every C-CDA template, ~1MB, 432 `<sch:pattern>` blocks) carries only a liability disclaimer in its own
header, with no explicit redistribution grant the way [the XSD excerpt](xsd_excerpt_POCD_MT000040_Observation.xsd)'s
source file does — so rather than copying its English rule descriptions verbatim, this file restates
each rule in its own words. What's reproduced exactly is the functional substance: the real XPath
`test` expressions, element/attribute names, template and value-set OIDs, and `CONF` identifiers,
all verifiable against the source. **To read HL7's own original wording, see the source below.**

- Source: [HL7/CDA-ccda-2.1](https://github.com/HL7/CDA-ccda-2.1), `validation/Consolidated CDA
  Templates for Clinical Notes (US Realm) DSTU R2.1.sch`
- Pattern: `p-urn-hl7ii-2.16.840.1.113883.10.20.22.4.7-2014-06-09-errors` (Allergy Intolerance
  Observation, template `2.16.840.1.113883.10.20.22.4.7`, extension `2014-06-09`)

These are the rules a real `<observation>` element must satisfy to conform to that template — i.e.
everything the [generic XSD type](xsd_excerpt_POCD_MT000040_Observation.xsd) leaves optional or
unconstrained, that this one Schematron pattern pins down instead.

| CONF | Rule (restated) | Real XPath `test` |
|---|---|---|
| 1098-7379 | `classCode` must equal `OBS` | `@classCode='OBS'` |
| 1098-7380 | `moodCode` must equal `EVN` | `@moodCode='EVN'` |
| 1098-7381 | Exactly one `templateId` with `root` = `2.16.840.1.113883.10.20.22.4.7` and `extension` = `2014-06-09` | `count(cda:templateId[@root='2.16.840.1.113883.10.20.22.4.7'][@extension='2014-06-09'])=1` |
| 1098-7382 | At least one `id` | `count(cda:id) > 0` |
| 1098-15947 | Exactly one `code` element | `count(cda:code)=1` |
| 1098-15948 | That `code`'s own `@code` must equal `ASSERTION` | `cda:code[@code='ASSERTION']` |
| 1098-32153 | That `code`'s `@codeSystem` must equal `2.16.840.1.113883.5.4` (HL7 ActCode) | `cda:code[@codeSystem='2.16.840.1.113883.5.4']` |
| 1098-19084 | Exactly one `statusCode` | `count(cda:statusCode)=1` |
| 1098-19085 | That `statusCode`'s `@code` must equal `completed` | `cda:statusCode[@code='completed']` |
| 1098-7387 | Exactly one `effectiveTime` | `count(cda:effectiveTime)=1` |
| 1098-31538 | That `effectiveTime` must have exactly one `low` | `cda:effectiveTime[count(cda:low)=1]` |
| 1098-7390 | Exactly one `value` with `xsi:type="CD"`, whose code must come from the dynamic value set **Allergy and Intolerance Type** (`urn:oid:2.16.840.1.113883.3.88.12.3221.6.2`) | `count(cda:value[@xsi:type='CD'])=1` |
| 1098-7402 … 1098-7419 (one combined assertion) | Exactly one `participant` with `@typeCode="CSM"`, containing exactly one `participantRole` with `@classCode="MANU"`, containing exactly one `playingEntity` with `@classCode="MMAT"`, containing exactly one `code` whose value must come from the dynamic value set **Substance Reactant for Intolerance** (`urn:oid:2.16.840.1.113762.1.4.1010.1`) | `count(cda:participant[@typeCode='CSM'][count(cda:participantRole[@classCode='MANU'][count(cda:playingEntity[@classCode='MMAT'][count(cda:code)=1])=1])=1])=1` |

Two further rules in the same pattern apply only to a branch of this template used for a *negated*
finding (`inversionInd="true"` on a nested `SUBJ` `entryRelationship`) — out of scope for this example,
since the allergy entries used here (`../condensedHl7AllergiesSection.xml`) aren't negated findings.

## How this maps to the project's CSV schema

[`../field_property_template_hl7_allergies_section.csv`](../field_property_template_hl7_allergies_section.csv)
reproduces most of the rules above directly. `classCode`/`moodCode` and `code`/`codeSystem` are each
modeled as a `COMPOSITE` element with two `CONSTITUENT` attribute fields (fixed-value `ENUMERATION`
on each), rather than one row per attribute — this is the allergy observation's own `fieldId 15`
(`classCode`/`moodCode`) and `fieldId 19` (`code`/`codeSystem`) in the CSV. `statusCode` stays a plain
`ENUMERATION` attribute (only one attribute worth checking). The nested
`participant`/`participantRole`/`playingEntity`/`code` structure is modeled as container fields down to
`playingEntity`, with its `code`/`codeSystem` pair again a `COMPOSITE`/`CONSTITUENT` (`fieldId 31`). The
two coded values this pattern binds to a dynamic value set (`value`'s code at `fieldId 26`,
`playingEntity/code`'s code at `fieldId 32`) are each the `CODED_VALUE` constituent of their pair.

The two value-set-bound rules (CONF 1098-7390 and 1098-7402/7419) are exactly the gap `CODED_VALUE`
was built to close, but only partially: a value set like "Allergy and Intolerance Type" or "Substance
Reactant for Intolerance" is **dynamic** and backed by a real external terminology (SNOMED CT,
RxNorm) — far too large to enumerate as a literal list in a CSV cell. The shipped schema instead
validates those two fields in open mode (identity + presence only, `fieldId 26`) or against a
schema-declared `codeSystemCode` constraint (`fieldId 32`), and documents (`../README.md`) how to opt
into real membership enforcement by importing an actual code list via the engine's "Code Systems" CLI
feature.
`templateId` and `id` aren't modeled in this example at all (dropped when the XML fixture was
trimmed — see `../README.md`); `effectiveTime`/`low` **is** modeled (`fieldId 23`/`24`, as `STRING` —
see limitation 3 in `../README.md`), so CONF 1098-7387/1098-31538 do have a CSV equivalent, just not a
`DATE`-typed one.
