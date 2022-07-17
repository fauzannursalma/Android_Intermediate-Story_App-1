package com.dicoding.fauzann.storyapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserAuth(
    val token: String,
    val isLogin: Boolean
) : Parcelable
