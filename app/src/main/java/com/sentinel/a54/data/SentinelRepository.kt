package com.sentinel.a54.data

import com.sentinel.a54.contracts.*
import kotlinx.coroutines.flow.Flow

class SentinelRepository(private val dao: SentinelDao) {
    val allPackages: Flow<List<PackageEntity>> = dao.getAllPackages()
    val allEvidence: Flow<List<Evidence>> = dao.getAllEvidence()
    val allSignals: Flow<List<Signal>> = dao.getAllSignals()

    suspend fun getAllPackagesSync(): List<PackageEntity> = dao.getAllPackagesSync()
    suspend fun insertPackage(pkg: PackageEntity) = dao.insertPackage(pkg)
    suspend fun insertFile(file: FileEntity) = dao.insertFile(file)
    suspend fun insertEvidence(evidence: Evidence) = dao.insertEvidence(evidence)
    suspend fun insertSignal(signal: Signal) = dao.insertSignal(signal)
    suspend fun insertScanCheckpoint(checkpoint: ScanCheckpoint) = dao.insertScanCheckpoint(checkpoint)
    suspend fun getLatestCheckpoint(): ScanCheckpoint? = dao.getLatestCheckpoint()
    suspend fun insertScanResult(scanResult: ScanResult) = dao.insertScanResult(scanResult)
}
