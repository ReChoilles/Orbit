package com.qx.orbit.bili.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qx.orbit.bili.data.api.FavoriteApi
import com.qx.orbit.bili.data.model.FavoriteFolder
import com.qx.orbit.bili.data.remote.CookieManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteFolderViewModel : ViewModel() {
    private val _folderList = MutableStateFlow<List<FavoriteFolder>>(emptyList())
    val folderList: StateFlow<List<FavoriteFolder>> = _folderList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        if (_isLoading.value) return
        val midStr = CookieManager.getInfoFromCookie("DedeUserID")
        val mid = midStr.toLongOrNull()
        if (mid == null || mid == 0L) {
            _errorMessage.value = "未登录，无法获取收藏夹列表"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val folders = FavoriteApi.getFavoriteFolders(mid)
                _folderList.value = folders
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
