package com.jobstream.employer;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobstream.employer.system.adapters.ExperienceTimelineAdapter;
import com.jobstream.employer.system.other.SkillIcons;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;

    MaterialToolbar toolbar;
    CircleImageView photoView;
    ImageView bannerView;
    ViewGroup skillGroup;
    ChipGroup keywordChipGroup;
    RecyclerView timelineExperienceRV;
    HorizontalScrollView skillItemContainer;
    MaterialTextView profileName,
            profileBio;
    MaterialCardView skillPrompt,
            experiencePrompt;

    ExperienceTimelineAdapter experienceTimelineAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        photoView = findViewById(R.id.photoView);
        bannerView = findViewById(R.id.bannerView);
        skillGroup = findViewById(R.id.skillGroup);
        skillItemContainer = findViewById(R.id.skillItemContainer);
        keywordChipGroup = findViewById(R.id.keywordChipGroup);
        timelineExperienceRV = findViewById(R.id.timelineExperienceRV);
        profileName = findViewById(R.id.profileName);
        profileBio = findViewById(R.id.profileBio);
        skillPrompt = findViewById(R.id.skillPrompt);
        experiencePrompt = findViewById(R.id.experiencePrompt);

        toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        findViewById(R.id.btnMessage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog alertDialog = new AlertDialog.Builder(UserProfileActivity.this).create();
                if (!alertDialog.isShowing()) {
                    final View dialogView = LayoutInflater.from(UserProfileActivity.this).inflate(R.layout.dialog_layout_send_message, null);
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertDialog.setView(dialogView);

                    TextInputEditText inputMessage = dialogView.findViewById(R.id.inputMessage);
                    MaterialButton btnOk = dialogView.findViewById(R.id.btnOk);
                    MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);

                    btnOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (inputMessage.getText().toString().isEmpty()) {
                                inputMessage.setError("Please enter a message.");
                            } else {
                                final ACProgressFlower dialog = new ACProgressFlower.Builder(UserProfileActivity.this)
                                        .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                                        .themeColor(Color.WHITE)
                                        .fadeColor(Color.DKGRAY)
                                        .build();
                                dialog.show();

                                String text = inputMessage.getText().toString();
                                inputMessage.getText().clear();
                                HashMap<String, Object> conversation = new HashMap<>();
                                conversation.put("participant", Arrays.asList(auth.getCurrentUser().getUid(), getIntent().getStringExtra("userId")));
                                conversation.put("timestamp", generateTimestamp());
                                db.collection("conversations")
                                        .document(auth.getCurrentUser().getUid() + getIntent().getStringExtra("userId"))
                                        .update(conversation)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    HashMap<String, Object> message = new HashMap<>();
                                                    message.put("user", db.collection("users").document(auth.getCurrentUser().getUid()));
                                                    message.put("message", text);
                                                    message.put("type", "text");
                                                    message.put("timestamp", generateTimestamp());
                                                    db.collection("conversations")
                                                            .document(auth.getCurrentUser().getUid() + getIntent().getStringExtra("userId"))
                                                            .collection("messages")
                                                            .add(message)
                                                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                    if (task.isSuccessful()) {
                                                                        dialog.dismiss();
                                                                        alertDialog.dismiss();
                                                                        Toast.makeText(UserProfileActivity.this, "Message sent.", Toast.LENGTH_SHORT).show();
                                                                    } else {
                                                                        dialog.dismiss();
                                                                        inputMessage.setEnabled(true);
                                                                        Toast.makeText(UserProfileActivity.this, "Sending failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                } else {
                                                    db.collection("conversations")
                                                            .document(auth.getCurrentUser().getUid() + getIntent().getStringExtra("userId"))
                                                            .set(conversation)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        HashMap<String, Object> message = new HashMap<>();
                                                                        message.put("user", db.collection("users").document(auth.getCurrentUser().getUid()));
                                                                        message.put("message", text);
                                                                        message.put("type", "text");
                                                                        message.put("timestamp", generateTimestamp());
                                                                        db.collection("conversations")
                                                                                .document(auth.getCurrentUser().getUid() + getIntent().getStringExtra("userId"))
                                                                                .collection("messages")
                                                                                .add(message)
                                                                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            dialog.dismiss();
                                                                                            alertDialog.dismiss();
                                                                                            Toast.makeText(UserProfileActivity.this, "Message sent.", Toast.LENGTH_SHORT).show();
                                                                                        } else {
                                                                                            dialog.dismiss();
                                                                                            inputMessage.setEnabled(true);
                                                                                            Toast.makeText(UserProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                                                        }
                                                                                    }
                                                                                });
                                                                    } else {
                                                                        dialog.dismiss();
                                                                        inputMessage.setEnabled(true);
                                                                        Toast.makeText(UserProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        });
                            }
                        }
                    });

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.show();
                }
            }
        });

        initExperienceTimelineView();

        updateUI();
    }

    private Timestamp generateTimestamp() {
        return Timestamp.now();
    }

    private void updateUI() {
        db.collection("users")
                .document(getIntent().getStringExtra("userId"))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                if (task.getResult().exists()) {
                                    String photoUrl = task.getResult().getString("photoUrl");
                                    String bannerUrl = task.getResult().getString("bannerUrl");
                                    String firstName = task.getResult().getString("firstName");
                                    String lastName = task.getResult().getString("lastName");
                                    String bio = task.getResult().getString("bio");
                                    List<String> keyword = (List<String>) task.getResult().get("keyword");
                                    List<Map<String, Object>> skill = (List<Map<String, Object>>) task.getResult().get("skillList");
                                    List<Map<String, Object>> experience = (List<Map<String, Object>>) task.getResult().get("experienceTimeline");

                                    if (photoUrl != null) {
                                        if (!photoUrl.isEmpty()) {
                                            Glide.with(UserProfileActivity.this)
                                                    .load(photoUrl)
                                                    .into(photoView);
                                        }
                                    }

                                    if (bannerUrl != null) {
                                        if (!bannerUrl.isEmpty()) {
                                            Glide.with(UserProfileActivity.this)
                                                    .load(bannerUrl)
                                                    .into(bannerView);
                                        }
                                    }

                                    if (bio != null) {
                                        if (!bio.isEmpty()) {
                                            profileBio.setText(bio);
                                        }
                                    } else {
                                        profileBio.setText("This user has not set anything for their bio.");
                                    }

                                    profileName.setText(firstName + " " + lastName);
                                    toolbar.setSubtitle(firstName + " " + lastName);

                                    if (keyword != null) {
                                        if (keyword.size() > 0) {
                                            keywordChipGroup.removeAllViews();
                                            for (int i = 0; i < keyword.size(); i++) {
                                                addKeywordChip(keyword.get(i));
                                            }
                                        }
                                    }

                                    if (skill != null) {
                                        if (skill.size() > 0) {
                                            skillGroup.removeAllViews();
                                            for (int i = 0; i < skill.size(); i++) {
                                                loadSkill(skill.get(i));
                                            }
                                            skillPrompt.setVisibility(View.GONE);
                                            skillItemContainer.setVisibility(View.VISIBLE);
                                        } else {
                                            skillItemContainer.setVisibility(View.GONE);
                                            skillPrompt.setVisibility(View.VISIBLE);
                                        }
                                    } else {
                                        skillItemContainer.setVisibility(View.GONE);
                                        skillPrompt.setVisibility(View.VISIBLE);
                                    }

                                    if (experience != null) {
                                        if (experience.size() > 0) {
                                            experiencePrompt.setVisibility(View.GONE);
                                            timelineExperienceRV.setVisibility(View.VISIBLE);

                                            experienceTimelineAdapter = new ExperienceTimelineAdapter(experience);
                                            timelineExperienceRV.setAdapter(experienceTimelineAdapter);
                                        } else {
                                            timelineExperienceRV.setVisibility(View.GONE);
                                            experiencePrompt.setVisibility(View.VISIBLE);
                                        }
                                    } else {
                                        timelineExperienceRV.setVisibility(View.GONE);
                                        experiencePrompt.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        }
                    }
                });
    }

    private void loadSkill(Map<String, Object> skill) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_layout_skill, skillGroup, false);
        ImageView skillIcon = view.findViewById(R.id.skillIcon);
        MaterialTextView skillTitle = view.findViewById(R.id.skillTitle);

        SkillIcons skillIcons = new SkillIcons();
        String icon = (String) skill.get("icon");

        skillIcon.setImageResource(skillIcons.getIcon(icon));
        skillTitle.setText((String) skill.get("title"));
        skillTitle.setSelected(true);

        skillGroup.addView(view, skillGroup.getChildCount() - 1);
    }

    private void addKeywordChip(String keyword) {
        final Chip chip = new Chip(this);
        chip.setTextAppearance(R.style.ChipTextAppearance);
        chip.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.white)));
        chip.setTextColor(getResources().getColor(R.color.textColorButton));
        chip.setText(keyword);
        chip.setChipIcon(getResources().getDrawable(R.drawable.ic_icon_keyword_tinted));
        keywordChipGroup.addView(chip);
    }

    public void initExperienceTimelineView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        timelineExperienceRV.setLayoutManager(layoutManager);
    }
}