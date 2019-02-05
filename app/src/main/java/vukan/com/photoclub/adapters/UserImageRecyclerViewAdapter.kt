package vukan.com.photoclub.adapters

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.SoundEffectConstants
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.user_image.view.*
import vukan.com.photoclub.GlideApp
import vukan.com.photoclub.R
import vukan.com.photoclub.database.Database
import vukan.com.photoclub.models.Image
import vukan.com.photoclub.views.ImageDetailsActivity

class UserImageRecyclerViewAdapter(var images: List<Image>, var database: Database, var activity: AppCompatActivity) :
    RecyclerView.Adapter<UserImageRecyclerViewAdapter.UserImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserImageViewHolder {
        return UserImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.user_image, parent, false))
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onBindViewHolder(holderUser: UserImageViewHolder, position: Int) {
        GlideApp.with(activity)
            .load(FirebaseStorage.getInstance().reference.child(Uri.parse(images[position].imageUrl).lastPathSegment))
            .into(holderUser.view.image_user)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    inner class UserImageViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        private val mAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade)
        private val mAudioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        init {
            mAnimation.duration = 100

            view.setOnClickListener {
                it.startAnimation(mAnimation)
                mAudioManager.playSoundEffect(SoundEffectConstants.CLICK)
                val intentImageDetails = Intent(activity, ImageDetailsActivity::class.java)
                intentImageDetails.putExtra("imageUrl", images[adapterPosition].imageUrl)
                activity.startActivity(
                    intentImageDetails, ActivityOptions.makeCustomAnimation(
                        activity,
                        R.anim.fade_in,
                        R.anim.fade_out
                    ).toBundle()
                )
            }

            view.setOnLongClickListener {
                val popupMenu = PopupMenu(activity, it)
                popupMenu.inflate(R.menu.menu_user_image)
                popupMenu.setOnMenuItemClickListener { item ->
                    if (item.itemId == R.id.delete_image) {
                        database.deleteImage(images[adapterPosition].imageUrl)
                        Snackbar.make(it, activity.getString(R.string.image_deleted), Snackbar.LENGTH_SHORT)
                            .show()
                    }
                    true
                }
                popupMenu.show()
                true
            }
        }
    }
}