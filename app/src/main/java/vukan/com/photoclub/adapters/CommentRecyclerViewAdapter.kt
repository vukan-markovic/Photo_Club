package vukan.com.photoclub.adapters

import android.app.ActivityOptions
import android.content.Intent
import android.media.AudioManager
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.SoundEffectConstants
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.image_comment.view.*
import kotlinx.android.synthetic.main.profile_activity.view.*
import vukan.com.photoclub.database.Database
import vukan.com.photoclub.R
import vukan.com.photoclub.views.CommentsActivity
import vukan.com.photoclub.views.ProfileActivity
import vukan.com.photoclub.models.Comment

class CommentRecyclerViewAdapter(var comments: List<Comment>, var database: Database, var activity: CommentsActivity) :
    RecyclerView.Adapter<CommentRecyclerViewAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.image_comment, parent, false))
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.view.comment_content.setText(comments[position].content)
        holder.view.comment_username.text = comments[position].username
        holder.view.comment_date_time.text = comments[position].dateTime.toDate().toString()
        Glide.with(activity).load(comments[position].profilePictureUrl).into(holder.view.comment_profile_picture)
        if (comments[position].userID == FirebaseAuth.getInstance().currentUser?.uid)
            holder.view.comment_content.isEnabled = false
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    inner class CommentViewHolder(var view: View) : RecyclerView.ViewHolder(view), TextWatcher {
        private val mAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade)
        private val mAudioManager = activity.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager

        init {
            mAnimation.duration = 100
            if (view.comment_content.isEnabled) view.comment_content.addTextChangedListener(this)

            view.setOnLongClickListener {
                if (activity.getImageUrl() != null) {
                    val popupMenu = PopupMenu(activity, it)
                    popupMenu.inflate(R.menu.menu_popup_comment)
                    popupMenu.setOnMenuItemClickListener { item ->
                        if (item.itemId == R.id.delete_comment) {
                            if (activity.getImageUrl() != null) {
                                database.deleteComment(
                                    comments[adapterPosition].commentID,
                                    activity.getImageUrl()!!
                                )
                                Snackbar.make(
                                    it,
                                    activity.getString(R.string.deleted_comment),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                        true
                    }
                    popupMenu.show()
                }
                true
            }

            view.profile_picture.setOnClickListener {
                it.startAnimation(mAnimation)
                mAudioManager.playSoundEffect(SoundEffectConstants.CLICK)
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

        override fun afterTextChanged(s: Editable?) {
            if (activity.getImageUrl() != null) {
                database.updateComment(
                    comments[adapterPosition].commentID,
                    comments[adapterPosition].content,
                    activity.getImageUrl()!!
                )
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
}