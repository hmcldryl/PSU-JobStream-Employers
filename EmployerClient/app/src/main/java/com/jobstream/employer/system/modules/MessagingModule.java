package com.jobstream.employer.system.modules;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.jobstream.employer.R;
import com.jobstream.employer.system.models.Message;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagingModule {

    FirebaseAuth auth;
    FirebaseFirestore db;

    TextInputLayout inputChat;
    TextInputEditText inputMessage;
    NestedScrollView messageContainer;
    LinearLayout messageGroup;
    View cover;

    Context context;

    String messageId;

    StuffFormatter formatter;

    public MessagingModule(FirebaseAuth auth, FirebaseFirestore db, NestedScrollView messageContainer, LinearLayout messageGroup, View cover, TextInputLayout inputChat, TextInputEditText inputMessage, Context context, String messageId) {
        this.auth = auth;
        this.db = db;
        this.messageContainer = messageContainer;
        this.messageGroup = messageGroup;
        this.cover = cover;
        this.inputChat = inputChat;
        this.inputMessage = inputMessage;
        this.context = context;
        this.messageId = messageId;
    }

    private Message message() {
        return new Message(db.collection("employers").document(auth.getCurrentUser().getUid()),
                inputMessage.getText().toString(),
                "text",
                generateTimestamp());
    }

    private Message message(String imageUrl) {
        return new Message(db.collection("employers").document(auth.getCurrentUser().getUid()),
                imageUrl,
                "image",
                generateTimestamp());
    }

    private Query messageQuery() {
        return db.collection("conversations")
                .document(messageId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING);
    }

    public void sendMessage() {
        db.collection("conversations")
                .document(messageId)
                .collection("messages")
                .add(message())
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            if (!inputMessage.getText().toString().isEmpty()) {
                                inputMessage.getText().clear();
                            }
                            db.collection("conversations")
                                    .document(messageId)
                                    .update("timestamp", generateTimestamp());
                        } else {
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void showMessages() {
        messageQuery()
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                loadMessage(dc.getDocument().getDocumentReference("user"), dc.getDocument().getString("type"), dc.getDocument().getString("message"), dc.getDocument().getTimestamp("timestamp"));
                            }
                        }

                        messageContainer.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                messageContainer.fullScroll(View.FOCUS_DOWN);

                                if (cover.getVisibility() == View.VISIBLE) {
                                    cover.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            cover.setVisibility(View.GONE);
                                        }
                                    }, 200);
                                }
                            }
                        }, 600);
                    }
                });
    }

    private void loadMessage(DocumentReference user, String type, String message, Timestamp timestamp) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout_message, messageGroup, false);
        LinearLayout container = view.findViewById(R.id.container);

        if (user.getId().equals(auth.getCurrentUser().getUid())) {
            View messageView = LayoutInflater.from(context).inflate(R.layout.item_layout_message_sender, container, false);
            MaterialCardView item = messageView.findViewById(R.id.item);
            ImageView image = messageView.findViewById(R.id.image);
            CircleImageView profilePhoto = messageView.findViewById(R.id.profilePhoto);
            MaterialTextView chatMessage = messageView.findViewById(R.id.message);
            MaterialTextView chatTimestamp = messageView.findViewById(R.id.timestamp);

            user.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                String photoUrl = task.getResult().getString("photoUrl");
                                if (photoUrl != null) {
                                    if (!photoUrl.isEmpty()) {
                                        Glide.with(context)
                                                .load(photoUrl)
                                                .into(profilePhoto);
                                    }
                                }
                            }
                        }
                    });

            if (type.equals("image")) {
                chatMessage.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
                if (message != null) {
                    Glide.with(context)
                            .load(message)
                            .into(image);

                    item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                            if (!alertDialog.isShowing()) {
                                final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_layout_view_image, null);
                                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alertDialog.setView(dialogView);
                                alertDialog.setCancelable(true);

                                ImageView image = dialogView.findViewById(R.id.image);

                                Glide.with(context)
                                        .load(message)
                                        .into(image);

                                alertDialog.show();
                            }
                        }
                    });
                }
            } else {
                chatMessage.setText(message);
            }

            chatTimestamp.setText(new PrettyTime().format(timestamp.toDate()));

            container.addView(messageView);
        } else {
            View messageView = LayoutInflater.from(context).inflate(R.layout.item_layout_message_receiver, container, false);
            MaterialCardView item = messageView.findViewById(R.id.item);
            ImageView image = messageView.findViewById(R.id.image);
            CircleImageView profilePhoto = messageView.findViewById(R.id.profilePhoto);
            MaterialTextView chatMessage = messageView.findViewById(R.id.message);
            MaterialTextView chatTimestamp = messageView.findViewById(R.id.timestamp);

            user.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                String photoUrl = task.getResult().getString("photoUrl");
                                if (photoUrl != null) {
                                    if (!photoUrl.isEmpty()) {
                                        Glide.with(context)
                                                .load(photoUrl)
                                                .into(profilePhoto);
                                    }
                                }
                            }
                        }
                    });

            if (type.equals("image")) {
                chatMessage.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
                if (message != null) {
                    Glide.with(context)
                            .load(message)
                            .into(image);

                    item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                            if (!alertDialog.isShowing()) {
                                final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_layout_view_image, null);
                                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                alertDialog.setView(dialogView);
                                alertDialog.setCancelable(true);

                                ImageView image = dialogView.findViewById(R.id.image);

                                Glide.with(context)
                                        .load(message)
                                        .into(image);

                                alertDialog.show();
                            }
                        }
                    });
                }
            } else {
                chatMessage.setText(message);
            }

            chatTimestamp.setText(formatter.formatTimestamp(timestamp));

            container.addView(messageView);
        }
        messageGroup.addView(view);
    }

    private Timestamp generateTimestamp() {
        return Timestamp.now();
    }

    private String generateFileTimestamp() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy MM dd_HH mm ss", Locale.ENGLISH);
        String formatted = format.format(Calendar.getInstance().getTime());
        return formatted.replaceAll("\\s", "");
    }

    public void uploadImage(Uri uri) {
        Uri resultUri = uri;
        String imageFilename = "IMG_" + generateFileTimestamp() + ".jpg";
        FirebaseStorage.getInstance().getReference()
                .child("employers")
                .child(auth.getCurrentUser().getUid())
                .child("messages")
                .child("images")
                .child(imageFilename)
                .putFile(resultUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            task.getResult().getMetadata().getReference().getDownloadUrl()
                                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful()) {
                                                db.collection("conversations")
                                                        .document(messageId)
                                                        .collection("messages")
                                                        .add(message(task.getResult().toString()))
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                            } else {
                                                Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
