package vukan.com.photoclub

import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import vukan.com.photoclub.dataclasses.Comment
import vukan.com.photoclub.dataclasses.Image
import vukan.com.photoclub.dataclasses.User
import vukan.com.photoclub.fragments.*
import java.util.*

class Presenter(private var view: Fragment, private var database: FirestoreDatabase) {
    private val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    fun createComment(imageUrl: String) {
        val commentView = view as CommentsFragment
        database.createComment(
            Comment(
                System.currentTimeMillis().toString(),
                user?.uid.toString(),
                commentView.getCommentContent(),
                Timestamp.now(),
                user?.photoUrl.toString(),
                user?.displayName.toString()
            ), imageUrl
        )
        commentView.getAdapter().notifyItemInserted(commentView.getAdapter().itemCount - 1)
    }

    fun readComments(imageUrl: String) {
        val commentView = view as CommentsFragment
        commentView.setAdapter(database.readComments(imageUrl))
        commentView.getAdapter().notifyDataSetChanged()
    }

    fun updateComment(commentID: String, imageUrl: String) {
        val commentView: CommentsFragment = (view as CommentsFragment)
        database.updateComment(commentID, commentView.getCommentContent(), imageUrl)
    }

    fun deleteComment(commentID: String, imageUrl: String) {
        database.deleteComment(commentID, imageUrl)
    }

    fun createImage(imageUrl: String) {
        val userView = view as ProfileFragment
        database.createImage(
            Image(
                Timestamp.now(),
                imageUrl,
                userView.getTerms(),
                user!!.uid,
                user.photoUrl.toString(), 0L
            )
        )
        userView.getAdapter().notifyItemInserted(userView.getAdapter().itemCount - 1)
    }

    fun readImages() {
        val mainView: MainFragment = view as MainFragment
        mainView.setAdapter(database.readImages())
        mainView.getAdapter().notifyDataSetChanged()
    }

    fun readImage(imageUrl: String) {
        val imageView: ImageDetailsFragment = view as ImageDetailsFragment
        val image: Image = database.readImage(imageUrl)
        imageView.setDateTime(image.dateTime)
        imageView.setLikesCount(image.likesCount)
        Glide.with(imageView.getImage().context).asDrawable().load(image.imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView.getImage())
    }

    fun readImagesSearch(query: String) {
        val exploreView: ExploreFragment = view as ExploreFragment
        exploreView.setAdapter(database.readImagesSearch(query))
        exploreView.getAdapter().notifyDataSetChanged()
    }

    fun readUserImages(userID: String) {
        val userView: ProfileFragment = view as ProfileFragment
        userView.setAdapter(database.readUserImages(userID))
        userView.getAdapter().notifyDataSetChanged()
    }

    fun updateImage(imageUrl: String) {
        database.updateImage(imageUrl)
    }

    fun deleteImage(imageUrl: String) {
        database.deleteImage(imageUrl)
    }

    fun downloadImage(imageUrl: String) {
        database.downloadImage(imageUrl)
    }

    fun createUser() {
        val image = user?.photoUrl
            ?: "https://moonvillageassociation.org/wp-content/uploads/2018/06/default-profile-picture1-744x744.jpg"
        val name: String = user?.displayName ?: "User" + UUID.randomUUID()
        database.createUser(User(image.toString(), user!!.uid, name))
    }

    fun readUser(userID: String) {
        val userView: ProfileFragment = view as ProfileFragment
        val user: User = database.readUser(userID)
        Glide.with(userView.getProfilePicture().context).asDrawable().load(user.profilePictureUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(userView.getProfilePicture())
        userView.setUsername(user.username)
    }

    fun updateProfilePicture(imageUrl: String) {
        val userView: ProfileFragment = view as ProfileFragment
        database.updateProfilePicture(imageUrl)
        Glide.with(userView.getProfilePicture().context).asDrawable().load(imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(userView.getProfilePicture())
    }

    fun updateUsername(username: String) {
        database.updateUsername(username)
    }

    fun addAdd(index: Int, ad: UnifiedNativeAd) {
        val mainView: MainFragment = view as MainFragment
        val images: MutableList<Any> = database.readImages()
        images.add(index, ad)
        mainView.setAdapter(images)
        mainView.getAdapter().notifyDataSetChanged()
    }
}