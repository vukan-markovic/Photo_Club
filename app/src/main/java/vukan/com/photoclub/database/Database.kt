package vukan.com.photoclub.database

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.bumptech.glide.Glide
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
import vukan.com.photoclub.views.*
import vukan.com.photoclub.models.Comment
import vukan.com.photoclub.models.Image
import vukan.com.photoclub.models.User
import java.io.File
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class Database(private var view: AppCompatActivity? = null) {
    private val mUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private var builder: UserProfileChangeRequest.Builder = UserProfileChangeRequest.Builder()
    private var firestore = FirebaseFirestore.getInstance()
    private var storage: StorageReference = FirebaseStorage.getInstance().reference

    fun createComment(imageUrl: String, content: String) {
        if (mUser != null) {
            val comment =
                Comment(
                    System.currentTimeMillis().toString(),
                    content,
                    Timestamp.now(),
                    mUser.photoUrl.toString(),
                    mUser.uid,
                    mUser.displayName.toString()
                )
            firestore.collection("images").document(Uri.parse(imageUrl).lastPathSegment).collection("comments")
                .document(comment.commentID)
                .set(comment, SetOptions.merge())
                .addOnSuccessListener {
                    Log.i("FirestoreDatabase", "New comment is added.")
                }
                .addOnFailureListener {
                    Log.i("FirestoreDatabase", it.message)
                }
        }
    }

    fun updateComment(commentID: String, content: String, imageUrl: String) {
        firestore.collection("images").document(Uri.parse(imageUrl).lastPathSegment).collection("comments")
            .document(commentID)
            .update("content", content, "dateTime", Timestamp.now())
            .addOnSuccessListener {
                Log.i("FirestoreDatabase", "Comment is updated.")
            }
            .addOnFailureListener {
                Log.i("FirestoreDatabase", it.message)
            }

    }

    fun deleteComment(commentID: String, imageUrl: String) {
        firestore.collection("images").document(Uri.parse(imageUrl).lastPathSegment).collection("comments")
            .document(commentID).delete()
            .addOnSuccessListener {
                Log.i("FirestoreDatabase", "Comment is deleted.")
            }
            .addOnFailureListener {
                Log.i("FirestoreDatabase", it.message)
            }
    }

    fun createImage(imageUrl: Uri) {
        if (mUser != null) {
            firestore.collection("images").document(imageUrl.lastPathSegment)
                .set(
                    Image(
                        Timestamp.now(),
                        imageUrl.toString(),
                        0L,
                        mUser.photoUrl.toString(),
                        mUser.uid
                    ), SetOptions.merge()
                )
                .addOnSuccessListener {
                    Log.i("FirebaseDatabase", "New image added.")
                }
                .addOnFailureListener {
                    Log.i("FirebaseDatabase", it.message)
                }
            storage.child(imageUrl.lastPathSegment).putFile(
                imageUrl, StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .build()
            ).addOnSuccessListener {
                Log.i("FirebaseStorage", "Image added.")
            }.addOnFailureListener {
                Log.i("FirebaseStorage", it.message)
            }
        }
    }

    fun readImage(imageUrl: String) {
        firestore.collection("images").document(Uri.parse(imageUrl).lastPathSegment).get().addOnSuccessListener {
            updateUI(it.toObject(Image::class.java))
            Log.i("FirebaseDatabase", "Image loaded.")
        }.addOnFailureListener {
            Log.i("FirebaseDatabase", it.message)
        }
    }

    private fun updateUI(image: Image?) {
        if (image != null) {
            (view as ImageDetailsActivity).setDateTime(image.dateTime)
            (view as ImageDetailsActivity).setLikesCount(image.likesCount)
            GlideApp.with((view as ImageDetailsActivity)).load(storage.child(Uri.parse(image.imageUrl).lastPathSegment))
                .into((view as ImageDetailsActivity).getImage())
        }
    }

    fun updateImage(imageUrl: String, i: Long) {
        val document = firestore.collection("images").document(Uri.parse(imageUrl).lastPathSegment)
        firestore.runTransaction {
            val newLikes = it.get(document).getLong("likesCount")!! + i
            it.update(document, "likesCount", newLikes)
            newLikes
        }.addOnSuccessListener {
            Log.i("FirebaseDatabase", "New image added.")
        }.addOnFailureListener {
            Log.i("FirebaseDatabase", it.message)
        }
    }

    fun deleteImage(imageUrl: String) {
        firestore.collection("images").document(Uri.parse(imageUrl).lastPathSegment).delete()
            .addOnSuccessListener {
                Log.i("FirebaseDatabase", "Image deleted.")
            }.addOnFailureListener {
                Log.i("FirebaseDatabase", it.message)
            }

        storage.child(Uri.parse(imageUrl).lastPathSegment).delete()
            .addOnSuccessListener {
                Log.i("FirebaseStorage", "Image deleted.")
            }.addOnFailureListener {
                Log.i("FirebaseStorage", it.message)
            }
    }

    fun downloadImage(imageUrl: String) {
        storage.child(Uri.parse(imageUrl).lastPathSegment).getFile(File.createTempFile("images", "jpg"))
            .addOnSuccessListener {
                Log.i("FirebaseStorage", "Image downloaded.")
            }.addOnFailureListener {
                Log.i("FirebaseStorage", it.message)
            }
    }

    fun createUser() {
        if (mUser != null) {
            val image = mUser.photoUrl ?: view?.getString(R.string.default_profile_picture)
            val name: String = mUser.displayName ?: view?.getString(R.string.user) + UUID.randomUUID()
            val user = User(image.toString(), mUser.uid, name)
            firestore.collection("users").document(user.userID).set(user, SetOptions.merge())
        }
    }

    fun readUser(userID: String) {
        firestore.collection("users").document(userID).get().addOnSuccessListener {
            updateUI2(it.toObject(User::class.java))
            Log.i("FirebaseDatabase", "User loaded.")
        }.addOnFailureListener {
            Log.i("FirebaseDatabase", it.message)
        }
    }

    private fun updateUI2(user: User?) {
        if (user != null) {
            Glide.with((view as ProfileActivity)).load(user.profilePictureUrl)
                .into((view as ProfileActivity).getProfilePicture())
            (view as ProfileActivity).setUsername(user.username)
        }
    }

    fun updateProfilePicture(imageUrl: Uri) {
        builder.setPhotoUri(imageUrl)
        mUser?.updateProfile(builder.build())?.addOnSuccessListener {
            Log.i("Update user", "User updated.")
            firestore.collection("users").document(mUser.uid).update("profilePictureUrl", mUser.photoUrl.toString())
                .addOnSuccessListener {
                    Log.i("FirebaseDatabase", "Profile picture updated.")
                }.addOnFailureListener {
                    Log.i("FirebaseDatabase", it.message)
                }
        }?.addOnFailureListener {
            Log.i("Update user", it.message)
        }
    }

    fun updateUsername(username: String) {
        builder.setDisplayName(username)
        mUser?.updateProfile(builder.build())?.addOnSuccessListener {
            Log.i("Update user", "User updated.")
        }?.addOnFailureListener {
            Log.i("Update user", it.message)
        }
        firestore.collection("users").document(mUser!!.uid).update("username", username)
            .addOnSuccessListener {
                Log.i("FirebaseDatabase", "Username updated.")
            }.addOnFailureListener {
                Log.i("FirebaseDatabase", it.message)
            }
    }

    fun deleteUser() {
        firestore.collection("users").document(mUser!!.uid).delete()
            .addOnSuccessListener {
                Log.i("FirebaseDatabase", "User deleted.")
            }.addOnFailureListener {
                Log.i("FirebaseDatabase", it.message)
            }
    }
}