package vukan.com.photoclub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import kotlinx.android.synthetic.main.native_ad.view.*
import vukan.com.photoclub.R
import vukan.com.photoclub.databinding.ImageBinding
import vukan.com.photoclub.dataclasses.Image
import vukan.com.photoclub.viewmodels.ImageViewModel

class ImageAdRecyclerViewAdapter(
    private var list: ArrayList<Any>,
    listener: ItemClickListener,
    var fragment: Fragment
) : PagedListAdapter<Image, RecyclerView.ViewHolder>(DIFF_CALLBACK) {
    private var itemClickListener: ItemClickListener = listener
    private var viewModel: ImageViewModel = ViewModelProviders.of(fragment).get(ImageViewModel::class.java)
    private val image = 0
    private val ad = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ad -> {
                val unifiedNativeLayoutView = LayoutInflater.from(
                    parent.context
                ).inflate(
                    R.layout.native_ad,
                    parent, false
                )
                return UnifiedNativeAdViewHolder(unifiedNativeLayoutView)
            }
            else -> {
                return ImageViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.image,
                        parent, false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        when (viewType) {
            ad -> {
                val nativeAd = list[position] as UnifiedNativeAd
                populateNativeAdView(nativeAd, (holder as UnifiedNativeAdViewHolder).adView)
            }
            else -> {
                val imageHolder = holder as ImageViewHolder
                imageHolder.binding.viewModel = viewModel
                imageHolder.binding.setVariable(BR.viewModel, image)
                imageHolder.binding.executePendingBindings()
            }
        }
    }

    inner class ImageViewHolder(var binding: ImageBinding) : RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        override fun onClick(v: View?) {
            itemClickListener.onItemClick(viewModel.image.value?.imageUrl!!)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val recyclerViewItem = list[position]
        return if (recyclerViewItem is UnifiedNativeAd) {
            ad
        } else image
    }

    interface ItemClickListener {
        fun onItemClick(imageUrl: String)
    }

    private fun populateNativeAdView(
        nativeAd: UnifiedNativeAd,
        adView: UnifiedNativeAdView
    ) {
        (adView.headlineView as TextView).text = nativeAd.headline
        (adView.bodyView as TextView).text = nativeAd.body
        (adView.callToActionView as Button).text = nativeAd.callToAction
        val icon = nativeAd.icon

        if (icon == null) adView.iconView.visibility = View.INVISIBLE
        else {
            (adView.iconView as ImageView).setImageDrawable(icon.drawable)
            adView.iconView.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) adView.priceView.visibility = View.INVISIBLE
        else {
            adView.priceView.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) adView.storeView.visibility = View.INVISIBLE
        else {
            adView.storeView.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) adView.starRatingView.visibility = View.INVISIBLE
        else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) adView.advertiserView.visibility = View.INVISIBLE
        else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }
        adView.setNativeAd(nativeAd)
    }

    inner class UnifiedNativeAdViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val adView: UnifiedNativeAdView = view.ad_view as UnifiedNativeAdView

        init {
            adView.mediaView = adView.ad_media as MediaView
            adView.headlineView = adView.ad_headline
            adView.bodyView = adView.ad_body
            adView.callToActionView = adView.ad_call_to_action
            adView.iconView = adView.ad_icon
            adView.priceView = adView.ad_price
            adView.starRatingView = adView.ad_stars
            adView.storeView = adView.ad_store
            adView.advertiserView = adView.ad_advertiser
        }
    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<Image>() {
            override fun areItemsTheSame(
                oldImage: Image,
                newImage: Image
            ): Boolean =
                oldImage.imageUrl == newImage.imageUrl

            override fun areContentsTheSame(
                oldImage: Image,
                newImage: Image
            ): Boolean =
                oldImage == newImage
        }
    }
}