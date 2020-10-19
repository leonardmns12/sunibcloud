package com.leydevelopment.sunibcloud;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.owncloud.android.lib.resources.activities.model.Activity;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

public class ActivityAdapter extends  RecyclerView.Adapter<ActivityAdapter.MyViewHolder>{

    List<Activity> activities;
    Context context;
    PrettyTime pt = new PrettyTime();
    public ActivityAdapter(Context ct , List<Activity> ac) {
        context = ct;
        activities = ac;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.activity_in_list , parent , false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.subject.setText(activities.get(position).getSubject());
        holder.time.setText(pt.format(activities.get(position).getDatetime()));

        switch (activities.get(position).getType()) {
            case "file_changed" :
                holder.activityIcon.setImageResource(R.drawable.ic_baseline_layers_24);
                break;
            case "shared" :
                holder.activityIcon.setImageResource(R.drawable.ic_baseline_insert_link_24);
                break;
            case "file_deleted" :
                holder.activityIcon.setImageResource(R.drawable.ic_baseline_remove_24);
                break;
            case "public_links" :
                holder.activityIcon.setImageResource(R.drawable.ic_baseline_save_alt_24);
                break;
            default :
                holder.activityIcon.setImageResource(R.drawable.ic_baseline_add_24);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView subject , time;
        ImageView activityIcon;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            subject = itemView.findViewById(R.id.subject);
            activityIcon = itemView.findViewById(R.id.activityIcon);
            time = itemView.findViewById(R.id.time);
        }
    }
}
