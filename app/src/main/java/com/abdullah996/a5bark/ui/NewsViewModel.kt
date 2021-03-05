package com.abdullah996.a5bark.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdullah996.a5bark.NewsApplication
import com.abdullah996.a5bark.model.Article
import com.abdullah996.a5bark.model.NewsResponse
import com.abdullah996.a5bark.repository.NewsRepository
import com.abdullah996.a5bark.utill.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
        app:Application,
        val newsReposiroty: NewsRepository
):AndroidViewModel(app) {

    val breakingNews:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    val searchNews:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()

    var breakingNewsResponse:NewsResponse?=null
    var searchNewsResponse:NewsResponse?=null

    var breakingNewsPage=1
    var seaechNewsPage=1



    init {
        getBreakingNews("us")
    }
    fun getBreakingNews(countryCode:String)=viewModelScope.launch {
        safeBreakingNewsCall(countryCode)
    }

    fun searchNews(searchQuery:String)=viewModelScope.launch {
       safeSearchNewsCall(searchQuery)
    }
    fun saveArticle(article: Article)=viewModelScope.launch {
        newsReposiroty.upsert(article)
    }

    fun getSavedNews()=newsReposiroty.getSavedNews()

    fun deleteArticle(article: Article)=viewModelScope.launch {
        newsReposiroty.deleteArticle(article)
    }



    private fun handleBreakingNewsResponse(response:Response<NewsResponse>):Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let {resultResponse->
                breakingNewsPage++
                if (breakingNewsResponse==null){
                    breakingNewsResponse=resultResponse
                }else{
                    val oldArticles=breakingNewsResponse?.articles
                    val newArticles=resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(breakingNewsResponse?:resultResponse)
            }
        }
        return Resource.Error(response.message())
    }
    private fun handleSearchNewsResponse(response:Response<NewsResponse>):Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let {resultResponse->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private suspend fun safeSearchNewsCall(searchQuery: String){
        searchNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()){
                val response=newsReposiroty.searchNews(searchQuery,seaechNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            }else{
                searchNews.postValue(Resource.Error("no internet connection"))
            }
        }catch (t:Throwable){
            when(t) {
                is IOException->searchNews.postValue(Resource.Error("network failure"))
                else -> searchNews.postValue(Resource.Error("conversion error"))
            }
        }
    }


    private suspend fun safeBreakingNewsCall(countryCode: String){
        breakingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()){
                val response=newsReposiroty.getBreakingNews(countryCode,breakingNewsPage)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            }else{
                 breakingNews.postValue(Resource.Error("no internet connection"))
            }
        }catch (t:Throwable){
            when(t) {
                    is IOException->breakingNews.postValue(Resource.Error("network failure"))
                    else -> breakingNews.postValue(Resource.Error("conversion error"))
            }
        }
    }

    private fun hasInternetConnection():Boolean{
        val connectivityManager=getApplication<NewsApplication>().getSystemService(
                Context.CONNECTIVITY_SERVICE
        )as ConnectivityManager
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            val activeNetwork=connectivityManager.activeNetwork ?:return false
            val capability=connectivityManager.getNetworkCapabilities(activeNetwork)?:return false
            return when{
                capability.hasTransport(TRANSPORT_WIFI)-> true
                capability.hasTransport(TRANSPORT_CELLULAR)->true
                capability.hasTransport(TRANSPORT_ETHERNET)-> true
                else->false
            }

        }
        else{
             connectivityManager.activeNetworkInfo?.run {
                 return when(type){
                     TYPE_WIFI->true
                     TYPE_MOBILE->true
                     TYPE_ETHERNET->true
                     else->false
                 }
             }
        }
        return false
    }
}