package vukan.com.photoclub.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import vukan.com.photoclub.repository.Repository
import vukan.com.photoclub.dataclasses.Image

class ImagesViewModel : ViewModel() {
    private var images: CollectionReference = FirebaseFirestore.getInstance().collection("images")
    private var repository: Repository = Repository(images)
    private var imagesLiveData: MediatorLiveData<PagedList<Image>> = MediatorLiveData()

    init {
        imagesLiveData.addSource(repository) {
            imagesLiveData.postValue(it.toObjects(Image::class.java) as PagedList<Image>?)
        }
    }

    fun readImagesHome(): ArrayList<Any> {
        return repository.readImagesHome()
    }

    fun readImagesSearch(search: String): Query {
        return repository.readImagesSearch(search)
    }

    fun readUserImages(userId: String): Query {
        return repository.readUserImages(userId)
    }

    fun deleteImage(imageUrl: String) {
        repository.deleteImage(imageUrl)
    }
}