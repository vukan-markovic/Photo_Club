package vukan.com.photoclub.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.explore_fragment.*
import vukan.com.photoclub.FirestoreDatabase
import vukan.com.photoclub.Presenter
import vukan.com.photoclub.R
import vukan.com.photoclub.adapters.ImageAdRecyclerViewAdapter
import vukan.com.photoclub.dataclasses.Image

class ExploreFragment : Fragment(), TextWatcher {
    private var presenter: Presenter = Presenter(this, FirestoreDatabase())
    private lateinit var adapter: ImageAdRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.explore_fragment,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        images_explore.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        images_explore.overScrollMode = View.OVER_SCROLL_NEVER
        images_explore.itemAnimator = DefaultItemAnimator()
        search.addTextChangedListener(this)
    }

    override fun afterTextChanged(s: Editable?) {
        presenter.readImagesSearch(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    fun setAdapter(images: List<Image>) {
        adapter = ImageAdRecyclerViewAdapter(images, presenter, this)
        images_explore.adapter = adapter
    }

    fun getAdapter(): ImageAdRecyclerViewAdapter {
        return adapter
    }
}