package com.jobstream.employer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.jobstream.employer.R;
import com.jobstream.employer.system.adapters.AdapterConversationListRV;
import com.jobstream.employer.system.models.Conversation;

public class MessagesFragment extends Fragment {

    FirebaseAuth auth;
    FirebaseFirestore db;

    MaterialCardView messagePrompt;
    RecyclerView messagesList;

    Context context;

    AdapterConversationListRV adapterConversationListRV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        messagePrompt = view.findViewById(R.id.messagePrompt);
        messagesList = view.findViewById(R.id.messagesList);

        Query query = db.collection("conversations")
                .whereArrayContains("participant", db.collection("employers").document(auth.getCurrentUser().getUid()))
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Conversation> options = new FirestoreRecyclerOptions.Builder<Conversation>()
                .setQuery(query, Conversation.class)
                .build();

        adapterConversationListRV = new AdapterConversationListRV(options, context, auth.getCurrentUser().getUid());
        LinearLayoutManager manager = new LinearLayoutManager(context);
        DividerItemDecoration decoration = new DividerItemDecoration(context, manager.getOrientation());

        messagesList.setHasFixedSize(true);
        messagesList.setLayoutManager(manager);
        messagesList.addItemDecoration(decoration);
        messagesList.setAdapter(adapterConversationListRV);

        db.collection("conversations")
                .whereArrayContains("participant", db.collection("employers").document(auth.getCurrentUser().getUid()))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null) {
                            if (value.size() > 0) {
                                messagePrompt.setVisibility(View.GONE);
                                messagesList.setVisibility(View.VISIBLE);
                            } else {
                                messagesList.setVisibility(View.GONE);
                                messagePrompt.setVisibility(View.VISIBLE);
                            }
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

    @Override
    public void onStart() {
        super.onStart();
        adapterConversationListRV.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapterConversationListRV.stopListening();
    }
}