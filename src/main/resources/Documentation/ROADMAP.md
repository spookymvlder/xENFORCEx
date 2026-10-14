# Roadmap

Durable, cross-session backlog for xENFORCEx (formerly xml-auto-validator). Check items off as they land; append new
sub-items as they're discovered rather than guessing them in ahead of time. Within a single working
session, granular sub-steps for whichever phase is active are also tracked with Claude Code's
session-scoped task list — this file is the part that survives between sessions.

Phases are worked in order. Phase 5 is a placeholder, deliberately not scheduled yet.

## Phase 1 — Fix identified inconsistencies

From a full read-only audit of `ImportSpec.txt` against actual code behavior (2026-08-18/19). Each
item below is a spec-vs-behavior mismatch, verified by running the program against a modified CSV
fixture, not just inferred from reading code.

- [x] **`dataType` is documented as Required but silently unenforced.** No schema-side check exists
      for "dataType is required unless groupingType is CONTAINER/COMPOSITE/CONSTITUENT" — a blank
      `dataType` on an ordinary field just skips all type-specific validation forever, with zero
      signal to the author.
      **Decided:** default to `STRING` and log a `WARNING`, then continue. Implement in
      `FieldPropertyFactory.applyDefaults` (alongside the other blank-column defaults there) rather
      than as a schema-validator check, since it's a default, not a rejection. The warning should
      name the field and say a dataType was expected.

- [x] **Four columns crash the whole program instead of logging an error.** Verified directly: a
      typo in any of these produces an uncaught `IllegalArgumentException` with a raw stack trace,
      zero entries in the error log, and the run dies before any other field is checked.
      - `dataType` (e.g. `sting`) — `FieldDataType.valueOf`
      - `required` (e.g. `X`) — `Usage.valueOfAbbreviation` throws `IllegalArgumentException` directly
      - `groupingType` (e.g. `CONTAINR`) — `GroupingType.valueOf`
      - `condRequiredLogic` / `condRepeatedLogic` (e.g. `XOR`) — `LinkLogic.valueOf`
      Root cause: these four are parsed in `FieldPropertyFactory.parseRow`/`FieldPropertyConverter`,
      *after* OpenCSV's own binding step, so they miss the try/catch in `ReadCSV` that turns a bad
      `active`/`fieldRepeated`/`isAttribute`/`expireDate` value into a clean FATAL log entry (those
      go through custom `AbstractBeanField` converters, which OpenCSV wraps). Fix should make these
      four report through the same error-log pipeline — likely FATAL, matching the severity the spec
      already assigns to unrecoverable schema-shape problems — rather than throwing raw.

