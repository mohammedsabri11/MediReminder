package com.erh.erh;

import android.content.Context;
import android.content.SharedPreferences;


public class SettingPrefManager {

    private static SettingPrefManager mInstance;
    private static Context mCtx;

    // Shared preferences file name
    private static final String PREF_NAME = "Setting";

    private static final String IS_FIRST_TIME = "IsFirstTime";
    private SettingPrefManager(Context context) {
        mCtx = context;

    }

    public static synchronized SettingPrefManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SettingPrefManager(context);
        }
        return mInstance;
    }




    public void setFirstTime() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_FIRST_TIME, true);

        editor.apply();
    }


    public boolean isFirstTime() {

        SharedPreferences pref = mCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(IS_FIRST_TIME, false);
    }

}


