package vukan.com.photoclub.views

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.SoundEffectConstants
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.profile_activity.*
import vukan.com.photoclub.R
import vukan.com.photoclub.adapters.UserImageRecyclerViewAdapter
import vukan.com.photoclub.database.Database
import vukan.com.photoclub.models.Image
import java.io.File

class ProfileActivity : AppCompatActivity(), TextWatcher {
    private val imagePicker: Int = 1
    private val profilePicturePicker: Int = 2
    private var isCamera = false
    private var images: MutableList<Image> = ArrayList()
    private val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private lateinit var mAdapterHome: UserImageRecyclerViewAdapter
    private lateinit var mDatabase: Database
    private lateinit var mAudioManager: AudioManager
    private lateinit var mAnimation: Animation
    private lateinit var imageUrl: String
    private lateinit var query: Query
    private lateinit var listener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)
        mDatabase = Database(this)

        if (intent != null && intent.hasExtra("userID")) {
            query = FirebaseFirestore.getInstance().collection("images")
                .whereEqualTo("userID", intent.getStringExtra("userID"))
            mDatabase.readUser(intent.getStringExtra("userID"))
            camera.systemUiVisibility = View.GONE
            profile_picture.isClickable = false
        } else {
            query = FirebaseFirestore.getInstance().collection("images").whereEqualTo("userID", user!!.uid)
            profile_username.setText(user.displayName.toString())
            Glide.with(profile_picture.context).load(user.photoUrl).into(profile_picture)
            profile_username.addTextChangedListener(this)
        }

        mAdapterHome = UserImageRecyclerViewAdapter(images, mDatabase, this)
        user_images.adapter = mAdapterHome
        user_images.layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAnimation = AnimationUtils.loadAnimation(this, R.anim.fade)
        mAnimation.duration = 100

        camera.setOnClickListener {
            mAudioManager.playSoundEffect(SoundEffectConstants.CLICK)
            showPopUpMenu(it, imagePicker)
        }

        profile_picture.setOnClickListener {
            it.startAnimation(mAnimation)
            mAudioManager.playSoundEffect(SoundEffectConstants.CLICK)
            showPopUpMenu(it, profilePicturePicker)
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (s.toString().isNotBlank()) mDatabase.updateUsername(s.toString().trim())
    }

    private fun createImageFile(): File {
        return File.createTempFile(
            System.currentTimeMillis().toString(),
            ".jpg",
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        ).apply {
            imageUrl = absolutePath
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    private fun showPopUpMenu(view: View, picker: Int) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.menu_popup_profile)
        popupMenu.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.camera_upload) {
                isCamera = true
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    takePictureIntent.resolveActivity(packageManager).also {
                        takePictureIntent.putExtra(
                            MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                                this,
                                "vukan.com.photoclub.fileprovider",
                                createImageFile()
                            )
                        )
                        startActivityForResult(
                            takePictureIntent,
                            picker,
                            ActivityOptions.makeCustomAnimation(
                                this,
                                R.anim.fade_in,
                                R.anim.fade_out
                            ).toBundle()
                        )
                    }
                }
            } else if (item.itemId == R.id.file_upload) {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
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
        if (resultCode == RESULT_OK && intent != null && intent.data != null) {
            if (requestCode == imagePicker) {
                if (isCamera) mDatabase.createImage(Uri.parse(imageUrl))
                else mDatabase.createImage(intent.data)
                Snackbar.make(profile_activity, getString(R.string.image_added), Snackbar.LENGTH_SHORT).show()
            } else if (requestCode == profilePicturePicker) {
                if (isCamera) {
                    Glide.with(this).load(imageUrl).into(profile_picture)
                    mDatabase.updateProfilePicture(Uri.parse(imageUrl))
                } else {
                    Glide.with(this).load(intent.data).into(profile_picture)
                    mDatabase.updateProfilePicture(intent.data)
                }
                Snackbar.make(profile_activity, getString(R.string.profile_picture_updated), Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
        isCamera = false
    }

    override fun onStart() {
        super.onStart()
        listener = query.addSnapshotListener(MetadataChanges.INCLUDE) { snapshots, _ ->
            if (snapshots != null && !snapshots.isEmpty) {
                for (dc in snapshots.documentChanges) {
                    val image = dc.document.toObject(Image::class.java)
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            images.add(image)
                            mAdapterHome.notifyItemInserted(images.indexOf(image))
                        }
                        DocumentChange.Type.MODIFIED -> {
                            var index = 0
                            for (c in images) {
                                if (c.imageUrl == image.imageUrl) index = images.indexOf(c)
                                break
                            }
                            images[index] = image
                            mAdapterHome.notifyItemChanged(index)
                        }
                        DocumentChange.Type.REMOVED -> {
                            val index = images.indexOf(image)
                            images.remove(image)
                            mAdapterHome.notifyItemRemoved(index)
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

    fun setUsername(username: String) {
        profile_username.setText(username)
        if (intent != null && intent.hasExtra("userID")) profile_username.isEnabled = false
    }

    fun getProfilePicture(): CircleImageView {
        return profile_picture
    }
}