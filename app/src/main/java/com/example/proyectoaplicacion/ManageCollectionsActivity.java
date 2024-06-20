package com.example.proyectoaplicacion;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class ManageCollectionsActivity extends AppCompatActivity {

    private Button buttonNewProject, buttonNewEvent, buttonNewTrack, buttonReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

        buttonNewProject = findViewById(R.id.newProjectButon);
        buttonNewEvent = findViewById(R.id.newEventButon);
        buttonNewTrack = findViewById(R.id.newTrackButon);
        buttonReturn = findViewById(R.id.returnButton);

        buttonNewProject.setOnClickListener(v -> startActivity(new Intent(ManageCollectionsActivity.this, NewCollectionProjectActivity.class)));
        buttonNewEvent.setOnClickListener(v -> startActivity(new Intent(ManageCollectionsActivity.this, NewCollectionEventActivity.class)));
        buttonNewTrack.setOnClickListener(v -> startActivity(new Intent(ManageCollectionsActivity.this, NewCollectionTrackActivity.class)));
        buttonReturn.setOnClickListener(v -> finish());
    }
}