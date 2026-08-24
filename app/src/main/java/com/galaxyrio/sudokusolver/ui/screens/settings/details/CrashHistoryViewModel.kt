package com.galaxyrio.sudokusolver.ui.screens.settings.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.galaxyrio.sudokusolver.data.crash.CrashHistoryRepository
import com.galaxyrio.sudokusolver.data.crash.CrashRecord
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CrashHistoryUiState(
    val isLoading: Boolean = true,
    val isClearing: Boolean = false,
    val records: List<CrashRecord> = emptyList(),
    val loadFailed: Boolean = false,
    val clearFailed: Boolean = false,
)

class CrashHistoryViewModel(
    private val repository: CrashHistoryRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(CrashHistoryUiState())
    val uiState: StateFlow<CrashHistoryUiState> = mutableUiState.asStateFlow()
    private var loadJob: Job? = null
    private var clearJob: Job? = null

    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                val records = withContext(Dispatchers.IO) { repository.list() }
                mutableUiState.value = CrashHistoryUiState(
                    isLoading = false,
                    records = records,
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                mutableUiState.update { current ->
                    current.copy(isLoading = false, loadFailed = true)
                }
            }
        }
    }

    fun clear() {
        loadJob?.cancel()
        clearJob?.cancel()
        clearJob = viewModelScope.launch {
            mutableUiState.update { current ->
                current.copy(isClearing = true, clearFailed = false)
            }
            try {
                val cleared = withContext(Dispatchers.IO) { repository.clear() }
                if (cleared) {
                    mutableUiState.value = CrashHistoryUiState(isLoading = false)
                } else {
                    mutableUiState.update { current ->
                        current.copy(isClearing = false, clearFailed = true)
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                mutableUiState.update { current ->
                    current.copy(isClearing = false, clearFailed = true)
                }
            }
        }
    }

    fun consumeClearFailure() {
        mutableUiState.update { current -> current.copy(clearFailed = false) }
    }

    companion object {
        fun factory(repository: CrashHistoryRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { CrashHistoryViewModel(repository) }
            }
    }
}

data class CrashDetailsUiState(
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val record: CrashRecord? = null,
    val missing: Boolean = false,
    val deleteFailed: Boolean = false,
)

class CrashDetailsViewModel(
    private val reportId: String,
    private val repository: CrashHistoryRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(CrashDetailsUiState())
    val uiState: StateFlow<CrashDetailsUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            val record = try {
                withContext(Dispatchers.IO) { repository.get(reportId) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                null
            }
            mutableUiState.value = CrashDetailsUiState(
                isLoading = false,
                record = record,
                missing = record == null,
            )
        }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            mutableUiState.update { current ->
                current.copy(isDeleting = true, deleteFailed = false)
            }
            try {
                val deleted = withContext(Dispatchers.IO) { repository.delete(reportId) }
                if (deleted) {
                    onDeleted()
                } else {
                    val recordStillExists = withContext(Dispatchers.IO) {
                        repository.get(reportId) != null
                    }
                    mutableUiState.update { current ->
                        current.copy(
                            isDeleting = false,
                            record = if (recordStillExists) current.record else null,
                            missing = !recordStillExists,
                            deleteFailed = recordStillExists,
                        )
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                mutableUiState.update { current ->
                    current.copy(isDeleting = false, deleteFailed = true)
                }
            }
        }
    }

    fun consumeDeleteFailure() {
        mutableUiState.update { current -> current.copy(deleteFailed = false) }
    }

    companion object {
        fun factory(
            reportId: String,
            repository: CrashHistoryRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { CrashDetailsViewModel(reportId, repository) }
        }
    }
}
