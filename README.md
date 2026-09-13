# Sentinel-A54 Architecture Specification

## 1. Executive Summary
Sentinel-A54 is an evidence-first, origin-first, uncertainty-aware local forensic scanner designed for the Samsung Galaxy A54 5G (Android 16/API 36). It inventories legally observable states and inspecting accessible artifacts via static (non-executing) analysis. It strictly avoids arbitrary code execution, kernel tampering, or undocumented privilege escalation.

## 2. Goals & Non-Goals
*   **Goals**: Detect observable malware indicators, persistence artifacts, static analysis of APK/ZIP/DEX/ELF/PE, correlate entities (File/Cert/Installer/Service/Setting), and provide deterministic, redacted audit reports.
*   **Non-Goals**: Exploits, root privilege bypass, packet interception, device-clean certification, password/key guessing.

## 3. Assumptions & Environment
*   **Target**: Samsung Galaxy A54 5G (SM-A546*).
*   **OS**: Android 16 (API 36) with One UI 8.5.
*   **Stack**: Kotlin, Jetpack Compose, Material 3, Room Database, Coroutines/Flow, Retrofit for REST-based Gemini AI Advisory.
*   **State Classifications**: `ACTIVE`, `DEGRADED`, `BLOCKED`, `NOT_APPLICABLE`, `USER_NOT_GRANTED`, `UNKNOWN_DUE_TO_ERROR`, `UNKNOWN_DUE_TO_LIMITATION`.

## 4. Architecture Core
`UI -> ScanOrchestrator -> SharedContracts <- {PlatformAdapters, AnalysisEngines, Room, OptionalAI, OptionalML}`

The pipeline operates in the following stages:
1.  **SelfIntegrity**: Verify own APK signers and temper indicators.
2.  **CapabilityProfile**: Probe actual runtime capabilities (e.g., FGS, WorkManager bounds).
3.  **Snapshot**: Capture canonical hash of observable states.
4.  **Package+Policy**: Inventory packages via standard `PackageManager`.
5.  **ArtifactIntake**: Validate URIs, copy to temporary private storage.
6.  **StaticDecomposer**: Route to Dex, Elf, Pe, Entropy, or standard Archive walkers.
7.  **Decode/Emulate**: Process artifacts statically to uncover nested fragments.
8.  **Diff & OriginGraph**: Merge results, form origin linkages (e.g., File->Package).
9.  **IndependentScore**: Evaluate evidence based on independent gates.
10. **OptionalAI/ML**: Solicit advisory-only threat intelligence (hallucinations rejected).
11. **Audit/Report**: Generate redacted deterministic report.

## 5. Entities & Contracts
Stored in Room, utilizing strictly immutable ID correlations.
*   `PackageEntity`, `FileEntity`, `CertificateEntity`, `ServiceEntity`, `SettingEntity`, `ArtifactEntity`
*   `Evidence`, `Signal`, `SignalGroup`, `OriginLink`
*   `Snapshot`, `SnapshotDiff`, `ScanCheckpoint`, `ScanResult`

## 6. Threat Model Doctrine
*   A single capability (VPN, AccessibilityService) alone is **not** evidence of malware.
*   A generic string (`HTTP`, `Base64-like`) alone is **not** evidence.
*   Threat confidence is separate from origin confidence.
*   Evidence must carry its own source, extraction method, and limitations context.

## 7. Known Limitations (FGS/WorkManager)
Android 16 imposes strict FGS quotas and WorkManager background boundaries. Scan checkpoints (using Room) are processed in atomic transaction chunks. If a process dies or timeouts, it resumes at the last known chunk ID.

## 8. AI/ML Limits
Gemini API (`gemini-3.1-pro-preview`) is used strictly for an advisory role, evaluating signals under a "HIGH_CONFIDENCE_MALWARE" evaluation framework. Any external entity IDs proposed by the AI must map to valid local immutable records to prevent hallucination exploits.

## 9. Code Implementation Details
Refer to the Kotlin sources under `app/src/main/java/com/sentinel/a54/`:
*   `/contracts`: Frozen entity data schemas.
*   `/data`: Local persistence with Room DAOs and Repositories.
*   `/engine`: Orchestration logic for invoking scanner phases and managing checkpoints.
*   `/ai`: REST-based interaction with Gemini Pro using Kotlin Serialization.
*   `/ui`: Jetpack Compose with Material 3 (Luxury Dark Theme), designed for the forensic persona.
