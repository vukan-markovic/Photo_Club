package vukan.com.photoclub

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthenticationObserver : LifecycleObserver {
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var mFirebaseUser: FirebaseUser
    private lateinit var intent: Intent
    private lateinit var context: Context

    fun signIn(context: Context): Intent? {
        this.context = context
        mFirebaseAuth = FirebaseAuth.getInstance()
        mAuthStateListener.onAuthStateChanged(mFirebaseAuth)
        mFirebaseUser = mFirebaseAuth.currentUser!!

        if (mFirebaseAuth.currentUser == null) {
            intent =
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
                        .build()
        }

        return intent
    }

    fun signOut() {
        AuthUI.getInstance()
            .signOut(context)
    }

    fun delete() {
        AuthUI.getInstance()
            .delete(context)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun attachListener() {
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun detachListener() {
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
    }
}