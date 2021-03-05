package com.abdullah996.a5bark.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.abdullah996.a5bark.model.Article
import retrofit2.http.DELETE


@Dao
interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(article: Article):Long


    @Query("SELECT * FROM articles")
    fun getAllArticles():LiveData<List<Article>>

    @Delete
    suspend fun deleteArticle(article: Article)
}