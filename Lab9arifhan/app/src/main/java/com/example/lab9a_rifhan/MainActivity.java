package com.example.lab9a_rifhan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    // Request codes for permission and camera intent
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSIONS = 100;

    private Button btnCapture;     // Button to capture image
    private ImageView imageView;   // ImageView to display captured image

    private String currentPhotoPath; // Path of the current image file
    private Uri photoUri;            // URI for storing image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind UI elements
        btnCapture = findViewById(R.id.btnCapture);
        imageView = findViewById(R.id.imageView);

        // When capture button is clicked, check permissions and take picture
        btnCapture.setOnClickListener(view -> {
            if (checkAndRequestPermissions()) {
                takePicture();
            }
        });

        // Button to go to the gallery activity
        Button btnGallery = findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Checks if necessary permissions are granted and requests them if not.
     * Handles CAMERA and WRITE_EXTERNAL_STORAGE permissions (on Android < Q).
     */
    private boolean checkAndRequestPermissions() {
        // Check camera permission
        boolean cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        // For Android < 10 (Q), check write permission
        boolean writePermission = true;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }

        // Request permissions if needed
        if (!cameraPermission || !writePermission) {
            String[] permissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
                    new String[]{Manifest.permission.CAMERA} :
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
            return false;
        }
        return true;
    }

    /**
     * Launches the camera app to take a picture and save it to file or MediaStore.
     */
    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Check if there's a camera app available
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile;
            try {
                // Create a file where the photo should be saved
                photoFile = createImageFile();

                if (photoFile != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // For Android 10+, use MediaStore URI
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    } else {
                        // For Android < 10, use FileProvider URI
                        photoUri = FileProvider.getUriForFile(this,
                                getApplicationContext().getPackageName() + ".provider",
                                photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    }

                    // Start camera activity
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            } catch (IOException e) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Creates an image file with a unique name and returns it.
     * For Android Q+, uses MediaStore, otherwise uses public Pictures directory.
     */
    private File createImageFile() throws IOException {
        // Generate a timestamped image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";

        File imageFile;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore to save image for Android 10+
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyApp");

            // Insert the image entry into MediaStore
            photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (photoUri == null) throw new IOException("Failed to create MediaStore entry");

            // Convert the URI to actual file path
            currentPhotoPath = getRealPathFromURI(photoUri);
            return new File(currentPhotoPath);
        } else {
            // Use file in external Pictures directory for Android < 10
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            imageFile = new File(storageDir, imageFileName);
            currentPhotoPath = imageFile.getAbsolutePath();
            return imageFile;
        }
    }

    /**
     * Receives the result from the camera app and loads the captured image.
     * Rotates the image if necessary based on EXIF orientation.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result is from camera and successful
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                // Decode bitmap from saved file path
                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);

                // Read EXIF data to correct orientation
                ExifInterface exif = new ExifInterface(currentPhotoPath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                // Rotate bitmap based on orientation
                Bitmap rotatedBitmap = rotateBitmap(bitmap, orientation);
                imageView.setImageBitmap(rotatedBitmap);
                Toast.makeText(this, "Photo saved in Gallery", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Rotates a bitmap based on the EXIF orientation value.
     */
    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();

        // Apply the correct rotation based on orientation
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                return bitmap;
        }

        // Return rotated bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * Converts a URI from MediaStore to an actual file path string.
     * Used to access the image file created using MediaStore (Android Q+).
     */
    private String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return null;
    }

    /**
     * Handles the result from permission request dialog.
     * If all permissions are granted, proceed to take picture.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean grantedAll = true;

            // Check if all permissions are granted
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    grantedAll = false;
                    break;
                }
            }

            if (grantedAll) {
                takePicture();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}