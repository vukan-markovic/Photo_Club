package vukan.com.photoclub.fragments

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.SoundEffectConstants
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.android.synthetic.main.profile_content.*
import kotlinx.android.synthetic.main.profile_fragment.*
import vukan.com.photoclub.R
import vukan.com.photoclub.adapters.ImageRecyclerViewAdapter
import vukan.com.photoclub.databinding.ProfileFragmentBinding
import vukan.com.photoclub.dataclasses.Image
import vukan.com.photoclub.viewmodels.ImageViewModel
import vukan.com.photoclub.viewmodels.ImagesViewModel
import vukan.com.photoclub.viewmodels.UserViewModel
import java.util.*
import kotlin.collections.ArrayList

class ProfileFragment : Fragment(), ImageRecyclerViewAdapter.ItemClickListener {
    private val imagePicker: Int = 1
    private val profilePicturePicker: Int = 2
    private lateinit var mViewModel: UserViewModel
    private lateinit var mImagesViewModel: ImagesViewModel
    private lateinit var mBinding: ProfileFragmentBinding
    private lateinit var mAudioManager: AudioManager
    private lateinit var mAnimation: Animation
    private lateinit var userID: String
    private var user: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
    private var terms: ArrayList<String> = ArrayList()
    private var own: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.profile_fragment,
            container,
            false
        )
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)
        mImagesViewModel = ViewModelProviders.of(this).get(ImagesViewModel::class.java)
        mBinding.viewModel = mViewModel
        mBinding.setLifecycleOwner(this)
        if (ProfileFragmentArgs.fromBundle(arguments!!).userId == FirebaseAuth.getInstance().currentUser!!.uid) {
            userID = FirebaseAuth.getInstance().currentUser!!.uid
            own = true
        } else {
            userID = ProfileFragmentArgs.fromBundle(arguments!!).userId
            camera_fab.systemUiVisibility = View.GONE
            profile_picture.isEnabled = false
            profile_username.isEnabled = false
        }

        mViewModel.readUser(userID)

        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(10)
            .setPageSize(20)
            .build()

        val options = FirestorePagingOptions.Builder<Image>()
            .setLifecycleOwner(this)
            .setQuery(
                mImagesViewModel.readUserImages(userID),
                config,
                Image::class.java
            )
            .build()

        user_images.adapter = ImageRecyclerViewAdapter(options, this, this)
        user_images.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        mAudioManager = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        mAnimation.duration = 100
        profile_username.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(20))

        camera_fab.setOnClickListener {
            mAudioManager.playSoundEffect(SoundEffectConstants.CLICK)
            showPopUpMenu(it, imagePicker)
        }

        profile_picture.setOnClickListener {
            it?.startAnimation(mAnimation)
            mAudioManager.playSoundEffect(SoundEffectConstants.CLICK)
            showPopUpMenu(it, profilePicturePicker)
        }

        update_username.setOnClickListener{
            if (profile_username.text.toString().isNotEmpty() && profile_username.text.toString().isNotBlank() && profile_username.text.toString() != "") mViewModel.updateUsername(userID, profile_username.text.toString())
        }
    }

    private fun showPopUpMenu(view: View, picker: Int) {
        val popupMenu = PopupMenu(context, view)
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
                val chooser = Intent.createChooser(intent, "Choose picture")
                if (intent.resolveActivity(context!!.packageManager) != null)
                    startActivityForResult(chooser, picker)
            }

            true
        }
        popupMenu.show()
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == imagePicker && resultCode == RESULT_OK && data!!.data != null) {
            val image = FirebaseVisionImage.fromBitmap(data.getParcelableExtra("data"))
            val textRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
            val labelDetector = FirebaseVision.getInstance().visionLabelDetector
            val faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(
                FirebaseVisionFaceDetectorOptions.Builder()
                    .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                    .setContourMode(FirebaseVisionFaceDetectorOptions.NO_CONTOURS)
                    .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                    .setLandmarkMode(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
                    .build()
            )

            textRecognizer.processImage(image)
                .addOnSuccessListener {
                    val blocks = it.textBlocks
                    if (blocks.size != 0) {
                        for (i in blocks.indices) {
                            val lines = blocks[i].lines
                            for (j in lines.indices) {
                                val elements = lines[j].elements
                                for (k in elements.indices) {
                                    terms.add(elements[k].text)
                                }
                            }
                        }
                    }
                }

            labelDetector.detectInImage(image)
                .addOnSuccessListener {
                    for (i in it.iterator()) {
                        terms.add(i.label)
                    }
                }

            faceDetector.detectInImage(image)
                .addOnSuccessListener {
                    for (i in it.iterator()) {
                        if (i.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                            if (i.smilingProbability > 0.7) terms.add("smile")
                            else terms.add("sad")
                        }
                        if (i.rightEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                            if (i.rightEyeOpenProbability < 0.7 && i.leftEyeOpenProbability < 0.7) terms.add("sleep")
                            else terms.add("awake")
                        }
                    }
                }

            @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            val imageView = ImageView(context)
            imageView.setImageURI(data.data)
            mViewModel.createImage(
                Image(
                    Calendar.getInstance().time,
                    data.data.toString(),
                    terms,
                    FirebaseAuth.getInstance().currentUser!!.uid,
                    user.photoUrl.toString()
                ), imageView
            )

        } else if (requestCode == profilePicturePicker && resultCode == RESULT_OK) mViewModel.updateProfilePicture(
            userID,
            data?.data
        )
    }

    override fun onItemClick(imageUrl: String) {
        ProfileFragmentDirections.profileFragmentToImageDetailsFragment(imageUrl)
    }

    fun deleteImage(viewModel: ImageViewModel): Boolean {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.menu_image)
        popupMenu.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.delete_image) mImagesViewModel.deleteImage(viewModel.image.value?.imageUrl!!)
            false
        }

        return true
    }
}