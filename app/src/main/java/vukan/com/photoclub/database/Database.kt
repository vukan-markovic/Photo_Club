package vukan.com.photoclub.database

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import vukan.com.photoclub.GlideApp
import vukan.com.photoclub.R
import vukan.com.photoclub.models.Comment
import vukan.com.photoclub.models.Image
import vukan.com.photoclub.models.User
import vukan.com.photoclub.views.ImageDetailsActivity
import vukan.com.photoclub.views.ProfileActivity
import java.io.ByteArrayOutputStream
import kotlin.random.Random

class Database(private var view: AppCompatActivity? = null) {
    private var builder: UserProfileChangeRequest.Builder = UserProfileChangeRequest.Builder()
    private var firestore = FirebaseFirestore.getInstance()
    private var storage: StorageReference = FirebaseStorage.getInstance().reference

    fun createComment(imageUrl: String, content: String) {
        val comment =
            Comment(
                System.currentTimeMillis().toString(),
                content,
                Timestamp.now(),
                FirebaseAuth.getInstance().currentUser!!.uid,
                FirebaseAuth.getInstance().currentUser?.displayName.toString()
            )
        firestore.collection("images").document(imageUrl).collection("comments")
            .document(comment.commentID)
            .set(comment, SetOptions.merge())
    }

    fun deleteComment(commentID: String, imageUrl: String) {
        firestore.collection("images").document(imageUrl).collection("comments")
            .document(commentID).delete()
    }

    fun createImage(imageUrl: Uri) {
        val image = Image(
            Timestamp.now(),
            System.currentTimeMillis().toString(),
            FirebaseAuth.getInstance().currentUser!!.uid
        )

        firestore.collection("images").document(image.imageUrl)
            .set(image, SetOptions.merge())

        storage.child(image.imageUrl).putFile(
            imageUrl, StorageMetadata.Builder()
                .setContentType("image/jpg").build()
        )
    }

    fun createImageBitmap(imageBitmap: Bitmap) {
        val image = Image(
            Timestamp.now(),
            System.currentTimeMillis().toString(),
            FirebaseAuth.getInstance().currentUser!!.uid
        )

        firestore.collection("images").document(image.imageUrl)
            .set(image, SetOptions.merge())
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        storage.child(image.imageUrl)
            .putBytes(baos.toByteArray(), StorageMetadata.Builder().setContentType("image/jpg").build())
    }

    fun readImage(imageUrl: String) {
        firestore.collection("images").document(imageUrl).get().addOnSuccessListener {
            setImage(it.toObject(Image::class.java))
        }
    }

    fun deleteImage(imageUrl: String) {
        firestore.collection("images").document(imageUrl).delete()
        storage.child(imageUrl).delete()
    }

    fun createUser(firebaseUser: FirebaseUser) {
        val user = User(firebaseUser.uid, firebaseUser.displayName.toString())
        if (user.username == "null") user.username = "User" + Random(1).nextInt()
        firestore.collection("users").document(user.userID).set(user, SetOptions.merge())
        val baos = ByteArrayOutputStream()
        BitmapFactory.decodeResource(view?.resources, R.drawable.person).compress(Bitmap.CompressFormat.PNG, 100, baos)
        storage.child(user.userID)
            .putBytes(baos.toByteArray(), StorageMetadata.Builder().setContentType("image/jpg").build())
    }

    fun readUser(userID: String) {
        firestore.collection("users").document(userID).get().addOnSuccessListener {
            setUser(it.toObject(User::class.java))
        }
    }

    fun updateProfilePicture(imageUrl: Uri) {
        storage.child(FirebaseAuth.getInstance().currentUser!!.uid).delete().addOnSuccessListener {
            storage.child(FirebaseAuth.getInstance().currentUser!!.uid).putFile(
                imageUrl, StorageMetadata.Builder()
                    .setContentType("image/jpg").build()
            )
        }
    }

    fun updateProfilePictureBitmap(imageBitmap: Bitmap) {
        storage.child(FirebaseAuth.getInstance().currentUser!!.uid).delete().addOnSuccessListener {
            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            storage.child(FirebaseAuth.getInstance().currentUser!!.uid)
                .putBytes(baos.toByteArray(), StorageMetadata.Builder().setContentType("image/jpg").build())
        }
    }

    fun updateUsername(username: String) {
        builder.setDisplayName(username)
        FirebaseAuth.getInstance().currentUser?.updateProfile(builder.build())
        firestore.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
            .update("username", username)
    }

    private fun setImage(image: Image?) {
        if (image != null) {
            val imageDetailsActivity = view as ImageDetailsActivity
            imageDetailsActivity.setDateTime(image.dateTime)
            GlideApp.with(imageDetailsActivity).load(storage.child(image.imageUrl))
                .into(imageDetailsActivity.getImage())
        }
    }

    private fun setUser(user: User?) {
        val profileActivity = view as ProfileActivity
        if (user != null) {
            GlideApp.with(profileActivity).load(storage.child(user.userID)).into(profileActivity.getProfilePicture())
            profileActivity.setUsername(user.username)
        }
    }
}