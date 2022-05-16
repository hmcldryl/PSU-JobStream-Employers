package com.jobstream.employer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobstream.employer.system.modules.MessagingModule;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MessageActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;

    MaterialToolbar toolbar;
    NestedScrollView messageContainer;
    LinearLayout messageGroup;
    View cover;
    TextInputLayout inputChat;
    TextInputEditText inputMessage;
    ImageButton btnUploadMedia,
            btnUploadDocument,
            btnSendChat;

    MessagingModule messagingModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.toolbar);
        messageContainer = findViewById(R.id.messageContainer);
        messageGroup = findViewById(R.id.messageGroup);
        cover = findViewById(R.id.cover);
        inputChat = findViewById(R.id.inputChat);
        inputMessage = findViewById(R.id.inputMessage);
        btnUploadMedia = findViewById(R.id.btnUploadMedia);
        btnUploadDocument = findViewById(R.id.btnUploadDocument);
        btnSendChat = findViewById(R.id.btnSendChat);

        cover.setVisibility(View.VISIBLE);

        messagingModule = new MessagingModule(auth, db, messageContainer, messageGroup, cover, inputChat, inputMessage, this, getIntent().getStringExtra("id"));
        messagingModule.showMessages();

        updateUi();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        btnUploadMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(MessageActivity.this);
            }
        });

        btnUploadDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MessageActivity.this, "Coming Soon", Toast.LENGTH_SHORT).show();
                //uploadDocument();
            }
        });

        btnSendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputCheck()) {
                    messagingModule.sendMessage();
                }
            }
        });
    }

    private boolean inputCheck() {
        if (inputChat.getEditText().getText().toString().isEmpty()) {
            inputChat.setError("Please enter a message.");
            return false;
        } else {
            return true;
        }
    }

    private void updateUi() {
        db.collection("users")
                .document(getIntent().getStringExtra("userId"))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            String name = task.getResult().getString("firstName") + " " + task.getResult().getString("lastName");
                            String email = task.getResult().getString("email");
                            toolbar.setTitle(name);
                            toolbar.setSubtitle(email);
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                messagingModule.uploadImage(result.getUri());
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, result.getError().toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}