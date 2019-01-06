package vukan.com.photoclub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import vukan.com.photoclub.BR
import vukan.com.photoclub.R
import vukan.com.photoclub.databinding.ImageCommentBinding
import vukan.com.photoclub.dataclasses.Comment
import vukan.com.photoclub.viewmodels.CommentViewModel

class CommentRecyclerViewAdapter(
    options: FirestorePagingOptions<Comment>,
    listener: ItemClickListener,
    var fragment: Fragment
) :
    FirestorePagingAdapter<Comment, CommentRecyclerViewAdapter.ImageViewHolder>(options) {
    private var itemClickListener: ItemClickListener = listener
    private var viewModel: CommentViewModel = ViewModelProviders.of(fragment).get(CommentViewModel::class.java)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.image_comment,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int, comment: Comment) {
        holder.binding.viewModel = viewModel
        holder.binding.setVariable(BR.viewModel, viewModel)
        holder.binding.executePendingBindings()
    }

    inner class ImageViewHolder(var binding: ImageCommentBinding) : RecyclerView.ViewHolder(binding.root),
        View.OnLongClickListener {

        override fun onLongClick(v: View?): Boolean {
            itemClickListener.onItemClick(viewModel.comment.value?.commentID!!)
            return true
        }
    }

    interface ItemClickListener {
        fun onItemClick(commentID: String)
    }
}