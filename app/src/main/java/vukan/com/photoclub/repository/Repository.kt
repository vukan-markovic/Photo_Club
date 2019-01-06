package vukan.com.photoclub.repository

import android.os.Handler
import androidx.lifecycle.LiveData
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import vukan.com.photoclub.dataclasses.Comment
import vukan.com.photoclub.dataclasses.Image
import java.util.*

class Repository(private var collection: CollectionReference) : LiveData<QuerySnapshot>() {
    private var listenerRemovePending: Boolean = false
    private var handler: Handler = Handler()
    private var listener: MyValueEventListener = MyValueEventListener()
    private lateinit var listenerRegistration: ListenerRegistration


    private var removeListener: Runnable = Runnable {
        listenerRegistration.remove()
        listenerRemovePending = false
    }

    override fun onActive() {
        super.onActive()
        if (listenerRemovePending) handler.removeCallbacks(removeListener)
        else listenerRegistration = collection.addSnapshotListener(listener)
        listenerRemovePending = false
    }

    override fun onInactive() {
        super.onInactive()
        handler.postDelayed(removeListener, 2000)
        listenerRemovePending = true
    }

    inner class MyValueEventListener : EventListener<QuerySnapshot> {
        override fun onEvent(snapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) {
            value = snapshot
        }
    }

    fun createComment(comment: Comment) {
        collection.document(comment.commentID).set(comment)
    }

    fun readComments(): Query {
        return collection.limit(30).orderBy("dateTime")
//        collection
//            .whereEqualTo("capital", true)
//            .get()
//            .addOnSuccessListener { documents ->
//                for (document in documents) {
//                    Log.d(TAG, document.id + " => " + document.data)
//                }
//            }
    }

    fun updateComment(commentId: String, content: String) {
        collection.document(commentId).update("content", content, "dateTime", Calendar.getInstance().time)
    }

    fun deleteComment(commentId: String) {
        collection.document(commentId).delete()
    }

    fun readImagesHome(): ArrayList<Any> {
        val images = ArrayList<Any>()
        collection.limit(30).orderBy("dateTime").addSnapshotListener { querySnapshot, _ ->
            for (query in querySnapshot!!) {
                images.add(querySnapshot.documents[0].toObject(Image::class.java)!!)
            }
        }
        return images
    }

    fun readImagesSearch(search: String): Query {
        return collection.whereArrayContains("terms", search)
    }

    fun readUserImages(userId: String): Query {
        return collection.whereEqualTo("userId", userId).limit(30).orderBy("dateTime")
    }

    fun deleteImage(imageUrl: String) {
        collection.document(imageUrl).delete()
//        storage.delete().addOnSuccessListener {
//        }.addOnFailureListener {
//        }
    }
}