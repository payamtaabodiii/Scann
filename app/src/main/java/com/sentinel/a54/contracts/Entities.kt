package com.sentinel.a54.contracts

import androidx.room.Entity
import androidx.room.PrimaryKey

// --- ENUMS ---
enum class CapabilityState { ACTIVE, DEGRADED, BLOCKED, NOT_APPLICABLE, USER_NOT_GRANTED, UNKNOWN_DUE_TO_ERROR, UNKNOWN_DUE_TO_LIMITATION }
enum class SnapshotDiffResult { USER_INITIATED_CHANGE, CORRELATED_CHANGE, UNCORRELATED_CHANGE, REAPPEARANCE_AFTER_REMOVAL, CAPABILITY_LOSS, CAPABILITY_GAIN, OBSERVATION_UNAVAILABLE }
enum class Verdict { BENIGN, INDETERMINATE, SUSPICIOUS, HIGH_CONFIDENCE_MALWARE }
enum class OriginStrength { DIRECT, STRONG_CORRELATION, WEAK_CORRELATION, TEMPORAL_ONLY, UNRESOLVED }
enum class OriginType { EXACT_ACCESSIBLE_FILE, EXACT_DOCUMENT_URI, EXACT_PACKAGE_ID, KNOWN_INSTALLER, CORRELATED_ORIGIN, PACKAGE_ONLY, UNKNOWN_DUE_TO_SANDBOX, UNKNOWN_DUE_TO_PACKAGE_VISIBILITY }

// --- ENTITIES ---
@Entity(tableName = "packages")
data class PackageEntity(
    @PrimaryKey val id: String,
    val packageName: String,
    val version: String,
    val uid: Int,
    val installer: String?,
    val installTime: Long,
    val updateTime: Long,
    val permissions: String,
    val components: String,
    val exportedState: String,
    val launcherPresence: Boolean,
    val nativePresence: Boolean,
    val signers: String
)

@Entity(tableName = "files")
data class FileEntity(@PrimaryKey val id: String, val path: String, val size: Long, val sha256: String)

@Entity(tableName = "certificates")
data class CertificateEntity(@PrimaryKey val id: String, val subject: String, val issuer: String, val validFrom: Long, val validTo: Long, val fingerprint: String)

@Entity(tableName = "services")
data class ServiceEntity(@PrimaryKey val id: String, val packageName: String, val serviceName: String, val exported: Boolean)

@Entity(tableName = "settings")
data class SettingEntity(@PrimaryKey val id: String, val key: String, val value: String, val capabilityState: String)

@Entity(tableName = "artifacts")
data class ArtifactEntity(@PrimaryKey val id: String, val sourceUri: String, val size: Long, val sha256: String, val magicType: String)

@Entity(tableName = "evidence")
data class Evidence(
    @PrimaryKey val id: String,
    val sourceState: String,
    val extractionMethod: String,
    val observedAt: Long,
    val normalizedValue: String,
    val displayValue: String,
    val confidence: String,
    val caveat: String,
    val relatedPackageId: String?,
    val relatedCertId: String?,
    val capabilityState: String
)

@Entity(tableName = "signals")
data class Signal(@PrimaryKey val id: String, val ruleId: String, val ruleVersion: String, val evidenceIds: String, val origin: String)

@Entity(tableName = "signal_groups")
data class SignalGroup(@PrimaryKey val id: String, val signalIds: String, val groupLogic: String)

@Entity(tableName = "origin_links")
data class OriginLink(@PrimaryKey val id: String, val typedRelation: String, val strength: String, val originConfidence: String, val caveats: String, val evidenceIds: String)

@Entity(tableName = "snapshots")
data class Snapshot(@PrimaryKey val id: String, val timestamp: Long, val canonicalHash: String, val evidenceIds: String)

@Entity(tableName = "snapshot_diffs")
data class SnapshotDiff(@PrimaryKey val id: String, val oldSnapshotId: String, val newSnapshotId: String, val diffType: String, val changes: String)

@Entity(tableName = "archive_entries")
data class ArchiveEntry(@PrimaryKey val id: String, val artifactId: String, val entryName: String, val size: Long, val sha256: String)

@Entity(tableName = "entropy_windows")
data class EntropyWindow(@PrimaryKey val id: String, val artifactId: String, val offset: Long, val length: Long, val entropy: Double)

@Entity(tableName = "decoder_attempts")
data class DecoderAttempt(@PrimaryKey val id: String, val artifactId: String, val transform: String, val success: Boolean)

@Entity(tableName = "decoder_results")
data class DecoderResult(@PrimaryKey val id: String, val attemptId: String, val resultType: String, val outputHash: String)

@Entity(tableName = "decoded_fragments")
data class DecodedFragment(@PrimaryKey val id: String, val resultId: String, val offset: Long, val content: String)

@Entity(tableName = "scan_issues")
data class ScanIssue(@PrimaryKey val id: String, val issueType: String, val message: String)

@Entity(tableName = "scan_checkpoints")
data class ScanCheckpoint(
    @PrimaryKey val id: String,
    val scanId: String,
    val phase: String,
    val unitCursors: String,
    val completedIds: String,
    val ruleVersion: String,
    val limitsVersion: String,
    val time: Long
)

@Entity(tableName = "scan_results")
data class ScanResult(@PrimaryKey val id: String, val checkpointId: String, val verdict: String, val scoreId: String)

@Entity(tableName = "score_breakdowns")
data class ScoreBreakdown(@PrimaryKey val id: String, val totalScore: Double, val signalContributions: String)

@Entity(tableName = "known_good_records")
data class KnownGoodRecord(@PrimaryKey val id: String, val entityHash: String, val entityType: String, val source: String, val addedAt: Long)

@Entity(tableName = "ai_responses")
data class AiResponse(@PrimaryKey val id: String, val promptHash: String, val responseText: String, val model: String, val timestamp: Long)

@Entity(tableName = "self_integrity_results")
data class SelfIntegrityResult(@PrimaryKey val id: String, val isTampered: Boolean, val signerHash: String, val timestamp: Long)
