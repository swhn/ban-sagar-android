package com.bansagar.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansagar.app.data.model.Slang
import com.bansagar.app.data.preferences.UserPreferencesRepository
import com.bansagar.app.domain.model.Timeframe
import com.bansagar.app.domain.repository.SlangRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val activeTimeframe: Timeframe = Timeframe.Month,
    val showNsfw: Boolean = false,
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
            prefs.showNsfw.collect { show ->
                _uiState.value = _uiState.value.copy(showNsfw = show)
            }
        }
    }

    fun selectTab(tab: SortTab) {
        if (_uiState.value.activeTab == tab) return
        _uiState.value = _uiState.value.copy(activeTab = tab)
        loadSlangs()
    }

    fun selectTimeframe(timeframe: Timeframe) {
        if (_uiState.value.activeTimeframe == timeframe) return
        _uiState.value = _uiState.value.copy(activeTimeframe = timeframe)
        if (_uiState.value.activeTab == SortTab.Trending) loadSlangs()
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
                val newItems = fetchForTab(state.activeTab, state.activeTimeframe, PAGE_SIZE, currentOffset)
                currentOffset += newItems.size
                _uiState.value = _uiState.value.copy(
                    slangs = state.slangs + newItems,
                    isLoadingMore = false,
                    canLoadMore = newItems.size == PAGE_SIZE,
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingMore = false)
            }
        }
    }

    private fun loadSlangs() {
        currentOffset = 0
        val tab = _uiState.value.activeTab
        val timeframe = _uiState.value.activeTimeframe
        val wasRefreshing = _uiState.value.isRefreshing

        viewModelScope.launch {
            // Step 1: show cached data instantly (only on initial/tab switch, not pull-to-refresh)
            if (!wasRefreshing) {
                val cached = getCachedForTab(tab, timeframe)
                if (cached.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        slangs = cached,
                        isLoading = false,
                        error = null,
                        canLoadMore = cached.size == PAGE_SIZE && tab != SortTab.Random,
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null, canLoadMore = true)
                }
            }

            // Step 2: fetch fresh from network (always)
            try {
                val slangs = fetchForTab(tab, timeframe, PAGE_SIZE, 0)
                currentOffset = slangs.size
                _uiState.value = _uiState.value.copy(
                    slangs = slangs,
                    isLoading = false,
                    isRefreshing = false,
                    canLoadMore = slangs.size == PAGE_SIZE && tab != SortTab.Random,
                    error = null,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = if (_uiState.value.slangs.isNotEmpty()) null else e.message ?: "Failed to load",
                    isLoading = false,
                    isRefreshing = false,
                )
            }
        }
    }

    private suspend fun getCachedForTab(tab: SortTab, timeframe: Timeframe): List<Slang> = try {
        when (tab) {
            SortTab.Latest -> repository.getCachedLatest(PAGE_SIZE)
            SortTab.Top -> repository.getCachedTop(PAGE_SIZE)
            SortTab.Trending, SortTab.Random -> repository.getCachedAll().let { all ->
                if (tab == SortTab.Random) all.shuffled().take(PAGE_SIZE) else all.take(PAGE_SIZE)
            }
        }
    } catch (_: Exception) { emptyList() }

    private suspend fun fetchForTab(tab: SortTab, timeframe: Timeframe, limit: Int, offset: Int): List<Slang> =
        when (tab) {
            SortTab.Trending -> repository.getTrending(timeframe, limit, offset)
            SortTab.Latest -> repository.getLatest(limit, offset)
            SortTab.Top -> repository.getTop(limit, offset)
            SortTab.Random -> repository.getRandom(limit)
        }
}
