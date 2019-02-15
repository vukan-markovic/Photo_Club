package vukan.com.photoclub.adapters

import android.app.ActivityOptions
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.image.view.*
import vukan.com.photoclub.GlideApp
import vukan.com.photoclub.R
import vukan.com.photoclub.models.Image
import vukan.com.photoclub.views.ImageDetailsActivity
import vukan.com.photoclub.views.ProfileActivity

class HomeImageRecyclerViewAdapter(var images: List<Image>, var activity: AppCompatActivity) :
    RecyclerView.Adapter<HomeImageRecyclerViewAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.image, parent, false))
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        if (images.isNotEmpty()) {
            GlideApp.with(activity)
                .load(FirebaseStorage.getInstance().reference.child(images[position].userID))
                .into(holder.view.image_profile_picture)
            GlideApp.with(activity)
                .load(FirebaseStorage.getInstance().reference.child(images[position].imageUrl))
                .into(holder.view.image_home)
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    inner class ImageViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        private val mAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade)

        init {
            mAnimation.duration = 125

            view.image_home.setOnClickListener {
                it.startAnimation(mAnimation)
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
        }
    }
}