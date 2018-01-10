package com.engineering.allgeier.mycetesterapp.phone;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Date;

/*
 * @author Dominik Kupka
 ***********************************************************
 ***********************************************************
 ************** MyCE CE-Device Automatization **************
 ******************** VERSION 1.0.1 ************************
 ***************** Class c_CallReceiver ********************
 ***********************************************************
 * Programmed by Dominik Kupka (Systemengineering)
 * recomli GmbH
 * Bretonischer Ring 13, 85630 Grasbrunn
 * Date: 04.12.2017
 */

public class c_CallReceiver extends c_PhonecallReceiver {

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start){
        final String s_LOG_TAG = "TelephonyAnswer";

        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);

        try {
            if (tm == null) {
                throw new NullPointerException("tm == null");
            }

           // tm.getClass().getMethod("answerRingingCall").invoke(tm);
        } catch (Exception e) {
            Log.e(s_LOG_TAG, "Unable to use the Telephony Manager directly.", e);
        }
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start){
        //
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end){
        //
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start){
        //
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end){
        //
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start){
        //
    }
}