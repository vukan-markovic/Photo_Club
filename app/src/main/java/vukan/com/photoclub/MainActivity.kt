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
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import com.crashlytics.android.Crashlytics
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.appinvite.AppInviteInvitation
import com.google.android.material.navigation.NavigationView
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.main_activity.*
import vukan.com.photoclub.databinding.MainActivityBinding
import vukan.com.photoclub.fragments.MainFragment

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var navController: NavController

    init {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        DataBindingUtil.setContentView<MainActivityBinding>(this, R.layout.main_activity)
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713")
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_add_a_photo)
        supportActionBar?.setIcon(R.drawable.ic_mode_comment)
        navController = this.findNavController(R.id.navigation_host_fragment)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.main_fragment, R.id.main_fragment), drawer_layout)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupActionBarWithNavController(this, navController, drawer_layout)
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(toolbar, navController, drawer_layout)
        NavigationUI.setupWithNavController(navigation_view, navController)
        NavigationUI.setupWithNavController(bottom_navigation, navController)
        navigation_view.setNavigationItemSelectedListener(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, drawer_layout)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                drawer_layout.openDrawer(GravityCompat.START)
                true
            }
            else -> item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
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
                    AppInviteInvitation.IntentBuilder("")
                        .setMessage("Come and share your photos with friends at PhotoClub! :D")
                        .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                        .setCustomImage(Uri.parse(getString(R.string.invite_image_url)))
                        .build()
                )
            }

            R.id.sign_out -> {
                AuthenticationObserver().signOut()
            }

            R.id.delete -> {
                AuthenticationObserver().delete()
                MainFragment().deleteUser()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}