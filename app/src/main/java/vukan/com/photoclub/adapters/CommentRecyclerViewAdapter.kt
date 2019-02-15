package vukan.com.photoclub.adapters

import android.app.ActivityOptions
import android.content.Intent
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.image_comment.view.*
import vukan.com.photoclub.GlideApp
import vukan.com.photoclub.R
import vukan.com.photoclub.database.Database
import vukan.com.photoclub.models.Comment
import vukan.com.photoclub.views.CommentsActivity
import vukan.com.photoclub.views.ProfileActivity

class CommentRecyclerViewAdapter(var comments: List<Comment>, var database: Database, var activity: CommentsActivity) :
    RecyclerView.Adapter<CommentRecyclerViewAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.image_comment, parent, false))
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        if (comments.isNotEmpty()) {
            holder.view.comment_content.text = comments[position].content
            holder.view.comment_username.text = comments[position].username
            holder.view.comment_date_time.text = comments[position].dateTime.toDate().toString()
            GlideApp.with(activity).load(FirebaseStorage.getInstance().reference.child(comments[position].userID))
                .into(holder.view.comment_profile_picture)
        }
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    inner class CommentViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        private val mAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade)

        init {
            mAnimation.duration = 125

            if (comments.isNotEmpty()) {
                view.setOnLongClickListener {
                    val popupMenu = PopupMenu(activity, it)
                    popupMenu.inflate(R.menu.menu_popup_comment)
                    popupMenu.setOnMenuItemClickListener { item ->
                        if (item.itemId == R.id.delete_comment) {
                            database.deleteComment(
                                comments[adapterPosition].commentID,
                                activity.getImageUrl()!!
                            )
                            notifyItemRemoved(adapterPosition)
                            Snackbar.make(
                                it,
                                activity.getString(R.string.deleted_comment),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        true
                    }
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        if (comments[adapterPosition].userID == FirebaseAuth.getInstance().currentUser?.uid) popupMenu.show()
                    }
                    true
                }
            }

            view.comment_profile_picture.setOnClickListener {
                it.startAnimation(mAnimation)
                val profileIntent = Intent(activity, ProfileActivity::class.java)
                profileIntent.putExtra("userID", comments[adapterPosition].userID)
                activity.startActivity(
                    profileIntent, ActivityOptions.makeCustomAnimation(
                        activity,
                        R.anim.fade_in,
                        R.anim.fade_out
                    ).toBundle()
                )
            }
        }
    }
}