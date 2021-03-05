package com.abdullah996.a5bark.model

import com.abdullah996.a5bark.model.Article

data class NewsResponse(
        val articles: MutableList<Article>,
        val status: String,
        val totalResults: Int
)