package com.jobstream.employer;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class SignInActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;

    MaterialTextView btnTerms,
            btnPolicy;
    TextInputEditText inputEmail,
            inputPassword;
    MaterialButton btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnTerms = findViewById(R.id.btnTerms);
        btnPolicy = findViewById(R.id.btnPolicy);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnSignIn = findViewById(R.id.btnSignIn);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputCheck()) {
                    signIn();
                }
            }
        });

        btnTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog alertDialog = new AlertDialog.Builder(SignInActivity.this).create();
                if (!alertDialog.isShowing()) {
                    final View dialogView = LayoutInflater.from(SignInActivity.this).inflate(R.layout.dialog_webview, null);
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertDialog.setCancelable(true);
                    alertDialog.setView(dialogView);

                    WebView webView = dialogView.findViewById(R.id.webView);

                    db.collection("system")
                            .document("data")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        String htmlTerms = task.getResult().getString("textTermsConditions");

                                        webView.loadData(htmlTerms, "text/html", "UTF-8");
                                    }
                                }
                            });

                    alertDialog.show();
                }
            }
        });

        btnPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog alertDialog = new AlertDialog.Builder(SignInActivity.this).create();
                if (!alertDialog.isShowing()) {
                    final View dialogView = LayoutInflater.from(SignInActivity.this).inflate(R.layout.dialog_webview, null);
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertDialog.setCancelable(true);
                    alertDialog.setView(dialogView);

                    WebView webView = dialogView.findViewById(R.id.webView);

                    db.collection("system")
                            .document("data")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        String htmlPolicy = task.getResult().getString("textPrivacyPolicy");

                                        webView.loadData(htmlPolicy, "text/html", "UTF-8");
                                    }
                                }
                            });

                    alertDialog.show();
                }
            }
        });
    }

    private void signIn() {
        final ACProgressFlower dialog = new ACProgressFlower.Builder(SignInActivity.this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .fadeColor(Color.DKGRAY)
                .build();
        dialog.show();

        auth.signInWithEmailAndPassword(inputEmail.getText().toString(), inputPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            verifyAccount(dialog);
                        } else {
                            dialog.dismiss();
                            Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private boolean isAnEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        if (email == null) {
            return false;
        } else {
            return pattern.matcher(email).matches();
        }
    }

    private boolean inputCheck() {
        if (inputEmail.getText().toString().isEmpty()) {
            inputEmail.setError("Please enter a valid email.");
            return false;
        } else if (!isAnEmail(inputEmail.getText().toString())) {
            inputEmail.setError("Please enter a valid email.");
            return false;
        } else if (inputPassword.getText().toString().isEmpty()) {
            inputPassword.setError("Please enter your password.");
            return false;
        } else {
            return true;
        }
    }

    private void verifyAccount(ACProgressFlower dialog) {
        if (auth.getCurrentUser() != null) {
            db.collection("employers")
                    .document(auth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                dialog.dismiss();
                                startActivity(new Intent(SignInActivity.this, MainActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                            } else {
                                auth.signOut();
                                dialog.dismiss();
                                Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            dialog.dismiss();
            Toast.makeText(this, "Sign in failed. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();
        }
    }
}