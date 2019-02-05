package vukan.com.photoclub.views

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.SoundEffectConstants
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.image_details_activity.*
import vukan.com.photoclub.R
import vukan.com.photoclub.database.Database

class ImageDetailsActivity : AppCompatActivity() {
    private lateinit var mDatabase: Database
    private lateinit var mImageUrl: String
    private lateinit var mAnimation: Animation
    private lateinit var mAudioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_details_activity)
        mAnimation = AnimationUtils.loadAnimation(this, R.anim.fade)
        mAnimation.duration = 100
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mDatabase = Database(this)

        if (intent != null && intent.hasExtra("imageUrl")) {
            mImageUrl = intent.getStringExtra("imageUrl")
            mDatabase.readImage(mImageUrl)
        }

        comments.setOnClickListener {
            it.startAnimation(mAnimation)
            mAudioManager.playSoundEffect(SoundEffectConstants.CLICK)
            val intentComments = Intent(this, CommentsActivity::class.java)
            intentComments.putExtra("imageUrl", mImageUrl)
            startActivity(
                intentComments, ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.fade_in,
                    R.anim.fade_out
                ).toBundle()
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun getImage(): ImageView {
        return image_detail
    }

    fun setDateTime(dateTime: Timestamp) {
        image_detail_date_time.text = dateTime.toDate().toString()
    }

    fun setLikesCount(count: Long) {
        likesCount.text = count.toString()
    }
}