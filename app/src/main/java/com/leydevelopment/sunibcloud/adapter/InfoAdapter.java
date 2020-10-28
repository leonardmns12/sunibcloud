package com.leydevelopment.sunibcloud.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.leydevelopment.sunibcloud.R;
import com.leydevelopment.sunibcloud.models.Info;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Calendar;
import java.util.Date;

public class InfoAdapter extends FirestoreRecyclerAdapter<Info, InfoAdapter.InfoHolder> {

    private PrettyTime pt = new PrettyTime();
    private OnInfoListener mOnInfoListener;

    public InfoAdapter(@NonNull FirestoreRecyclerOptions<Info> options , OnInfoListener onInfoListener) {
        super(options);
        this.mOnInfoListener = onInfoListener;
    }

    @Override
    protected void onBindViewHolder(@NonNull InfoHolder holder, int position, @NonNull Info model) {
        holder.title.setText(model.getTitle());
        if(model.getDescription().length() > 12){
            holder.description.setText(model.getDescription().substring(0,12) + " . . .");
        } else {
            holder.description.setText(model.getDescription());
        }
        String time = model.getTime();
        long timestampLong = Long.parseLong(time)*1000;
        Date d = new Date(timestampLong);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        holder.time.setText(pt.format(c));
    }

    @NonNull
    @Override
    public InfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.information_list_layout, parent ,false);
        return new InfoHolder(v,mOnInfoListener);
    }

    class InfoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        TextView description;
        TextView time;
        OnInfoListener onInfoListener;

        public InfoHolder(@NonNull View itemView , OnInfoListener onInfoListener) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            time = itemView.findViewById(R.id.time);
            this.onInfoListener = onInfoListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onInfoListener.OnInfoClick(getAdapterPosition());
        }
    }

    public interface OnInfoListener {
        void OnInfoClick(int position);
    }
}