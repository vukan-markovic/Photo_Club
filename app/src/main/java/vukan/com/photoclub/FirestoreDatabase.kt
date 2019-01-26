package vukan.com.photoclub

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import vukan.com.photoclub.dataclasses.Comment
import vukan.com.photoclub.dataclasses.Image
import vukan.com.photoclub.dataclasses.User
import java.io.File

class FirestoreDatabase {
    private var builder: UserProfileChangeRequest.Builder = UserProfileChangeRequest.Builder()
    private var firestore = FirebaseFirestore.getInstance()
    private var storage: StorageReference = FirebaseStorage.getInstance().reference
    private var user: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    fun createComment(comment: Comment, imageUrl: String) {
        firestore.collection("images").document(imageUrl).collection("comments").document(comment.commentID)
            .set(comment)
    }

    fun readComments(imageUrl: String): List<Comment> {
        lateinit var comments: List<Comment>
        firestore.collection("images").document(imageUrl).collection("comments").limit(30).orderBy("dateTime").get()
            .addOnSuccessListener {
                comments = it.toObjects(Comment::class.java)
            }
        return comments
    }

    fun updateComment(commentId: String, content: String, imageUrl: String) {
        firestore.collection("images").document(imageUrl).collection("comments").document(commentId)
            .update("content", content, "dateTime", Timestamp.now())
    }

    fun deleteComment(commentId: String, imageUrl: String) {
        firestore.collection("images").document(imageUrl).collection("comments").document(commentId).delete()
    }

    fun createImage(image: Image) {
        firestore.collection("images").document(image.imageUrl).set(image)
        storage.child(image.imageUrl).putFile(
            Uri.parse(image.imageUrl)
            , StorageMetadata.Builder()
                .setContentType("image/jpg")
                .build()
        )
    }

    fun readImages(): MutableList<Any> {
        lateinit var images: MutableList<Any>
        firestore.collection("images").limit(30).orderBy("dateTime").get().addOnSuccessListener {
            images = it.toObjects(Any::class.java)
        }
        return images
    }

    fun readImagesSearch(search: String): List<Image> {
        lateinit var images: List<Image>
        firestore.collection("images").whereArrayContains("terms", search).get().addOnSuccessListener {
            images = it.toObjects(Image::class.java)
        }
        return images
    }

    fun readUserImages(userId: String): List<Image> {
        lateinit var images: List<Image>
        firestore.collection("images").whereEqualTo("userID", userId).limit(30).orderBy("dateTime").get()
            .addOnSuccessListener {
                images = it.toObjects(Image::class.java)
            }
        return images
    }

    fun readImage(imageUrl: String): Image {
        lateinit var image: Image
        firestore.collection("images").document(imageUrl).get().addOnSuccessListener {
            image = it.toObject(Image::class.java)!!
        }
        return image
    }

    fun updateImage(imageUrl: String): Long {
        var likes = 0L
        val document = firestore.collection("images").document(imageUrl)
        firestore.runTransaction {
            val newLikes = it.get(document).getLong("likesCount")!! + 1L
            it.update(document, "likesCount", newLikes)
            newLikes
        }.addOnSuccessListener {
            likes = it
        }
        return likes
    }

    fun deleteImage(imageUrl: String) {
        firestore.collection("images").document(imageUrl).delete()
        storage.child(imageUrl).delete()
    }

    fun downloadImage(imageUrl: String) {
        storage.child(imageUrl).getFile(File.createTempFile("images", "jpg"))
    }

    fun createUser(user: User) {
        firestore.collection("users").document(user.userID).set(user)
    }

    fun readUser(userID: String): User {
        lateinit var user: User
        firestore.collection("users").document(userID).get().addOnSuccessListener {
            user = it.toObject(User::class.java)!!
        }
        return user
    }

    fun updateProfilePicture(imageUrl: String) {
        builder.setPhotoUri(Uri.parse(imageUrl))
        user?.updateProfile(builder.build())
        firestore.collection("users").document(user!!.uid).update("profilePictureUrl", imageUrl)
    }

    fun updateUsername(username: String) {
        builder.setDisplayName(username)
        user?.updateProfile(builder.build())
        firestore.collection("users").document(user!!.uid).update("username", username)
    }

    fun deleteUser() {
        firestore.collection("users").document(user!!.uid).delete()
    }
}