package com.stepsync.presentation.social

import android.content.SharedPreferences
import android.util.Log
import androidx. lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com. stepsync.data.model. Friend
import com.stepsync.data.model.LeaderboardEntry
import com.stepsync.domain.repository.FriendRepository
import com.stepsync.domain.repository.ChallengeRepository
import com. stepsync.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow. MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines. flow.stateIn
import com.stepsync.domain.repository.AchievementRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val challengeRepository: ChallengeRepository,
    private val achievementRepository: AchievementRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val userId = sharedPreferences.getString(Constants.KEY_USER_ID, "") ?: ""

    private val _uiState = MutableStateFlow<SocialUiState>(SocialUiState. Idle)
    val uiState: StateFlow<SocialUiState> = _uiState. asStateFlow()

    val friends:  StateFlow<List<Friend>> = friendRepository
        .getAllFriends(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingRequests: StateFlow<List<Friend>> = friendRepository
        .getPendingRequests(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val globalLeaderboard: StateFlow<List<LeaderboardEntry>> = challengeRepository
        .getGlobalLeaderboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addFriend(friendCode: String) {
        if (userId.isEmpty()) {
            _uiState.value = SocialUiState.Error("User not authenticated")
            return
        }

        if (friendCode.isBlank()) {
            _uiState.value = SocialUiState.Error("Friend code is required")
            return
        }

        viewModelScope.launch {
            try {
                _uiState. value = SocialUiState. Loading
                Log.d("SocialViewModel", "Adding friend with code: $friendCode")

                // ✅ Use friend code instead of email
                friendRepository.addFriendByCode(userId, friendCode. trim())

                try {
                    val friendsCount = friendRepository.getFriendsCount(userId)
                    achievementRepository.updateAchievementProgress(userId, "first_friend", friendsCount)
                    achievementRepository.updateAchievementProgress(userId, "friend_5", friendsCount)
                    achievementRepository.updateAchievementProgress(userId, "friend_10", friendsCount)
                    Log.d("SocialViewModel", "Updated friend achievements: $friendsCount friends")
                } catch (e:  Exception) {
                    Log.e("SocialViewModel", "Failed to update achievement", e)
                }
                _uiState.value = SocialUiState.Success("Friend request sent!")
                Log.d("SocialViewModel", "Friend request sent successfully")
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Failed to add friend"
                Log. e("SocialViewModel", "Error adding friend: $errorMessage", e)
                _uiState.value = SocialUiState.Error(errorMessage)
            }
        }
    }

    fun removeFriend(friendId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = SocialUiState.Loading
                Log.d("SocialViewModel", "Removing friend:  $friendId")

                friendRepository.removeFriend(friendId)

                _uiState.value = SocialUiState.Success("Friend removed")
                Log.d("SocialViewModel", "Friend removed successfully")
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Failed to remove friend"
                Log.e("SocialViewModel", "Error removing friend: $errorMessage", e)
                _uiState.value = SocialUiState.Error(errorMessage)
            }
        }
    }

    fun acceptFriendRequest(friendId:  Long) {
        viewModelScope.launch {
            try {
                _uiState.value = SocialUiState.Loading
                Log.d("SocialViewModel", "Accepting friend request: $friendId")

                friendRepository.acceptFriendRequest(friendId)

                _uiState.value = SocialUiState.Success("Friend request accepted!")
                Log.d("SocialViewModel", "Friend request accepted successfully")
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Failed to accept request"
                Log.e("SocialViewModel", "Error accepting request: $errorMessage", e)
                _uiState.value = SocialUiState.Error(errorMessage)
            }
        }
    }

    fun resetUiState() {
        _uiState.value = SocialUiState.Idle
    }
}

sealed class SocialUiState {
    object Idle : SocialUiState()
    object Loading :  SocialUiState()
    data class Success(val message: String) : SocialUiState()
    data class Error(val message:  String) : SocialUiState()
}