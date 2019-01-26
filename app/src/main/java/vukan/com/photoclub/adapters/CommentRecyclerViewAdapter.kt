package vukan.com.photoclub.adapters

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.image_comment.view.*
import kotlinx.android.synthetic.main.profile_fragment.view.*
import vukan.com.photoclub.Presenter
import vukan.com.photoclub.R
import vukan.com.photoclub.dataclasses.Comment
import vukan.com.photoclub.fragments.CommentsFragment
import vukan.com.photoclub.fragments.ProfileFragment

class CommentRecyclerViewAdapter(
    var comments: List<Comment>,
    var presenter: Presenter,
    var fragment: CommentsFragment
) :
    RecyclerView.Adapter<CommentRecyclerViewAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.image_comment, parent, false))
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        with(holder.view) {
            this.comment_content.setText(comments[position].content)
            this.comment_username.text = comments[position].username
            this.comment_date_time.text = comments[position].dateTime.toString()
        }
        Glide.with(holder.view.comment_profile_picture.context).asDrawable().load(comments[position].profilePictureUrl)
            .transition(
                DrawableTransitionOptions.withCrossFade()
            ).into(holder.view.comment_profile_picture)
    }

    inner class CommentViewHolder(var view: View) : RecyclerView.ViewHolder(view), TextWatcher {
        init {
            if (comments[adapterPosition].userID == FirebaseAuth.getInstance().currentUser?.uid)
                view.comment_content.isEnabled = false
            else view.comment_content.addTextChangedListener(this)

            view.setOnLongClickListener {
                val popupMenu = PopupMenu(it.context, it)
                popupMenu.inflate(R.menu.menu_popup_comment)
                popupMenu.setOnMenuItemClickListener { item ->
                    if (item.itemId == R.id.delete_comment) {
                        presenter.deleteComment(
                            comments[adapterPosition].commentID,
                            fragment.getImageUrl()
                        )
                        Snackbar.make(it, "Comment deleted!", Snackbar.LENGTH_SHORT).show()
                        notifyItemRemoved(adapterPosition)
                    }
                    true
                }

                Runnable {
                    popupMenu.show()
                }
                true
            }

            view.profile_picture.setOnClickListener {
                val profileFragment = ProfileFragment()
                val bundle = Bundle()
                bundle.putString("userID", comments[adapterPosition].userID)
                profileFragment.arguments = bundle
                fragment.fragmentManager?.beginTransaction()?.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                    ?.replace(vukan.com.photoclub.R.id.host_fragment, profileFragment)?.addToBackStack(null)?.commit()
            }
        }

        override fun afterTextChanged(s: Editable?) {
            presenter.updateComment(comments[adapterPosition].commentID, fragment.getImageUrl())
            notifyItemChanged(adapterPosition)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    override fun getItemCount(): Int {
        return comments.size
    }
}