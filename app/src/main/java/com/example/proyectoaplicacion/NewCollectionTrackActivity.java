package com.example.proyectoaplicacion;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewCollectionTrackActivity extends AppCompatActivity {

    private Spinner spinnerProject, spinnerEvent;
    private EditText editTextTrack;
    private Button buttonSubmit, buttonReturn;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_track);

        db = FirebaseFirestore.getInstance();

        spinnerProject = findViewById(R.id.projectSpinner);
        spinnerEvent = findViewById(R.id.eventSpinner);
        editTextTrack = findViewById(R.id.trackEditText);

        buttonSubmit = findViewById(R.id.submitButton);
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

        buttonSubmit.setOnClickListener(v -> submit_track());
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

    private void submit_track() {
        String newTrack = editTextTrack.getText().toString();
        String event = spinnerEvent.getSelectedItem().toString();
        String project = spinnerProject.getSelectedItem().toString();

        if (newTrack != null && !newTrack.isEmpty()) {
            if (event != null && !event.isEmpty()) {
                if (project != null && !project.isEmpty()) {
                    Map<String, Object> trackToSave = new HashMap<>();
                    trackToSave.put("track", newTrack);
                    trackToSave.put("event", event);
                    trackToSave.put("project", project);

                    db.collection("Tracks")
                            .add(trackToSave)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(NewCollectionTrackActivity.this, "Track saved successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> System.err.println("Exception adding document to Firebase: " + e));
                } else {
                    Toast.makeText(NewCollectionTrackActivity.this, "Project can't be empty", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(NewCollectionTrackActivity.this, "Event can't be empty", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(NewCollectionTrackActivity.this, "Track can't be empty", Toast.LENGTH_SHORT).show();
        }
    }
}