# Self follow-up SDK audit

## Scope

This audit is source-only. No Mac, network, emulator, Docker, account, package download, vehicle write, or privileged screen-off action was performed.

## Observed source facts

- `app/src/demo/.../GatewayFactory.kt` returns a demo-only `VehicleReadGateway` whose state is `MOCK_ONLY`; its lifecycle methods are no-ops.
- `app/src/main/.../VehicleReadGateway.kt` models `MOCK_ONLY`, `CONNECTING`, `READY`, `UNAVAILABLE`, and `ERROR`, plus read-only `occupancy` state. The interface exposes `start`, `refresh`, and `stop`, but no vehicle write method.
- The existing console diagnosis records `CAR_SEATS` capability/read-only checks and callback/error/timeout distinctions. It explicitly does not claim a DDS round trip or physical vehicle write.
- The main manifest was not changed. The source archive contains no approved request to restore `INTERNET`; the offline voice probe treats an inherited INTERNET permission as a failure.
- `MainActivity` disables narration in `onStop` and on `ACTION_SCREEN_OFF`, while the ViewModel stops its session/gateway path in `onBackground`/`onDisplaySuspended`.

## Evidence versus hypothesis

### Evidence

1. The demo gateway is mock-only and has no vehicle write API.
2. The read gateway contract represents occupancy as read state and separates unavailable/error outcomes.
3. Existing diagnosis distinguishes HTTP console responses from SDK callbacks and DDS readiness.
4. Lifecycle source paths revoke narration allowance on stop and display suspension.

### Hypotheses not promoted to facts

- An observed `CAR_SEATS` capability would establish only the SDK-reported access type and areas; it would not prove a successful vehicle write.
- A console state response would not prove `console → bridge → SDK callback → app state` round-trip integrity.
- Removing or restoring INTERNET could affect any network-backed SDK path, but this source-only audit does not establish that the seat read path needs network access.

## Exact parent read-only procedure

1. On an approved, parked native target, inspect the existing Pleos flavor logs/source for `CAR_SEATS` permission access, Vehicle initialization, capability access type, supported areas, and current occupancy callback.
2. Confirm the app calls only read/callback APIs and records timeout/error/cleanup outcomes separately. Do not call a vehicle write API.
3. Separately GET the already-approved console catalog/state endpoints, recording HTTP status, timestamp, bridge/DDS readiness, and actual gear without changing state or exposing credentials.
4. Compare the four points only when all are present: console response, bridge readiness, SDK callback, and app state. Any false readiness, null actual value, callback timeout, or error means “HTTP internal state only”.
5. For `ACTION_SCREEN_OFF`, use an OS-supported test procedure only if the parent confirms it is available. Otherwise report unexecuted; do not use privileged or synthetic shortcuts.

## Lifecycle probe boundary

`FamilyCareLifecycleProbeTest` drives the real Compose UI (`enterDemo` → debug console → `kidsNoise`) and observes `MainActivity.narrationStatus`. Its JSON evidence intentionally sets `physicalListeningVerified` to `false`; software callbacks are not acoustic or physical-listening proof.
