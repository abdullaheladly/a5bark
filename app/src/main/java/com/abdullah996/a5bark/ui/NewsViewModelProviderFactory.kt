package com.abdullah996.a5bark.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.abdullah996.a5bark.repository.NewsRepository

class NewsViewModelProviderFactory(
        val app:Application,
        val newsRepository: NewsRepository
):ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NewsViewModel(app,newsRepository)as T
    }
}