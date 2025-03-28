package com.example.connectify

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class NewPostActivity : AppCompatActivity() {

    private lateinit var imageUri: Uri
    private lateinit var imageView: ImageView
    private lateinit var removeImageButton: Button
    private val PICK_IMAGE_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post)

        val captionInput = findViewById<EditText>(R.id.captionInput)
        imageView = findViewById(R.id.imageView)
        removeImageButton = findViewById(R.id.removeImageButton)
        val selectImageButton = findViewById<Button>(R.id.selectImageButton)
        val uploadButton = findViewById<Button>(R.id.uploadButton)

        // Open the gallery to select an image
        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Upload the post
        uploadButton.setOnClickListener {
            val caption = captionInput.text.toString()
            val intent = Intent()
            intent.putExtra("caption", caption)
            intent.putExtra("imageUri", imageUri.toString())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        // Remove image functionality
        removeImageButton.setOnClickListener {
            imageView.setImageURI(null) // Clear the image
            removeImageButton.visibility = View.GONE // Hide the remove button
            imageView.visibility = View.GONE // Hide the image
        }
    }

    // Handle the result from image selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data ?: return
            imageView.setImageURI(imageUri)
            imageView.visibility = View.VISIBLE // Show image preview
            removeImageButton.visibility = View.VISIBLE // Show remove button
        }
    }
}
