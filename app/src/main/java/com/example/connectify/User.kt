package com.example.connectify

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(val userName: String, val profileImageUri: String) : Parcelable

