package com.jobstream.employer.system.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.vipulasri.timelineview.TimelineView;
import com.google.android.material.textview.MaterialTextView;
import com.jobstream.employer.R;

import java.util.List;
import java.util.Map;

public class ExperienceTimelineAdapter extends RecyclerView.Adapter<ExperienceTimelineAdapter.ExperienceTimelineViewHolder> {
    final List<Map<String, Object>> experienceTimelineList;

    public ExperienceTimelineAdapter(List<Map<String, Object>> experienceTimelineList) {
        this.experienceTimelineList = experienceTimelineList;
    }

    @Override
    public void onBindViewHolder(@NonNull ExperienceTimelineAdapter.ExperienceTimelineViewHolder holder, int position) {
        Map<String, Object> experienceTimeline = experienceTimelineList.get(position);

        String time = (String) experienceTimeline.get("time");
        String description = (String) experienceTimeline.get("description");

        holder.timeTextView.setText(time);
        holder.descriptionTextView.setText(description);
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }

    @Override
    public int getItemCount() {
        return experienceTimelineList.size();
    }

    @NonNull
    @Override
    public ExperienceTimelineAdapter.ExperienceTimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.item_layout_experience_timeline, null);
        return new ExperienceTimelineViewHolder(view, viewType);
    }

    static class ExperienceTimelineViewHolder extends RecyclerView.ViewHolder {
        final TimelineView timelineView;
        final MaterialTextView timeTextView;
        final MaterialTextView descriptionTextView;

        public ExperienceTimelineViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            timelineView = itemView.findViewById(R.id.timeline);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);

            timelineView.initLine(viewType);
        }
    }
}
