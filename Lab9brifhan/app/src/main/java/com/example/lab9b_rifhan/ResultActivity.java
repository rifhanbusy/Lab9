package com.example.lab9b_rifhan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * ResultActivity displays the scanned result (QR/barcode content) from MainActivity.
 * If the result is a valid URL, the user can open it in a browser.
 * Also provides a button to return back to MainActivity.
 */
public class ResultActivity extends AppCompatActivity {

    TextView resultText;         // Displays scanned result content
    Button openBrowserBtn;       // Button to open content in browser if it’s a valid URL
    Button backBtn;              // Button to go back to MainActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result); // Sets the layout for this activity

        // Link UI elements with layout IDs
        resultText = findViewById(R.id.resultText);
        openBrowserBtn = findViewById(R.id.openBrowserBtn);
        backBtn = findViewById(R.id.backBtn);

        // Get the scanned result passed from MainActivity
        String content = getIntent().getStringExtra("result");

        // Display the scanned content in the TextView
        resultText.setText(content);

        // When "Open in Browser" button is clicked
        openBrowserBtn.setOnClickListener(v -> {
            // Check if the scanned result is a valid URL
            if (URLUtil.isValidUrl(content)) {
                // Create an intent to open the URL in a web browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(content));
                startActivity(browserIntent);
            } else {
                // If not a valid URL, show message in the result text
                resultText.setText("Not a valid URL:\n" + content);
            }
        });

        // When "Back" button is clicked
        backBtn.setOnClickListener(v -> {
            finish(); // Close this activity and return to MainActivity
        });
    }
}