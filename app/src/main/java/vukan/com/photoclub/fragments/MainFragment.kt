package vukan.com.photoclub.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.size
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.android.synthetic.main.toast_custom.view.*
import vukan.com.photoclub.AuthenticationObserver
import vukan.com.photoclub.BuildConfig
import vukan.com.photoclub.R
import vukan.com.photoclub.adapters.ImageAdRecyclerViewAdapter
import vukan.com.photoclub.databinding.MainFragmentBinding
import vukan.com.photoclub.dataclasses.User
import vukan.com.photoclub.viewmodels.ImageViewModel
import vukan.com.photoclub.viewmodels.ImagesViewModel
import vukan.com.photoclub.viewmodels.UserViewModel

class MainFragment : Fragment(), ImageAdRecyclerViewAdapter.ItemClickListener {
    private var signIn = 1
    private lateinit var mViewModel: ImagesViewModel
    private lateinit var mUserViewModel: UserViewModel
    private val authentication: AuthenticationObserver = AuthenticationObserver()
    private lateinit var mBinding: MainFragmentBinding
    private lateinit var mFirebaseUser: FirebaseUser
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var adLoader: AdLoader
    private val numberOfAds = 5
    private var mNativeAds: ArrayList<UnifiedNativeAd> = ArrayList()
    private var list: ArrayList<Any> = ArrayList()

    companion object {
        private const val background_key = "background"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.main_fragment,
            container,
            false
        )
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val intent: Intent? = authentication.signIn(context!!)
        if (intent != null) startActivityForResult(intent, signIn)
        mViewModel = ViewModelProviders.of(this).get(ImagesViewModel::class.java)
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)
        mBinding.viewModel = mViewModel
        mBinding.setLifecycleOwner(this)
        list = mViewModel.readImagesHome()

        swipe_refresh.setOnRefreshListener {
            mViewModel.readImagesHome()
        }

        remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build()
        remoteConfig.setConfigSettings(configSettings)
        val defaultConfigMap: MutableMap<String, Any> = HashMap()
        defaultConfigMap[background_key] = "Photo club"
        remoteConfig.setDefaults(defaultConfigMap)
        var cacheExpiration: Long = 3600
        if (remoteConfig.info.configSettings.isDeveloperModeEnabled) cacheExpiration = 0
        remoteConfig.fetch(cacheExpiration)
            .addOnCompleteListener(activity!!) { task ->
                if (task.isSuccessful) {
                    remoteConfig.activateFetched()
                } else {
                    Toast.makeText(
                        context, "Fetch Failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (remoteConfig.getString(background_key) != "default")
                    activity!!.window.setTitle("Take a picture")
            }

        adLoader = AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
            .forUnifiedNativeAd {
                mNativeAds.add(it)
                if (!adLoader.isLoading) {
                    insertAdsInMenuItems()
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(errorCode: Int) {
                    if (!adLoader.isLoading) {
                        insertAdsInMenuItems()
                    }
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setRequestMultipleImages(false)
                    .build()
            )
            .build()

        adLoader.loadAds(AdRequest.Builder().build(), numberOfAds)
        images_home.adapter = ImageAdRecyclerViewAdapter(list, this, this)
        images_home.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == signIn) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                Snackbar.make(view!!, "Signed in", Snackbar.LENGTH_SHORT).show()
                mFirebaseUser = FirebaseAuth.getInstance().currentUser!!
                mUserViewModel.createUser(
                    User(
                        mFirebaseUser.photoUrl.toString(),
                        mFirebaseUser.uid,
                        mFirebaseUser.displayName.toString()
                    )
                )
                mUserViewModel.readUser(mFirebaseUser.uid)
            } else if (IdpResponse.fromResultIntent(data) == null) {
                activity?.finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        authentication.detachListener()
    }

    override fun onResume() {
        super.onResume()
        authentication.attachListener()
    }

    fun deleteUser() {
        mUserViewModel.deleteUser(mFirebaseUser.uid)
        showToast("Account is deleted :(")
    }

    override fun onItemClick(imageUrl: String) {
        MainFragmentDirections.mainFragmentToImageDetailsFragment(imageUrl)
    }

    fun getUser(viewModel: ImageViewModel) {
        MainFragmentDirections.mainFragmentToProfileFragment(viewModel.image.value?.userID!!)
    }

    @SuppressLint("InflateParams")
    private fun showToast(message: String) {
        val layout = layoutInflater.inflate(R.layout.toast_custom, null)
        layout.toast_message.text = message
        val toast = Toast(context)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            toast.view.layoutDirection = context?.resources?.configuration?.layoutDirection!!
        }
        toast.show()
    }

    fun insertAdsInMenuItems() {
        if (mNativeAds.isEmpty()) return
        val offset = (images_home.size / mNativeAds.size) + 1
        var index = 0
        for (ad in mNativeAds) {
            list.add(index, ad)
            index += offset
        }
    }
}