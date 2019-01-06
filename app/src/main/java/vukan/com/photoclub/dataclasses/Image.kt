package vukan.com.photoclub.dataclasses

import java.util.*

data class Image(
    var dateTime: Date,
    var imageUrl: String,
    var terms: ArrayList<String>,
    var userID: String,
    var profilePictureUrl: String
)