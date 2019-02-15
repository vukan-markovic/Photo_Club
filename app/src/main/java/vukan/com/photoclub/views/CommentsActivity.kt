package vukan.com.photoclub.views

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.InputFilter
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.image_comments_activity.*
import vukan.com.photoclub.R
import vukan.com.photoclub.adapters.CommentRecyclerViewAdapter
import vukan.com.photoclub.database.Database
import vukan.com.photoclub.models.Comment

class CommentsActivity : AppCompatActivity() {
    private var commentsList: MutableList<Comment> = ArrayList()
    private var mDatabase: Database = Database()
    private var mAdapter: CommentRecyclerViewAdapter? = null
    private lateinit var mImageUrl: String
    private lateinit var collection: CollectionReference
    private lateinit var listener: ListenerRegistration
    private lateinit var mAnimation: Animation
    private lateinit var mInterstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_comments_activity)
        MobileAds.initialize(this, getString(R.string.adMob_id))
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = getString(R.string.add_id)
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                if (!mInterstitialAd.isLoading && !mInterstitialAd.isLoaded) mInterstitialAd.loadAd(AdRequest.Builder().build())
            }
        }
        if (!mInterstitialAd.isLoading && !mInterstitialAd.isLoaded) mInterstitialAd.loadAd(AdRequest.Builder().build())
        mAnimation = AnimationUtils.loadAnimation(this, R.anim.fade)
        mAnimation.duration = 150
        comment_input.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(60))
        comments.layoutManager = LinearLayoutManager(this)
        mAdapter = CommentRecyclerViewAdapter(commentsList, mDatabase, this)
        comments.adapter = mAdapter

        if (intent != null && intent.hasExtra("imageUrl")) {
            mImageUrl = intent.getStringExtra("imageUrl")
            collection = FirebaseFirestore.getInstance().collection("images").document(mImageUrl).collection("comments")
        }

        comment_send.setOnClickListener {
            if (comment_input.text.toString().trim().isNotBlank()) {
                it.startAnimation(mAnimation)
                mDatabase.createComment(mImageUrl, comment_input.text.toString())
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (mInterstitialAd.isLoaded) mInterstitialAd.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        listener = collection.addSnapshotListener(MetadataChanges.INCLUDE) { snapshots, _ ->
            if (snapshots != null && !snapshots.isEmpty) {
                for (dc in snapshots.documentChanges) {
                    val comment = dc.document.toObject(Comment::class.java)
                    if (dc.type == DocumentChange.Type.ADDED) {
                        commentsList.add(comment)
                        mAdapter?.notifyItemInserted(commentsList.indexOf(comment))
                    } else if (dc.type == DocumentChange.Type.REMOVED) {
                        commentsList.remove(comment)
                        mAdapter?.notifyItemRemoved(commentsList.indexOf(comment))
                    }
                }
                mAdapter = CommentRecyclerViewAdapter(commentsList, mDatabase, this)
                comments.adapter = mAdapter
                mAdapter!!.notifyDataSetChanged()
                comments.invalidate()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        commentsList.clear()
        listener.remove()
    }

    fun getImageUrl(): String? {
        return mImageUrl
    }
}