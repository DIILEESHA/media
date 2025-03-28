package com.example.connectify

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(val caption: String, val imageUri: Uri?) : Parcelable
