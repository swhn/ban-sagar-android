package com.bansagar.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansagar.app.data.model.Slang
import com.bansagar.app.data.preferences.UserPreferencesRepository
import com.bansagar.app.domain.repository.SlangRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortTab { Trending, Latest, Top, Random }

data class HomeUiState(
    val slangs: List<Slang> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val error: String? = null,
    val activeTab: SortTab = SortTab.Trending,
)

private const val PAGE_SIZE = 20

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SlangRepository,
    private val prefs: UserPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var currentOffset = 0

    init {
        loadSlangs()
        viewModelScope.launch {
            prefs.showNsfw.drop(1).collect { loadSlangs() }
        }
    }

    fun selectTab(tab: SortTab) {
        if (_uiState.value.activeTab == tab) return
        _uiState.value = _uiState.value.copy(activeTab = tab)
        loadSlangs()
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadSlangs()
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.canLoadMore ||
            state.activeTab == SortTab.Random) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            try {
                val newItems = fetchForTab(state.activeTab, PAGE_SIZE, currentOffset)
                currentOffset += newItems.size
                _uiState.value = _uiState.value.copy(
                    slangs = state.slangs + newItems,
                    isLoadingMore = false,
                    canLoadMore = newItems.size == PAGE_SIZE,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingMore = false)
            }
        }
    }

    private fun loadSlangs() {
        currentOffset = 0
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                canLoadMore = true,
            )
            try {
                val slangs = fetchForTab(_uiState.value.activeTab, PAGE_SIZE, 0)
                currentOffset = slangs.size
                _uiState.value = _uiState.value.copy(
                    slangs = slangs,
                    isLoading = false,
                    isRefreshing = false,
                    canLoadMore = slangs.size == PAGE_SIZE && _uiState.value.activeTab != SortTab.Random,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load",
                    isLoading = false,
                    isRefreshing = false,
                )
            }
        }
    }

    private suspend fun fetchForTab(tab: SortTab, limit: Int, offset: Int): List<Slang> {
        val showNsfw = prefs.showNsfw.first()
        return when (tab) {
            SortTab.Trending -> repository.getTrending(limit, offset, showNsfw)
            SortTab.Latest -> repository.getLatest(limit, offset, showNsfw)
            SortTab.Top -> repository.getTop(limit, offset, showNsfw)
            SortTab.Random -> repository.getRandom(limit, showNsfw)
        }
    }
}
