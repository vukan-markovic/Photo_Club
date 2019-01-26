package vukan.com.photoclub

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NavUtils
import androidx.core.app.ShareCompat
import androidx.core.view.GravityCompat
import com.crashlytics.android.Crashlytics
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.appinvite.AppInviteInvitation
import com.google.android.material.navigation.NavigationView
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.main_activity.*
import vukan.com.photoclub.fragments.ExploreFragment
import vukan.com.photoclub.fragments.MainFragment
import vukan.com.photoclub.fragments.ProfileFragment

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    init {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.main_activity)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        supportActionBar?.setIcon(R.mipmap.ic_launcher_round)
        navigation_view.setNavigationItemSelectedListener(this)

        bottom_navigation.setOnNavigationItemSelectedListener {
            when {
                it.itemId == R.id.main_fragment -> supportFragmentManager.beginTransaction().setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                ).replace(
                    vukan.com.photoclub.R.id.host_fragment,
                    MainFragment()
                ).addToBackStack(null).commit()
                it.itemId == R.id.explore_fragment
                -> supportFragmentManager.beginTransaction().setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                ).replace(
                    vukan.com.photoclub.R.id.host_fragment,
                    ExploreFragment()
                ).addToBackStack(null).commit()
                it.itemId == R.id.profile_fragment -> supportFragmentManager.beginTransaction().setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                ).replace(
                    vukan.com.photoclub.R.id.host_fragment,
                    ProfileFragment()
                ).addToBackStack(null).commit()
            }
            true
        }

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .add(R.id.host_fragment, MainFragment())
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                drawer_layout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        item.isChecked = true

        when (item.itemId) {
            R.id.share -> {
                startActivity(
                    ShareCompat.IntentBuilder.from(this)
                        .setText("Come and share your photos with friends at PhotoClub! :D")
                        .setType("text/plain")
                        .createChooserIntent()
                        .apply {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                        }
                )
            }

            R.id.invite -> {
                startActivity(
                    AppInviteInvitation.IntentBuilder("Photo club")
                        .setMessage("Come and share your photos with friends at PhotoClub! :D")
                        .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                        .setCustomImage(Uri.parse(getString(R.string.invite_image_url)))
                        .build()
                )
            }

            R.id.sign_out -> {
                AuthUI.getInstance().signOut(this)
            }

            R.id.delete -> {
                AuthUI.getInstance().delete(this)
                FirestoreDatabase().deleteUser()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) drawer_layout.closeDrawer(GravityCompat.START)
        else super.onBackPressed()
    }
}