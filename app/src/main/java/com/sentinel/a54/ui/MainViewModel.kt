package com.sentinel.a54.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentinel.a54.contracts.PackageEntity
import com.sentinel.a54.data.SentinelRepository
import com.sentinel.a54.engine.ScanOrchestrator
import com.sentinel.a54.engine.ScanUpdate
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ScanUiState(
    val isScanning: Boolean = false,
    val progress: Float = 0f,
    val currentAction: String = "Ready",
    val logs: List<String> = emptyList(),
    val packages: List<PackageEntity> = emptyList(),
    val systemApps: Int = 0,
    val userApps: Int = 0,
    val securityScore: Int = 100,
    val aiAnalysis: String? = null
)

class MainViewModel(
    private val repository: SentinelRepository,
    private val orchestrator: ScanOrchestrator
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState

    init {
        // Load initial packages if they exist in DB
        viewModelScope.launch {
            repository.allPackages.collect { pkgs ->
                val sysApps = pkgs.count { it.uid < 10000 }
                val userApps = pkgs.size - sysApps
                _uiState.update { 
                    it.copy(
                        packages = pkgs,
                        systemApps = sysApps,
                        userApps = userApps
                    )
                }
            }
        }
    }

    fun startScan() {
        if (_uiState.value.isScanning) return
        
        _uiState.update { 
            it.copy(
                isScanning = true, 
                progress = 0f, 
                logs = listOf("Initializing Sentinel-A54 Engine..."),
                aiAnalysis = null
            ) 
        }

        viewModelScope.launch {
            orchestrator.runFullScan().collect { update ->
                when (update) {
                    is ScanUpdate.Progress -> {
                        _uiState.update {
                            it.copy(
                                progress = update.percent,
                                currentAction = update.action,
                                logs = listOf(update.log) + it.logs.take(20)
                            )
                        }
                    }
                    is ScanUpdate.AiReport -> {
                        _uiState.update { it.copy(aiAnalysis = update.report) }
                    }
                    is ScanUpdate.Complete -> {
                        _uiState.update { 
                            it.copy(
                                isScanning = false,
                                currentAction = "Scan Complete",
                                progress = 1f,
                                securityScore = update.score,
                                logs = listOf("Scan finalized successfully.") + it.logs
                            ) 
                        }
                    }
                }
            }
        }
    }
}
