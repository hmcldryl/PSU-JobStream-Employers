package com.jobstream.employer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.jobstream.employer.system.models.Post;
import com.jobstream.employer.system.modules.StuffFormatter;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.sufficientlysecure.htmltextview.HtmlFormatter;
import org.sufficientlysecure.htmltextview.HtmlFormatterBuilder;
import org.sufficientlysecure.htmltextview.HtmlResImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;
import jp.wasabeef.richeditor.RichEditor;

public class NewJobPostActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;

    ViewGroup inputDescriptionGroup;
    MaterialToolbar toolbar;
    ChipGroup keywordChipGroup;
    TextInputLayout inputKeywordLayout;
    TextInputEditText inputTitle,
            inputKeyword,
            inputLocation,
            inputSalary;
    MaterialAutoCompleteTextView inputType;
    RichEditor richEditor;
    ImageView inputImage;
    FloatingActionButton btnUploadPost;

    List<String> keywordList;
    Uri imageUri;

    StuffFormatter formatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_job_post);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.toolbar);
        inputDescriptionGroup = findViewById(R.id.inputDescriptionGroup);
        inputTitle = findViewById(R.id.inputTitle);
        inputKeywordLayout = findViewById(R.id.inputKeywordLayout);
        inputKeyword = findViewById(R.id.inputKeyword);
        inputLocation = findViewById(R.id.inputLocation);
        inputSalary = findViewById(R.id.inputSalary);
        inputType = findViewById(R.id.inputType);
        richEditor = findViewById(R.id.richEditor);
        keywordChipGroup = findViewById(R.id.keywordChipGroup);
        inputImage = findViewById(R.id.inputImage);
        btnUploadPost = findViewById(R.id.btnUploadPost);

        formatter = new StuffFormatter();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setUpTypeDropdownList();

        inputDescriptionGroup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    inputDescriptionGroup.getParent().requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });

        inputKeywordLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String keywordString = inputKeyword.getText().toString().trim();
                if (!keywordString.isEmpty()) {
                    List<String> keywordStringList = splitString(keywordString);
                    if (!keywordStringList.isEmpty()) {
                        if (keywordList.size() < 10) {
                            if (keywordStringList.size() < 10) {
                                for (int i = 0; i < keywordStringList.size(); i++) {
                                    if (!keywordList.contains(keywordStringList.get(i))) {
                                        keywordList.add(keywordStringList.get(i));
                                        addKeywordChip(keywordStringList.get(i));
                                    }
                                }
                            } else {
                                inputKeyword.setError("You have already added the maximum number of keywords.");
                            }
                        } else {
                            inputKeyword.setError("You have already added the maximum number of keywords.");
                        }
                        inputKeyword.getText().clear();
                    } else {
                        inputKeyword.setError("Please enter a keyword.");
                    }
                } else {
                    inputKeyword.setError("Please enter a keyword.");
                }
            }
        });

        inputImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(4, 3)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(NewJobPostActivity.this);
            }
        });

        btnUploadPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputCheck();
            }
        });

        setupRichTextEditor();
    }

    private void setupRichTextEditor() {
        richEditor.setEditorHeight(250);
        richEditor.setEditorFontSize(14);
        richEditor.setEditorFontColor(getResources().getColor(R.color.textColor));
        richEditor.setPadding(8, 8, 8, 8);
        richEditor.setPlaceholder(getString(R.string.input_hint_description));

        findViewById(R.id.btnUndo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                richEditor.undo();
            }
        });

        findViewById(R.id.btnRedo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                richEditor.redo();
            }
        });

        findViewById(R.id.btnBold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                richEditor.setBold();
            }
        });

        findViewById(R.id.btnItalic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                richEditor.setItalic();
            }
        });

        findViewById(R.id.btnUnderline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                richEditor.setUnderline();
            }
        });

        findViewById(R.id.btnStrikethrough).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                richEditor.setStrikeThrough();
            }
        });

        findViewById(R.id.btnHeader).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.popup_window_layout_input_header, null);
                PopupWindow popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, true);

                view.findViewById(R.id.btnHeader1).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        richEditor.setHeading(1);
                    }
                });
                view.findViewById(R.id.btnHeader2).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        richEditor.setHeading(2);
                    }
                });
                view.findViewById(R.id.btnHeader3).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        richEditor.setHeading(3);
                    }
                });
                view.findViewById(R.id.btnHeader4).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        richEditor.setHeading(4);
                    }
                });
                view.findViewById(R.id.btnHeader5).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        richEditor.setHeading(5);
                    }
                });
                view.findViewById(R.id.btnHeader6).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        richEditor.setHeading(6);
                    }
                });

                popupWindow.showAsDropDown(v, 0, 0);
            }
        });

        findViewById(R.id.btnAlignLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                richEditor.setAlignLeft();
            }
        });

        findViewById(R.id.btnAlignCenter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                richEditor.setAlignCenter();
            }
        });

        findViewById(R.id.btnAlignRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                richEditor.setAlignRight();
            }
        });

        findViewById(R.id.btnBlockquote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                richEditor.setBlockquote();
            }
        });

        findViewById(R.id.btnInsertBullet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                richEditor.setBullets();
            }
        });

        findViewById(R.id.btnInsertNumber).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                richEditor.setNumbers();
            }
        });

        findViewById(R.id.btnInsertLink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog alertDialog = new AlertDialog.Builder(NewJobPostActivity.this).create();
                if (!alertDialog.isShowing()) {
                    final View dialogView = LayoutInflater.from(NewJobPostActivity.this).inflate(R.layout.dialog_layout_insert_link, null);
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertDialog.setCancelable(false);
                    alertDialog.setView(dialogView);

                    TextInputEditText inputLinkTitle = dialogView.findViewById(R.id.inputLinkTitle);
                    TextInputEditText inputLink = dialogView.findViewById(R.id.inputLink);
                    MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
                    MaterialButton btnOk = dialogView.findViewById(R.id.btnOk);

                    btnOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (inputLinkTitle.getText().toString().isEmpty()) {
                                inputLinkTitle.setError("Please enter a title.");
                            } else if (inputLink.getText().toString().isEmpty()) {
                                inputLink.setError("Please enter a link.");
                            } else {
                                alertDialog.dismiss();
                                richEditor.insertLink(inputLink.getText().toString(), inputLinkTitle.getText().toString());
                            }
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
        });
    }

    private void inputCheck() {
        if (inputTitle.getText().toString().isEmpty()) {
            inputTitle.setError("Please enter a title.");
        } else if (richEditor.getHtml() == null) {
            Toast.makeText(this, "Please enter a description.", Toast.LENGTH_SHORT).show();
        } else if (inputLocation.getText().toString().isEmpty()) {
            inputLocation.setError("Please enter a location.");
        } else if (inputSalary.getText().toString().isEmpty()) {
            inputSalary.setError("Please enter a salary or set to 0.");
        } else if (keywordList == null) {
            inputKeyword.setError("Please enter at least five (5) keywords.");
            Toast.makeText(this, "Please enter at least five (5) keywords.", Toast.LENGTH_SHORT).show();
        } else if (keywordList.isEmpty() || keywordList.size() < 5) {
            inputKeyword.setError("Please enter at least five (5) keywords.");
            Toast.makeText(this, "Please enter at least five (5) keywords.", Toast.LENGTH_SHORT).show();
        } else {
            reviewPost();
        }
    }

    private void reviewPost() {
        final AlertDialog alertDialog = new AlertDialog.Builder(NewJobPostActivity.this).create();
        if (!alertDialog.isShowing()) {
            final View dialogView = LayoutInflater.from(NewJobPostActivity.this).inflate(R.layout.dialog_layout_confirm_job_post, null);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.setCancelable(false);
            alertDialog.setView(dialogView);

            ConstraintLayout imageContainer = dialogView.findViewById(R.id.imageContainer);
            ImageView postImage = dialogView.findViewById(R.id.postImage);
            HtmlTextView postDescription = dialogView.findViewById(R.id.postDescription);
            MaterialTextView postTitle = dialogView.findViewById(R.id.postTitle);
            MaterialTextView postEmployerName = dialogView.findViewById(R.id.postEmployerName);
            MaterialTextView postLocation = dialogView.findViewById(R.id.postLocation);
            MaterialTextView postType = dialogView.findViewById(R.id.postType);
            MaterialTextView postSalary = dialogView.findViewById(R.id.postSalary);
            ChipGroup keywordChipGroup = dialogView.findViewById(R.id.keywordChipGroup);
            MaterialButton btnOk = dialogView.findViewById(R.id.btnOk);
            MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);

            db.collection("employers")
                    .document(auth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                postEmployerName.setText(task.getResult().getString("businessName"));
                            }
                        }
                    });

            if (imageUri != null) {
                Glide.with(this)
                        .load(imageUri)
                        .into(postImage);
            } else {
                imageContainer.setVisibility(View.GONE);
            }

            if (keywordList != null) {
                if (keywordList.size() > 0) {
                    for (int i = 0; i < keywordList.size(); i++) {
                        addKeywordChip(keywordList.get(i), keywordChipGroup);
                    }
                }
            }

            postTitle.setText(inputTitle.getText().toString());
            renderRichText(postDescription, richEditor.getHtml());
            postLocation.setText(inputLocation.getText().toString());
            postType.setText(inputType.getText().toString());
            postSalary.setText(formatter.formatSalary(Double.parseDouble(inputSalary.getText().toString())));

            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addNewPost();
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

    private Post post() {
        return new Post(db.collection("employers").document(auth.getCurrentUser().getUid()),
                inputTitle.getText().toString(),
                richEditor.getHtml(),
                inputLocation.getText().toString(),
                Double.parseDouble(inputSalary.getText().toString()),
                inputType.getText().toString(),
                "ongoing",
                generateTimestamp(),
                keywordList);
    }

    private Post post(String imageUrl) {
        return new Post(db.collection("employers").document(auth.getCurrentUser().getUid()),
                inputTitle.getText().toString(),
                richEditor.getHtml(),
                inputLocation.getText().toString(),
                Double.parseDouble(inputSalary.getText().toString()),
                imageUrl,
                inputType.getText().toString(),
                "ongoing",
                generateTimestamp(),
                keywordList);
    }

    private void addNewPost() {
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
                    .child("posts")
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
                                                    db.collection("posts")
                                                            .add(post(task.getResult().toString()))
                                                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                    if (task.isSuccessful()) {
                                                                        dialog.dismiss();
                                                                        finish();
                                                                        Toast.makeText(NewJobPostActivity.this, "Success", Toast.LENGTH_SHORT).show();
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
            db.collection("posts")
                    .add(post())
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                dialog.dismiss();
                                finish();
                                Toast.makeText(NewJobPostActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private List<String> splitString(String s) {
        String[] words = s.split(",");
        return Arrays.asList(words);
    }

    private Timestamp generateTimestamp() {
        return Timestamp.now();
    }

    private String generateFileTimestamp() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy MM dd_HH mm ss", Locale.ENGLISH);
        String formatted = format.format(Calendar.getInstance().getTime());
        return formatted.replaceAll("\\s", "");
    }

    private void setUpTypeDropdownList() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.type)));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, list);
        inputType.setAdapter(adapter);
    }

    private void addKeywordChip(String keyword) {
        final Chip chip = new Chip(this);
        chip.setTextAppearance(R.style.ChipTextAppearance);
        chip.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        chip.setTextColor(getResources().getColor(R.color.textColorLight));
        chip.setCloseIconVisible(true);
        chip.setCloseIconTintResource(R.color.white);
        chip.setText(keyword);
        chip.setChipIcon(getResources().getDrawable(R.drawable.ic_icon_keyword));
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keywordList.remove(keyword);
                keywordChipGroup.removeView(chip);
            }
        });
        keywordChipGroup.addView(chip);
    }


    private void addKeywordChip(String keyword, ChipGroup keywordChipGroup) {
        final Chip chip = new Chip(this);
        chip.setTextAppearance(R.style.ChipTextAppearance);
        chip.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        chip.setTextColor(getResources().getColor(R.color.textColorLight));
        chip.setText(keyword);
        chip.setChipIcon(getResources().getDrawable(R.drawable.ic_icon_keyword));
        keywordChipGroup.addView(chip);
    }

    private void renderRichText(HtmlTextView htmlTextView, String text) {
        Spanned formattedHtml = HtmlFormatter.formatHtml(new HtmlFormatterBuilder().setHtml(text).setImageGetter(new HtmlResImageGetter(htmlTextView.getContext())));
        htmlTextView.setText(formattedHtml);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                Glide.with(this)
                        .load(imageUri)
                        .into(inputImage);
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, result.getError().toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}