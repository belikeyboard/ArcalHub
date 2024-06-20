package com.example.proyectoaplicacion;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NewCollectionProjectActivity extends AppCompatActivity {

    private EditText editTextProject;
    private Button buttonSubmit, buttonReturn;

    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_project);

        editTextProject = findViewById(R.id.projectEditText);
        buttonSubmit = findViewById(R.id.submitButton);
        buttonReturn = findViewById(R.id.returnButton);

        db = FirebaseFirestore.getInstance();

        buttonSubmit.setOnClickListener(v -> submit_project());
        buttonReturn.setOnClickListener(v -> finish());
    }

    private void submit_project() {
        String newProject = editTextProject.getText().toString();

        if (newProject != null && !newProject.isEmpty()) {
            Map<String, Object> projectToSave = new HashMap<>();
            projectToSave.put("project", newProject);

            db.collection("Projects")
                    .add(projectToSave)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(NewCollectionProjectActivity.this, "Project saved successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> System.err.println("Exception adding document to Firebase: " + e));
        } else {
            Toast.makeText(NewCollectionProjectActivity.this, "Project can't be empty", Toast.LENGTH_SHORT).show();
        }
    }
}
