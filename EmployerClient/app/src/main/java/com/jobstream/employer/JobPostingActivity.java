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
import com.jobstream.employer.system.adapters.AdapterPostListRV;
import com.jobstream.employer.system.models.Post;

public class JobPostingActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;

    MaterialToolbar toolbar;
    RecyclerView postList;
    FloatingActionButton btnNewPost;

    AdapterPostListRV adapterPostListRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_posting);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.toolbar);
        postList = findViewById(R.id.postList);
        btnNewPost = findViewById(R.id.btnNewPost);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        btnNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(JobPostingActivity.this, NewJobPostActivity.class));
            }
        });

        Query query = db.collection("posts")
                .whereEqualTo("employer", db.collection("employers").document(auth.getCurrentUser().getUid()))
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build();

        adapterPostListRV = new AdapterPostListRV(options, this);
        LinearLayoutManager manager = new LinearLayoutManager(this);

        postList.setHasFixedSize(true);
        postList.setLayoutManager(manager);
        postList.setAdapter(adapterPostListRV);

        db.collection("posts")
                .whereEqualTo("employer", db.collection("employers").document(auth.getCurrentUser().getUid()))
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
        adapterPostListRV.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapterPostListRV.stopListening();
    }
}