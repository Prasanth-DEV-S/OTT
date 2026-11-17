package com.dealshot.app.presentation.mobile.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.data.remote.repository.SearchRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import com.dealshot.app.data.preferences.PreferenceManager
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val preferenceManager: PreferenceManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchState())
    val uiState: StateFlow<SearchState> = _uiState.asStateFlow()

    private val _effect = Channel<SearchEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _queryFlow = MutableStateFlow("")
    private var searchJob: Job? = null
    private var loadMoreJob: Job? = null

    init {
        observeQueryChanges()
        if (preferenceManager != null) {
            val saved = preferenceManager.getRecentSearches()
            if (saved.isNotEmpty()) {
                _uiState.update { it.copy(recentSearches = saved.take(6)) }
            }
        }
    }

    private fun observeQueryChanges() {
        viewModelScope.launch {
            _queryFlow
                .debounce(300L)
                .distinctUntilChanged()
                .collect { debouncedQuery ->
                    executeSearch(debouncedQuery)
                }
        }
    }

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> {
                _uiState.update { it.copy(query = intent.query, error = null) }
                _queryFlow.value = intent.query
            }

            SearchIntent.ClearQuery -> {
                searchJob?.cancel()
                loadMoreJob?.cancel()
                _uiState.update {
                    it.copy(
                        query = "",
                        results = emptyList(),
                        isLoading = false,
                        isLoadingMore = false,
                        hasSearched = false,
                        page = 1,
                        hasNext = false,
                        error = null
                    )
                }
                _queryFlow.value = ""
            }

            is SearchIntent.SearchSubmitted -> {
                addRecentSearch(intent.query)
                _uiState.update { it.copy(query = intent.query) }
                _queryFlow.value = intent.query
                viewModelScope.launch {
                    _effect.send(SearchEffect.DismissKeyboard)
                }
            }

            is SearchIntent.RecentSearchSelected -> {
                addRecentSearch(intent.query)
                _uiState.update { it.copy(query = intent.query, error = null) }
                _queryFlow.value = intent.query
                viewModelScope.launch {
                    _effect.send(SearchEffect.DismissKeyboard)
                }
            }

            is SearchIntent.RemoveRecentSearch -> {
                _uiState.update { state ->
                    val updated = state.recentSearches.filterNot { it.equals(intent.query, ignoreCase = true) }
                    preferenceManager?.saveRecentSearches(updated)
                    state.copy(recentSearches = updated)
                }
            }

            SearchIntent.ClearAllRecentSearches -> {
                preferenceManager?.saveRecentSearches(emptyList())
                _uiState.update { it.copy(recentSearches = emptyList()) }
            }

            SearchIntent.Retry -> {
                executeSearch(_uiState.value.query)
            }

            SearchIntent.LoadNextPage -> {
                loadNextPage()
            }

            is SearchIntent.VideoClicked -> {
                if (_uiState.value.query.isNotBlank()) {
                    addRecentSearch(_uiState.value.query)
                }
                viewModelScope.launch {
                    _effect.send(SearchEffect.OpenVideoDetails(intent.video))
                }
            }
        }
    }

    private fun executeSearch(query: String) {
        searchJob?.cancel()
        loadMoreJob?.cancel()

        val trimmed = query.trim()
        if (trimmed.isBlank() || trimmed.length < 2) {
            _uiState.update {
                it.copy(
                    results = emptyList(),
                    isLoading = false,
                    isLoadingMore = false,
                    hasSearched = false,
                    page = 1,
                    hasNext = false,
                    error = null
                )
            }
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, page = 1) }

            val result = searchRepository.searchVideos(trimmed, page = 1)

            result.fold(
                onSuccess = { searchResult ->
                    if (searchResult.items.isNotEmpty()) {
                        addRecentSearch(trimmed)
                    }
                    _uiState.update {
                        it.copy(
                            results = searchResult.items,
                            isLoading = false,
                            hasSearched = true,
                            page = searchResult.pagination?.page ?: 1,
                            hasNext = searchResult.pagination?.hasNext ?: false,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            results = emptyList(),
                            isLoading = false,
                            hasSearched = true,
                            error = error.localizedMessage ?: "Failed to find videos. Please try again."
                        )
                    }
                }
            )
        }
    }

    private fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasNext) return

        val trimmed = state.query.trim()
        if (trimmed.length < 2) return

        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            val nextPage = state.page + 1
            val result = searchRepository.searchVideos(trimmed, page = nextPage)

            result.fold(
                onSuccess = { searchResult ->
                    _uiState.update { current ->
                        val combined = (current.results + searchResult.items)
                            .distinctBy { it.id.ifBlank { it.altId.orEmpty() } }
                        current.copy(
                            results = combined,
                            isLoadingMore = false,
                            page = searchResult.pagination?.page ?: nextPage,
                            hasNext = searchResult.pagination?.hasNext ?: false
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
            )
        }
    }

    private fun addRecentSearch(query: String) {
        val clean = query.trim()
        if (clean.isBlank() || clean.length < 2) return
        _uiState.update { state ->
            val updated = (listOf(clean) + state.recentSearches.filterNot { it.equals(clean, ignoreCase = true) }).take(6)
            preferenceManager?.saveRecentSearches(updated)
            state.copy(recentSearches = updated)
        }
    }
}
