package vukan.com.photoclub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import kotlinx.android.synthetic.main.explore_fragment.*
import vukan.com.photoclub.R
import vukan.com.photoclub.adapters.ImageRecyclerViewAdapter
import vukan.com.photoclub.databinding.ExploreFragmentBinding
import vukan.com.photoclub.dataclasses.Image
import vukan.com.photoclub.viewmodels.ImagesViewModel

class ExploreFragment : Fragment(), ImageRecyclerViewAdapter.ItemClickListener {
    private lateinit var mViewModel: ImagesViewModel
    private lateinit var mBinding: ExploreFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.explore_fragment,
            container,
            false
        )
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProviders.of(this).get(ImagesViewModel::class.java)
        mBinding.viewModel = mViewModel
        mBinding.setLifecycleOwner(this)
        images_explore.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        search.setOnSearchClickListener {
            val config = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build()

            val options = FirestorePagingOptions.Builder<Image>()
                .setLifecycleOwner(this)
                .setQuery(
                    mViewModel.readImagesSearch(search.query.toString()),
                    config,
                    Image::class.java
                )
                .build()

            images_explore.adapter = ImageRecyclerViewAdapter(options, this, this)
        }
    }

    override fun onItemClick(imageUrl: String) {
        ExploreFragmentDirections.exploreFragmentToImageDetailsFragment(imageUrl)
    }
}