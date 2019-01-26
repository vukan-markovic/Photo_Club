package vukan.com.photoclub.fragments

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.image_details_fragment.*
import kotlinx.android.synthetic.main.main_activity.*
import vukan.com.photoclub.FirestoreDatabase
import vukan.com.photoclub.Presenter
import vukan.com.photoclub.R

class ImageDetailsFragment : Fragment() {
    private var presenter: Presenter = Presenter(this, FirestoreDatabase())
    private lateinit var imageUrl: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.image_details_fragment,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        imageUrl = arguments?.getString("imageUrl").toString()
        presenter.readImage(imageUrl)

        comments.setOnClickListener {
            val commentsFragment = CommentsFragment()
            val bundle = Bundle()
            bundle.putString("imageUrl", imageUrl)
            commentsFragment.arguments = bundle
            fragmentManager?.beginTransaction()?.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
                ?.replace(vukan.com.photoclub.R.id.host_fragment, commentsFragment)?.addToBackStack(null)?.commit()
        }

        val vibrantSwatch = Palette.from(
            MediaStore.Images.Media.getBitmap(
                context?.contentResolver,
                Uri.parse(imageUrl)
            )
        ).generate().vibrantSwatch

        with(toolbar) {
            this.setBackgroundColor(
                vibrantSwatch?.rgb ?: ContextCompat.getColor(
                    context,
                    R.color.primary
                )
            )
            this.setTitleTextColor(
                vibrantSwatch?.titleTextColor ?: ContextCompat.getColor(
                    context,
                    R.color.accent
                )
            )
        }
    }

    fun getImage(): AppCompatImageView {
        return image_detail
    }

    fun setDateTime(dateTime: Timestamp) {
        image_detail_date_time.text = dateTime.toString()
    }

    fun setLikesCount(count: Long) {
        likesCount.text = count.toString()
    }
}