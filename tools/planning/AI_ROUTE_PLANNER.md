# HuKi – AI-Assisted Hiking Route Planning
## Technical Implementation Brief & Development Roadmap

### 1. Objective

Implement an AI-assisted hiking route planning integration for HuKi (Android + iOS, Kotlin Multiplatform).

The goal is to allow users to describe a hiking route in natural language and use an external AI assistant (initially ChatGPT, but eventually Gemini, Claude and others) to generate a structured list of stops that can be imported directly into the HuKi Route Planner.

**Core concept:**

1. User describes a hiking route.
2. AI understands the request and identifies suitable locations and stops.
3. AI generates a HuKi route link following HuKi's public link specification.
4. User opens the link (or shares the AI answer to HuKi), launching HuKi.
5. HuKi validates the data, resolves locations and calculates the actual hiking route.
6. The Route Planner opens with the stops, where the user reviews and fixes them.
7. HuKi saves the route and supports GPX export.

**Core principle:** HuKi provides the integration and routing infrastructure. The AI provider supplies natural-language understanding and route planning.

No proprietary AI algorithm or paid AI API is required for the initial implementation.

---

### 2. Decision: Manual prompt + link first, native AI integrations later

**Decided:** start with the manual prompt + link direction (Phase 1A → 1B). Native AI integrations (Phase 3) are postponed.

Why:
- Every Phase 3 integration needs the same core as Phase 1B: an import model, name → coordinate resolving and a Route Planner entry. 1B is a prerequisite either way; the only open choice is the transport, and a link works with every assistant today.
- **AppFunctions** (Android): experimental, recent Android versions only, callable by Gemini / system agents only. The ChatGPT app cannot call it, no iOS counterpart.
- **App Intents** (iOS): reaches Siri / Shortcuts / Apple Intelligence, which (to be verified) does not support Hungarian — HuKi's primary audience.
- **MCP / ChatGPT app**: requires a backend, app review and users connecting the app — heavy investment before demand is proven.
- Phase 3 integrations become thin adapters on top of the Phase 1B import use case.

---

### 3. Important Architectural Principles

- AI is responsible for understanding the user's request and identifying suitable hiking locations.
- HuKi is responsible for validating stop data, geocoding, routing and GPX generation.
- AI must not generate the final GPX geometry.
- The route import format must be independent of any particular AI provider.
- The same link format should work with ChatGPT, Gemini, Claude and other compatible assistants.
- The integration should not require users to install a dedicated HuKi ChatGPT plugin for the initial workflow.
- HTTPS links are the primary transport, shared text is the fallback.
- The shared KMP layer owns route import, parsing and validation logic.
- The Route Planner is the confirmation step: imported stops land in the planner, where the user can move, remove or replace them. No separate import preview screen.

---

### 4. Existing HuKi Infrastructure to Reuse

- **Android App Links**: already verified for `https://huki.hu/place` (`assetlinks.json` on huki.hu, intent filter in `AndroidManifest.xml`). Add a `/route` path prefix.
- **Incoming links**: `IncomingLinkActivity` → `MainActivity` → `IncomingLinkStore` → `MainViewModel`. `LocationLinkParser` already recognizes the `huki.hu` hosts.
- **Shared text** (Android): `IncomingLink.Text` is already wired for `text/plain` shares → free fallback for sharing an AI answer to HuKi, no clipboard needed.
- **Bundled destinations**: `Destinations.kt` already contains many targets (e.g. Három-kő, Tar-kő). Match stop names against them before calling LocationIQ.
- **Geocoding**: LocationIQ search (already used for name-only location links).
- **Route Planner**: GraphHopper + HuKi-Routing fallback, round-trip support, GPX export.

**Gap — iOS Universal Links are not set up**: no Associated Domains entitlement, only `onOpenURL`. Needs the `applinks:huki.hu` entitlement + `apple-app-site-association` hosted on huki.hu.

---

### 5. Route Link Specification (proposed v1)

**No Base64 / encoded JSON in the URL.** LLMs cannot reliably produce Base64 by hand (they silently corrupt bytes), and an encoded payload cannot be read or debugged. Use human-readable query parameters instead.

Format:

```
https://huki.hu/route?v=1&name={route name}&s={stop}&s={stop}&...&loop=1
```

