package vukan.com.photoclub.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.main_fragment.*
import vukan.com.photoclub.BuildConfig
import vukan.com.photoclub.FirestoreDatabase
import vukan.com.photoclub.Presenter
import vukan.com.photoclub.R
import vukan.com.photoclub.adapters.ImageAdRecyclerViewAdapter
import java.util.*

class MainFragment : Fragment() {
    private var signIn = 1
    private val numberOfAds = 5
    private var remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    private var mNativeAds: ArrayList<UnifiedNativeAd> = ArrayList()
    private val defaultConfigMap: MutableMap<String, Any> = HashMap()
    private var cacheExpiration: Long = 3600
    private var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mFirebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private var presenter: Presenter = Presenter(this, FirestoreDatabase())
    private lateinit var adapter: ImageAdRecyclerViewAdapter
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var adLoader: AdLoader

    companion object {
        private const val background_key = "background"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.main_fragment,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        MobileAds.initialize(context, "ca-app-pub-3940256099942544~3347511713")
        mAuthStateListener = FirebaseAuth.AuthStateListener {
            if (it.currentUser == null) {
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(true, true)
                        .setAvailableProviders(
                            arrayListOf(
                                AuthUI.IdpConfig.FacebookBuilder().build(),
                                AuthUI.IdpConfig.TwitterBuilder().build(),
                                AuthUI.IdpConfig.GoogleBuilder().build(),
                                AuthUI.IdpConfig.PhoneBuilder().build(),
                                AuthUI.IdpConfig.EmailBuilder().build()
                            )
                        )
                        .setTheme(R.style.AuthTheme)
                        .setLogo(R.mipmap.ic_launcher)
                        .build(), signIn
                )
            }
        }

        images_home.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        images_home.overScrollMode = View.OVER_SCROLL_NEVER
        images_home.itemAnimator = DefaultItemAnimator()
        presenter.readImages()

        swipe_refresh.setOnRefreshListener {
            images_home.smoothScrollToPosition(0)
            presenter.readImages()
        }

        remoteConfig.setConfigSettings(
            FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        )

        defaultConfigMap[background_key] = "Photo club"
        remoteConfig.setDefaults(defaultConfigMap)
        if (remoteConfig.info.configSettings.isDeveloperModeEnabled) cacheExpiration = 0
        remoteConfig.fetch(cacheExpiration)
            .addOnCompleteListener(activity!!) { task ->
                if (task.isSuccessful) remoteConfig.activateFetched()
                if (remoteConfig.getString(background_key) != "default") activity!!.window.setTitle("Take a picture")
            }

        adLoader = AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
            .forUnifiedNativeAd {
                mNativeAds.add(it)
                if (!adLoader.isLoading) insertAdsInMenuItems()
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(errorCode: Int) {
                    if (!adLoader.isLoading) insertAdsInMenuItems()
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setRequestMultipleImages(false)
                    .build()
            )
            .build()

        adLoader.loadAds(AdRequest.Builder().build(), numberOfAds)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == signIn) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                Snackbar.make(view!!, "Signed in", Snackbar.LENGTH_SHORT).show()
                if (mFirebaseUser != null) {
                    presenter.createUser()
                    if (mFirebaseUser?.email != null && mFirebaseUser!!.isEmailVerified) mFirebaseUser?.sendEmailVerification()
                }
            } else if (IdpResponse.fromResultIntent(data) == null) activity?.finish()
        }
    }

    override fun onPause() {
        super.onPause()
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
    }

    fun insertAdsInMenuItems() {
        if (mNativeAds.isEmpty()) return
        val offset = (images_home.size / mNativeAds.size) + 1
        var index = 0
        for (ad in mNativeAds) {
            presenter.addAdd(index, ad)
            index += offset
        }
    }

    fun setAdapter(images: List<Any>) {
        adapter = ImageAdRecyclerViewAdapter(images, presenter, this)
        images_home.adapter = adapter
    }

    fun getAdapter(): ImageAdRecyclerViewAdapter {
        return adapter
    }
}