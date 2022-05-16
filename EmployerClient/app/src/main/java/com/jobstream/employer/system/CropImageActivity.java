package com.jobstream.employer.system;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.jobstream.employer.R;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class CropImageActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore firestore;

    MaterialToolbar toolbar;
    CropImageView cropImageView;
    ImageButton btnFlipVertical,
            btnFlipHorizontal,
            btnRotateLeft,
            btnRotateRight,
            btnSelect,
            btnClear,
            btnConfirm;

    ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.toolbar);

        cropImageView = findViewById(R.id.cropImageView);
        btnFlipVertical = findViewById(R.id.btnFlipVertical);
        btnFlipHorizontal = findViewById(R.id.btnFlipHorizontal);
        btnRotateLeft = findViewById(R.id.btnRotateLeft);
        btnRotateRight = findViewById(R.id.btnRotateRight);
        btnSelect = findViewById(R.id.btnSelect);
        btnClear = findViewById(R.id.btnClear);
        btnConfirm = findViewById(R.id.btnConfirm);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            cropImageView.setImageUriAsync(result.getData().getData());
                        }
                    }
                });

        cropImageView.setAspectRatio(getIntent().getIntExtra("x", 1), getIntent().getIntExtra("y", 1));
        cropImageView.setFixedAspectRatio(true);
        cropImageView.setGuidelines(CropImageView.Guidelines.ON_TOUCH);
        cropImageView.setShowProgressBar(true);
        cropImageView.setScaleType(CropImageView.ScaleType.FIT_CENTER);

        cropImageView.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
            @Override
            public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
                if (result.isSuccessful()) {
                    if (result.getUri() != null) {
                        validateImageData(result.getUri());
                    }
                } else {
                    Toast.makeText(CropImageActivity.this, "Cropping failed. " + result.getError().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnFlipVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.flipImageVertically();
            }
        });

        btnFlipHorizontal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.flipImageHorizontally();
            }
        });

        btnRotateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.rotateImage(-90);
            }
        });

        btnRotateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.rotateImage(90);
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.clearImage();
                launcher.launch(new Intent()
                        .setType("image/*")
                        .setAction(Intent.ACTION_GET_CONTENT));
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.clearImage();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filePath = new File(getFilesDir() + "/JobStream/").toURI().toString();
                cropImageView.saveCroppedImageAsync(Uri.parse(filePath));
            }
        });

        launcher.launch(new Intent()
                .setType("image/*")
                .setAction(Intent.ACTION_GET_CONTENT));
    }

    private void validateImageData(Uri imageUri) {
        if (getIntent().getStringExtra("type").equals("photo")) {
            uploadProfileDisplayPhoto(imageUri);
        } else {
            uploadBannerViewPhoto(imageUri);
        }
    }

    private void updatePhotoUrl(ACProgressFlower dialog, Uri uri) {
        String photoUrl = uri.toString();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("photoUrl", photoUrl);
        firestore.collection("employers")
                .document(auth.getCurrentUser().getUid())
                .update(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();

                            Intent resultData = new Intent();
                            resultData.putExtra("photoUrl", photoUrl);
                            resultData.putExtra("type", "photo");
                            setResult(RESULT_OK, resultData);
                            finish();

                            Toast.makeText(CropImageActivity.this, "Image upload success.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CropImageActivity.this, "Image upload failed. Please try again.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });
    }

    private void updateBannerUrl(ACProgressFlower dialog, Uri uri) {
        String bannerUrl = uri.toString();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("bannerUrl", bannerUrl);
        firestore.collection("employers")
                .document(auth.getCurrentUser().getUid())
                .update(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();

                            Intent resultData = new Intent();
                            resultData.putExtra("bannerUrl", bannerUrl);
                            resultData.putExtra("type", "banner");
                            setResult(RESULT_OK, resultData);
                            finish();

                            Toast.makeText(CropImageActivity.this, "Image upload success.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CropImageActivity.this, "Image upload failed. Please try again.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });
    }

    private void uploadProfileDisplayPhoto(Uri uri) {
        final ACProgressFlower dialog = new ACProgressFlower.Builder(CropImageActivity.this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(getResources().getColor(R.color.white))
                .text("Uploading...")
                .fadeColor(Color.DKGRAY).build();
        dialog.show();
        FirebaseStorage.getInstance().getReference()
                .child("employers")
                .child(auth.getCurrentUser().getUid())
                .child("images")
                .child("IMG_EMPLOYER_DISPLAY_PHOTO.jpg")
                .putFile(uri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();
                            result.addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        updatePhotoUrl(dialog, task.getResult());
                                    } else {
                                        Toast.makeText(CropImageActivity.this, "Image upload failed. Please try again.", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(CropImageActivity.this, "Image upload failed. Please try again.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });
    }

    private void uploadBannerViewPhoto(Uri uri) {
        final ACProgressFlower dialog = new ACProgressFlower.Builder(CropImageActivity.this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .fadeColor(Color.DKGRAY).build();
        dialog.show();

        FirebaseStorage.getInstance().getReference()
                .child("employers")
                .child(auth.getCurrentUser().getUid())
                .child("images")
                .child("IMG_EMPLOYER_BANNER_PHOTO.jpg")
                .putFile(uri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();
                            result.addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        updateBannerUrl(dialog, task.getResult());
                                    } else {
                                        Toast.makeText(CropImageActivity.this, "Image upload failed. Please try again.", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(CropImageActivity.this, "Image upload failed. Please try again.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });
    }
}