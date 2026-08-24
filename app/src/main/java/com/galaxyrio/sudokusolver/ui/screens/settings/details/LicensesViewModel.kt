package com.galaxyrio.sudokusolver.ui.screens.settings.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.galaxyrio.sudokusolver.data.licenses.LibraryLicense
import com.galaxyrio.sudokusolver.data.licenses.LicensesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LicensesUiState(
    val isLoading: Boolean = true,
    val libraries: List<LibraryLicense> = emptyList(),
    val filteredLibraries: List<LibraryLicense> = emptyList(),
    val searchQuery: String = "",
    val loadFailed: Boolean = false,
)

class LicensesViewModel(
    private val repository: LicensesRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(LicensesUiState())
    val uiState: StateFlow<LicensesUiState> = mutableUiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        load()
    }

    fun setSearchQuery(query: String) {
        mutableUiState.update { current ->
            current.copy(
                searchQuery = query,
                filteredLibraries = current.libraries.filteredBy(query),
            )
        }
    }

    fun retry() = load()

    private fun load() {
        loadJob?.cancel()
        mutableUiState.update { it.copy(isLoading = true, loadFailed = false) }
        loadJob = viewModelScope.launch {
            try {
                val libraries = repository.getLibraries()
                mutableUiState.update { current ->
                    current.copy(
                        isLoading = false,
                        libraries = libraries,
                        filteredLibraries = libraries.filteredBy(current.searchQuery),
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                mutableUiState.update {
                    it.copy(isLoading = false, loadFailed = true)
                }
            }
        }
    }

    companion object {
        fun factory(repository: LicensesRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { LicensesViewModel(repository) }
        }
    }
}

private fun List<LibraryLicense>.filteredBy(query: String): List<LibraryLicense> {
    val normalized = query.trim()
    if (normalized.isEmpty()) return this
    return filter { library ->
        library.name.contains(normalized, ignoreCase = true) ||
            library.artifactId.contains(normalized, ignoreCase = true) ||
            library.licenseName?.contains(normalized, ignoreCase = true) == true ||
            library.developers.any { it.contains(normalized, ignoreCase = true) }
    }
}
