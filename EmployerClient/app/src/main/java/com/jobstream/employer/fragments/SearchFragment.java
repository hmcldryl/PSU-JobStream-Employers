package com.jobstream.employer.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.jobstream.employer.R;
import com.jobstream.employer.UserProfileActivity;
import com.jobstream.employer.system.modules.StuffFormatter;

import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchFragment extends Fragment {

    FirebaseAuth auth;
    FirebaseFirestore db;

    MaterialCardView searchPrompt;
    TextInputLayout searchBar;
    TextInputEditText searchBarText;
    LinearLayout searchUserSection;
    ViewGroup searchUserGroup;
    MaterialTextView searchResultUser;

    Context context;

    StuffFormatter formatter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        searchPrompt = view.findViewById(R.id.searchPrompt);
        searchBar = view.findViewById(R.id.searchBar);
        searchBarText = view.findViewById(R.id.searchBarText);
        searchUserSection = view.findViewById(R.id.searchUserSection);
        searchUserGroup = view.findViewById(R.id.searchUserGroup);
        searchResultUser = view.findViewById(R.id.searchResultUser);

        formatter = new StuffFormatter();

        searchUserGroup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    searchUserGroup.getParent().requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });

        searchBar.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchPrompt.setVisibility(View.GONE);
                removeItems();
                if (searchBarText.getText().toString().isEmpty()) {
                    startSearch();
                } else {
                    startSearch(splitSearchQueryString(searchBarText.getText().toString().trim()));
                }
            }
        });

        searchBarText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchBarText.getText().toString().isEmpty()) {
                    removeItems();
                    searchUserSection.setVisibility(View.GONE);
                    searchPrompt.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void startSearch() {
        db.collection("users")
                .limit(100)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().getDocuments().size() > 0) {
                                int count = task.getResult().getDocuments().size();
                                String resultCount = count <= 1 ? count + " result" : formatter.formatNumber(count) + " results";
                                searchUserSection.setVisibility(View.VISIBLE);
                                searchResultUser.setText(resultCount);
                                for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                                    List<String> keyword = (List<String>) task.getResult().getDocuments().get(i).get("keyword");
                                    loadUser(task.getResult().getDocuments().get(i).getReference(),
                                            task.getResult().getDocuments().get(i).getString("firstName"),
                                            task.getResult().getDocuments().get(i).getString("lastName"),
                                            task.getResult().getDocuments().get(i).getString("program"),
                                            task.getResult().getDocuments().get(i).getString("email"),
                                            task.getResult().getDocuments().get(i).getString("bio"),
                                            task.getResult().getDocuments().get(i).getString("status"),
                                            task.getResult().getDocuments().get(i).getString("photoUrl"),
                                            task.getResult().getDocuments().get(i).getString("bannerUrl"),
                                            task.getResult().getDocuments().get(i).getTimestamp("timestamp"),
                                            keyword);
                                }
                            } else {
                                int count = task.getResult().getDocuments().size();
                                String resultCount = count <= 1 ? formatter.formatNumber(count) + " result" : formatter.formatNumber(count) + " results";
                                searchUserSection.setVisibility(View.VISIBLE);
                                searchResultUser.setText(resultCount);
                            }
                        } else {
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void startSearch(List<String> search) {
        db.collection("users")
                .whereArrayContainsAny("keyword", search)
                .limit(100)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().getDocuments().size() > 0) {
                                int count = task.getResult().getDocuments().size();
                                String resultCount = count <= 1 ? count + " result" : formatter.formatNumber(count) + " results";
                                searchUserSection.setVisibility(View.VISIBLE);
                                searchResultUser.setText(resultCount);
                                for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                                    List<String> keyword = (List<String>) task.getResult().getDocuments().get(i).get("keyword");
                                    loadUser(task.getResult().getDocuments().get(i).getReference(),
                                            task.getResult().getDocuments().get(i).getString("firstName"),
                                            task.getResult().getDocuments().get(i).getString("lastName"),
                                            task.getResult().getDocuments().get(i).getString("program"),
                                            task.getResult().getDocuments().get(i).getString("email"),
                                            task.getResult().getDocuments().get(i).getString("bio"),
                                            task.getResult().getDocuments().get(i).getString("status"),
                                            task.getResult().getDocuments().get(i).getString("photoUrl"),
                                            task.getResult().getDocuments().get(i).getString("bannerUrl"),
                                            task.getResult().getDocuments().get(i).getTimestamp("timestamp"),
                                            keyword);
                                }
                            } else {
                                int count = task.getResult().getDocuments().size();
                                String resultCount = count <= 1 ? formatter.formatNumber(count) + " result" : formatter.formatNumber(count) + " results";
                                searchUserSection.setVisibility(View.VISIBLE);
                                searchResultUser.setText(resultCount);
                            }
                        } else {
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void loadUser(DocumentReference user, String firstName, String lastName, String program, String email, String bio, String status, String photoUrl, String bannerUrl, Timestamp timestamp, List<String> keyword) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout_user_search, searchUserGroup, false);
        MaterialCardView item = view.findViewById(R.id.item);
        CircleImageView userPhoto = view.findViewById(R.id.userPhoto);
        ImageView userBanner = view.findViewById(R.id.userBanner);
        MaterialTextView userName = view.findViewById(R.id.userName);
        MaterialTextView userEmail = view.findViewById(R.id.userEmail);
        MaterialTextView userStatus = view.findViewById(R.id.userStatus);
        MaterialTextView userProgram = view.findViewById(R.id.userProgram);
        MaterialTextView userTimestamp = view.findViewById(R.id.userTimestamp);
        ChipGroup userKeyword = view.findViewById(R.id.userKeyword);

        if (photoUrl != null) {
            if (!photoUrl.isEmpty()) {
                if (!((Activity) context).isFinishing())
                Glide.with(context)
                        .load(photoUrl)
                        .into(userPhoto);
            }
        }

        if (bannerUrl != null) {
            if (!bannerUrl.isEmpty()) {
                Glide.with(context)
                        .load(bannerUrl)
                        .into(userBanner);
            }
        }

        if (keyword != null) {
            if (!keyword.isEmpty()) {
                for (int i = 0; i < keyword.size(); i++) {
                    addKeywordChip(keyword.get(i), userKeyword);
                }
            }
        }

        userName.setText(firstName + " " + lastName);
        userName.setSelected(true);
        userEmail.setText(email);
        userStatus.setText(status);
        userStatus.setSelected(true);
        userProgram.setText(program);
        userProgram.setSelected(true);
        userTimestamp.setText("Joined " + formatter.formatTimestamp(timestamp));

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, UserProfileActivity.class)
                        .putExtra("userId", user.getId()));
            }
        });

        searchUserGroup.addView(view);
    }

    private void addKeywordChip(String keyword, ChipGroup keywordChipGroup) {
        final Chip chip = new Chip(context);
        chip.setTextAppearance(R.style.ChipTextAppearance);
        chip.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        chip.setTextColor(getResources().getColor(R.color.textColorLight));
        chip.setText(keyword);
        chip.setChipIcon(getResources().getDrawable(R.drawable.ic_icon_keyword));
        keywordChipGroup.addView(chip);
    }

    private List<String> splitSearchQueryString(String s) {
        String[] words = s.split(",");
        return Arrays.asList(words);
    }

    private void removeItems() {
        searchUserGroup.removeAllViews();
        searchUserSection.setVisibility(View.GONE);
    }
}