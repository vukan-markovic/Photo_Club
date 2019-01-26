package vukan.com.photoclub.dataclasses

import com.google.firebase.Timestamp

data class Image(
    var dateTime: Timestamp,
    var imageUrl: String,
    var terms: List<String>,
    var userID: String,
    var profilePictureUrl: String,
    var likesCount: Long
)