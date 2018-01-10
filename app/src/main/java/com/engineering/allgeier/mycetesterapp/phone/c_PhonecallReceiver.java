package com.engineering.allgeier.mycetesterapp.phone;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import java.util.Date;

/*
 * @author Dominik Kupka
 ***********************************************************
 ***********************************************************
 ************** MyCE CE-Device Automatization **************
 ******************** VERSION 1.0.1 ************************
 ************** Class c_PhonecallReceiver ******************
 ***********************************************************
 * Programmed by Dominik Kupka (Systemengineering)
 * recomli GmbH
 * Bretonischer Ring 13, 85630 Grasbrunn
 * Date: 04.12.2017
 */

public abstract class c_PhonecallReceiver extends BroadcastReceiver {

    //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations

    private static int i_lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date date_callStartTime;
    private static boolean b_isIncoming;
    private static String s_savedNumber;


    @Override
    public void onReceive(Context context, Intent intent) {

        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            s_savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        }
        else{
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            int state = 0;
            if(stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                state = TelephonyManager.CALL_STATE_IDLE;
            }
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            }
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                state = TelephonyManager.CALL_STATE_RINGING;
            }


            onCallStateChanged(context, state, number);
        }
    }

    //Derived classes should override these to respond to specific events of interest
    protected abstract void onIncomingCallReceived(Context ctx, String number, Date start);
    protected abstract void onIncomingCallAnswered(Context ctx, String number, Date start);
    protected abstract void onIncomingCallEnded(Context ctx, String number, Date start, Date end);

    protected abstract void onOutgoingCallStarted(Context ctx, String number, Date start);
    protected abstract void onOutgoingCallEnded(Context ctx, String number, Date start, Date end);

    protected abstract void onMissedCall(Context ctx, String number, Date start);

    //Deals with actual events

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    public void onCallStateChanged(Context context, int state, String number) {
        if(i_lastState == state){
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                b_isIncoming = true;
                date_callStartTime = new Date();
                s_savedNumber = number;
                onIncomingCallReceived(context, number, date_callStartTime);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if(i_lastState != TelephonyManager.CALL_STATE_RINGING){
                    b_isIncoming = false;
                    date_callStartTime = new Date();
                    onOutgoingCallStarted(context, s_savedNumber, date_callStartTime);
                }
                else
                {
                    b_isIncoming = true;
                    date_callStartTime = new Date();
                    onIncomingCallAnswered(context, s_savedNumber, date_callStartTime);
                }

                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if(i_lastState == TelephonyManager.CALL_STATE_RINGING){
                    //Ring but no pickup-  a miss
                    onMissedCall(context, s_savedNumber, date_callStartTime);
                }
                else if(b_isIncoming){
                    onIncomingCallEnded(context, s_savedNumber, date_callStartTime, new Date());
                }
                else{
                    onOutgoingCallEnded(context, s_savedNumber, date_callStartTime, new Date());
                }
                break;
        }
        i_lastState = state;
    }
}