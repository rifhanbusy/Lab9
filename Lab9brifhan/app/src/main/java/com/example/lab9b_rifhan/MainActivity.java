package com.example.lab9b_rifhan;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

/**
 * MainActivity is the main screen of the app that allows users to scan QR/barcodes
 * either by using the camera or by selecting an image from the gallery.
 * It handles permission requests, launches the appropriate scanner or gallery picker,
 * decodes the barcode using ZXing library, and passes the result to another activity for display.
 */

public class MainActivity extends AppCompatActivity {

    Button cameraBtn, galleryBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize buttons
        cameraBtn = findViewById(R.id.cameraBtn);
        galleryBtn = findViewById(R.id.galleryBtn);

        // Request necessary permissions (camera and storage)
        requestPermissions();

        // Set click listeners for camera and gallery buttons
        cameraBtn.setOnClickListener(v -> startCameraScan());
        galleryBtn.setOnClickListener(v -> selectImageFromGallery());
    }

    // Request runtime permissions: camera and external storage access
    void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, 1);
    }

    // Launch the camera scanner using ZXing’s IntentIntegrator
    void startCameraScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureActivity.class); // Use custom capture activity
        integrator.setOrientationLocked(false); // Allow screen rotation
        integrator.setPrompt("Scan a QR or Barcode"); // Instruction shown to user
        integrator.initiateScan(); // Start scan
    }

    // Start gallery image picker intent to select an image
    void selectImageFromGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(pickPhoto);
    }

    // Handle result from gallery image picker
    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Check if image was selected successfully
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    decodeFromGallery(imageUri); // Decode QR/barcode from selected image
                }
            });

    // Decode a barcode or QR code from an image URI selected from gallery
    void decodeFromGallery(Uri uri) {
        try {
            // Convert URI to Bitmap
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

            // Convert Bitmap to BinaryBitmap for ZXing processing
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BitmapLuminanceSource(bitmap)));

            // Decode the image using MultiFormatReader
            Result result = new MultiFormatReader().decode(binaryBitmap);

            // If decoding is successful, open result activity with the content
            openResultActivity(result.getText());
        } catch (Exception e) {
            // Show failure message if decoding fails
            Toast.makeText(this, "Failed to decode QR code", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle result from camera scan
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Parse scan result from ZXing library
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            // If result is not null, open result activity to show the content
            openResultActivity(result.getContents());
        } else {
            // If result is null, pass to superclass handler
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Open a new activity to display the decoded result content
    void openResultActivity(String content) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("result", content); // Pass scanned/decoded result
        startActivity(intent);
    }
}