- `v`: link format version (required).
- `name`: route name (optional).
- `s`: one stop, repeated, in route order (2+ required). Form: `{name}` or `{name}@{lat},{lon}`.
- `loop=1`: circular route back to the first stop (optional) — avoids repeating the start as the last stop.

Example (from the Répáshuta conversation):

```
https://huki.hu/route?v=1&name=Bükk – Három-kő és Tar-kő&s=Hereg-rét&s=Muflon-kő@48.0575,20.48208&s=Három-kő@48.06032,20.47315&s=Tar-kő@48.0563,20.46152&loop=1
```

Parser requirements:
- Tolerant: accept both percent-encoded and raw Unicode (accents like "ő", spaces, en dashes), since AI output is inconsistent.
- Validate the version, stop count limits, coordinate ranges and that coordinates are near Hungary / the other stops.
- Never trust AI coordinates blindly: a coordinate far from the name's resolved location is replaced or flagged.
- Preserve stop ordering.

Name resolving order per stop:
1. Bundled `Destinations` name match (offline, exact data).
2. AI-supplied coordinates, if valid and plausible.
3. LocationIQ search, biased to the other stops' area.
4. Unresolved → the stop stays empty in the Route Planner for the user to pick.

**Fallback transport — shared text**: the AI also outputs the link in its answer; the user can share the whole answer to HuKi as text, and HuKi extracts the first `huki.hu/route` link from it.

**Postponed for v1**: distinguishing mandatory stops from optional POIs, hosted payloads (`/route/{id}`, needs a backend), signed tokens.

---

### 6. AI Prompt Integration

The initial integration uses manually supplied instructions rather than an AI API or native AI integration.

The prompt consists of two separate sections:

**[HuKi AI Instructions]**
- HuKi integration description.
- Route link specification v1, with an example.
- Stop identification requirements (real, named, reachable places; start at a parking / trailhead / station).
- Coordinate instructions (only when known reliably, otherwise name only).
- Output format requirements: the link as a single line, ready to tap.

**[User Request]**
- The actual natural-language hiking request.

Example:

"Plan a 12–15 km circular hiking route around Répáshuta. I want to visit natural rocky viewpoints, especially Három-kő and Tar-kő."

Initially, the combined prompt is manually pasted into AI assistants.

Once validated, HuKi offers a **"Copy HuKi prompt"** action (instructions copied to the clipboard, the user pastes them into their assistant with their request).

Prompt prefilling via `https://chatgpt.com/?q={URL_ENCODED_PROMPT}` is an observed, undocumented mechanism — fragile on mobile, not a priority. Only consider it after the PoC proves usage.

Embed the link specification directly in the prompt rather than relying on the AI retrieving a remote specification.

---

### 7. Deep Link Integration

Use HTTPS URLs rather than custom URL schemes as the primary mechanism.

**Android:**
- Extend the existing verified App Link intent filter with `/route`.
- Map it to a new `IncomingLink` type, consumed by `MainViewModel` → Route Planner.

**iOS:**
- Add the Associated Domains entitlement (`applinks:huki.hu`).
- Host `apple-app-site-association` on huki.hu (covering `/place` and `/route`).
- Handle incoming URLs through `onOpenURL` / `onContinueUserActivity`.

Important:
- ChatGPT's in-app browser may affect link handling — test link taps from ChatGPT, Gemini and Claude on both platforms.
- Provide a webpage fallback on `huki.hu/route` with an explicit "Open in HuKi" action and store links.
- Test installed and non-installed scenarios.

---

### 8. KMP Architecture

Shared KMP components (commonMain):
- `RouteLink` domain model (version, name, ordered stops, loop flag).
- `RouteLinkParser` (tolerant URL parsing + extraction from shared text).
- `RouteLinkValidator`.
- `RouteStopResolver` (Destinations → AI coordinates → LocationIQ).
- Route Planner entry: open the planner with pre-filled stops.
- Existing GPX generation / export.

Platform-specific components:

**Android:**
- App Link intent filter, `IncomingLink` mapping.
- Phase 3: optional AppFunctions adapter.

**iOS:**
- Universal Links.
- Phase 3: optional App Intents adapter.

Avoid duplicating business logic between Android and iOS.

---

### 9. Development Roadmap

## Phase 1A – AI Integration Proof of Concept

**Goal:** Validate the AI-to-HuKi communication format without implementing any HuKi UI or modifying the existing application.

