package com.dealshot.app.presentation.mobile.search

import com.dealshot.app.data.remote.dto.VideoDto

data class SearchState(
    val query: String = "",
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val results: List<VideoDto> = emptyList(),
    val page: Int = 1,
    val hasNext: Boolean = false,
    val recentSearches: List<String> = listOf("Action", "Comedy", "Thriller", "Documentary"),
    val suggestedTags: List<String> = listOf("Trending", "Action", "Comedy", "Heist", "Tech", "Food", "Supercars"),
    val error: String? = null,
    val hasSearched: Boolean = false
)

sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data object ClearQuery : SearchIntent
    data class SearchSubmitted(val query: String) : SearchIntent
    data class RecentSearchSelected(val query: String) : SearchIntent
    data class RemoveRecentSearch(val query: String) : SearchIntent
    data object ClearAllRecentSearches : SearchIntent
    data object Retry : SearchIntent
    data object LoadNextPage : SearchIntent
    data class VideoClicked(val video: VideoDto) : SearchIntent
}

sealed interface SearchEffect {
    data class OpenVideoDetails(val video: VideoDto) : SearchEffect
    data object DismissKeyboard : SearchEffect
}
