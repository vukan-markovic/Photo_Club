package vukan.com.photoclub.models

import com.google.firebase.Timestamp

data class Image(
    var dateTime: Timestamp = Timestamp.now(),
    var imageUrl: String = "",
    var likesCount: Long = 0L,
    var profilePictureUrl: String = "",
    var userID: String = ""
)