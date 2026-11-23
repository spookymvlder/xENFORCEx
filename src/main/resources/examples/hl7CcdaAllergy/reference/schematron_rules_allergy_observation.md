# Allergies section templates — real Schematron rules

This is **not** a copy of HL7's Schematron file. The real file (one combined Schematron covering
every C-CDA template, ~1MB, 432 `<sch:pattern>` blocks) carries only a liability disclaimer in its own
header, with no explicit redistribution grant the way [the XSD excerpt](xsd_excerpt_POCD_MT000040_Observation.xsd)'s
source file does — so rather than copying its English rule descriptions verbatim, this file restates
each rule in its own words. What's reproduced exactly is the functional substance: the real XPath
`test` expressions, element/attribute names, template and value-set OIDs, and `CONF` identifiers,
all verifiable against the source. **To read HL7's own original wording, see the source below.**

- Source: [HL7/CDA-ccda-2.1](https://github.com/HL7/CDA-ccda-2.1), `validation/Consolidated CDA
  Templates for Clinical Notes (US Realm) DSTU R2.1.sch`

This fixture's CSV schema models all four templates chained in a real Allergies section entry, so all
four real patterns are covered below, not just one:

| Template | Pattern | Root OID | Extension |
|---|---|---|---|
| Allergy Concern Act | `p-urn-hl7ii-2.16.840.1.113883.10.20.22.4.30-2015-08-01-errors` | `2.16.840.1.113883.10.20.22.4.30` | `2015-08-01` |
| Allergy - Intolerance Observation (V2) | `p-urn-hl7ii-2.16.840.1.113883.10.20.22.4.7-2014-06-09-errors` | `2.16.840.1.113883.10.20.22.4.7` | `2014-06-09` |
| Reaction Observation | `p-urn-hl7ii-2.16.840.1.113883.10.20.22.4.9-2014-06-09-errors` | `2.16.840.1.113883.10.20.22.4.9` | `2014-06-09` |
| Severity Observation | `p-urn-hl7ii-2.16.840.1.113883.10.20.22.4.8-2014-06-09-errors` | `2.16.840.1.113883.10.20.22.4.8` | `2014-06-09` |

Each pattern's rules are what the real `<act>`/`<observation>` element must satisfy to conform to that
template — i.e. everything the [generic XSD type](xsd_excerpt_POCD_MT000040_Observation.xsd) (`Act` for
the Concern Act, `Observation` for the other three) leaves optional or unconstrained, that the
Schematron pins down instead. Each pattern below is also split into an `-errors-abstract` rule (the
substantive asserts, reused by other templates that extend this one) and the concrete rule that adds the
`templateId` check itself — restated together here as one table per template.

## Allergy Concern Act

Template `2.16.840.1.113883.10.20.22.4.30`, extension `2015-08-01`. The wrapping `<act>` for an
allergy entry — contains the Allergy - Intolerance Observation below via a `SUBJ` `entryRelationship`.

| CONF | Rule (restated) | Real XPath `test` |
|---|---|---|
| 1198-7469 | `classCode` must equal `ACT` | `@classCode='ACT'` |
| 1198-7470 | `moodCode` must equal `EVN` | `@moodCode='EVN'` |
| 1198-7471 | Exactly one `templateId` with `root` = `2.16.840.1.113883.10.20.22.4.30` and `extension` = `2015-08-01` | `count(cda:templateId[@root='2.16.840.1.113883.10.20.22.4.30'][@extension='2015-08-01'])=1` |
| 1198-7472 | At least one `id` | `count(cda:id) > 0` |
| 1198-7477 | Exactly one `code` element | `count(cda:code)=1` |
| 1198-19158 | That `code`'s own `@code` must equal `CONC` | `cda:code[@code='CONC']` |
| 1198-7485 | Exactly one `statusCode` | `count(cda:statusCode)=1` |
| 1198-19086 | That `statusCode`'s `@code` must come from the **dynamic** value set `ProblemAct statusCode` (`urn:oid:2.16.840.1.113883.11.20.9.19`) | `cda:statusCode[@code and @code=document('voc.xml')/voc:systems/voc:system[@valueSetOid='2.16.840.1.113883.11.20.9.19']/voc:code/@value]` |
| 1198-7498 | Exactly one `effectiveTime` | `count(cda:effectiveTime)=1` |
| 1198-7504 | If `statusCode/@code='active'`, `effectiveTime` must have a `low` | `((../cda:statusCode[@code='active']) and (cda:low)) or not(../cda:statusCode[@code='active'])` |
| 1198-10085 | If `statusCode/@code='completed'`, `effectiveTime` must have a `high` | `count(cda:statusCode[@code='completed'])=0 or (count(cda:statusCode[@code='completed'])=1 and count(cda:effectiveTime[cda:high])=1)` |
| 1198-7509 / 1198-14925 / 1198-7915 | At least one `entryRelationship` with `@typeCode="SUBJ"` containing exactly one Allergy - Intolerance Observation (template `2.16.840.1.113883.10.20.22.4.7`/`2014-06-09`) | `count(cda:entryRelationship[@typeCode='SUBJ'][count(cda:observation[cda:templateId[@root='2.16.840.1.113883.10.20.22.4.7' and @extension='2014-06-09']])=1]) > 0` |

## Allergy - Intolerance Observation (V2)

Template `2.16.840.1.113883.10.20.22.4.7`, extension `2014-06-09`.

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

## Reaction Observation

Template `2.16.840.1.113883.10.20.22.4.9`, extension `2014-06-09`. Nested inside the Allergy -
Intolerance Observation above via a `MFST` `entryRelationship`.

| CONF | Rule (restated) | Real XPath `test` |
|---|---|---|
| 1098-7325 | `classCode` must equal `OBS` | `@classCode='OBS'` |
| 1098-7326 | `moodCode` must equal `EVN` | `@moodCode='EVN'` |
| 1098-7323 | Exactly one `templateId` with `root` = `2.16.840.1.113883.10.20.22.4.9` and `extension` = `2014-06-09` | `count(cda:templateId[@root='2.16.840.1.113883.10.20.22.4.9'][@extension='2014-06-09'])=1` |
| 1098-7329 | At least one `id` | `count(cda:id) > 0` |
| 1098-16851 | Exactly one `code` element | `count(cda:code)=1` |
| 1098-31124 | That `code`'s own `@code` must equal `ASSERTION` | `cda:code[@code='ASSERTION']` |
| 1098-7328 | Exactly one `statusCode` | `count(cda:statusCode)=1` |
| 1098-19114 | That `statusCode`'s `@code` must equal `completed` | `cda:statusCode[@code='completed']` |
| 1098-7335 | Exactly one `value` with `xsi:type="CD"`, whose code must come from the dynamic value set **Problem** (`urn:oid:2.16.840.1.113883.3.88.12.3221.7.4`) | `count(cda:value[@xsi:type='CD'])=1` |

## Severity Observation

Template `2.16.840.1.113883.10.20.22.4.8`, extension `2014-06-09`. Nested inside the Reaction
Observation above via a `SUBJ` `entryRelationship`.

| CONF | Rule (restated) | Real XPath `test` |
|---|---|---|
| 1098-7345 | `classCode` must equal `OBS` | `@classCode='OBS'` |
| 1098-7346 | `moodCode` must equal `EVN` | `@moodCode='EVN'` |
| 1098-7347 | Exactly one `templateId` with `root` = `2.16.840.1.113883.10.20.22.4.8` and `extension` = `2014-06-09` | `count(cda:templateId[@root='2.16.840.1.113883.10.20.22.4.8'][@extension='2014-06-09'])=1` |
| 1098-19168 | Exactly one `code` element | `count(cda:code)=1` |
| 1098-19169 | That `code`'s own `@code` must equal `SEV` | `cda:code[@code='SEV']` |
| 1098-7352 | Exactly one `statusCode` | `count(cda:statusCode)=1` |
| 1098-19115 | That `statusCode`'s `@code` must equal `completed` | `cda:statusCode[@code='completed']` |
| 1098-7356 | Exactly one `value` with `xsi:type="CD"`, whose code must come from the dynamic value set **Severity** (`urn:oid:2.16.840.1.113883.3.88.12.3221.6.8`) | `count(cda:value[@xsi:type='CD'])=1` |

Note this pattern has no `id` requirement at all, unlike every other template here — not an omission,
that's genuinely what the real pattern asserts.

## How this maps to the project's CSV schema

[`../field_property_template_hl7_allergies_section.csv`](../field_property_template_hl7_allergies_section.csv)
reproduces most of the rules above directly, across all four templates. The recurring shape:
`classCode`/`moodCode` and (where present) `code`/`codeSystem` are each modeled as a `COMPOSITE` element
with two `CONSTITUENT` attribute fields (fixed-value `ENUMERATION` on each), rather than one row per
attribute. `statusCode` stays a plain `ENUMERATION` attribute (only one attribute worth checking) except
on the Allergy Concern Act, where the real rule binds it to a *dynamic* value set rather than a fixed
code — the CSV fixes it to the one value (`active`) actually used in this fixture, narrower than the
real rule, same simplification applied to every other dynamic-value-set field below.

**Allergy Concern Act** (`fieldId 4` = `classCode`/`moodCode` composite, `7` = `id`, `8` = `code`/
`codeSystem` composite, `11` = `statusCode`, `12`/`13` = `effectiveTime`/`low`, `14` = the `SUBJ`
`entryRelationship` wrapping the observation below) covers CONF 1198-7469/7470/7472/7477/19158/7485/7498
directly. 1198-19086 (dynamic `ProblemAct statusCode` value set) is the fixed-vs-dynamic simplification
above. 1198-7504/10085 (conditional `low`/`high` on `statusCode`) aren't modeled as a conditional — `low`
is unconditionally `RE` instead. 1198-7471 (`templateId`) isn't modeled (dropped with every `templateId`
in this fixture — see `../README.md`). 1198-7509/14925/7915 (the `SUBJ` wrapper containing exactly one
Allergy - Intolerance Observation) **is** the real structure this CSV builds (`fieldId 14` → `15`).

**Allergy - Intolerance Observation** (`fieldId 15` = `classCode`/`moodCode` composite, `19` =
`code`/`codeSystem` composite, `22` = `statusCode`, `23`/`24` = `effectiveTime`/`low`, `25` = `value`
composite) covers CONF 1098-7379/7380/7382*/15947/15948/32153/19084/19085/7387/31538 directly (*`id`,
like `templateId`, isn't modeled). The nested `participant`/`participantRole`/`playingEntity`/`code`
structure (CONF 1098-7402…7419) is modeled as container fields down to `playingEntity`, with its own
`code`/`codeSystem` pair a `COMPOSITE`/`CONSTITUENT` (`fieldId 31`).

**Reaction Observation** (`fieldId 37` = `classCode`/`moodCode` composite, `40` = `id`, `41` =
`code`/`codeSystem` composite, `44` = `statusCode`, `45`-`47` = `effectiveTime`/`low`/`high`, `48` =
`value` composite) covers CONF 1098-7325/7326/7329/16851/31124/7328/19114 directly; 1098-7323
(`templateId`) isn't modeled.

**Severity Observation** (`fieldId 54` = `classCode`/`moodCode` composite, `57` = `code`/`codeSystem`
composite, `60` = `statusCode`, `61` = `value` composite) covers CONF 1098-7345/7346/19168/19169/7352/
19115 directly; 1098-7347 (`templateId`) isn't modeled. This template genuinely has no `id` rule to skip
(see the note above the table).

The four coded values each pattern binds to a dynamic external value set — allergy `value`'s code at
`fieldId 26`/SNOMED, `playingEntity/code`'s substance code at `fieldId 32`/RxNorm, reaction `value`'s
code at `fieldId 49`/SNOMED, severity `value`'s code at `fieldId 62`/SNOMED — are each the `CODED_VALUE`
constituent of their pair — exactly the gap `CODED_VALUE` was built to close, but only partially: SNOMED
CT and RxNorm are far too large to enumerate as a literal list in a CSV cell. The shipped schema instead
validates those four fields in open mode (identity + presence only; `fieldId 26`, `49`, `62`) or against
a schema-declared `codeSystemCode` constraint (`fieldId 32`), and documents (`../README.md`) how to opt
into real membership enforcement by importing an actual code list via the engine's "Code Systems" CLI
feature.
