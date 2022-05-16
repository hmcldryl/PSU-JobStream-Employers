package com.jobstream.employer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.jobstream.employer.R;
import com.jobstream.employer.system.modules.StuffFormatter;

public class DashboardFragment extends Fragment {

    FirebaseAuth auth;
    FirebaseFirestore db;

    MaterialTextView textTotalPostCount,
            textTotalActivePostCount,
            textTotalArchivedPostCount;

    Context context;

    StuffFormatter formatter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        textTotalPostCount = view.findViewById(R.id.textTotalPostCount);
        textTotalActivePostCount = view.findViewById(R.id.textTotalActivePostCount);
        textTotalArchivedPostCount = view.findViewById(R.id.textTotalArchivedPostCount);

        formatter = new StuffFormatter();

        updateUi();

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void updateUi() {
        db.collection("posts")
                .whereEqualTo("employer", db.collection("employers").document(auth.getCurrentUser().getUid()))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        int totalPostCount;
                        int totalActivePostCount = 0;
                        int totalArchivedPostCount = 0;


                        if (auth.getCurrentUser() != null) {
                            if (value != null) {
                                if (value.getDocuments().size() > 0) {
                                    totalPostCount = value.getDocuments().size();
                                    textTotalPostCount.setText(formatter.formatNumber(totalPostCount));

                                    for (int i = 0; i < value.getDocuments().size(); i++) {
                                        if (value.getDocuments().get(i).getString("status").equals("ongoing")) {
                                            totalActivePostCount++;
                                        }
                                        if (value.getDocuments().get(i).getString("status").equals("archived") || value.getDocuments().get(i).getString("status").equals("done")) {
                                            totalArchivedPostCount++;
                                        }
                                    }

                                    textTotalActivePostCount.setText(formatter.formatNumber(totalActivePostCount));
                                    textTotalArchivedPostCount.setText(formatter.formatNumber(totalArchivedPostCount));
                                }
                            }
                        }
                    }
                });
    }
}