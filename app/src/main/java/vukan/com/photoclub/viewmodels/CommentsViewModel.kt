package vukan.com.photoclub.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import vukan.com.photoclub.repository.Repository
import vukan.com.photoclub.dataclasses.Comment

class CommentsViewModel : ViewModel() {
    private lateinit var comments: CollectionReference
    private var liveData: Repository =
        Repository(comments)
    private var commentsLiveData: MediatorLiveData<PagedList<Comment>> = MediatorLiveData()

    init {
        commentsLiveData.addSource(liveData) {
            commentsLiveData.postValue(it.toObjects(Comment::class.java) as PagedList<Comment>?)
        }
    }

    fun createComment(comment: Comment) {
        liveData.createComment(comment)
    }

    fun readComments(): Query {
        return liveData.readComments()
    }

    fun updateComment(commentId: String, content: String) {
        liveData.updateComment(commentId, content)
    }

    fun deleteComment(commentId: String) {
        liveData.deleteComment(commentId)
    }

    fun setImageUrl(imageUrl: String) {
        comments = FirebaseFirestore.getInstance().collection("images").document(imageUrl).collection("comments")
    }
}