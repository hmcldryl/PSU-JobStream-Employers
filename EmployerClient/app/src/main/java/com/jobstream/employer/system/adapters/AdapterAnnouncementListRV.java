package com.jobstream.employer.system.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
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
import com.jobstream.employer.R;
import com.jobstream.employer.system.models.Announcement;

import org.ocpsoft.prettytime.PrettyTime;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterAnnouncementListRV extends FirestoreRecyclerAdapter<Announcement, AdapterAnnouncementListRV.AnnouncementHolder> {

    final Context context;
    FirebaseFirestore db;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */

    public AdapterAnnouncementListRV(@NonNull FirestoreRecyclerOptions<Announcement> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull AnnouncementHolder holder, int position, @NonNull Announcement model) {
        db = FirebaseFirestore.getInstance();

        model.getUser().get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            holder.businessName.setText(task.getResult().getString("businessName"));
                        }
                    }
                });

        if (model.getImageUrl() != null) {
            if (!model.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(model.getImageUrl())
                        .into(holder.announcementImage);
            }
        } else {
            holder.announcementImageContainer.setVisibility(View.GONE);
        }

        holder.announcementTitle.setText(model.getTitle());
        holder.announcementDescription.setText(model.getDescription());
        holder.announcementTimestamp.setText(new PrettyTime().format(model.getTimestamp().toDate()));

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(context, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.popup_menu_announcement, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.btnDelete) {
                            getSnapshots().getSnapshot(holder.getAdapterPosition())
                                    .getReference()
                                    .delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });
    }

    @NonNull
    @Override
    public AnnouncementHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout_announcement, parent, false);
        return new AnnouncementHolder(view);
    }

    static class AnnouncementHolder extends RecyclerView.ViewHolder {
        final MaterialCardView item;
        final ConstraintLayout announcementImageContainer;
        final CircleImageView businessPhoto;
        final ImageView announcementImage;
        final MaterialTextView businessName;
        final MaterialTextView announcementTitle;
        final MaterialTextView announcementDescription;
        final MaterialTextView announcementTimestamp;

        public AnnouncementHolder(View view) {
            super(view);
            item = view.findViewById(R.id.item);
            businessPhoto = view.findViewById(R.id.businessPhoto);
            businessName = view.findViewById(R.id.businessName);
            announcementImageContainer = view.findViewById(R.id.announcementImageContainer);
            announcementImage = view.findViewById(R.id.announcementImage);
            announcementTitle = view.findViewById(R.id.announcementTitle);
            announcementDescription = view.findViewById(R.id.announcementDescription);
            announcementTimestamp = view.findViewById(R.id.announcementTimestamp);
        }
    }
}
