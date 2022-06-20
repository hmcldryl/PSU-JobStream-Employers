package com.jobstream.employer;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.jobstream.employer.system.modules.StuffFormatter;
import com.rajat.pdfviewer.PdfViewerActivity;

import org.sufficientlysecure.htmltextview.HtmlFormatter;
import org.sufficientlysecure.htmltextview.HtmlFormatterBuilder;
import org.sufficientlysecure.htmltextview.HtmlResImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;
import de.hdodenhof.circleimageview.CircleImageView;

public class JobPostActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseStorage storage;

    MaterialToolbar toolbar;
    ConstraintLayout imageContainer;
    ImageView postImage;
    HtmlTextView postDescription;
    MaterialTextView postTitle,
            postEmployerName,
            postLocation,
            postType,
            postSalary;
    ChipGroup keywordChipGroup;
    ExtendedFloatingActionButton btnViewApplication;

    StuffFormatter formatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_post);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        toolbar = findViewById(R.id.toolbar);
        imageContainer = findViewById(R.id.imageContainer);
        postImage = findViewById(R.id.postImage);
        postTitle = findViewById(R.id.postTitle);
        postEmployerName = findViewById(R.id.postEmployerName);
        postLocation = findViewById(R.id.postLocation);
        postType = findViewById(R.id.postType);
        postSalary = findViewById(R.id.postSalary);
        postDescription = findViewById(R.id.postDescription);
        keywordChipGroup = findViewById(R.id.keywordChipGroup);
        keywordChipGroup = findViewById(R.id.keywordChipGroup);
        btnViewApplication = findViewById(R.id.btnViewApplication);

        formatter = new StuffFormatter();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.btnDelete) {
                    Toast.makeText(JobPostActivity.this, "Please contact tech support to delete this post.", Toast.LENGTH_SHORT).show();
                    /*showAlertDialogDelete(getString(R.string.dialog_title_alert),
                            getString(R.string.dialog_subtitle_delete_post),
                            getString(R.string.dialog_alert_message_delete_post),
                            getString(R.string.button_text_no),
                            getString(R.string.button_text_yes));*/
                } else if (item.getItemId() == R.id.btnStatus) {
                    showAlertDialogStatus(getString(R.string.dialog_title_alert),
                            getString(R.string.dialog_subtitle_update_post),
                            getString(R.string.dialog_alert_message_update_post),
                            getString(R.string.button_text_no),
                            getString(R.string.button_text_yes));
                }
                return false;
            }
        });

        btnViewApplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postId = getIntent().getStringExtra("postId");

                db.collection("applications")
                        .whereEqualTo("post", db.collection("posts").document(postId))
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult() == null) {
                                        Toast.makeText(JobPostActivity.this, "No job applications yet.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        showViewApplicationDialog();
                                    }
                                } else {
                                    Toast.makeText(JobPostActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        updateUi();
    }

    private void showViewApplicationDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(JobPostActivity.this).create();
        if (!alertDialog.isShowing()) {
            final View dialogView = LayoutInflater.from(JobPostActivity.this).inflate(R.layout.dialog_layout_view_application, null);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.setCancelable(true);
            alertDialog.setView(dialogView);

            ViewGroup applicationGroup = dialogView.findViewById(R.id.applicationGroup);

            String postId = getIntent().getStringExtra("postId");

            db.collection("applications")
                    .whereEqualTo("post", db.collection("posts").document(postId))
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                applicationGroup.removeAllViews();
                                int count = task.getResult().getDocuments().size();
                                if (count > 0) {
                                    for (int i = 0; i < count; i++) {
                                        loadApplication(task.getResult().getDocuments().get(i).getReference(),
                                                task.getResult().getDocuments().get(i).getDocumentReference("post"),
                                                task.getResult().getDocuments().get(i).getString("intro"),
                                                (List<DocumentReference>) task.getResult().getDocuments().get(i).get("document"),
                                                task.getResult().getDocuments().get(i).getTimestamp("timestamp"),
                                                applicationGroup);
                                    }
                                }
                            }
                        }
                    });

            alertDialog.show();
        }
    }

    private void showAlertDialogStatus(String title, String subtitle, String message, String cancelText, String confirmText) {
        final AlertDialog alertDialog = new AlertDialog.Builder(JobPostActivity.this).create();
        if (!alertDialog.isShowing()) {
            final View dialogView = LayoutInflater.from(JobPostActivity.this).inflate(R.layout.dialog_layout_alert_message, null);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.setCancelable(false);
            alertDialog.setView(dialogView);

            MaterialTextView textAlertTitle = dialogView.findViewById(R.id.textAlertTitle);
            MaterialTextView textAlertSubtitle = dialogView.findViewById(R.id.textAlertSubtitle);
            MaterialTextView textAlertMessage = dialogView.findViewById(R.id.textAlertMessage);
            MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
            MaterialButton btnOk = dialogView.findViewById(R.id.btnOk);

            textAlertTitle.setText(title);
            textAlertSubtitle.setText(subtitle);
            textAlertMessage.setText(message);
            btnCancel.setText(cancelText);
            btnOk.setText(confirmText);

            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                    String postId = getIntent().getStringExtra("postId");
                    updateStatus(postId);
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                }
            });

            alertDialog.show();
        }
    }

    private void showAlertDialogDelete(String title, String subtitle, String message, String cancelText, String confirmText) {
        final AlertDialog alertDialog = new AlertDialog.Builder(JobPostActivity.this).create();
        if (!alertDialog.isShowing()) {
            final View dialogView = LayoutInflater.from(JobPostActivity.this).inflate(R.layout.dialog_layout_alert_message, null);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.setCancelable(false);
            alertDialog.setView(dialogView);

            MaterialTextView textAlertTitle = dialogView.findViewById(R.id.textAlertTitle);
            MaterialTextView textAlertSubtitle = dialogView.findViewById(R.id.textAlertSubtitle);
            MaterialTextView textAlertMessage = dialogView.findViewById(R.id.textAlertMessage);
            MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
            MaterialButton btnOk = dialogView.findViewById(R.id.btnOk);

            textAlertTitle.setText(title);
            textAlertSubtitle.setText(subtitle);
            textAlertMessage.setText(message);
            btnCancel.setText(cancelText);
            btnOk.setText(confirmText);

            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                    String postId = getIntent().getStringExtra("postId");
                    updateStatus(postId);
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                }
            });

            alertDialog.show();
        }
    }

    private void updateStatus(String postId) {
        final ACProgressFlower dialog = new ACProgressFlower.Builder(this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .fadeColor(Color.DKGRAY).build();
        dialog.show();

        db.collection("posts")
                .document(postId)
                .update("status", "done")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            Toast.makeText(JobPostActivity.this, "Update success.", Toast.LENGTH_SHORT).show();
                        } else {
                            dialog.dismiss();
                            Toast.makeText(JobPostActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void deletePost(String postId, String imageUrl) {
        final ACProgressFlower dialog = new ACProgressFlower.Builder(this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .fadeColor(Color.DKGRAY).build();
        dialog.show();

        db.collection("posts")
                .document(postId)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (imageUrl != null) {
                                if (!imageUrl.isEmpty()) {
                                    deletePostImage(dialog, imageUrl);
                                } else {
                                    dialog.dismiss();
                                    Toast.makeText(JobPostActivity.this, "Successfully deleted your post.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            } else {
                                dialog.dismiss();
                                Toast.makeText(JobPostActivity.this, "Successfully deleted your post.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            dialog.dismiss();
                            Toast.makeText(JobPostActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void deletePostImage(ACProgressFlower dialog, String url) {
        storage.getReferenceFromUrl(url)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            Toast.makeText(JobPostActivity.this, "Successfully deleted your post.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            dialog.dismiss();
                            Toast.makeText(JobPostActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void updateUi() {
        String employerId = getIntent().getStringExtra("employerId");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String title = getIntent().getStringExtra("title");
        String location = getIntent().getStringExtra("location");
        String type = getIntent().getStringExtra("type");
        Double salary = Double.parseDouble(getIntent().getStringExtra("salary"));
        String description = getIntent().getStringExtra("description");
        String timestamp = getIntent().getStringExtra("timestamp");
        List<String> keyword = getIntent().getStringArrayListExtra("keyword");

        db.collection("employers")
                .document(employerId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            postEmployerName.setText(task.getResult().getString("businessName"));
                        }
                    }
                });

        if (imageUrl != null) {
            if (!imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .into(postImage);
            }
        } else {
            imageContainer.setVisibility(View.GONE);
        }

        if (keyword != null) {
            if (keyword.size() > 0) {
                for (int i = 0; i < keyword.size(); i++) {
                    addKeywordChip(keyword.get(i));
                }
            }
        }

        if (salary > 0) {
            postSalary.setText(formatter.formatSalary(salary));
        } else {
            postSalary.setText("N/A");
        }

        postTitle.setText(title);
        renderRichText(postDescription, description);
        postLocation.setText(location);
        postType.setText(type);
        toolbar.setSubtitle("Posted " + timestamp);
    }

    private void loadApplication(DocumentReference application, DocumentReference post, String intro, List<DocumentReference> documentList, Timestamp timestamp, ViewGroup applicationGroup) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_layout_job_application, applicationGroup, false);
        MaterialCardView item = view.findViewById(R.id.item);
        CircleImageView businessPhoto = view.findViewById(R.id.businessPhoto);
        MaterialTextView applicationIntro = view.findViewById(R.id.applicationIntro);
        MaterialTextView businessName = view.findViewById(R.id.businessName);
        MaterialTextView postTitle = view.findViewById(R.id.postTitle);
        MaterialTextView postJobLocation = view.findViewById(R.id.postLocation);
        MaterialTextView postSalary = view.findViewById(R.id.postSalary);
        MaterialTextView postType = view.findViewById(R.id.postType);
        MaterialTextView postTimestamp = view.findViewById(R.id.postTimestamp);

        post.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            String title = task.getResult().getString("title");
                            String location = task.getResult().getString("location");
                            Double salary = task.getResult().getLong("salary").doubleValue();
                            String type = task.getResult().getString("type");
                            DocumentReference employer = task.getResult().getDocumentReference("employer");
                            String photoUrl = task.getResult().getString("photoUrl");

                            employer.get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                businessName.setText(task.getResult().getString("businessName"));

                                                if (photoUrl != null) {
                                                    if (!photoUrl.isEmpty()) {
                                                        Glide.with(JobPostActivity.this)
                                                                .load(photoUrl)
                                                                .into(businessPhoto);
                                                    }
                                                }
                                            }
                                        }
                                    });

                            if (salary > 0) {
                                postSalary.setText(formatter.formatSalary(salary));
                            } else {
                                postSalary.setText("N/A");
                            }

                            postTitle.setText(title);
                            applicationIntro.setText(getString(R.string.text_quotation) + intro + getString(R.string.text_quotation));
                            postJobLocation.setText(location);
                            postType.setText(type);
                            postTimestamp.setText("applied " + formatter.formatTimestamp(timestamp));

                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    postTimestamp.setText("applied " + formatter.formatTimestamp(timestamp));
                                }
                            }, 0, 60000);

                            item.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    final AlertDialog alertDialog = new AlertDialog.Builder(JobPostActivity.this).create();
                                    if (!alertDialog.isShowing()) {
                                        final View dialogView = LayoutInflater.from(JobPostActivity.this).inflate(R.layout.dialog_layout_job_application, null);
                                        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        alertDialog.setView(dialogView);
                                        alertDialog.setCancelable(true);

                                        MaterialTextView applicationIntro = dialogView.findViewById(R.id.applicationIntro);
                                        ViewGroup applicationGroup = dialogView.findViewById(R.id.applicationGroup);

                                        applicationIntro.setText(intro);

                                        for (int i = 0; i < documentList.size(); i++) {
                                            View documentView = LayoutInflater.from(JobPostActivity.this).inflate(R.layout.item_layout_application_document, applicationGroup, false);
                                            LinearLayout item = documentView.findViewById(R.id.item);
                                            ImageView documentThumbnail = documentView.findViewById(R.id.documentThumbnail);
                                            MaterialTextView documentName = documentView.findViewById(R.id.documentName);
                                            MaterialTextView documentType = documentView.findViewById(R.id.documentType);

                                            documentList.get(i).get()
                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                String docName = task.getResult().getString("documentName");
                                                                String docType = task.getResult().getString("documentType");
                                                                String documentUrl = task.getResult().getString("documentUrl");

                                                                if (docType.equals("application/pdf")) {
                                                                    Glide.with(JobPostActivity.this)
                                                                            .load(getResources().getDrawable(R.drawable.ic_icon_file_document))
                                                                            .into(documentThumbnail);
                                                                } else {
                                                                    Glide.with(JobPostActivity.this)
                                                                            .load(getResources().getDrawable(R.drawable.ic_icon_file_image))
                                                                            .into(documentThumbnail);
                                                                }

                                                                documentName.setText(docName);
                                                                documentType.setText(docType.equals("application/pdf") ? "PDF Document" : "JPEG Image");

                                                                item.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        if (docType.equals("application/pdf")) {
                                                                            view.getContext().startActivity(PdfViewerActivity.Companion.launchPdfFromUrl(
                                                                                    JobPostActivity.this,
                                                                                    documentUrl,
                                                                                    docName,
                                                                                    "",
                                                                                    false
                                                                            ));
                                                                        } else {
                                                                            final AlertDialog alertDialog = new AlertDialog.Builder(JobPostActivity.this).create();
                                                                            if (!alertDialog.isShowing()) {
                                                                                final View dialogView = LayoutInflater.from(JobPostActivity.this).inflate(R.layout.dialog_layout_view_image, null);
                                                                                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                                                alertDialog.setView(dialogView);
                                                                                alertDialog.setCancelable(true);

                                                                                ImageView image = dialogView.findViewById(R.id.image);

                                                                                Glide.with(JobPostActivity.this)
                                                                                        .load(documentUrl)
                                                                                        .into(image);

                                                                                alertDialog.show();
                                                                            }
                                                                        }
                                                                    }
                                                                });

                                                                applicationGroup.addView(documentView, 0);
                                                            }
                                                        }
                                                    });
                                        }

                                        alertDialog.show();
                                    }
                                }
                            });

                            applicationGroup.addView(view, 0);
                        }
                    }
                });
    }

    private void addKeywordChip(String keyword) {
        final Chip chip = new Chip(this);
        chip.setTextAppearance(R.style.ChipTextAppearance);
        chip.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.color_primary)));
        chip.setTextColor(getResources().getColor(R.color.text_color_light));
        chip.setText(keyword);
        chip.setChipIcon(getResources().getDrawable(R.drawable.ic_icon_keyword));
        keywordChipGroup.addView(chip);
    }

    private void renderRichText(HtmlTextView htmlTextView, String text) {
        Spanned formattedHtml = HtmlFormatter.formatHtml(new HtmlFormatterBuilder().setHtml(text).setImageGetter(new HtmlResImageGetter(htmlTextView.getContext())));
        htmlTextView.setText(formattedHtml);
    }
}