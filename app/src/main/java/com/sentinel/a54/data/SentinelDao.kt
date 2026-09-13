package com.sentinel.a54.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sentinel.a54.contracts.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SentinelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(pkg: PackageEntity)

    @Query("SELECT * FROM packages")
    fun getAllPackages(): Flow<List<PackageEntity>>

    @Query("SELECT * FROM packages")
    suspend fun getAllPackagesSync(): List<PackageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidence(evidence: Evidence)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignal(signal: Signal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: Snapshot)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanCheckpoint(checkpoint: ScanCheckpoint)

    @Query("SELECT * FROM scan_checkpoints ORDER BY time DESC LIMIT 1")
    suspend fun getLatestCheckpoint(): ScanCheckpoint?
    
    @Query("SELECT * FROM evidence")
    fun getAllEvidence(): Flow<List<Evidence>>
    
    @Query("SELECT * FROM signals")
    fun getAllSignals(): Flow<List<Signal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanResult(scanResult: ScanResult)
}
