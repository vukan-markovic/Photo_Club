package vukan.com.photoclub.views

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.SoundEffectConstants
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.image_comment.*
import kotlinx.android.synthetic.main.image_comments_activity.*
import vukan.com.photoclub.database.Database
import vukan.com.photoclub.R
import vukan.com.photoclub.adapters.CommentRecyclerViewAdapter
import vukan.com.photoclub.models.Comment

class CommentsActivity : AppCompatActivity() {
    private var commentsList: MutableList<Comment> = ArrayList()
    private var mDatabase: Database = Database()
    private var mAdapter: CommentRecyclerViewAdapter? = null
    private lateinit var mImageUrl: String
    private lateinit var collection: CollectionReference
    private lateinit var listener: ListenerRegistration
    private lateinit var mAnimation: Animation
    private lateinit var mAudioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_comments_activity)
        mAnimation = AnimationUtils.loadAnimation(this, R.anim.fade)
        mAnimation.duration = 100
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        comments.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        if (intent != null && intent.hasExtra("imageUrl")) {
            mImageUrl = intent.getStringExtra("imageUrl")
            collection = FirebaseFirestore.getInstance().collection("images").document(mImageUrl).collection("comments")
        }

        comment_send.setOnClickListener {
            it.startAnimation(mAnimation)
            mAudioManager.playSoundEffect(SoundEffectConstants.CLICK)
            mDatabase.createComment(mImageUrl, comment_content.text.toString())
            Snackbar.make(it, getString(R.string.comment_added), Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        listener = collection.addSnapshotListener(MetadataChanges.INCLUDE) { snapshots, _ ->
            if (snapshots != null && !snapshots.isEmpty) {
                if (commentsList.isEmpty()) {
                    for (d in snapshots) commentsList.add(d.toObject(Comment::class.java))
                    mAdapter = CommentRecyclerViewAdapter(commentsList, mDatabase, this)
                    comments.adapter = mAdapter
                    mAdapter!!.notifyDataSetChanged()
                }
                for (dc in snapshots.documentChanges) {
                    val comment = dc.document.toObject(Comment::class.java)
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            commentsList.add(comment)
                            mAdapter?.notifyItemInserted(commentsList.indexOf(comment))
                        }
                        DocumentChange.Type.MODIFIED -> {
                            var index = 0
                            for (c in commentsList) {
                                if (c.commentID == comment.commentID) index = commentsList.indexOf(c)
                                break
                            }
                            commentsList[index] = comment
                            mAdapter?.notifyItemChanged(index)
                        }
                        DocumentChange.Type.REMOVED -> {
                            val index = commentsList.indexOf(comment)
                            commentsList.remove(comment)
                            mAdapter?.notifyItemRemoved(index)
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        listener.remove()
    }

    fun getImageUrl(): String? {
        return mImageUrl
    }
}