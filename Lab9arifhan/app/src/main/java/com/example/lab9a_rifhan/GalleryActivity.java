package com.example.lab9a_rifhan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * Activity to display a gallery of images stored in /Pictures/MyApp directory.
 */
public class GalleryActivity extends AppCompatActivity {

    private GridView gridView;                      // GridView to display image thumbnails
    private ImageAdapter adapter;                   // Custom adapter to load and display images
    private ArrayList<String> imagePaths;           // List to store paths of all images found

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);  // Set the layout from XML

        gridView = findViewById(R.id.gridView);     // Get GridView from layout
        imagePaths = new ArrayList<>();             // Initialize the image path list

        loadImages();                               // Load images from local storage

        adapter = new ImageAdapter(this, imagePaths); // Set up the adapter with context and data
        gridView.setAdapter(adapter);                 // Attach adapter to the GridView

        // Handle click on a grid item (image thumbnail)
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedImagePath = imagePaths.get(position);         // Get the selected image path
            Intent intent = new Intent(GalleryActivity.this, ImageViewActivity.class);
            intent.putExtra("imagePath", selectedImagePath);             // Pass image path to next activity
            startActivityForResult(intent, 100);                         // Start activity expecting a result
        });
    }

    /**
     * Loads all JPG images from /Pictures/MyApp directory into the imagePaths list.
     */
    private void loadImages() {
        imagePaths.clear();  // Clear any existing data first

        // Access the directory: /storage/emulated/0/Pictures/MyApp
        File picturesDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyApp");

        // Check if directory exists and is a folder
        if (picturesDir.exists() && picturesDir.isDirectory()) {
            File[] files = picturesDir.listFiles(); // List all files in the directory
            if (files != null) {
                for (File file : files) {
                    // Only add files that end with .jpg (case insensitive)
                    if (file.getAbsolutePath().toLowerCase().endsWith(".jpg")) {
                        imagePaths.add(file.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * Reloads the image list and refreshes the GridView.
     */
    private void refreshGallery() {
        loadImages();               // Reload the image paths from storage
        adapter.notifyDataSetChanged(); // Tell the adapter that data has changed
    }

    /**
     * Called when returning from ImageViewActivity.
     * If an image was deleted or changed, refresh the gallery.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If returning from image viewer with OK result, refresh the gallery
        if (requestCode == 100 && resultCode == RESULT_OK) {
            refreshGallery();
        }
    }
}