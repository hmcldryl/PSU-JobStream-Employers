package com.jobstream.employer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.jobstream.employer.system.adapters.AdapterAnnouncementListRV;
import com.jobstream.employer.system.models.Announcement;

public class AnnouncementActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;

    MaterialToolbar toolbar;
    RecyclerView announcementList;
    FloatingActionButton btnNewAnnouncement;

    AdapterAnnouncementListRV adapterAnnouncementListRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.toolbar);
        announcementList = findViewById(R.id.announcementList);
        btnNewAnnouncement = findViewById(R.id.btnNewAnnouncement);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        btnNewAnnouncement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AnnouncementActivity.this, NewAnnouncementActivity.class));
            }
        });

        Query query = db.collection("announcements")
                .whereEqualTo("user", db.collection("employers").document(auth.getCurrentUser().getUid()))
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Announcement> options = new FirestoreRecyclerOptions.Builder<Announcement>()
                .setQuery(query, Announcement.class)
                .build();

        adapterAnnouncementListRV = new AdapterAnnouncementListRV(options, this);
        LinearLayoutManager manager = new LinearLayoutManager(this);

        announcementList.setHasFixedSize(true);
        announcementList.setLayoutManager(manager);
        announcementList.setAdapter(adapterAnnouncementListRV);

        db.collection("announcements")
                .whereEqualTo("user", db.collection("employers").document(auth.getCurrentUser().getUid()))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null) {
                            if (value.size() > 0) {
                                //
                            } else {
                                //
                            }
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        adapterAnnouncementListRV.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapterAnnouncementListRV.stopListening();
    }
}