package com.engineering.allgeier.mycetesterapp.phone;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

/*
 * @author Dominik Kupka
 ***********************************************************
 ***********************************************************
 ************** MyCE CE-Device Automatization **************
 ******************** VERSION 1.0.1 ************************
 ************** Class c_PhoneStateReceiver *****************
 ***********************************************************
 * Programmed by Dominik Kupka (Systemengineering)
 * recomli GmbH
 * Bretonischer Ring 13, 85630 Grasbrunn
 * Date: 04.12.2017
 */

public class c_PhoneStateReceiver extends BroadcastReceiver {

    public static String s_TAG="c_PhoneStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            Log.d(s_TAG,"c_PhoneStateReceiver**Call State=" + state);

            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                Log.d(s_TAG,"c_PhoneStateReceiver**Idle");
            } else if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                // Incoming call
                String incomingNumber =
                        intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                Log.d(s_TAG,"c_PhoneStateReceiver**Incoming call " + incomingNumber);

                if (!killCall(context)) { // Using the method defined earlier
                    Log.d(s_TAG,"c_PhoneStateReceiver **Unable to kill incoming call");
                }

            } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                Log.d(s_TAG,"c_PhoneStateReceiver **Offhook");
            }
        } else if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            // Outgoing call
            String outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d(s_TAG,"c_PhoneStateReceiver **Outgoing call " + outgoingNumber);

            setResultData(null); // Kills the outgoing call

        } else {
            Log.d(s_TAG,"c_PhoneStateReceiver **unexpected intent.action=" + intent.getAction());
        }
    }

    public static boolean killCall(Context context) {
        try {
            // Get the boring old TelephonyManager
            TelephonyManager telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            // Get the getITelephony() method
            Class classTelephony = Class.forName(telephonyManager.getClass().getName());
            Method methodGetITelephony = classTelephony.getDeclaredMethod("getITelephony");

            // Ignore that the method is supposed to be private
            methodGetITelephony.setAccessible(true);

            // Invoke getITelephony() to get the ITelephony interface
            Object telephonyInterface = methodGetITelephony.invoke(telephonyManager);

            // Get the endCall method from ITelephony
            Class telephonyInterfaceClass =
                    Class.forName(telephonyInterface.getClass().getName());
            Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod("endCall");

            // Invoke endCall()
            methodEndCall.invoke(telephonyInterface);

        } catch (Exception ex) { // Many things can go wrong with reflection calls
            Log.d(s_TAG,"c_PhoneStateReceiver **" + ex.toString());
            return false;
        }
        return true;
    }
}