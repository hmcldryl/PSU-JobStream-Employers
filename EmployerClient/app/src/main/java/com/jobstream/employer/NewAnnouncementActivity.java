package com.jobstream.employer;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.jobstream.employer.system.models.Announcement;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class NewAnnouncementActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;

    MaterialToolbar toolbar;
    TextInputEditText inputTitle,
            inputDescription;
    ImageView inputImage;
    FloatingActionButton btnUploadAnnouncement;

    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_announcement);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.toolbar);
        inputTitle = findViewById(R.id.inputTitle);
        inputDescription = findViewById(R.id.inputDescription);
        inputImage = findViewById(R.id.inputImage);
        btnUploadAnnouncement = findViewById(R.id.btnUploadAnnouncement);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        inputImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(4, 3)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(NewAnnouncementActivity.this);
            }
        });

        btnUploadAnnouncement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputCheck();
            }
        });
    }

    private void inputCheck() {
        if (inputTitle.getText().toString().isEmpty()) {
            inputTitle.setError("Please enter a title.");
        } else if (inputDescription.getText().toString().isEmpty()) {
            inputDescription.setError("Please enter a description.");
        } else {
            addNewAnnouncement();
        }
    }

    private Announcement announcement() {
        return new Announcement(db.collection("employers").document(auth.getCurrentUser().getUid()),
                "employer",
                inputTitle.getText().toString(),
                inputDescription.getText().toString(),
                generateTimestamp());
    }

    private Announcement announcement(String imageUrl) {
        return new Announcement(db.collection("employers").document(auth.getCurrentUser().getUid()),
                "employer",
                inputTitle.getText().toString(),
                inputDescription.getText().toString(),
                generateTimestamp(),
                imageUrl);
    }

    private void addNewAnnouncement() {
        final ACProgressFlower dialog = new ACProgressFlower.Builder(this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .fadeColor(Color.DKGRAY).build();
        dialog.show();

        if (imageUri != null) {
            String imageFilename = "IMG_" + generateFileTimestamp() + ".jpg";
            FirebaseStorage.getInstance().getReference()
                    .child("employers")
                    .child(auth.getCurrentUser().getUid())
                    .child("announcements")
                    .child("images")
                    .child(imageFilename)
                    .putFile(imageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                task.getResult().getMetadata().getReference().getDownloadUrl()
                                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Uri> task) {
                                                if (task.isSuccessful()) {
                                                    db.collection("announcements")
                                                            .add(announcement(task.getResult().toString()))
                                                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                    if (task.isSuccessful()) {
                                                                        dialog.dismiss();
                                                                        finish();
                                                                        Toast.makeText(NewAnnouncementActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                                                    } else {
                                                                        dialog.dismiss();
                                                                        Toast.makeText(NewAnnouncementActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        });
                            }
                        }
                    });
        } else {
            db.collection("announcements")
                    .add(announcement())
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                dialog.dismiss();
                                finish();
                                Toast.makeText(NewAnnouncementActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            } else {
                                dialog.dismiss();
                                Toast.makeText(NewAnnouncementActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private Timestamp generateTimestamp() {
        return Timestamp.now();
    }

    private String generateFileTimestamp() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy MM dd_HH mm ss", Locale.ENGLISH);
        String formatted = format.format(Calendar.getInstance().getTime());
        return formatted.replaceAll("\\s", "");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                inputImage.setImageURI(imageUri);
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, result.getError().toString(), Toast.LENGTH_LONG).show();
            }
        }
    }
}