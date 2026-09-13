package com.sentinel.a54.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sentinel.a54.contracts.*

@Database(
    entities = [
        PackageEntity::class, FileEntity::class, CertificateEntity::class, ServiceEntity::class,
        SettingEntity::class, ArtifactEntity::class, Evidence::class, Signal::class,
        SignalGroup::class, OriginLink::class, Snapshot::class, SnapshotDiff::class,
        ArchiveEntry::class, EntropyWindow::class, DecoderAttempt::class, DecoderResult::class,
        DecodedFragment::class, ScanIssue::class, ScanCheckpoint::class, ScanResult::class,
        ScoreBreakdown::class, KnownGoodRecord::class, AiResponse::class, SelfIntegrityResult::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sentinelDao(): SentinelDao
}
