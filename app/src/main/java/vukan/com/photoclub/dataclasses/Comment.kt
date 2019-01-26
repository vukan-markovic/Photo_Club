package vukan.com.photoclub.dataclasses

import com.google.firebase.Timestamp

data class Comment(
    var commentID: String,
    var userID: String,
    var content: String,
    var dateTime: Timestamp,
    var profilePictureUrl: String,
    var username: String
)