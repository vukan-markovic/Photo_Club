package vukan.com.photoclub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.image_comment.*
import kotlinx.android.synthetic.main.image_comments_fragment.*
import vukan.com.photoclub.FirestoreDatabase
import vukan.com.photoclub.Presenter
import vukan.com.photoclub.R
import vukan.com.photoclub.adapters.CommentRecyclerViewAdapter
import vukan.com.photoclub.dataclasses.Comment

class CommentsFragment : Fragment() {
    private var presenter: Presenter = Presenter(this, FirestoreDatabase())
    private lateinit var adapter: CommentRecyclerViewAdapter
    private lateinit var imageUrl: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.image_comments_fragment,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        comments.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        comments.overScrollMode = View.OVER_SCROLL_NEVER
        comments.itemAnimator = DefaultItemAnimator()
        imageUrl = arguments?.getString("imageUrl").toString()
        presenter.readComments(imageUrl)

        comment_send.setOnClickListener {
            presenter.createComment(imageUrl)
            Snackbar.make(it, "Added comment!", Snackbar.LENGTH_SHORT).show()
        }
    }

    fun setAdapter(commentsList: List<Comment>) {
        adapter = CommentRecyclerViewAdapter(commentsList, presenter, this)
        comments.adapter = adapter
    }

    fun getAdapter(): CommentRecyclerViewAdapter {
        return adapter
    }

    fun getCommentContent(): String {
        return comment_content.text.toString()
    }

    fun getImageUrl(): String {
        return imageUrl
    }
}