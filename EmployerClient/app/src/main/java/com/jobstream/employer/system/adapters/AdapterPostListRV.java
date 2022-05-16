package com.jobstream.employer.system.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobstream.employer.JobPostActivity;
import com.jobstream.employer.R;
import com.jobstream.employer.system.models.Post;
import com.jobstream.employer.system.modules.StuffFormatter;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;

public class AdapterPostListRV extends FirestoreRecyclerAdapter<Post, AdapterPostListRV.PostHolder> {

    final Context context;
    FirebaseFirestore db;

    StuffFormatter formatter;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */

    public AdapterPostListRV(@NonNull FirestoreRecyclerOptions<Post> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull PostHolder holder, int position, @NonNull Post model) {
        db = FirebaseFirestore.getInstance();

        formatter = new StuffFormatter();

        model.getEmployer().get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            holder.postEmployerName.setText(task.getResult().getString("businessName"));
                        }
                    }
                });

        if (model.getImageUrl() != null) {
            Glide.with(context)
                    .load(model.getImageUrl())
                    .into(holder.postImage);
        } else {
            holder.imageContainer.setVisibility(View.GONE);
        }

        if (model.getSalary() > 0) {
            holder.postSalary.setText(formatter.formatSalary(model.getSalary()));
        } else {
            holder.postSalary.setVisibility(View.GONE);
        }

        holder.postTitle.setText(model.getTitle());
        holder.postJobLocation.setText(model.getLocation());
        holder.postType.setText(model.getType());
        holder.postTimestamp.setText(formatter.formatTimestamp(model.getTimestamp()));

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, JobPostActivity.class)
                        .putExtra("employerId", model.getEmployer().getId())
                        .putExtra("postId", getSnapshots().getSnapshot(holder.getAdapterPosition()).getReference().getId())
                        .putExtra("imageUrl", model.getImageUrl())
                        .putExtra("title", model.getTitle())
                        .putExtra("description", model.getDescription())
                        .putExtra("location", model.getLocation())
                        .putExtra("type", model.getType())
                        .putExtra("salary", String.valueOf(model.getSalary()))
                        .putStringArrayListExtra("keyword", (ArrayList<String>) model.getKeyword())
                        .putExtra("timestamp", new PrettyTime().format(model.getTimestamp().toDate())));
            }
        });
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout_post, parent, false);
        return new PostHolder(view);
    }

    static class PostHolder extends RecyclerView.ViewHolder {
        final MaterialCardView item;
        final ConstraintLayout imageContainer;
        final ImageView postImage;
        final MaterialTextView postTitle;
        final MaterialTextView postEmployerName;
        final MaterialTextView postJobLocation;
        final MaterialTextView postType;
        final MaterialTextView postSalary;
        final MaterialTextView postTimestamp;

        public PostHolder(View view) {
            super(view);
            item = view.findViewById(R.id.item);
            imageContainer = view.findViewById(R.id.imageContainer);
            postImage = view.findViewById(R.id.postImage);
            postTitle = view.findViewById(R.id.postTitle);
            postEmployerName = view.findViewById(R.id.postEmployerName);
            postJobLocation = view.findViewById(R.id.postLocation);
            postType = view.findViewById(R.id.postType);
            postSalary = view.findViewById(R.id.postSalary);
            postTimestamp = view.findViewById(R.id.postTimestamp);
        }
    }
}
