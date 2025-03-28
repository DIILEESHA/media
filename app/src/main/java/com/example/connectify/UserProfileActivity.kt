package com.example.connectify

import com.example.connectify.Post
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import android.widget.TextView

class UserProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        val profileImage = findViewById<CircleImageView>(R.id.profileImage)
        val coverImage = findViewById<ImageView>(R.id.coverImage)
        val userNameTextView = findViewById<TextView>(R.id.userName)
        val followersCount = findViewById<TextView>(R.id.followersCount)
        val followingCount = findViewById<TextView>(R.id.followingCount)
        val postCount = findViewById<TextView>(R.id.postCount)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        // Set dummy data for profile section
        val userName = intent.getStringExtra("userName") ?: "User Profile"
        userNameTextView.text = userName
        followersCount.text = " 100"
        followingCount.text = "150"
        postCount.text = "3"

        // Load cover image from URL
        Glide.with(this)
            .load("https://images.pexels.com/photos/394545/pexels-photo-394545.jpeg?auto=compress&cs=tinysrgb&w=600")
            .into(coverImage)

        // Load profile image from URL
        Glide.with(this)
            .load("https://images.pexels.com/photos/1642228/pexels-photo-1642228.jpeg?auto=compress&cs=tinysrgb&w=600")
            .into(profileImage)

        // Add dummy posts if empty
        if (PostDataSource.getPosts().isEmpty()) {
            val dummyPosts = listOf(
                Post("", Uri.parse("https://images.pexels.com/photos/1108572/pexels-photo-1108572.jpeg?auto=compress&cs=tinysrgb&w=600")),
                Post("", Uri.parse("https://images.pexels.com/photos/957024/forest-trees-perspective-bright-957024.jpeg?auto=compress&cs=tinysrgb&w=600")),
                Post("", Uri.parse("https://images.pexels.com/photos/807598/pexels-photo-807598.jpeg?auto=compress&cs=tinysrgb&w=600"))
            )
            dummyPosts.forEach { PostDataSource.addPost(it) }
        }

        // Load posts in a grid layout with 3 columns
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = PostAdapter(PostDataSource.getPosts())
    }
}
