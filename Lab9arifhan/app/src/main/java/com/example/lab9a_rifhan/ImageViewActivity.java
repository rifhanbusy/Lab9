package com.example.lab9a_rifhan;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.media.ExifInterface;

import java.io.File;
import java.io.IOException;

/**
 * ImageViewActivity displays a full-size image selected from the gallery,
 * shows the date the image was taken (from EXIF data),
 * and allows the user to delete the image with confirmation.
 */
public class ImageViewActivity extends AppCompatActivity {

    ImageView fullImageView;   // ImageView to display the full-size photo
    Button deleteButton;       // Button to trigger delete action
    String imagePath;          // Path of the image passed from GalleryActivity

    /**
     * onCreate() initializes the view, loads the image with correct rotation,
     * and sets up the delete button's functionality.
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        // Connect UI components
        fullImageView = findViewById(R.id.fullImageView);
        deleteButton = findViewById(R.id.deleteButton);

        // Get image path from intent (passed from GalleryActivity)
        imagePath = getIntent().getStringExtra("imagePath");

        // Display the image if a valid path is received
        if (imagePath != null) {
            displayImageWithRotation(imagePath);
        }

        // When delete button is clicked, show confirmation dialog
        deleteButton.setOnClickListener(v -> {
            confirmAndDeleteImage();
        });
    }

    /**
     * displayImageWithRotation() decodes the image from file,
     * applies necessary rotation based on EXIF metadata,
     * and displays it in the ImageView.
     * It also extracts and displays the image timestamp (if available).
     */
    private void displayImageWithRotation(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path); // Decode image
        String timestamp = "Unknown"; // Default timestamp

        try {
            ExifInterface exif = new ExifInterface(path); // Read EXIF metadata

            // Get orientation information from EXIF
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix(); // Matrix to apply rotation
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90); break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180); break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270); break;
            }

            // Create a new bitmap with applied rotation
            bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            // Extract the date taken from EXIF
            String exifDate = exif.getAttribute(ExifInterface.TAG_DATETIME);
            if (exifDate != null) {
                // Format the date to make it user-friendly
                timestamp = exifDate.replace(":", "-").replaceFirst(" ", " @ ");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set the image in the UI
        fullImageView.setImageBitmap(bitmap);

        // Set the timestamp in the TextView
        TextView timestampView = findViewById(R.id.imageTimestamp);
        timestampView.setText("Taken on: " + timestamp);
    }

    /**
     * confirmAndDeleteImage() shows a confirmation dialog to the user
     * before proceeding to delete the image.
     */
    private void confirmAndDeleteImage() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Yes", (dialog, which) -> deleteImage())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * deleteImage() deletes the image file from storage,
     * displays a Toast message based on the result,
     * and sets the result to RESULT_OK to notify GalleryActivity to refresh the grid.
     */
    private void deleteImage() {
        File file = new File(imagePath); // Create File object for the image path
        if (file.exists()) {
            if (file.delete()) {
                // Deletion successful
                Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);  // Send signal to parent activity (GalleryActivity) to refresh
                finish();              // Close this activity
            } else {
                // Deletion failed
                Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}