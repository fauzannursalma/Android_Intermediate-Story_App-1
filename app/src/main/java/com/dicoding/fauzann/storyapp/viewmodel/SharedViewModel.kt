package com.dicoding.fauzann.storyapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.fauzann.storyapp.utils.UserPreference
import com.dicoding.fauzann.storyapp.model.UserAuth
import kotlinx.coroutines.launch

class SharedViewModel(private val pref: UserPreference) : ViewModel() {
    fun getUser() : LiveData<UserAuth> {
        return pref.getUser().asLiveData()
    }

    fun saveUser(user: UserAuth) {
        viewModelScope.launch {
            pref.saveUser(user)
        }
    }

    fun logout() {
        viewModelScope.launch {
            pref.logout()
        }
    }
}