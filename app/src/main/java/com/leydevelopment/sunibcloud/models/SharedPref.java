package com.leydevelopment.sunibcloud.models;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Objects;

public class SharedPref {

    protected Context mContext;
    protected String key;
    public final String USED_QUOTA_KEY = "used_quota";
    public final String TOTAL_QUOTA_KEY = "total_quota";

    public SharedPref(String key , Context mContext) {
        this.key = key;
        this.mContext = mContext;
    }

    public String getQuotaUsed() {
        try{
            SharedPreferences sharedPref = Objects.requireNonNull(mContext).getSharedPreferences("MainActivity",mContext.MODE_PRIVATE);
            String value = sharedPref.getString(key, "");
            return value;
        }catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }
}

