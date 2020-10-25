package com.leydevelopment.sunibcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.leydevelopment.sunibcloud.R;
import com.owncloud.android.lib.resources.trashbin.model.TrashbinFile;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

public class TrashbinAdapter extends RecyclerView.Adapter<TrashbinAdapter.MyViewHolder> {
    List<TrashbinFile> trashbinFile;
    Context context;
    private OnItemListener mOnItemListener;
    private PrettyTime pt = new PrettyTime();

    public TrashbinAdapter(List<TrashbinFile> trashbinFile, Context ct , OnItemListener onItemListener) {
        this.trashbinFile = trashbinFile;
        this.context = ct;
        this.mOnItemListener = onItemListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.trashbin_list_layout , parent , false);
        return new MyViewHolder(view , mOnItemListener);
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
        holder.time.setText(pt.format(new Date(trashbinFile.get(position).getTimestamp() * 1000)));
    }

    @Override
    public int getItemCount() {
        return trashbinFile.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView trashbinIcon;
        TextView trashbinPath , time;
        OnItemListener onItemListener;
        public MyViewHolder(@NonNull View itemView , OnItemListener onItemListener) {
            super(itemView);
            time = itemView.findViewById(R.id.time);
            trashbinIcon = itemView.findViewById(R.id.trashbinIcon);
            trashbinPath = itemView.findViewById(R.id.trashbinPath);
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemListener.OnItemClick(getAdapterPosition());
        }
    }

    public interface OnItemListener {
        void OnItemClick(int position);
    }
}
