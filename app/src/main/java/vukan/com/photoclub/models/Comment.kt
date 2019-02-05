package vukan.com.photoclub.models

import com.google.firebase.Timestamp

data class Comment(
    var commentID: String = "",
    var content: String = "",
    var dateTime: Timestamp = Timestamp.now(),
    var profilePictureUrl: String = "",
    var userID: String = "",
    var username: String = ""
)