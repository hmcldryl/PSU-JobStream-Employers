package com.jobstream.employer;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseUser user;

    MaterialToolbar toolbar;

    MaterialTextView textBtnVerifyEmail,
            textBtnChangeEmail,
            textBtnChangePassword,
            textBtnCheckUpdates;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        user = auth.getCurrentUser();

        toolbar = findViewById(R.id.toolbar);
        textBtnVerifyEmail = findViewById(R.id.textBtnVerifyEmail);
        textBtnChangeEmail = findViewById(R.id.textBtnChangeEmail);
        textBtnChangePassword = findViewById(R.id.textBtnChangePassword);
        textBtnCheckUpdates = findViewById(R.id.textBtnCheckUpdates);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        textBtnVerifyEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.getCurrentUser().sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SettingsActivity.this, "Verification mail sent to your account email address.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        textBtnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog alertDialog = new AlertDialog.Builder(SettingsActivity.this).create();
                if (!alertDialog.isShowing()) {
                    final View dialogView = LayoutInflater.from(SettingsActivity.this).inflate(R.layout.dialog_layout_change_password, null);
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertDialog.setCancelable(true);
                    alertDialog.setView(dialogView);

                    TextInputEditText inputCurrentPassword = dialogView.findViewById(R.id.inputCurrentPassword);
                    TextInputEditText inputNewPassword = dialogView.findViewById(R.id.inputNewPassword);
                    TextInputEditText inputConfirmNewPassword = dialogView.findViewById(R.id.inputConfirmNewPassword);
                    MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
                    MaterialButton btnOk = dialogView.findViewById(R.id.btnOk);

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });

                    btnOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String currentPassword = inputCurrentPassword.getText().toString().trim();
                            String newPassword = inputNewPassword.getText().toString().trim();
                            String confirmNewPassword = inputConfirmNewPassword.getText().toString().trim();

                            if (currentPassword.isEmpty()) {
                                inputCurrentPassword.setError("Please enter your current password.");
                            } else if (newPassword.isEmpty()) {
                                inputNewPassword.setError("Please enter your new password.");
                            } else if (confirmNewPassword.isEmpty()) {
                                inputConfirmNewPassword.setError("Please re-enter your new password.");
                            } else if (!newPassword.equals(confirmNewPassword)) {
                                inputNewPassword.setError("Password does not match.");
                                inputConfirmNewPassword.setError("Password does not match.");
                            } else {
                                updatePassword(newPassword);
                            }
                        }
                    });

                    alertDialog.show();
                }
            }
        });

        textBtnCheckUpdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateApp();
            }
        });
    }

    private void updateEmail(String newEmail) {
        user.verifyBeforeUpdateEmail(newEmail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Verification email sent, proceed to your PSU mail inbox to complete the process.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void updatePassword(String newPassword) {
        user.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Password updated.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void initializeChangeEmail() {
        textBtnChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog alertDialog = new AlertDialog.Builder(SettingsActivity.this).create();
                if (!alertDialog.isShowing()) {
                    final View dialogView = LayoutInflater.from(SettingsActivity.this).inflate(R.layout.dialog_layout_change_email, null);
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertDialog.setCancelable(true);
                    alertDialog.setView(dialogView);

                    TextInputEditText inputEmail = dialogView.findViewById(R.id.inputEmail);
                    MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
                    MaterialButton btnOk = dialogView.findViewById(R.id.btnOk);

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });

                    btnOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String email = inputEmail.getText().toString().trim();

                            if (email.isEmpty()) {
                                inputEmail.setError("Please enter your PSU email.");
                            } else if (!email.substring(email.indexOf("@") + 1).equals("psu.palawan.edu.ph")) {
                                inputEmail.setError("Please enter a valid PSU email.");
                            } else {
                                updateEmail(email);
                            }
                        }
                    });

                    alertDialog.show();
                }
            }
        });
    }

    private void updateApp() {
        AppUpdater appUpdater = new AppUpdater(this)
                .setTitleOnUpdateAvailable("Update Available")
                .setContentOnUpdateAvailable("Download the latest version of PSU JobStream for Employers.")
                .setContentOnUpdateNotAvailable("No Updates Available")
                .setTitleOnUpdateNotAvailable("You have the latest version of PSU JobStream for Employers.")
                .setDisplay(Display.DIALOG)
                .setUpdateFrom(UpdateFrom.GITHUB)
                .setGitHubUserAndRepo("hmcldryl", "PSU-JobStream-Employers");
        appUpdater.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (user != null) {
            if (user.isEmailVerified()) {
                textBtnVerifyEmail.setVisibility(View.GONE);
                initializeChangeEmail();
            } else {
                textBtnVerifyEmail.setVisibility(View.VISIBLE);
                textBtnChangeEmail.setVisibility(View.GONE);
            }
        }
    }
}