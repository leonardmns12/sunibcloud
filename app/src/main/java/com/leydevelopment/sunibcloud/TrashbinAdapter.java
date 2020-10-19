package com.leydevelopment.sunibcloud;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.owncloud.android.lib.resources.trashbin.model.TrashbinFile;

import java.util.List;

public class TrashbinAdapter extends RecyclerView.Adapter<TrashbinAdapter.MyViewHolder> {
    List<TrashbinFile> trashbinFile;
    Context context;

    public TrashbinAdapter(List<TrashbinFile> trashbinFile, Context ct) {
        this.trashbinFile = trashbinFile;
        this.context = ct;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.trashbin_list_layout , parent , false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrashbinAdapter.MyViewHolder holder, int position) {
        holder.trashbinPath.setText(trashbinFile.get(position).getFileName());
        if (trashbinFile.get(position).getFileName().endsWith(".pdf")) {
            holder.trashbinIcon.setImageResource(R.drawable.ic_baseline_picture_as_pdf_24);
        } else if (trashbinFile.get(position).getFileName().endsWith(".apk")) {
            holder.trashbinIcon.setImageResource(R.drawable.ic_baseline_android_24);
        } else if (trashbinFile.get(position).getFileName().endsWith(".mp4")) {
            holder.trashbinIcon.setImageResource(R.drawable.ic_baseline_movie_creation_24);
        } else if (trashbinFile.get(position).getFileName().endsWith(".docx")) {
            holder.trashbinIcon.setImageResource(R.drawable.ic_baseline_assignment_24);
        } else if (trashbinFile.get(position).getFileName().endsWith(".png") ||
                trashbinFile.get(position).getFileName().endsWith(".jpg") ||
                trashbinFile.get(position).getFileName().endsWith(".jpeg")) {
            holder.trashbinIcon.setImageResource(R.drawable.image);
        } else {
            holder.trashbinIcon.setImageResource(R.drawable.ic_baseline_folder_24);
        }
    }

    @Override
    public int getItemCount() {
        return trashbinFile.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView trashbinIcon;
        TextView trashbinPath;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            trashbinIcon = itemView.findViewById(R.id.trashbinIcon);
            trashbinPath = itemView.findViewById(R.id.trashbinPath);
        }
    }
}
