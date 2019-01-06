package vukan.com.photoclub

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class BindingAdapters {

    @BindingAdapter("imageUrl")
    fun loadImage(view: ImageView, url: String) {
        if (!url.isEmpty())
            Glide.with(view.context).load(url).transition(DrawableTransitionOptions.withCrossFade()).into(view)
    }
}