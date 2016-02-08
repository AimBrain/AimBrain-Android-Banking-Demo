package com.aimbrain.bankingdemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.aimbrain.sdk.AMBNApplication.AMBNApplication;
import com.aimbrain.sdk.Manager;
import com.aimbrain.bankingdemo.applicationState.ApplicationState;
import com.aimbrain.bankingdemo.helpers.Constants;
import com.crashlytics.android.Crashlytics;

import java.util.UUID;

import io.fabric.sdk.android.Fabric;


public class MyApplication extends AMBNApplication{

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Manager.getInstance().startCollectingData(null);
        ApplicationState.initialize(this);
        checkInterfaceUUID();
        Manager.getInstance().configure("AIMBRAIN_API_KEY", "AIMBRAIN_API_SECRET");
    }

    private void checkInterfaceUUID(){
        SharedPreferences prefs = this.getSharedPreferences(Constants.SHARED_PREFS_TAG, Context.MODE_PRIVATE);
        String hardcodedInstallIdVersion = prefs.getString(Constants.HARDCODED_INSTALL_ID_TAG, "");
        if(!hardcodedInstallIdVersion.equals(Constants.HARDCODED_INSTALL_ID_VERSION)){
            String interfaceUUID = UUID.randomUUID().toString();
            Log.i("UUID", "changed to: " + interfaceUUID);
            prefs.edit().putString(Constants.INTERFACE_UUID, interfaceUUID).apply();
            prefs.edit().putString(Constants.HARDCODED_INSTALL_ID_TAG, Constants.HARDCODED_INSTALL_ID_VERSION).apply();
        }else {
            Log.i("UUID", "still the same: " + prefs.getString(Constants.INTERFACE_UUID, null));
        }
    }
}
