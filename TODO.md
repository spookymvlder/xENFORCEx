# TODO

Lightweight, undated development notes that don't belong in either of the project's other two
tracking documents:

- [ROADMAP.md](src/main/resources/Documentation/ROADMAP.md) — the phased backlog, worked in order.
- [ImportSpec.txt](src/main/resources/Documentation/ImportSpec.txt)'s own OPEN QUESTIONS section —
  unresolved questions specific to CSV schema mechanics.

If a note here turns out to belong in one of those instead, move it rather than leaving a third copy
of the same question.

## Open

- **`FieldDataType.ROOT` is still referenced.** `FormParser.parseFormIndex` constructs
  `new FieldData("root", FieldDataType.ROOT, i)` as a placeholder value for the synthetic root field
  of each parsed form instance. Confirm this is still needed, or whether it's a leftover from an
  earlier design that can be removed now that the anchor/root distinction (see the Glossary in
  README.md) is well-established.
- **Expanded `expireDate` functionality.** The format bug was fixed (see ImportSpec.txt's CHANGE LOG),
  but there was a vaguer, never-specified idea about doing more with field-level expiration than
  simply validating the `active`/`expireDate` pair at import time. No concrete proposal exists yet -
  revisit if a real use case shows up rather than guessing at scope now.

## Resolved since originally noted

Kept briefly for continuity, since these were open questions for a long time before this round of
work:

- Whether to keep `displayOrder`/`hierarchyLevel` or formalize them as UI-only metadata -
  `displayOrder` is documented as retained UI metadata with no parser effect.
  `hierarchyLevel`/`sectionHeaderOverride` remain genuinely open; see ImportSpec.txt's OPEN QUESTIONS
  rather than tracking the question in two places.
- Handling a field that's active but already past its `expireDate` - `validateActiveRange` reports
  this as a WARNING.
- Confirming `fieldRepeated`/`minOccurs` defaults - resolved, and superseded by more deliberate
  behavior than originally anticipated: `minOccurs` was decoupled entirely from `required`'s Usage
  code, so the two validation stages stay disjoint (see ImportSpec.txt's CHANGE LOG).
- Adding `groupingType` - implemented (`NONE`/`CONTAINER`/`COMPOSITE`/`CONSTITUENT`).
- Documenting how the root/anchor elements are derived - thoroughly documented in ImportSpec.txt's
  `parentId` entry and this project's README Glossary.
- "Update range properties" - superseded by the ordered-type range/duration work (signed relative
  date offsets, the unified `TypedPredicateFactory` comparator across numeric/date/time/duration
  types); revisit only if something more specific was originally meant here.