This phase is deliberately manual and cheap.

1. Finalize the route link specification v1.
2. Write the complete HuKi AI Instructions prompt.
3. Write representative hiking requests (HU + EN).
4. Manually paste the prompts into ChatGPT, Gemini and Claude.
5. Evaluate whether each assistant reliably generates valid, tappable HuKi route links.
6. Check stop resolving: how many stop names match bundled Destinations / LocationIQ, how accurate the AI coordinates are.

Test cases:
- Link format compliance (version, stops, encoding of accented names).
- Stop identification (real, reachable places).
- Coordinate accuracy vs. resolved names.
- Stop ordering.
- Circular route handling (`loop=1`).
- Ambiguous names (e.g. multiple "Kő-hegy").

**Exit criteria:** At least one AI provider reliably generates valid HuKi route links whose stops resolve to the intended places.

No app changes, AI Route Planner screen, paid AI API or backend required.

## Phase 1B – HuKi Integration

**Goal:** Implement production-ready route importing using the validated format.

1. Implement the shared KMP parser, validator and stop resolver (+ unit tests).
2. Extend Android App Links with `/route`.
3. Set up iOS Universal Links (entitlement + `apple-app-site-association`).
4. Shared-text fallback: extract the route link from shared AI answers.
5. Open the Route Planner with the resolved stops; unresolved stops stay empty for the user to pick.
6. Webpage fallback on `huki.hu/route`.
7. Analytics (import success / failure, resolve rate — no stop names, PII rule) and Crashlytics non-fatals for parse failures.
8. Maestro E2E for the route link (like `maestro_location_link.yaml`).
9. Test the complete AI → HuKi → GPX workflow on both platforms.

**Exit criteria:** A user can import, review, calculate and save an AI-generated hiking route on both platforms.

## Phase 2 – In-App Entry Point

**Goal:** Make the AI-assisted workflow discoverable from HuKi.

1. "Plan with AI" entry (e.g. Menu / Route Planner) explaining the flow.
2. "Copy HuKi prompt" action.
3. Only if usage justifies it: a natural-language input field + prompt prefilling for supported assistants, with clipboard fallback.

## Phase 3 – Native AI Integrations (postponed)

Thin adapters on top of the Phase 1B import use case. Re-evaluate availability before starting.

**Android AppFunctions:**
- Native Android interface for exposing app functionality to authorized AI agents.
- Gemini / system agents only; not accessible to the ChatGPT Android app.
- Experimental / limited availability, device requirements.

Documentation:
https://developer.android.com/ai/appfunctions

**iOS App Intents:**
- Exposes HuKi actions to Siri, Shortcuts, Spotlight and Apple Intelligence.
- Native Swift AppIntents calling shared KMP use cases.
- Availability depends on device, OS, language (Hungarian support to be verified) and region.

Documentation:
https://developer.apple.com/documentation/appintents

**MCP / ChatGPT Apps:**
- A remote MCP server exposing e.g. `search_hiking_locations`, `create_hiking_route`, `get_route_preview`.
- Requires a backend, app review and users connecting the app; mobile client support to be verified.

Documentation:
https://developers.openai.com/apps-sdk/

---

### 10. Constraints and Non-Goals

For the initial implementation:
- No paid AI API.
- No custom AI model or NLP algorithm.
- No MCP server.
- No backend (self-contained links only).
- No Base64 / encoded payloads in links.
- No dependency on Gemini or any single assistant.
- No AI-generated GPX geometry.
- No automatic saving of unverified routes.

HuKi remains responsible for geocoding, routing, validation and GPX generation.

---

### 11. Success Criteria

A user can:

1. Describe a hiking route in natural language.
2. Provide the request to an AI assistant along with HuKi's instructions.
3. Receive a valid HuKi route link.
4. Open the link (or share the answer) in HuKi.
5. Review the stops in the Route Planner.
6. Obtain a properly calculated hiking route and GPX.

The same link format works with ChatGPT, Gemini, Claude and other compatible assistants.

---

### 12. Immediate Next Task

Start with Phase 1A.

The first deliverables:

1. Route link specification v1 (section 5, finalized).
2. Complete HuKi AI Instructions prompt.
3. Representative hiking test requests (HU + EN).
4. Results of manual runs in ChatGPT, Gemini and Claude.

**Do not implement app changes until the proof of concept is successful.**
