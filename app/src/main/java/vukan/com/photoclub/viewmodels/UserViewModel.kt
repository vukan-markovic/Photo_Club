package vukan.com.photoclub.viewmodels

import android.net.Uri
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import vukan.com.photoclub.repository.Repository2
import vukan.com.photoclub.dataclasses.Image
import vukan.com.photoclub.dataclasses.User

class UserViewModel : ViewModel() {
    private var users: CollectionReference = FirebaseFirestore.getInstance().collection("users")
    private var repository: Repository2 =
        Repository2(users)
    private var _user: MediatorLiveData<User> = MediatorLiveData()
    val user: LiveData<User>
        get() = _user

    init {
        _user.addSource(repository) {
            _user.postValue(it.toObject(User::class.java))
        }
    }

    fun createUser(user: User) {
        repository.createUser(user)
    }

    fun readUser(userId: String) {
        repository.readUser(userId)
    }

    fun deleteUser(userId: String) {
        repository.deleteUser(userId)
    }

    fun updateProfilePicture(userId: String, imageUrl: Uri?) {
        repository.updateProfilePicture(userId, imageUrl)
    }

    fun updateUsername(userId: String, username: String) {
        repository.updateUsername(userId, username)
    }

    fun createImage(image: Image, imageView: ImageView) {
        repository.createImage(image, imageView)
    }
}