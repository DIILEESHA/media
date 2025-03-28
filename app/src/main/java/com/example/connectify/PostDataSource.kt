

// PostDataSource.kt
package com.example.connectify



object PostDataSource {
    private val posts = mutableListOf<Post>()

    fun addPost(post: Post) {
        posts.add(post)
    }

    fun getPosts(): List<Post> {
        return posts
    }
}
