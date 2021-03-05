package com.abdullah996.a5bark.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abdullah996.a5bark.R
import com.abdullah996.a5bark.adabters.NewsAdabter
import com.abdullah996.a5bark.ui.NewsActivity
import com.abdullah996.a5bark.ui.NewsViewModel
import com.abdullah996.a5bark.utill.Resource
import com.abdullah996.a5bark.utill.constants
import com.abdullah996.a5bark.utill.constants.Companion.SEARCH_NEWS_TIME_DELAY
import kotlinx.android.synthetic.main.fragment_breaking_news.*
import kotlinx.android.synthetic.main.fragment_search_news.*
import kotlinx.android.synthetic.main.fragment_search_news.paginationProgressBar
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchNewsFragment:Fragment(R.layout.fragment_search_news) {

    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdabter
    val TAG = "SearchNewsFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        setupRecyclerView()

        newsAdapter.setOnItemClickListener {
            val bundle=Bundle().apply {
                putSerializable("article",it)
            }
            findNavController().navigate(
                    R.id.action_searchNewsFragment2_to_articleFragment,
                    bundle
            )
        }

        var job: Job? = null
        etSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if(editable.toString().isNotEmpty()) {
                        viewModel.searchNews(editable.toString())
                    }
                }
            }
        }

        viewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->
            when(response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles)
                        val totalPages=newsResponse.totalResults/ constants.QUERY_PAGE_SIZE +2
                        isLastPage=viewModel.seaechNewsPage==totalPages
                        if (isLastPage){
                            rvSearchNews.setPadding(0,0,0,0)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity,"an error occered : $message", Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun hideProgressBar() {
        paginationProgressBar.visibility = View.INVISIBLE
        isLoading=false
    }

    private fun showProgressBar() {
        paginationProgressBar.visibility = View.VISIBLE
        isLoading=true
    }
    var isLoading=false
    var isLastPage=false
    var isScrolling=false
    val scrollListener=object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrolling=true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager=recyclerView.layoutManager as LinearLayoutManager
            val firstVisiableItemPosition=layoutManager.findFirstVisibleItemPosition()
            val visiableItemCount=layoutManager.childCount
            val totalItemCount=layoutManager.itemCount


            val isNotLoadingAndNotLastPage=!isLoading&&!isLastPage
            val isLastItem=firstVisiableItemPosition+visiableItemCount >= totalItemCount
            val isNotAtBeginning=firstVisiableItemPosition >=0
            val isTotalMoreThanVisiable=totalItemCount>= constants.QUERY_PAGE_SIZE

            val shouldPaginate=isNotLoadingAndNotLastPage && isLastItem&&isNotAtBeginning &&
                    isTotalMoreThanVisiable && isScrolling
            if (shouldPaginate){
                viewModel.searchNews(etSearch.text.toString())
                isScrolling=false
            }


        }
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdabter()
        rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)
        }
    }
}



