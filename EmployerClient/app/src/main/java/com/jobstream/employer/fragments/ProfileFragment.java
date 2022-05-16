package com.jobstream.employer.fragments;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.jobstream.employer.R;
import com.jobstream.employer.system.CropImageActivity;

import java.text.DecimalFormat;
import java.util.HashMap;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    FirebaseAuth auth;
    FirebaseFirestore db;

    CircleImageView photoView;
    ImageView bannerView;
    MaterialTextView profileName,
            profileDescription,
            profileWebsite,
            profileEmail;
    FloatingActionButton btnEditProfile;

    Context context;

    ActivityResultLauncher<Intent> launcher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        photoView = view.findViewById(R.id.photoView);
        bannerView = view.findViewById(R.id.bannerView);
        profileName = view.findViewById(R.id.profileName);
        profileDescription = view.findViewById(R.id.profileDescription);
        profileWebsite = view.findViewById(R.id.profileWebsite);
        profileEmail = view.findViewById(R.id.profileEmail);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            if (result.getData().getStringExtra("type").equals("photo")) {
                                Glide.with(context)
                                        .load(result.getData().getStringExtra("photoUrl"))
                                        .into(photoView);
                            } else {
                                Glide.with(context)
                                        .load(result.getData().getStringExtra("bannerUrl"))
                                        .into(bannerView);
                            }

                        }
                    }
                });

        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launcher.launch(new Intent(getActivity(), CropImageActivity.class)
                        .putExtra("x", 1)
                        .putExtra("y", 1)
                        .putExtra("type", "photo"));
            }
        });

        bannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launcher.launch(new Intent(getActivity(), CropImageActivity.class)
                        .putExtra("x", 3)
                        .putExtra("y", 1)
                        .putExtra("type", "banner"));
            }
        });

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                if (!alertDialog.isShowing()) {
                    final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_layout_edit_profile, null);
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertDialog.setCancelable(false);
                    alertDialog.setView(dialogView);

                    TextInputEditText inputBusinessName = dialogView.findViewById(R.id.inputBusinessName);
                    TextInputEditText inputBusinessEmail = dialogView.findViewById(R.id.inputBusinessEmail);
                    TextInputEditText inputBusinessWebsite = dialogView.findViewById(R.id.inputBusinessWebsite);
                    TextInputEditText inputBusinessDescription = dialogView.findViewById(R.id.inputBusinessDescription);
                    MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
                    MaterialButton btnOk = dialogView.findViewById(R.id.btnOk);

                    btnOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (inputBusinessName.getText().toString().isEmpty() && inputBusinessEmail.getText().toString().isEmpty() && inputBusinessWebsite.getText().toString().isEmpty() && inputBusinessDescription.getText().toString().isEmpty()) {
                                inputBusinessName.setError("Please enter a business name.");
                                inputBusinessEmail.setError("Please enter an email.");
                                inputBusinessWebsite.setError("Please enter a website.");
                                inputBusinessDescription.setError("Please enter a description.");
                            } else {
                                updateProfileInfo(alertDialog, inputBusinessName.getText().toString(), inputBusinessEmail.getText().toString(), inputBusinessWebsite.getText().toString(), inputBusinessDescription.getText().toString());
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

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private HashMap<String, Object> businessInfo(String businessName, String businessEmail, String businessWebsite, String businessDescription) {
        HashMap<String, Object> map = new HashMap<>();
        if (businessName != null && !businessName.isEmpty()) {
            map.put("businessName", businessName);
        }
        if (businessEmail != null && !businessEmail.isEmpty()) {
            map.put("businessEmail", businessEmail);
        }
        if (businessWebsite != null && !businessWebsite.isEmpty()) {
            map.put("businessWebsite", businessWebsite);
        }
        if (businessDescription != null && !businessDescription.isEmpty()) {
            map.put("businessDescription", businessDescription);
        }
        return map;
    }

    private void updateProfileInfo(AlertDialog alertDialog, String businessName, String businessEmail, String businessWebsite, String businessDescription) {
        final ACProgressFlower dialog = new ACProgressFlower.Builder(context)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .fadeColor(Color.DKGRAY)
                .build();
        dialog.show();

        db.collection("employers")
                .document(auth.getCurrentUser().getUid())
                .update(businessInfo(businessName, businessEmail, businessWebsite, businessDescription))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            alertDialog.dismiss();
                            Toast.makeText(context, "Profile updated.", Toast.LENGTH_SHORT).show();
                        } else {
                            dialog.dismiss();
                            alertDialog.dismiss();
                            Toast.makeText(context, "Profile update failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private String formatFollow(Number count) {
        char[] suffix = {' ', 'k', 'M'};
        long numValue = count.longValue();
        int value = (int) Math.floor(Math.log10(numValue));
        int base = value / 3;
        if (value >= 3 && base < suffix.length) {
            return new DecimalFormat("#0.00").format(numValue / Math.pow(10, base * 3)) + suffix[base];
        } else {
            return new DecimalFormat().format(numValue);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            if (!auth.getCurrentUser().getUid().isEmpty()) {
                db.collection("employers")
                        .document(auth.getCurrentUser().getUid())
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                if (value != null) {
                                    if (value.exists()) {
                                        String photoUrl = value.getString("photoUrl");
                                        String bannerUrl = value.getString("bannerUrl");
                                        String businessName = value.getString("businessName");
                                        String businessDescription = value.getString("businessDescription");
                                        String businessEmail = value.getString("businessEmail");
                                        String businessWebsite = value.getString("businessWebsite");

                                        if (photoUrl != null) {
                                            if (!photoUrl.isEmpty()) {
                                                Glide.with(context)
                                                        .load(photoUrl)
                                                        .into(photoView);

                                                photoView.setOnLongClickListener(new View.OnLongClickListener() {
                                                    @Override
                                                    public boolean onLongClick(View view) {
                                                        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                                                        if (!alertDialog.isShowing()) {
                                                            final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_layout_view_image, null);
                                                            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                            alertDialog.setView(dialogView);
                                                            alertDialog.setCancelable(true);

                                                            ImageView image = dialogView.findViewById(R.id.image);

                                                            Glide.with(context)
                                                                    .load(photoUrl)
                                                                    .into(image);

                                                            alertDialog.show();
                                                        }
                                                        return true;
                                                    }
                                                });
                                            }
                                        }

                                        if (bannerUrl != null) {
                                            if (!bannerUrl.isEmpty()) {
                                                Glide.with(context)
                                                        .load(bannerUrl)
                                                        .into(bannerView);

                                                bannerView.setOnLongClickListener(new View.OnLongClickListener() {
                                                    @Override
                                                    public boolean onLongClick(View view) {
                                                        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                                                        if (!alertDialog.isShowing()) {
                                                            final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_layout_view_image, null);
                                                            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                            alertDialog.setView(dialogView);
                                                            alertDialog.setCancelable(true);

                                                            ImageView image = dialogView.findViewById(R.id.image);

                                                            Glide.with(context)
                                                                    .load(bannerUrl)
                                                                    .into(image);

                                                            alertDialog.show();
                                                        }
                                                        return true;
                                                    }
                                                });
                                            }
                                        }

                                        profileName.setText(businessName);
                                        profileDescription.setText(businessDescription);
                                        profileEmail.setText(businessEmail);

                                        if (businessEmail != null) {
                                            profileEmail.setText(businessEmail);
                                        } else {
                                            profileEmail.setVisibility(View.GONE);
                                        }

                                        if (businessWebsite != null) {
                                            profileWebsite.setText(businessWebsite);
                                        } else {
                                            profileWebsite.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }
                        });
            }
        }
    }
}