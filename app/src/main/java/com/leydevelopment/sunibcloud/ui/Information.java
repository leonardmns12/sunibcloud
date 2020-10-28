package com.leydevelopment.sunibcloud.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.leydevelopment.sunibcloud.MainActivity;
import com.leydevelopment.sunibcloud.R;
import com.leydevelopment.sunibcloud.adapter.InfoAdapter;
import com.leydevelopment.sunibcloud.models.Info;

public class Information extends AppCompatActivity implements InfoAdapter.OnInfoListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference infoRef = db.collection("Information");

    private InfoAdapter adapter;
    private Context mContext;

    @Override
    public void onBackPressed() {
       Intent intent = new Intent(this , MainActivity.class);
       startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        getSupportActionBar().hide();
        mContext = this;
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        Query query = infoRef.orderBy("time", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Info> options = new FirestoreRecyclerOptions.Builder<Info>()
                .setQuery(query, Info.class)
                .build();

        adapter = new InfoAdapter(options,this);


        RecyclerView recyclerView = findViewById(R.id.recyclerinfo);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void OnInfoClick(int position) {
        Info info = adapter.getItem(position);
        Intent intent = new Intent(Information.this , ViewInformation.class);
        intent.putExtra("infoData" , info);
        startActivity(intent);
    }
}