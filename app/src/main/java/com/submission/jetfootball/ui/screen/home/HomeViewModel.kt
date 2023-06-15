package com.submission.jetfootball.ui.screen.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.submission.jetfootball.data.PlayerRepository
import com.submission.jetfootball.data.local.Player
import com.submission.jetfootball.model.PlayersData
import com.submission.jetfootball.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: PlayerRepository) : ViewModel() {

    private val _allPlayer = MutableStateFlow<UiState<List<Player>>>(UiState.Loading)
    val allPlayer = _allPlayer.asStateFlow()

    private val _homeState = mutableStateOf(HomeState())
    val homeState: State<HomeState> = _homeState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllPlayer().collect { player ->
                when (player.isEmpty()) {
                    true -> repository.insertAllPlayer(PlayersData.players)
                    else -> _allPlayer.value = UiState.Success(player)
                }
            }
        }
    }

    private fun searchPlayer(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.searchPlayer(query)
                .catch { _allPlayer.value = UiState.Error(it.message.toString()) }
                .collect { _allPlayer.value = UiState.Success(it) }
        }
    }

    fun updateFavoritePlayer(id: Int, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFavoritePlayer(id, isFavorite)
        }
    }

    fun onQueryChange(query: String) {
        _homeState.value = _homeState.value.copy(query = query)
        searchPlayer(query)
    }

}