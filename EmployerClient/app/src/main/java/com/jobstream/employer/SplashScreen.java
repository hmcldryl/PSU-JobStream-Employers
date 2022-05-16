package com.jobstream.employer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashScreen extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            if (!auth.getCurrentUser().getUid().isEmpty()) {
                db.collection("employers")
                        .document(auth.getCurrentUser().getUid())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult() != null) {
                                        Toast.makeText(SplashScreen.this, "Welcome, " + task.getResult().getString("businessName") + "!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SplashScreen.this, MainActivity.class));
                                    } else {
                                        auth.signOut();
                                        startActivity(new Intent(SplashScreen.this, SignInActivity.class));
                                    }
                                    finish();
                                } else {
                                    auth.signOut();
                                    startActivity(new Intent(SplashScreen.this, SignInActivity.class));
                                    finish();
                                }
                            }
                        });
            }
        } else {
            startActivity(new Intent(SplashScreen.this, SignInActivity.class));
            finish();
        }
    }
}