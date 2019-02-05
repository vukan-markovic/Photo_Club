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
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.image.view.*
import vukan.com.photoclub.GlideApp
import vukan.com.photoclub.R
import vukan.com.photoclub.database.Database
import vukan.com.photoclub.models.Image
import vukan.com.photoclub.views.ImageDetailsActivity
import vukan.com.photoclub.views.ProfileActivity
import java.io.File

class HomeImageRecyclerViewAdapter(var images: List<Image>, var database: Database, var activity: AppCompatActivity) :
    RecyclerView.Adapter<HomeImageRecyclerViewAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.image, parent, false))
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Glide.with(activity)
            .load(images[position].profilePictureUrl)
            .into(holder.view.image_profile_picture)
        GlideApp.with(activity)
            .load(FirebaseStorage.getInstance().reference.child(Uri.parse(images[position].imageUrl).lastPathSegment))
            .into(holder.view.image)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    inner class ImageViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        private val mAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade)
        private val mAudioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        init {
            mAnimation.duration = 100

            view.image.setOnClickListener {
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

            view.image_profile_picture.setOnClickListener {
                it.startAnimation(mAnimation)
                mAudioManager.playSoundEffect(SoundEffectConstants.CLICK)
                val intentProfile = Intent(activity, ProfileActivity::class.java)
                intentProfile.putExtra("userID", images[adapterPosition].userID)
                activity.startActivity(
                    intentProfile, ActivityOptions.makeCustomAnimation(
                        activity,
                        R.anim.fade_in,
                        R.anim.fade_out
                    ).toBundle()
                )
            }

            view.setOnLongClickListener {
                val popupMenu = PopupMenu(activity, it)
                popupMenu.inflate(R.menu.menu_image)
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.download_image -> {
                            database.downloadImage(images[adapterPosition].imageUrl)
                            Snackbar.make(it, activity.getString(R.string.image_downloaded), Snackbar.LENGTH_SHORT)
                                .show()
                        }
                        R.id.share_image -> {
                            val shareIntent = Intent()
                            shareIntent.action = Intent.ACTION_SEND
                            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(images[adapterPosition].imageUrl))
                            shareIntent.type = "image/jpeg"

                            activity.startActivity(
                                Intent.createChooser(
                                    shareIntent,
                                    "Share"
                                ), ActivityOptions.makeCustomAnimation(
                                    activity,
                                    R.anim.fade_in,
                                    R.anim.fade_out
                                ).toBundle()
                            )
                        }
                        R.id.gallery -> {
                            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
                                mediaScanIntent.data = Uri.fromFile(File(images[adapterPosition].imageUrl))
                                activity.sendBroadcast(mediaScanIntent)
                            }
                        }
                    }
                    true
                }
                popupMenu.show()
                true
            }

            view.like.setOnClickListener {
                if (it.contentDescription == "on") {
                    it.setBackgroundResource(R.drawable.ic_like_white)
                    it.contentDescription = "off"
                    database.updateImage(images[adapterPosition].imageUrl, -1)
                } else if (it.contentDescription == "off") {
                    it.setBackgroundResource(R.drawable.ic_like_red)
                    it.contentDescription = "on"
                    database.updateImage(images[adapterPosition].imageUrl, 1)
                }
            }
        }
    }
}