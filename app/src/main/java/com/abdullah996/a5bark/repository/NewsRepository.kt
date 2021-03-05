package com.abdullah996.a5bark.repository

import com.abdullah996.a5bark.api.RetrofitInstance
import com.abdullah996.a5bark.db.ArticleDatabase
import com.abdullah996.a5bark.model.Article

class NewsRepository(
        val db:ArticleDatabase

) {

    suspend fun getBreakingNews(countryCode:String,pageNumber:Int)=
            RetrofitInstance.api.getBreakingNews(countryCode,pageNumber)


    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
            RetrofitInstance.api.searchForNews(searchQuery, pageNumber)

    suspend fun upsert(article: Article)= db.getArticleDao().upsert(article)

    fun getSavedNews()=db.getArticleDao().getAllArticles()

    suspend fun deleteArticle(article: Article)=db.getArticleDao().deleteArticle(article)
}