package com.example.connectify

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class EditPostActivity : AppCompatActivity() {

    private lateinit var editCaptionEditText: EditText
    private lateinit var postImageView: ImageView
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)

        // Initialize views
        editCaptionEditText = findViewById(R.id.editCaptionEditText)
        postImageView = findViewById(R.id.postImageView)
        saveButton = findViewById(R.id.saveButton)

        // Get the data passed from MainActivity
        val postCaption = intent.getStringExtra("postCaption")
        val postImageUri = Uri.parse(intent.getStringExtra("postImageUri"))

        // Set initial values
        editCaptionEditText.setText(postCaption)
        Glide.with(this)
            .load(postImageUri)
            .into(postImageView)

        // Save button click listener to return the updated data to MainActivity
        saveButton.setOnClickListener {
            val editedCaption = editCaptionEditText.text.toString()
            val editedImageUri = postImageUri.toString()

            val resultIntent = Intent().apply {
                putExtra("editedCaption", editedCaption)
                putExtra("editedImageUri", editedImageUri)
                putExtra("postCaption", postCaption) // Pass the original caption to identify the post
            }

            setResult(RESULT_OK, resultIntent)
            finish() // Close the EditPostActivity
        }
    }
}
