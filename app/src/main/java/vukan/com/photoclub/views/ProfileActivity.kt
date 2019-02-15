package vukan.com.photoclub.views

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.profile_activity.*
import vukan.com.photoclub.R
import vukan.com.photoclub.adapters.UserImageRecyclerViewAdapter
import vukan.com.photoclub.database.Database
import vukan.com.photoclub.models.Image
import java.util.*

class ProfileActivity : AppCompatActivity(), TextWatcher {
    private val imagePicker: Int = 1
    private val profilePicturePicker: Int = 2
    private var isCamera = false
    private var images: MutableList<Image> = ArrayList()
    private lateinit var mAdapterUser: UserImageRecyclerViewAdapter
    private lateinit var mDatabase: Database
    private lateinit var mAnimation: Animation
    private lateinit var query: Query
    private lateinit var listener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)
        mDatabase = Database(this)
        user_images.layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        mAnimation = AnimationUtils.loadAnimation(this, R.anim.fade)
        mAnimation.duration = 150

        if (intent != null && intent.hasExtra("userID")) {
            if (intent.getStringExtra("userID") != FirebaseAuth.getInstance().currentUser?.uid) {
                query = FirebaseFirestore.getInstance().collection("images")
                    .whereEqualTo("userID", intent.getStringExtra("userID"))
                camera.hide()
                profile_picture.isClickable = false
                profile_picture.setOnClickListener(null)
                mDatabase.readUser(intent.getStringExtra("userID"))
            } else loadThisUser()
        } else loadThisUser()
        mAdapterUser = UserImageRecyclerViewAdapter(images, mDatabase, this)
        user_images.adapter = mAdapterUser
    }

    private fun loadThisUser() {
        query = FirebaseFirestore.getInstance().collection("images")
            .whereEqualTo("userID", FirebaseAuth.getInstance().currentUser!!.uid)
        mDatabase.readUser(FirebaseAuth.getInstance().currentUser!!.uid)
        profile_username.addTextChangedListener(this)
        profile_username.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(20))

        camera.setOnClickListener {
            showPopUpMenu(it, imagePicker)
        }

        profile_picture.setOnClickListener {
            it.startAnimation(mAnimation)
            showPopUpMenu(it, profilePicturePicker)
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (s.toString().trim().isNotBlank()) mDatabase.updateUsername(s.toString().trim())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    private fun showPopUpMenu(view: View, picker: Int) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.menu_popup_profile)
        popupMenu.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.camera_upload) {
                isCamera = true
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (intent.resolveActivity(packageManager) != null)
                    startActivityForResult(
                        intent,
                        picker,
                        ActivityOptions.makeCustomAnimation(
                            this,
                            R.anim.fade_in,
                            R.anim.fade_out
                        ).toBundle()
                    )
            } else if (item.itemId == R.id.file_upload) {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/jpg"
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                if (intent.resolveActivity(packageManager) != null)
                    startActivityForResult(
                        Intent.createChooser(
                            intent,
                            getString(R.string.choose_picture)
                        ),
                        picker,
                        ActivityOptions.makeCustomAnimation(
                            this,
                            R.anim.fade_in,
                            R.anim.fade_out
                        ).toBundle()
                    )
            }
            true
        }
        popupMenu.show()
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == RESULT_OK && intent != null) {
            if (requestCode == imagePicker) {
                if (isCamera) mDatabase.createImageBitmap(intent.getParcelableExtra("data"))
                else if (intent.data != null) mDatabase.createImage(intent.data)
                Snackbar.make(profile_activity, getString(R.string.image_added), Snackbar.LENGTH_SHORT).show()
            } else if (requestCode == profilePicturePicker) {
                if (isCamera) {
                    Glide.with(this).load(intent.getParcelableExtra("data") as Bitmap)
                        .into(profile_picture)
                    mDatabase.updateProfilePictureBitmap(intent.getParcelableExtra("data"))
                } else if (intent.data != null) {
                    Glide.with(this).load(intent.data).into(profile_picture)
                    mDatabase.updateProfilePicture(intent.data)
                }
                Snackbar.make(profile_activity, getString(R.string.profile_picture_updated), Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
        isCamera = false
    }

    override fun onResume() {
        super.onResume()
        listener = query.addSnapshotListener(MetadataChanges.INCLUDE) { snapshots, _ ->
            if (snapshots != null && !snapshots.isEmpty) {
                for (dc in snapshots.documentChanges) {
                    val image = dc.document.toObject(Image::class.java)
                    if (dc.type == DocumentChange.Type.ADDED) {
                        images.add(image)
                        mAdapterUser.notifyItemInserted(images.indexOf(image))
                    } else if (dc.type == DocumentChange.Type.REMOVED) {
                        images.remove(image)
                        mAdapterUser.notifyItemRemoved(images.indexOf(image))
                    }
                }
            }
            mAdapterUser = UserImageRecyclerViewAdapter(images, mDatabase, this)
            user_images.adapter = mAdapterUser
            mAdapterUser.notifyDataSetChanged()
            user_images.invalidate()
        }
    }

    override fun onPause() {
        super.onPause()
        images.clear()
        listener.remove()
    }

    fun setUsername(username: String) {
        profile_username.setText(username)
        if (intent != null && intent.hasExtra("userID")) {
            if (intent.getStringExtra("userID") != FirebaseAuth.getInstance().currentUser?.uid)
                profile_username.isEnabled = false
        }
    }

    fun getProfilePicture(): CircleImageView {
        return profile_picture
    }
}