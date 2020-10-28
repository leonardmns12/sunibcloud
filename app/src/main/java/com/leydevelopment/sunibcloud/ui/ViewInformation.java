package com.leydevelopment.sunibcloud.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.leydevelopment.sunibcloud.R;
import com.leydevelopment.sunibcloud.models.Info;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Calendar;
import java.util.Date;

public class ViewInformation extends AppCompatActivity {

    private TextView  name , position, timestamp, description;
    private PrettyTime pt = new PrettyTime();
    private String getDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_information);
        Info info = (Info) getIntent().getSerializableExtra("infoData");
        name        = findViewById(R.id.name);
        position    = findViewById(R.id.position);
        timestamp   = findViewById(R.id.timestamp);
        description = findViewById(R.id.description);
        name.setText(info.getName());
        position.setText(info.getPosition());
        getDesc = info.getDescription().replace("\\n" , System.getProperty("line.separator"));
        description.setText(getDesc);
        long currTime = Long.parseLong(info.getTime()) * 1000;
        Date d = new Date(currTime);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        timestamp.setText(pt.format(c));

        getSupportActionBar().setTitle(info.getTitle());


    }
}