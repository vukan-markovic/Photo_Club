package vukan.com.photoclub.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.image.view.*
import kotlinx.android.synthetic.main.native_ad.view.*
import vukan.com.photoclub.Presenter
import vukan.com.photoclub.R
import vukan.com.photoclub.dataclasses.Image
import vukan.com.photoclub.fragments.ImageDetailsFragment
import vukan.com.photoclub.fragments.ProfileFragment

class ImageAdRecyclerViewAdapter(var imagesAds: List<Any>, var presenter: Presenter, var fragment: Fragment) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val image = 0
    private val ad = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ad -> {
                UnifiedNativeAdViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        vukan.com.photoclub.R.layout.native_ad,
                        parent,
                        false
                    )
                )
            }
            else -> {
                ImageViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        vukan.com.photoclub.R.layout.image,
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ad -> {
                populateNativeAdView(
                    imagesAds[position] as UnifiedNativeAd,
                    (holder as UnifiedNativeAdViewHolder).adView
                )
            }
            else -> {
                val imageHolder = holder as ImageViewHolder
                val image: Image = imagesAds[position] as Image
                Glide.with(imageHolder.view.image_profile_picture.context).asDrawable().load(image.profilePictureUrl)
                    .transition(DrawableTransitionOptions.withCrossFade()).into(imageHolder.view.image_profile_picture)
                Glide.with(imageHolder.view.image.context).asDrawable().load(image.imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade()).into(holder.view.image)
            }
        }
    }

    inner class ImageViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        init {
            val image: Image = imagesAds[adapterPosition] as Image

            view.image.setOnClickListener {
                val imageDetailsFragment = ImageDetailsFragment()
                val bundle = Bundle()
                bundle.putString("imageUrl", image.imageUrl)
                imageDetailsFragment.arguments = bundle
                fragment.fragmentManager?.beginTransaction()?.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                    ?.replace(vukan.com.photoclub.R.id.host_fragment, imageDetailsFragment)
                    ?.addToBackStack(null)?.commit()
            }

            view.image_profile_picture.setOnClickListener {
                val profileFragment = ProfileFragment()
                val bundle = Bundle()
                bundle.putString("userID", image.userID)
                profileFragment.arguments = bundle
                fragment.fragmentManager?.beginTransaction()?.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                    ?.replace(vukan.com.photoclub.R.id.host_fragment, profileFragment)
                    ?.addToBackStack(null)?.commit()
            }

            view.setOnLongClickListener {
                val popupMenu = PopupMenu(it.context, it)
                popupMenu.inflate(vukan.com.photoclub.R.menu.menu_image)
                popupMenu.setOnMenuItemClickListener { item ->
                    if (item.itemId == vukan.com.photoclub.R.id.delete_image) {
                        presenter.deleteImage(image.imageUrl)
                        Snackbar.make(it, "Image deleted!", Snackbar.LENGTH_SHORT).show()
                        notifyItemRemoved(adapterPosition)
                    } else if (item.itemId == vukan.com.photoclub.R.id.download_image) {
                        presenter.downloadImage(image.imageUrl)
                        Snackbar.make(it, "Image downloaded!", Snackbar.LENGTH_SHORT).show()
                    }
                    true
                }

                Runnable {
                    popupMenu.show()
                }

                true
            }

            view.like.setOnClickListener {
                it.animate().start()
                presenter.updateImage(image.imageUrl)
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (imagesAds[position] is UnifiedNativeAd) ad else image
    }

    override fun getItemCount(): Int {
        return imagesAds.size
    }

    private fun populateNativeAdView(
        nativeAd: UnifiedNativeAd,
        adView: UnifiedNativeAdView
    ) {
        (adView.headlineView as AppCompatTextView).text = nativeAd.headline
        (adView.bodyView as AppCompatTextView).text = nativeAd.body
        (adView.callToActionView as MaterialButton).text = nativeAd.callToAction
        if (nativeAd.icon == null) adView.iconView.visibility = View.INVISIBLE
        else {
            (adView.iconView as AppCompatImageView).setImageDrawable(nativeAd.icon.drawable)
            adView.iconView.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) adView.priceView.visibility = View.INVISIBLE
        else {
            adView.priceView.visibility = View.VISIBLE
            (adView.priceView as AppCompatTextView).text = nativeAd.price
        }

        if (nativeAd.store == null) adView.storeView.visibility = View.INVISIBLE
        else {
            adView.storeView.visibility = View.VISIBLE
            (adView.storeView as AppCompatTextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) adView.starRatingView.visibility = View.INVISIBLE
        else {
            (adView.starRatingView as AppCompatRatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) adView.advertiserView.visibility = View.INVISIBLE
        else {
            (adView.advertiserView as AppCompatTextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }
        adView.setNativeAd(nativeAd)
    }

    class UnifiedNativeAdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val adView: UnifiedNativeAdView = view.ad_view as UnifiedNativeAdView

        init {
            with(adView) {
                this.mediaView = adView.ad_media as MediaView
                this.headlineView = adView.ad_headline
                this.bodyView = adView.ad_body
                this.callToActionView = adView.ad_call_to_action
                this.iconView = adView.ad_icon
                this.priceView = adView.ad_price
                this.starRatingView = adView.ad_stars
                this.storeView = adView.ad_store
                this.advertiserView = adView.ad_advertiser
            }

        }
    }
}