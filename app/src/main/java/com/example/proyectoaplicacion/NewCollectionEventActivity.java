package com.example.proyectoaplicacion;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class NewCollectionEventActivity extends AppCompatActivity {

    private Spinner spinnerProject;
    private EditText editTextEvent;
    private Button buttonSubmit, buttonReturn;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        db = FirebaseFirestore.getInstance();

        spinnerProject = findViewById(R.id.projectSpinner);
        editTextEvent = findViewById(R.id.eventEditText);

        buttonSubmit = findViewById(R.id.submitButton);
        buttonReturn = findViewById(R.id.returnButton);

        populateProjectSpinner();

        buttonSubmit.setOnClickListener(v -> submit_event());
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

    private void submit_event() {
        String newEvent = editTextEvent.getText().toString();
        String project = spinnerProject.getSelectedItem().toString();

        if (newEvent != null && !newEvent.isEmpty()) {
            if (project != null && !project.isEmpty()) {
                Map<String, Object> eventToSave = new HashMap<>();
                eventToSave.put("event", newEvent);
                eventToSave.put("project", project);

                db.collection("Events")
                        .add(eventToSave)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(NewCollectionEventActivity.this, "Event saved successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> System.err.println("Exception adding document to Firebase: " + e));
            } else {
                Toast.makeText(NewCollectionEventActivity.this, "Project can't be empty", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(NewCollectionEventActivity.this, "Event can't be empty", Toast.LENGTH_SHORT).show();
        }
    }
}