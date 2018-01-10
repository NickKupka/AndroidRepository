package com.engineering.allgeier.mycetesterapp.network;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/*
 * @author Dominik Kupka
 ***********************************************************
 ***********************************************************
 ************** MyCE CE-Device Automatization **************
 ******************** VERSION 1.0.1 ************************
 ************** Class c_NetworkManagement ******************
 ***********************************************************
 * Programmed by Dominik Kupka (Systemengineering)
 * recomli GmbH
 * Bretonischer Ring 13, 85630 Grasbrunn
 * Date: 04.12.2017
 */
public class c_NetworkManagement extends Activity {

    /*
     This method isOnline returns a boolean and checks if the mobile device has a valid network
    connection (return: true) or not (return: false). This is needed for sms, phone and mail tests.
    If the method returns false, the tests will not be performed.
    */

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            Log.v("Internettest", "INTERNET VORHANDEN");
            return true;
        }else {
            return false;
        }
    }
}
