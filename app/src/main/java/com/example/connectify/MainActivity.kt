package com.example.connectify

import com.example.connectify.Post
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import de.hdodenhof.circleimageview.CircleImageView


class MainActivity : AppCompatActivity() {

    private val posts = mutableListOf<Post>()
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var commentInputSection: LinearLayout
    companion object {
        const val NEW_POST_REQUEST_CODE = 1
        const val EDIT_POST_REQUEST_CODE = 2
        const val CAPTION_LIMIT = 100 // Character limit for caption
    }

    private val userImages = listOf(
        "https://temitope50thpraisefiesta.com/assets/loga-CSJSn1GG.png",
        "https://images.pexels.com/photos/1831794/pexels-photo-1831794.jpeg?auto=compress&cs=tinysrgb&w=600",
        "https://images.pexels.com/photos/3491678/pexels-photo-3491678.jpeg?auto=compress&cs=tinysrgb&w=600",
        "https://images.pexels.com/photos/8722744/pexels-photo-8722744.jpeg?auto=compress&cs=tinysrgb&w=600&lazy=load"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in
        val sharedPref = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            // Redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Prevent back navigation to MainActivity without login
            return
        }

        setContentView(R.layout.activity_feed)

        // Profile button click listener

        val profileButton = findViewById<ImageButton>(R.id.profileButton)
        profileButton.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            intent.putExtra("userName", "Anna Farea")
            startActivity(intent)
        }

        // Floating action button to add new post
        val newPostFab = findViewById<FloatingActionButton>(R.id.addPostFab)
        newPostFab.setOnClickListener {
            startActivityForResult(Intent(this, NewPostActivity::class.java), NEW_POST_REQUEST_CODE)
        }

        // Setup stories section
        setupStoriesSection()

        // Adding hardcoded dummy posts
        val dummyPost1 = Post("Exploring the beauty of nature!", Uri.parse("https://images.pexels.com/photos/2325447/pexels-photo-2325447.jpeg?auto=compress&cs=tinysrgb&w=600"))
        val dummyPost2 = Post("Loving the sunset vibes", Uri.parse("https://images.pexels.com/photos/69776/tulips-bed-colorful-color-69776.jpeg?auto=compress&cs=tinysrgb&w=600"))
        val dummyPost3 = Post("City lights at night", Uri.parse("https://images.pexels.com/photos/1477166/pexels-photo-1477166.jpeg?auto=compress&cs=tinysrgb&w=600"))

        // Add the dummy posts to the feed
        addPostToFeed(dummyPost1.caption, dummyPost1.imageUri)
        addPostToFeed(dummyPost2.caption, dummyPost2.imageUri)
        addPostToFeed(dummyPost3.caption, dummyPost3.imageUri)

        // Logout button click listener
        val logoutButton: ImageButton = findViewById(R.id.logoutButton)


        logoutButton.setOnClickListener {
            // Create an AlertDialog to confirm logout
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    // Clear shared preferences to log the user out
                    val editor = sharedPref.edit()
                    editor.putBoolean("isLoggedIn", false) // Set login state to false
                    editor.apply()

                    // Redirect to LoginActivity
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish() // Close the current activity and prevent back navigation
                }
                .setNegativeButton("No") { dialog, _ ->
                    // Dismiss the dialog if the user cancels
                    dialog.dismiss()
                }
                .create()

            alertDialog.show()
        }



    }

    private fun setupStoriesSection() {
        val storiesLayout = findViewById<LinearLayout>(R.id.storiesLayout) // Use HorizontalScrollView for horizontal scrolling
        storiesLayout.removeAllViews()

        val storiesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        for (userImageUri in userImages) {
            val storyView = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 0, 16, 0)
                }
            }

            // Story Image with Border
            val storyImageView = ShapeableImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(150, 150)
                shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, 75f) // Circular shape
                    .build()
                Glide.with(this)
                    .load(userImageUri)
                    .override(150, 150)
                    .into(this)

                // Add a colorful ring around the image
                background = ContextCompat.getDrawable(context, R.drawable.story_highlight_border)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            // Add user's name below the story image
            val userName = TextView(this).apply {
                text = "User Name" // Replace with actual username
                gravity = Gravity.CENTER
                textSize = 12f
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setPadding(0, 8, 0, 0)
            }

            // Set up click listener for story
            storyImageView.setOnClickListener {
                showStoryPopup(userImageUri)
            }

            storyView.addView(storyImageView)
            storyView.addView(userName)

            storiesContainer.addView(storyView)
        }

        storiesLayout.addView(storiesContainer)
    }

    private fun showStoryPopup(imageUri: String) {
        val dialogView = layoutInflater.inflate(R.layout.popup_story, null)
        val storyPopupImage = dialogView.findViewById<ImageView>(R.id.storyPopupImage)
        val heartIcon = dialogView.findViewById<ImageView>(R.id.heartIcon)

        // Load the image into the popup view
        Glide.with(this)
            .load(imageUri)
            .into(storyPopupImage)

        // Set the heart icon click behavior
        heartIcon.setOnClickListener {
            heartIcon.setImageResource(R.drawable.ic_heart_filled)  // Change heart to filled state when clicked
        }

        // Create and configure the dialog
        val dialog = AlertDialog.Builder(this, R.style.CustomDialog).create()
        dialog.setView(dialogView)

        // Set up the top loader (ProgressBar) for the popup story
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.VISIBLE


        Thread {
            for (i in 0..100) {
                Thread.sleep(50)
                progressBar.progress = i
            }
        }.start()


        // Set a timer for auto-disappearance
        dialog.show()
        progressBar.postDelayed({
            progressBar.visibility = View.GONE
            dialog.dismiss()
        }, 5000)
    }

    //posts receives
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == NEW_POST_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val caption = data.getStringExtra("caption")
            val imageUri = data.getStringExtra("imageUri")?.let { Uri.parse(it) }

            if (caption != null && imageUri != null) {
                posts.add(Post(caption, imageUri))
                addPostToFeed(caption, imageUri)
            }
        }
    }

    // Function to open UserProfileActivity with the posts data
    private fun openUserProfile() {
        val intent = Intent(this, UserProfileActivity::class.java)
        intent.putParcelableArrayListExtra("posts", ArrayList(posts)) // Pass the posts list
        startActivity(intent)
    }

    private fun addPostToFeed(caption: String, imageUri: Uri?) {
        val feedLayout = findViewById<LinearLayout>(R.id.feedLayout)

        val postCard = CardView(this).apply {
            radius = 16f
            cardElevation = 8f
            setContentPadding(16, 16, 16, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 16)
            }
        }

        val postLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Top section with user image, name, date, and 3-dot menu
        val topSectionLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_VERTICAL
        }

        // User profile image
        val userImage = CircleImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(90, 90).apply {
                setMargins(8, 8, 8, 8)
            }

            Glide.with(this)
                .load("https://images.pexels.com/photos/1642228/pexels-photo-1642228.jpeg?auto=compress&cs=tinysrgb&w=600") // Replace with a dummy profile image URL
                .placeholder(R.drawable.user_image) // Optional placeholder image
                .error(R.drawable.error_profile) // Optional error image
                .into(this)// Replace with the user's profile image resource
        }

        // Text layout for user name and date
        val textLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(16, 0, 0, 0)
            }
        }

        val userName = TextView(this).apply {
            text = "Anna Farea" // Replace with actual user name
            textSize = 16f

        }

        val postDate = TextView(this).apply {
            text = "Just now" // Replace with actual post date
            textSize = 12f

        }

        textLayout.addView(userName)
        textLayout.addView(postDate)

        // Three-dot overflow menu button
        val optionsButton = ImageButton(this).apply {
            setImageResource(R.drawable.ic_more_vert) // Replace with your 3-dot icon resource
            background = null
            setOnClickListener {
                showOptionsMenu(postCard)
            }
        }

        // Add components to top section layout
        topSectionLayout.addView(userImage)
        topSectionLayout.addView(textLayout)
        topSectionLayout.addView(optionsButton)

        // Post image view setup
        val postImageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                500
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        // Use Glide to load the image from the URL
        Glide.with(this)
            .load(imageUri)
            .placeholder(R.drawable.default_image) // Optional placeholder
            .error(R.drawable.error_image) // Optional error placeholder
            .into(postImageView)

        // Heart (like) and Comment icon setup
        val actionIconsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(-10, 8, 0, 0)
            }
        }

        val likeButton = ImageButton(this).apply {
            setImageResource(R.drawable.ic_heart_unliked)
            background = null
            setOnClickListener {
                val isLiked = tag as? Boolean ?: false
                tag = !isLiked
                setImageResource(
                    if (!isLiked) R.drawable.ic_heart_liked else R.drawable.ic_heart_unliked
                )
            }
        }

        val commentIcon = ImageButton(this).apply {
            setImageResource(R.drawable.ic_comment) // Replace with your comment icon
            background = null
            setOnClickListener {
                commentInputSection.visibility = if (commentInputSection.visibility == View.GONE) View.VISIBLE else View.GONE
            }
        }

        actionIconsLayout.addView(likeButton)
        actionIconsLayout.addView(commentIcon)

        // Caption with "Read More" functionality
        val postCaption = TextView(this).apply {
            textSize = 16f
            setPadding(0, 8, 0, 8)
            var isFullCaptionVisible = false
            var truncatedText = caption.take(CAPTION_LIMIT) + "..."

            text = truncatedText

            val spannable = SpannableStringBuilder(truncatedText).apply {
                append(" Read More")
                setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        text = if (isFullCaptionVisible) truncatedText + " Read More" else caption + " Read Less"
                        isFullCaptionVisible = !isFullCaptionVisible
                    }
                }, length - " Read More".length, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.teal_200)),
                    length - " Read More".length, length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            movementMethod = android.text.method.LinkMovementMethod.getInstance()
            text = spannable
        }

        // Comment Input Section
        commentInputSection = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val commentInput = EditText(this).apply {
            hint = "Add a comment..."
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val submitCommentButton = Button(this).apply {
            text = "Comment"
            setOnClickListener {
                val commentText = commentInput.text.toString()
                if (commentText.isNotEmpty()) {
                    addCommentToPost(postLayout, commentText)
                    commentInput.text.clear()
                }
            }
        }

        commentInputSection.addView(commentInput)
        commentInputSection.addView(submitCommentButton)

        val commentsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        postLayout.addView(topSectionLayout)    // Add top section
        postLayout.addView(postCaption)         // Add caption
        postLayout.addView(postImageView)       // Add image
        postLayout.addView(actionIconsLayout)   // Add action icons

        postLayout.addView(commentInputSection) // Add comment input section
        postLayout.addView(commentsLayout)      // Add comments layout

        postCard.addView(postLayout)
        feedLayout.addView(postCard,0)
    }
    private fun addCommentToPost(postLayout: LinearLayout, comment: String) {
        val commentsLayout = postLayout.getChildAt(postLayout.childCount - 1) as LinearLayout

        val commentTextView = TextView(this).apply {
            text = comment
            textSize = 14f
            setPadding(0, 4, 0, 4)
        }

        commentsLayout.addView(commentTextView)
    }

    private fun showOptionsMenu(postCard: CardView) {
        val options = arrayOf("Edit Post", "Delete Post")
        val builder = AlertDialog.Builder(this)


        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    val intent = Intent(this, EditPostActivity::class.java).apply {
                        putExtra("caption", "Current Caption")  // Replace with actual data
                        putExtra("imageUri", "Current Image URI")  // Replace with actual data
                    }
                    startActivityForResult(intent, EDIT_POST_REQUEST_CODE)


                }
                1 -> {
                    // Handle delete post
                    showDeleteConfirmationDialog(postCard)
                }
            }
        }
        builder.show()
    }

    private fun showDeleteConfirmationDialog(postCard: CardView) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Yes") { _, _ ->
                val feedLayout = findViewById<LinearLayout>(R.id.feedLayout)
                feedLayout.removeView(postCard) // Remove the post from feed
            }
            .setNegativeButton("No", null)
        builder.create().show()
    }
}
