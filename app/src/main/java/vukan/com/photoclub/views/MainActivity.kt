package vukan.com.photoclub.views

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.support.v4.app.ShareCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
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
    private var mDatabase = Database()
    private var mAdapterHome: HomeImageRecyclerViewAdapter? = null
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var listener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

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

        images_home.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
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
            R.id.delete -> {
                DeleteProfileDialog().show(supportFragmentManager, "Delete profile?")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == signIn) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                mDatabase.createUser()
                Snackbar.make(images_home, getString(R.string.sign_in), Snackbar.LENGTH_SHORT).show()
            } else {
                when {
                    IdpResponse.fromResultIntent(data) == null -> Snackbar.make(
                        images_home,
                        "Sign in cancelled!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    IdpResponse.fromResultIntent(data)?.error?.errorCode == ErrorCodes.NO_NETWORK -> Snackbar.make(
                        images_home,
                        "No internet connection",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    else -> Snackbar.make(
                        images_home,
                        IdpResponse.fromResultIntent(data)?.error.toString(),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onStart() {
        super.onStart()
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
        listener = collection.addSnapshotListener(MetadataChanges.INCLUDE) { snapshots, _ ->
            if (snapshots != null && !snapshots.isEmpty) {
                mAdapterHome = HomeImageRecyclerViewAdapter(images, mDatabase, this)
                images_home.adapter = mAdapterHome

                for (dc in snapshots.documentChanges) {
                    val image = dc.document.toObject(Image::class.java)
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            images.add(image)
                            mAdapterHome?.notifyItemInserted(images.indexOf(image))
                        }
                        DocumentChange.Type.MODIFIED -> {
                            var index = 0
                            for (c in images) {
                                if (c.imageUrl == image.imageUrl) index = images.indexOf(c)
                                break
                            }
                            images[index] = image
                            mAdapterHome?.notifyItemChanged(index)
                        }
                        DocumentChange.Type.REMOVED -> {
                            val index = images.indexOf(image)
                            images.remove(image)
                            mAdapterHome?.notifyItemRemoved(index)
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        images.clear()
    }

    override fun onStop() {
        super.onStop()
        listener.remove()
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
    }

    fun deleteProfile() {
        mFirebaseAuth.currentUser?.delete()?.addOnSuccessListener {
            mDatabase.deleteUser()
            Snackbar.make(images_home, R.string.profile_deleted, Snackbar.LENGTH_LONG)
                .show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    companion object {
        class DeleteProfileDialog : DialogFragment() {

            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                return activity?.let {
                    AlertDialog.Builder(it)
                        .setTitle(getString(R.string.dialog_title))
                        .setMessage(getString(R.string.dialog_message))
                        .setIcon(R.drawable.ic_delete)
                        .setPositiveButton(
                            android.R.string.yes
                        ) { _, _ ->
                            (activity as MainActivity).deleteProfile()
                        }
                        .setNegativeButton(
                            android.R.string.no
                        ) { _, _ -> }
                        .create()
                } ?: throw IllegalStateException("Activity cannot be null")
            }
        }
    }
}