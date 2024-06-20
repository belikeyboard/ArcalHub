package com.example.proyectoaplicacion;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import android.webkit.MimeTypeMap;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class DownloadActivity extends AppCompatActivity {
    private StorageReference storageReference;
    private FirebaseFirestore db;

    private Spinner spinnerProject, spinnerEvent, spinnerTrack, spinnerFile;
    private Button buttonDownloadFile, buttonReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        db = FirebaseFirestore.getInstance();

        spinnerProject = findViewById(R.id.projectSpinner);
        spinnerEvent = findViewById(R.id.eventSpinner);
        spinnerTrack = findViewById(R.id.trackSpinner);
        spinnerFile = findViewById(R.id.nameFileSpinner);
        buttonDownloadFile = findViewById(R.id.downloadButton);
        buttonReturn = findViewById(R.id.returnButton);

        //databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

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

        spinnerTrack.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                populateFileSpinner(
                        parent.getItemAtPosition(position).toString(),
                        spinnerEvent.getSelectedItem().toString(),
                        spinnerProject.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        buttonDownloadFile.setOnClickListener(v -> downloadFile());
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
        eventList.add("Empty");
        db.collection("Events")
                .whereEqualTo("project", project).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
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
        if (!event.equals("Empty")) {
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

    private void populateFileSpinner(String track, String event, String project) {
        ArrayList<String> fileList = new ArrayList<>();
        ArrayAdapter<String> fileAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fileList);
        fileAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFile.setAdapter(fileAdapter);
        fileList.add("Empty");
        Query query = db.collection("Files")
                .whereEqualTo("project", project);

        // Add conditions for event and track if they are not "Empty"
        if (!event.equals("Empty")) {
            query = query.whereEqualTo("event", event);
        }
        if (!track.equals("Empty")) {
            query = query.whereEqualTo("track", track);
        }
        query.get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        if (document.exists()) {
                            String fileName = document.getString("filename");
                            fileList.add(fileName);
                        }
                    }
                } else {
                    System.err.println("Exception retrieving files");
                }
            });
        fileAdapter.notifyDataSetChanged();
    }

    private void downloadFile() {
        String project = spinnerProject.getSelectedItem().toString();
        String event = spinnerEvent.getSelectedItem().toString();
        String track = spinnerTrack.getSelectedItem().toString();
        String selectedFile = spinnerFile.getSelectedItem() != null ? spinnerFile.getSelectedItem().toString() : null;

        if (selectedFile != null && !selectedFile.equals("Empty")) {
            Log.d("selectedFile", "selectedFile: " + selectedFile);
            Query query = db.collection("Files")
                    .whereEqualTo("project", project)
                    .whereEqualTo("filename", selectedFile);

            // Add conditions for event and track if they are not "Empty"
            if (!event.equals("Empty")) {
                query = query.whereEqualTo("event", event);
            }
            if (!track.equals("Empty")) {
                query = query.whereEqualTo("track", track);
            }
            query.get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("taskSuccessful",
                                    "project: " + project
                                            + ", event: " + event
                                            + ", track: " + track
                                            + ", selectedFile: " + selectedFile);
                            for (DocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    Log.d("document", "Document exists: true");
                                    String fileURL = document.getString("fileUrl");
                                    Log.d("fileURL", "File URL: " + fileURL);
                                    if (fileURL != null) {
                                        openFile(fileURL);
                                        return;
                                    }
                                } else {
                                    Log.d("document", "Document exists: false");
                                }
                            }
                            Toast.makeText(DownloadActivity.this, "File URL not found", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(DownloadActivity.this, "Failed to retrieve file URL", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(DownloadActivity.this, "No file selected or invalid file", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFile(String fileURL) {
        Uri uri = Uri.parse(fileURL);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setDataAndType(uri, getMimeType(uri.toString()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(DownloadActivity.this, "No app found to open this file type", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper function to get MIME type from URI
    private String getMimeType(String url) {
        String mimeType = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        return mimeType;
    }



//            String folderName = project
//                    + (!event.equals("Empty") ? ("_" + event) : "")
//                    + (!track.equals("Empty") ? ("_" + track) : "")
//                    + "_" + selectedFile
//                    + ".zip";
//            StorageReference fileRef = storageReference.child("uploads/" + selectedFile);
//
//            try {
//                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//                File file = new File(downloadsDir, selectedFile);
//
//                fileRef.getFile(file)
//                        .addOnSuccessListener(taskSnapshot -> {
//                            createZipFile(file, new File(downloadsDir, folderName));
//                        })
//                        .addOnFailureListener(e -> {
//                            e.printStackTrace();
//                            if (e instanceof StorageException) {
//                                StorageException se = (StorageException) e;
//                                int errorCode = se.getErrorCode();
//                                int httpResultCode = se.getHttpResultCode();
//                                Toast.makeText(DownloadActivity.this, "Error Code: " + errorCode + " HttpResult: " + httpResultCode, Toast.LENGTH_LONG).show();
//                            } else {
//                                Toast.makeText(DownloadActivity.this, "Failed to get file URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                            }
//                        });
//            } catch (Exception e) {
//                e.printStackTrace();
//                Toast.makeText(DownloadActivity.this, "Failed to download file", Toast.LENGTH_SHORT).show();
//            }




//    private void createZipFile(File fileToZip, File zipFile) {
//        try (FileOutputStream fos = new FileOutputStream(zipFile);
//             ZipOutputStream zos = new ZipOutputStream(fos)) {
//            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
//            zos.putNextEntry(zipEntry);
//
//            FileInputStream fis = new FileInputStream(fileToZip);
//            byte[] buffer = new byte[1024];
//            int length;
//            while ((length = fis.read(buffer)) >= 0) {
//                zos.write(buffer, 0, length);
//            }
//            fis.close();
//            zos.closeEntry();
//
//            Toast.makeText(this, "File downloaded and zipped successfully", Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Failed to create zip file", Toast.LENGTH_SHORT).show();
//        }
//    }
}
