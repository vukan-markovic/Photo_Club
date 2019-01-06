package vukan.com.photoclub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.image_comment.*
import kotlinx.android.synthetic.main.image_comments_fragment.*
import vukan.com.photoclub.R
import vukan.com.photoclub.adapters.CommentRecyclerViewAdapter
import vukan.com.photoclub.databinding.ImageCommentsFragmentBinding
import vukan.com.photoclub.dataclasses.Comment
import vukan.com.photoclub.viewmodels.CommentViewModel
import vukan.com.photoclub.viewmodels.CommentsViewModel
import java.util.*

class CommentsFragment : Fragment(), CommentRecyclerViewAdapter.ItemClickListener {
    private lateinit var mViewModel: CommentsViewModel
    private lateinit var mBinding: ImageCommentsFragmentBinding
    private lateinit var commentID: String
    private var user: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.image_comments_fragment,
            container,
            false
        )
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProviders.of(this).get(CommentsViewModel::class.java)
        mViewModel.setImageUrl(CommentsFragmentArgs.fromBundle(arguments!!).imageUrl)
        mBinding.viewModel = mViewModel
        mBinding.setLifecycleOwner(this)

        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(10)
            .setPageSize(20)
            .build()

        val options = FirestorePagingOptions.Builder<Comment>()
            .setLifecycleOwner(this)
            .setQuery(
                mViewModel.readComments(),
                config,
                Comment::class.java
            )
            .build()

        comments.adapter = CommentRecyclerViewAdapter(options, this, this)
        comments.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        comments.setHasFixedSize(true)

        comment_send.setOnClickListener {
            mViewModel.createComment(
                Comment(
                    System.currentTimeMillis().toString(),
                    FirebaseAuth.getInstance().currentUser!!.uid,
                    comment_content.text.toString(),
                    Calendar.getInstance().time,
                    user.photoUrl.toString(),
                    user.displayName.toString()
                )
            )
        }

        update_comment.setOnClickListener {
            mViewModel.updateComment(commentID, comment_content.text.toString())
        }
    }

    override fun onItemClick(commentID: String) {
        this.commentID = commentID
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.menu_popup_comment)
        popupMenu.setOnMenuItemClickListener {
            if (it.itemId == R.id.update_comment) {
                comment_content.isEnabled = true
                update_comment.visibility = View.VISIBLE
            }
            else mViewModel.deleteComment(commentID)
            true
        }
        Runnable {
            popupMenu.show()
        }
    }

    fun getUser(viewModel: CommentViewModel) {
        CommentsFragmentDirections.commentsFragmentToProfileFragment(viewModel.comment.value!!.userID)
    }
}