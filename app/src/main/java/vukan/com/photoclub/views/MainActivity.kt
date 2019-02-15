package vukan.com.photoclub.views

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ShareCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.crashlytics.android.Crashlytics
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.main_activity.*
import vukan.com.photoclub.R
import vukan.com.photoclub.adapters.HomeImageRecyclerViewAdapter
import vukan.com.photoclub.database.Database
import vukan.com.photoclub.models.Image

class MainActivity : AppCompatActivity() {
    private var signIn = 1
    private var mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var images: MutableList<Image> = ArrayList()
    private var collection = FirebaseFirestore.getInstance().collection("images")
    private var mAdapterHome: HomeImageRecyclerViewAdapter? = null
    private lateinit var mDatabase: Database
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var listener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        Fabric.with(this, Crashlytics())
        actionBar?.setLogo(R.mipmap.ic_launcher)
        images_home.layoutManager = LinearLayoutManager(this)
        mAdapterHome = HomeImageRecyclerViewAdapter(images, this)
        images_home.adapter = mAdapterHome
        mDatabase = Database(this)

        mAuthStateListener = FirebaseAuth.AuthStateListener {
            if (it.currentUser == null) {
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false, false)
                        .setAvailableProviders(
                            arrayListOf(
                                AuthUI.IdpConfig.EmailBuilder().build(),
                                AuthUI.IdpConfig.PhoneBuilder().build(),
                                AuthUI.IdpConfig.GoogleBuilder().build(),
                                AuthUI.IdpConfig.FacebookBuilder().build(),
                                AuthUI.IdpConfig.TwitterBuilder().build()
                            )
                        )
                        .setTheme(R.style.AuthTheme)
                        .setLogo(R.mipmap.ic_launcher)
                        .build(),
                    signIn,
                    ActivityOptions.makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out).toBundle()
                )
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile_activity -> {
                startActivity(
                    Intent(this, ProfileActivity::class.java), ActivityOptions.makeCustomAnimation(
                        this,
                        R.anim.fade_in,
                        R.anim.fade_out
                    ).toBundle()
                )
                true
            }
            R.id.share -> {
                startActivity(
                    ShareCompat.IntentBuilder.from(this)
                        .setText(getString(R.string.share_message))
                        .setType("text/plain")
                        .createChooserIntent(), ActivityOptions.makeCustomAnimation(
                        this,
                        R.anim.fade_in,
                        R.anim.fade_out
                    ).toBundle()
                )
                true
            }
            R.id.sign_out -> {
                AuthUI.getInstance().signOut(this).addOnSuccessListener {
                    Snackbar.make(images_home, getString(R.string.signed_out), Snackbar.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == signIn) {
            if (resultCode == RESULT_OK) {
                mDatabase.createUser(mFirebaseAuth.currentUser!!)
                Snackbar.make(images_home, getString(R.string.sign_in), Snackbar.LENGTH_SHORT).show()
            } else if (IdpResponse.fromResultIntent(data) == null) finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
        listener = collection.addSnapshotListener(MetadataChanges.INCLUDE) { snapshots, _ ->
            if (snapshots != null && !snapshots.isEmpty) {
                for (dc in snapshots.documentChanges) {
                    val image = dc.document.toObject(Image::class.java)
                    if (dc.type == DocumentChange.Type.ADDED) images.add(image)
                    else if (dc.type == DocumentChange.Type.REMOVED) images.remove(image)
                }
                mAdapterHome = HomeImageRecyclerViewAdapter(images, this)
                images_home.adapter = mAdapterHome
                mAdapterHome!!.notifyDataSetChanged()
                images_home.invalidate()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        images.clear()
        listener.remove()
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
    }
}