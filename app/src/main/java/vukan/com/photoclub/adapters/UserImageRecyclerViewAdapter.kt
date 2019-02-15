package vukan.com.photoclub.adapters

import android.app.ActivityOptions
import android.content.Intent
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import com.google.firebase.auth.FirebaseAuth
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

    override fun onBindViewHolder(holderUser: UserImageViewHolder, position: Int) {
        if (images.isNotEmpty()) {
            GlideApp.with(activity)
                .load(FirebaseStorage.getInstance().reference.child(images[position].imageUrl))
                .into(holderUser.view.image_user)
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    inner class UserImageViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        private val mAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade)

        init {
            mAnimation.duration = 125

            view.setOnClickListener {
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

            if (images.isNotEmpty()) {
                view.setOnLongClickListener {
                    val popupMenu = PopupMenu(activity, it)
                    popupMenu.inflate(R.menu.menu_user_image)
                    popupMenu.setOnMenuItemClickListener { item ->
                        if (item.itemId == R.id.delete_image) {
                            database.deleteImage(images[adapterPosition].imageUrl)
                            notifyItemRemoved(adapterPosition)
                            Snackbar.make(it, activity.getString(R.string.image_deleted), Snackbar.LENGTH_SHORT)
                                .show()
                        }
                        true
                    }
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        if (images[adapterPosition].userID == FirebaseAuth.getInstance().currentUser?.uid)
                            popupMenu.show()
                    }
                    true
                }
            }
        }
    }
}