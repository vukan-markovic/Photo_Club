package vukan.com.photoclub.fragments

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.palette.graphics.Palette
import kotlinx.android.synthetic.main.image_details_fragment.*
import kotlinx.android.synthetic.main.main_activity.*
import vukan.com.photoclub.R
import vukan.com.photoclub.databinding.ImageDetailsFragmentBinding
import vukan.com.photoclub.viewmodels.ImageViewModel

class ImageDetailsFragment : Fragment() {
    private lateinit var mViewModel: ImageViewModel
    private lateinit var mBinding: ImageDetailsFragmentBinding
    private val sDecelerator = DecelerateInterpolator()
    private val sOverShooter = OvershootInterpolator(10f)
    private lateinit var imageUrl: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.image_details_fragment,
            container,
            false
        )
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.window?.decorView?.apply { systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE }
        mViewModel = ViewModelProviders.of(this).get(ImageViewModel::class.java)
        mBinding.viewModel = mViewModel
        mBinding.setLifecycleOwner(this)
        imageUrl = ImageDetailsFragmentArgs.fromBundle(arguments!!).imageUrl
        mViewModel.readImage(imageUrl)
        like.animate().duration = 200

        like.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) v.animate().setInterpolator(sDecelerator).scaleX(.7f).scaleY(.7f)
            else if (event.action == MotionEvent.ACTION_UP) v.animate().setInterpolator(sOverShooter).scaleX(1f).scaleY(1f)
            mViewModel.updateImage(imageUrl)
            false
        }

        comments.setOnClickListener {
            ImageDetailsFragmentDirections.imageDetailsFragmentToCommentsFragment(imageUrl)
        }

        fun createPaletteSync(bitmap: Bitmap): Palette = Palette.from(bitmap).generate()

        val vibrantSwatch = createPaletteSync(
            MediaStore.Images.Media.getBitmap(
                context?.contentResolver,
                Uri.parse(imageUrl)
            )
        ).vibrantSwatch
        with(toolbar) {
            setBackgroundColor(
                vibrantSwatch?.rgb ?: ContextCompat.getColor(
                    context,
                    R.color.primary
                )
            )
            setTitleTextColor(
                vibrantSwatch?.titleTextColor ?: ContextCompat.getColor(
                    context,
                    R.color.accent
                )
            )
        }
    }
}