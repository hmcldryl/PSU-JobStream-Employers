package com.jobstream.employer.system.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.jobstream.employer.MessageActivity;
import com.jobstream.employer.R;
import com.jobstream.employer.system.models.Conversation;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterConversationListRV extends FirestoreRecyclerAdapter<Conversation, AdapterConversationListRV.ConversationHolder> {

    final Context context;
    final String uid;
    FirebaseFirestore db;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */

    public AdapterConversationListRV(@NonNull FirestoreRecyclerOptions<Conversation> options, Context context, String uid) {
        super(options);
        this.context = context;
        this.uid = uid;
    }

    @Override
    protected void onBindViewHolder(@NonNull ConversationHolder holder, int position, @NonNull Conversation model) {
        db = FirebaseFirestore.getInstance();

        List<DocumentReference> participant = model.getParticipant();
        Timestamp timestamp = model.getTimestamp();

        holder.messageTimestamp.setText(new PrettyTime().format(timestamp.toDate()));

        participant.get(1).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            String name = task.getResult().getString("firstName") + " " + task.getResult().getString("lastName");
                            String photoUrl = task.getResult().getString("photoUrl");
                            holder.userName.setText(name);
                            if (photoUrl != null) {
                                if (!photoUrl.isEmpty()) {
                                    Glide.with(context)
                                            .load(photoUrl)
                                            .into(holder.userPhoto);
                                }
                            }
                        }
                    }
                });

        getSnapshots().getSnapshot(holder.getAdapterPosition())
                .getReference()
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        int size = value.size();
                        if (size > 0) {
                            DocumentReference user = value.getDocuments().get(size - 1).getDocumentReference("user");
                            String message = value.getDocuments().get(size - 1).getString("message");
                            String type = value.getDocuments().get(size - 1).getString("type");

                            if (user != null && message != null) {
                                if (user.getId().equals(uid)) {
                                    if (type.equals("text")) {
                                        message = "You: " + message;
                                    } else {
                                        message = "You sent an image.";
                                    }
                                    holder.lastMessage.setText(message);
                                } else if (user.getId().isEmpty()) {
                                    holder.lastMessage.setVisibility(View.GONE);
                                } else {
                                    if (type.equals("image")) {
                                        message = "Image";
                                    }
                                    holder.lastMessage.setText(message);
                                }
                            } else {
                                holder.lastMessage.setVisibility(View.GONE);
                            }
                        }
                    }
                });

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = getSnapshots().getSnapshot(holder.getAdapterPosition()).getId();
                context.startActivity(new Intent(context, MessageActivity.class)
                        .putExtra("id", id)
                        .putExtra("userId", participant.get(1).getId()));
            }
        });

    }

    @NonNull
    @Override
    public ConversationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_conversation, parent, false);
        return new ConversationHolder(view);
    }

    static class ConversationHolder extends RecyclerView.ViewHolder {
        final CircleImageView userPhoto;
        final MaterialTextView userName;
        final MaterialTextView lastMessage;
        final MaterialTextView messageTimestamp;
        final LinearLayout item;
        final ImageButton btnDelete;

        public ConversationHolder(View view) {
            super(view);
            item = view.findViewById(R.id.item);
            userPhoto = view.findViewById(R.id.userPhoto);
            userName = view.findViewById(R.id.userName);
            lastMessage = view.findViewById(R.id.lastMessage);
            messageTimestamp = view.findViewById(R.id.messageTimestamp);
            btnDelete = view.findViewById(R.id.btnDelete);
        }
    }
}
