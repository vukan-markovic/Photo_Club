package vukan.com.photoclub.fragments

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.SoundEffectConstants
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.profile_content.*
import kotlinx.android.synthetic.main.profile_fragment.*
import vukan.com.photoclub.FirestoreDatabase
import vukan.com.photoclub.ImageProcessing
import vukan.com.photoclub.Presenter
import vukan.com.photoclub.R
import vukan.com.photoclub.adapters.ImageAdRecyclerViewAdapter
import vukan.com.photoclub.dataclasses.Image

class ProfileFragment : Fragment(), TextWatcher {
    private val imagePicker: Int = 1
    private val profilePicturePicker: Int = 2
    private var terms: ArrayList<String> = ArrayList()
    private var presenter: Presenter = Presenter(this, FirestoreDatabase())
    private var mAudioManager: AudioManager = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mAnimation: Animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
    private lateinit var userID: String
    private lateinit var adapter: ImageAdRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.profile_fragment,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (arguments != null) {
            userID = arguments?.getString("userID").toString()
            camera_fab.systemUiVisibility = View.GONE
            profile_picture.isEnabled = false
            profile_username.isEnabled = false
        } else {
            userID = FirebaseAuth.getInstance().currentUser?.uid.toString()
            profile_username.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(20))
            profile_username.addTextChangedListener(this)
        }

        user_images.layoutManager = GridLayoutManager(context, 4, RecyclerView.HORIZONTAL, false)
        user_images.overScrollMode = View.OVER_SCROLL_NEVER
        user_images.itemAnimator = DefaultItemAnimator()
        presenter.readUser(userID)
        presenter.readUserImages(userID)
        mAnimation.duration = 100

        camera_fab.setOnClickListener {
            mAudioManager.playSoundEffect(SoundEffectConstants.CLICK)
            showPopUpMenu(it, imagePicker)
        }

        profile_picture.setOnClickListener {
            it?.startAnimation(mAnimation)
            mAudioManager.playSoundEffect(SoundEffectConstants.CLICK)
            showPopUpMenu(it, profilePicturePicker)
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (!TextUtils.isEmpty(s) && s.toString().isNotEmpty() && s.toString().isNotBlank() && s.toString() != "") {
            presenter.updateUsername(s.toString().trim())
            Snackbar.make(view!!, "Username updated!", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    private fun showPopUpMenu(view: View, picker: Int) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.menu_popup_profile)
        popupMenu.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.camera) {
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    takePictureIntent.resolveActivity(context!!.packageManager)?.also {
                        startActivityForResult(takePictureIntent, picker)
                    }
                }
            } else {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_PICK
                intent.action = Intent.ACTION_GET_CONTENT
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                if (intent.resolveActivity(context!!.packageManager) != null)
                    startActivityForResult(
                        Intent.createChooser(
                            intent,
                            "Choose picture"
                        ), picker
                    )
            }

            true
        }

        Runnable {
            popupMenu.show()
        }
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == imagePicker && resultCode == RESULT_OK && data!!.data != null) {
            terms = ImageProcessing(data.getParcelableExtra("data")).processImage()
            presenter.createImage(data.data.toString())
            Snackbar.make(view!!, "New image added!", Snackbar.LENGTH_SHORT).show()
        } else if (requestCode == profilePicturePicker && resultCode == RESULT_OK) {
            presenter.updateProfilePicture(data?.data.toString())
            Snackbar.make(view!!, "Profile picture updated!", Snackbar.LENGTH_SHORT).show()
        }
    }

    fun setUsername(username: String) {
        profile_username.setText(username)
    }

    fun setAdapter(images: List<Image>) {
        adapter = ImageAdRecyclerViewAdapter(images, presenter, this)
        user_images.adapter = adapter
    }

    fun getAdapter(): ImageAdRecyclerViewAdapter {
        return adapter
    }

    fun getTerms(): ArrayList<String> {
        return terms
    }

    fun getProfilePicture(): CircleImageView {
        return profile_picture
    }
}