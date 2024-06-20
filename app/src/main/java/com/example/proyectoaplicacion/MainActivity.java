
package com.example.proyectoaplicacion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button buttonToUploadActivity, buttonToDownloadActivity, buttonManageCollections, buttonRegister, buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonToUploadActivity = findViewById(R.id.uploadFileMainButton);
        buttonToDownloadActivity = findViewById(R.id.downloadFileMainButton);
        buttonManageCollections = findViewById(R.id.manageCollectionsButton);
        buttonRegister = findViewById(R.id.registerButton);
        buttonLogout = findViewById(R.id.logoutButton);

        buttonToUploadActivity.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, UploadActivity.class)));
        buttonToDownloadActivity.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DownloadActivity.class)));
        buttonManageCollections.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ManageCollectionsActivity.class)));
        buttonRegister.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RegisterActivity.class)));
        buttonLogout.setOnClickListener(v -> finish());
    }
}
