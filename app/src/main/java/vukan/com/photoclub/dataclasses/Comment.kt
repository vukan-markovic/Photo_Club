package vukan.com.photoclub.dataclasses

import java.util.*

data class Comment(
    var commentID: String,
    var userID: String,
    var content: String,
    var dateTime: Date,
    var profilePictureUrl: String,
    var username: String
)