- [x] **`expireDate`'s documented format doesn't actually parse.** Spec says `yyyy-MM-dd`. Verified:
      that format throws a FATAL CSV parse error; `dd-MM-yyyy` is what's actually accepted. Cause:
      `CustomDateConverter`'s class-level default (`yyyy-MM-dd`) gets unconditionally overwritten by
      `ProcessingOverrides`'s constructor default (`dd-MM-yyyy`) via `ReadCSV`'s `setConverters()`
      call, and nothing ever calls `setDateFormat()` to change it. Decide which format is canonical —
      probably ISO, matching every other date-family type in the spec — and fix whichever side is
      wrong (likely `ProcessingOverrides`'s default). This will interact with Phase 2's config file,
      since date format is exactly the kind of thing that belongs there.

- [x] **`minLength`/`maxLength` apply to every dataType, not just strings.** Spec said "If string
      field...". Actual (was): `validateLength` ran unconditionally in `validateSimpleField` for any
      dataType, with no schema-side gate.
      **Decided:** scoped to STRING and CHARACTER for now. Implemented as `FieldDataType.
      supportsLength()` (switch-based, matching `TypedPredicateFactory.supportsRanges`'s existing
      style) rather than a boolean added to every enum constant's constructor call — extending the
      list later (EMAIL, URL, TELEPHONE, POSTAL_CODE, LABEL, COUNTRY_CODE are the natural next
      candidates) is a one-line edit to that single switch, read by both
      `SingleFieldPropertyValidator.validateLengthConstraints` (schema-side: reports an ERROR if set
      on an unsupported type, plus min<=max/min>=0 sanity) and `SingleFormFieldValidator` (document-
      side: skips the length check entirely for an unsupported type, rather than applying a
      meaningless one on top of the schema-level error).

- [x] **`regexPattern` schema-validity is checked late.** Was: not caught until a real document was
      validated (`SingleFormFieldValidator.validateRegex`'s `PatternSyntaxException` catch), unlike
      every other schema-shape problem (ranges, enumerations, cardinality), which is caught once at
      CSV import time.
      **Fixed:** added `SingleFieldPropertyValidator.validateRegexPattern`, which compiles the pattern
      at import and reports an ERROR if it doesn't. The document-side try/catch was deliberately kept
      as-is rather than removed: an ERROR doesn't halt the import, so a schema that still carries the
      bad pattern by the time a document is validated needs its own defense against the same
      exception, not just an earlier warning about it.

- [x] **"Required" labeling is overloaded across three different enforcement tiers**, which is what
      prompted this whole list.
      **Resolved with a rule, not new vocabulary:** rather than introducing tier labels into the doc,
      added a preamble sentence stating what Required/Optional actually mean for a blank cell -
      Required if blank is an error or produces a warned default (dataType), Optional if blank
      produces a silent, unremarkable default (`required` itself → O, `active` → true, `fieldRepeated`
      → F). Only one column's label was actually wrong under that rule: `required` (the column) was
      relabeled from Required to Optional. `fieldId`/`fieldName` (hard required) and `parentId`
      (contextually required, exempt for the anchor) already fit the rule as-is.
      **Bonus finding, fixed in the same pass:** `parentId`'s own text told authors to write the
      literal value `"root"` for the anchor field. Verified against the code: the only place `"root"`
      is checked as a special string is commented-out and dead; the actual anchor-detection logic is
      `parentId == null`, and the working fixtures already leave it blank, not `"root"`. Following the
      doc's own instruction literally would have broken import for that field (unresolvable parentId
      `"root"` -> FATAL). Fixed the wording to say "leave blank."

## Phase 2 — CLI settings menu and config file

Replaces `TEMPSETTINGS`'s hardcoded test values with a real, persistent-process CLI: a settings menu
backed by an importable/exportable config file, plus top-level modes for schema-only validation,
review (full pipeline, holds the result without committing), and a commit stub pending Phase 4's
output design.

Designed (2026-08-26):
- Config format: Java Properties (no new dependency; every setting is a flat scalar).
- Config keys (mapped from `ImportSettings`/`ProcessingOverrides`/`ErrorSettings`): `xmlFilePath`,
  `xsdFilePath`, `specFilePath`, `rootId`, `skipXsdValidation` (renamed from `testIgnoreXSD`),
  `dateFormat`, `errorThreshold`. Derived runtime state (`rootField`, `specImported`) is never
  persisted or menu-edited.
- `anchorElementName`/`anchorElementIndex` are excluded from the config and menu entirely — traced
  both to confirm: `anchorElementName` is auto-overwritten from the real anchor field right after CSV
  processing, before its only read site, so a human-supplied value is discarded before use; `TEMPSETTINGS`
  even hardcoded the wrong field name and nothing ever noticed, since it was never actually used.
  `anchorElementIndex` has exactly one read site and nothing anywhere gives meaning to a nonzero
  value. See the cleanup item below.
- `rootId` stays, confirmed genuinely necessary: it is not equivalent to "the anchor" (parentId ==
  null) — in the sample CSV, root (`application`) is a *child* of the anchor (`prospectiveDrivers`).
  It's the only mechanism to start real parsing past a wrapper level; nothing auto-derives it today.
- Settings menu: Cancel / Apply (runtime only) / Apply and save / Reload from file, modeled as three
  states (on-disk, active/runtime, working/in-progress-edit) so "only reload when manually triggered"
  falls out naturally rather than needing special-casing.
- Top-level menu: Edit settings / Validate schema only / Review / Commit pending form (stub) /
  Discard pending form / Review error logs (browses `ErrorSettings`' already-numbered in-session logs)
  / Exit. One pending reviewed form at a time; starting a new review while one is pending prompts to
  resolve it first.

Not yet scoped, tracked here rather than solved now:
- **Persist error logs to disk**, with an `errorLogDirectory` setting. Deliberately not added to the
  config/menu yet — a setting nothing reads or writes is the exact trap `anchorElementName` turned
  out to be. Append to one file per session vs. a new file per program start is TBD; if prior runs
  should be reloadable (not just archived), the current plain-text log format probably isn't enough
  and something structured (JSON is the leading candidate) is needed instead.
- **Find a default for `rootId`** for when it isn't set in config. Today, leaving it unset makes the
  "roots" list empty and produces a clean FATAL ("No field found that matches the fieldId set in
  Import Settings") rather than crashing, so this isn't urgent - it's about finding a friendlier
  default (if one exists) rather than fixing a broken state.
- **Remove `anchorElementName`/`anchorElementIndex` from `ImportSettings`/`ImportXml` entirely**,
  not just from the config/menu — the one real use (a DOM lookup for whitespace-stripping in
  `ImportXml.getDocument()`) can take `context.getAnchor()`'s fieldName directly and hardcode index
  `0` inline, with no settings object involved at all. Separate, smaller cleanup from adding the menu.

## Phase 3 — Review code system code

`FieldCodeSpec` (codeSystemName/Id/Version/Code) is built on every field and never read by anything
beyond storage. A second, unrelated, entirely unreferenced class `CodeSystem.java`
(`codeValues` list, `codeSystemPublisher`, `codeSystemDescription` — richer than `FieldCodeSpec`) is
never instantiated anywhere. Both look like early groundwork for the planned CODED_VALUE feature (see
memory: CODED_VALUE / CUSTOM plans). Decide what's actually needed, consolidate or remove the rest.

## Phase 4 — Design and implement program output

No structured output exists today — `FormDebugPrinter` is a plain human-readable `println` tree
walker, not built for machine consumption, and `main()` has no return value beyond printing error
logs. This is the open question from the very start of the project: stay in Java, convert to
Protobuf, or export validated XML/JSON. Design once there's a concrete consumer/use case in mind.

## Phase 5 — Reprocessing a form (placeholder, not yet scheduled)

Idea: for a form already parsed and validated and still in memory, allow small corrective edits
(e.g. changing one field's dataType) without a full CSV-edit-and-reimport cycle.

Explicitly not to be over-built when we get here: editing the CSV and reimporting the form is almost
always going to be faster than an in-memory patch mechanism, so this should stay small in scope —
figure out the minimal shape once there's an actual need in front of us, not ahead of time.
