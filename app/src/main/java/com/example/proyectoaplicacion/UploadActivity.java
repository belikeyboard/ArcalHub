package com.example.proyectoaplicacion;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;

    private Spinner spinnerProject, spinnerEvent, spinnerTrack;
    private Button buttonSelectFile, buttonUploadFile, buttonReturn;
    private TextView textViewFileName;

    private Uri fileUri;

    private StorageReference storageReference;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        spinnerProject = findViewById(R.id.projectSpinner);
        spinnerEvent = findViewById(R.id.eventSpinner);
        spinnerTrack = findViewById(R.id.trackSpinner);
        buttonSelectFile = findViewById(R.id.selectFileButton);
        textViewFileName = findViewById(R.id.selectedFileNameText);
        buttonUploadFile = findViewById(R.id.uploadButton);
        buttonReturn = findViewById(R.id.returnButton);

        populateProjectSpinner();
        spinnerProject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                populateEventSpinner(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        spinnerEvent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                populateTrackSpinner(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        buttonSelectFile.setOnClickListener(v -> openFileChooser());
        buttonUploadFile.setOnClickListener(v -> uploadFile());
        buttonReturn.setOnClickListener(v -> finish());
    }

    private void populateProjectSpinner() {
        ArrayList<String> projectList = new ArrayList<>();
        ArrayAdapter<String> projectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, projectList);
        projectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProject.setAdapter(projectAdapter);
        db.collection("Projects")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            if (document.exists()) {
                                String project = document.getString("project");
                                projectList.add(project);
                                projectAdapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                        System.err.println("Exception retrieving projects");
                    }
                });
    }

    private void populateEventSpinner(String project) {
        ArrayList<String> eventList = new ArrayList<>();
        ArrayAdapter<String> eventAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, eventList);
        eventAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEvent.setAdapter(eventAdapter);
        db.collection("Events")
                .whereEqualTo("project", project).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventList.add("Empty");
                        for (DocumentSnapshot document : task.getResult()) {
                            if (document.exists()) {
                                String event = document.getString("event");
                                eventList.add(event);
                                eventAdapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                        System.err.println("Exception retrieving events");
                    }
                });
    }

    private void populateTrackSpinner(String event) {
        ArrayList<String> trackList = new ArrayList<>();
        ArrayAdapter<String> trackAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, trackList);
        trackAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTrack.setAdapter(trackAdapter);
        trackList.add("Empty");
        if (event != "Empty") {
            db.collection("Tracks")
                    .whereEqualTo("event", event).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    String track = document.getString("track");
                                    trackList.add(track);
                                }
                            }
                        } else {
                            System.err.println("Exception retrieving tracks");
                        }
                    });
        }
        trackAdapter.notifyDataSetChanged();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            textViewFileName.setText(getFileName(fileUri));
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void uploadFile() {
        if (fileUri != null) {
            String project = spinnerProject.getSelectedItem().toString();
            String event = spinnerEvent.getSelectedItem().toString();
            String track = spinnerTrack.getSelectedItem().toString();
            if (TextUtils.isEmpty(project)) {
                Toast.makeText(this, "Please select a project", Toast.LENGTH_SHORT).show();
                return;
            }

            StorageReference fileReference = storageReference.child("uploads/" + getFileName(fileUri));

            fileReference.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        Map<String, Object> asset = new HashMap<>();
                        asset.put("project", project);
                        asset.put("event", event);
                        asset.put("track", track);
                        asset.put("fileUrl", uri.toString());
                        asset.put("upload_timestamp", DateUtils.getCurrentTimestamp());
                        asset.put("filename", getFileName(fileUri));

                        db.collection("Files").add(asset)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(UploadActivity.this, "File uploaded successfully", Toast.LENGTH_SHORT).show();
                                    textViewFileName.setText("No file selected");
                                    fileUri = null;
                                })
                                .addOnFailureListener(e -> Toast.makeText(UploadActivity.this, "Failed to upload file metadata", Toast.LENGTH_SHORT).show());
                    }))
                    .addOnFailureListener(e -> Toast.makeText(UploadActivity.this, "Failed to upload file", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }
}
