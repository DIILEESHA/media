package com.example.connectify
import com.example.connectify.Post
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProfileActivity : AppCompatActivity() {


    private val posts = mutableListOf<Post>()


    companion object {
            const val NEW_POST_REQUEST_CODE = 1
            const val EDIT_POST_REQUEST_CODE = 2
            const val CAPTION_LIMIT = 100 // Character limit for caption
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_feed)

            // Set up the Profile button
            val profileButton = findViewById<Button>(R.id.profileButton)
            // In MainActivity, Profile Button's OnClickListener
            profileButton.setOnClickListener {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putParcelableArrayListExtra("posts", ArrayList(posts))
                startActivity(intent)
            }


            // Set up the new post FAB
            val newPostFab = findViewById<FloatingActionButton>(R.id.addPostFab)
            newPostFab.setOnClickListener {
                startActivityForResult(Intent(this, NewPostActivity::class.java), NEW_POST_REQUEST_CODE)
            }
        }

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
            }

            // Add Image
            val postImageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    500
                )
                imageUri?.let {
                    setImageURI(it)
                } ?: run {
                    setImageResource(R.drawable.default_image)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            // Caption with "Read More" functionality
            val postCaption = TextView(this).apply {
                textSize = 16f
                setPadding(0, 8, 0, 8)
                var isFullCaptionVisible = false
                var truncatedText = caption.take(CAPTION_LIMIT) + "..." // Truncated text

                // Set the initial text to the truncated version
                text = truncatedText

                // Handle the "Read More" functionality
                val spannable = SpannableStringBuilder(truncatedText).apply {
                    append(" Read More")
                    setSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            // Toggle full caption visibility
                            if (isFullCaptionVisible) {
                                text = truncatedText + " Read More"
                            } else {
                                text = caption + " Read Less"
                            }
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

            // Comment Section
            val commentInput = EditText(this).apply {
                hint = "Add a comment..."
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val commentButton = Button(this).apply {
                text = "Comment"
                setOnClickListener {
                    val commentText = commentInput.text.toString()
                    if (commentText.isNotEmpty()) {
                        addCommentToPost(postLayout, commentText)
                        commentInput.text.clear() // Clear input after adding comment
                    }
                }
            }

            // Comments Layout
            val commentsLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Three-dot menu for options
            val optionsButton = ImageButton(this).apply {
                setImageResource(R.drawable.ic_more_vert) // Replace with your three dots icon
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener {
                    showOptionsMenu(postCard)
                }
            }

            // Add Image, Caption, Comment Input, and Comments to the Post Layout
            postLayout.addView(optionsButton) // Add options button
            postLayout.addView(postCaption)
            postLayout.addView(postImageView)
            postLayout.addView(commentInput)
            postLayout.addView(commentButton)
            postLayout.addView(commentsLayout) // Add comments layout


            postCard.addView(postLayout)

            // Add post to the feed
            feedLayout.addView(postCard)
        }

        private fun addCommentToPost(postLayout: LinearLayout, comment: String) {
            val commentsLayout = postLayout.getChildAt(postLayout.childCount - 1) as LinearLayout // Get the comments layout

            val commentTextView = TextView(this).apply {
                text = comment
                textSize = 14f
                setPadding(0, 4, 0, 4)
            }

            commentsLayout.addView(commentTextView) // Add the comment to the comments layout
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
                    (postCard.parent as? LinearLayout)?.removeView(postCard)
                    Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No", null)
            builder.show()
        }
    }

    fun Intent.putParcelableArrayListExtra(s: String, arrayList: ArrayList<Post>) {

    }
