package com.engineering.allgeier.mycetesterapp.bluetoothmyceapp;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothHealth;
import android.bluetooth.BluetoothProfile;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import com.engineering.allgeier.mycetesterapp.R;
import com.engineering.allgeier.mycetesterapp.mail.c_Mail;
import com.engineering.allgeier.mycetesterapp.mail.c_GMailSender;
import com.engineering.allgeier.mycetesterapp.mail.c_JSSEProvider;

import com.engineering.allgeier.mycetesterapp.network.c_NetworkManagement;

import com.engineering.allgeier.mycetesterapp.phone.c_AcceptCallActivity;
import com.engineering.allgeier.mycetesterapp.phone.c_CallReceiver;
import com.engineering.allgeier.mycetesterapp.phone.c_PhonecallReceiver;
import com.engineering.allgeier.mycetesterapp.phone.c_PhoneStateReceiver;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

public class MainActivity extends AppCompatActivity {

    private static final UUID s_BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    private BluetoothAdapter mBTAdapter;

    public static final int i_MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 101;

    private SingBroadcastReceiver mReceiver;

    private Thread thread;

    private boolean b_mIsConnect = true;
    private boolean b_isNetworkConnectionEstablished = false;

    private BluetoothDevice mDevice;
    private BluetoothA2dp mBluetoothA2DP;
    private BluetoothHeadset mBluetoothHeadset;
    private BluetoothHealth mBluetoothHealth;
    private BluetoothAdapter mBluetoothAdapter;

    final String s_LOG_TAG = "TelephonyAnswer";
    public static String s_phoneNumber = "";
    public static String s_emailAdresse = "";
    public static String s_InputParameter = "";


    String mPhoneNumber="";
    String possibleEmail=null;
    public static final int RequestPermissionCode = 1;
    private static final int REQUEST_CODE_EMAIL = 1;

    ArrayList<String> names;
    Uri EMAIL_ACCOUNTS_DATABASE_CONTENT_URI = Uri.parse("content://com.android.email.provider/account");


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        s_InputParameter = getIntent().getStringExtra("param");
        if (s_InputParameter == null || s_InputParameter.equals("")) {
            setContentView(R.layout.activity_main);



            Button button_eMailButton = (Button) findViewById(R.id.buttonEMAIL);
            Button button_smsButton = (Button) findViewById(R.id.buttonSMS);
            button_smsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    s_phoneNumber = getMyPhoneNumber();
                    setPhoneNumber(s_phoneNumber);
                    if (isSMSPermissionGranted()) {
                        sendSMS(s_phoneNumber, "This test is awesome and should be displayed on the headunit with special chars 1234567890?=)(/&%$§\"!");
                    }
                }
            });
            Button button_phonecallButton = (Button) findViewById(R.id.buttonPhonecall);
            button_phonecallButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setPhoneNumber("089357458684");
                    try {
                        if (isPermissionGranted()) {
                            call_action(s_phoneNumber);
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Your call has failed...", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });
            button_eMailButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  String possibleEmail = "";
                  Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
                  Account[] accounts = AccountManager.get(getApplicationContext()).getAccounts();
                  for (Account account : accounts) {
                      if (emailPattern.matcher(account.name).matches()) {
                          possibleEmail = account.name;

                      }
                  }

                        /*
                        We need to set the permittion for sending mails.
                        Also in AndroidManifest there is the network connection enabled.
                         */

                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                        /*
                        Before continuing with the sending of the email, check if a network connection is established.
                        Otherwise the email can not been sent.
                         */

                        b_isNetworkConnectionEstablished = isOnline();

                        if (b_isNetworkConnectionEstablished) {
                            try {
                                c_Mail m = new c_Mail("recompliceconnect@gmail.com", "re010417_EE61");
                                String[] array_s_toArr = {getEMailAdresse()};
                                m.setTo(array_s_toArr);
                                m.setFrom("recompliceconnect@gmail.com");
                                m.setSubject("This is an email sent using my c_Mail JavaMail wrapper from an Android device.");
                                m.setBody("Email body.");
                                try {
                                    //m.addAttachment("/sdcard/filelocation");
                                    Toast.makeText(getApplicationContext(), "try to send ", Toast.LENGTH_SHORT).show();
                                    if (m.send()) {
                                        Toast.makeText(getApplicationContext(), "Email was sent successfully.", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Email was not sent.", Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception e) {
                                    Log.e("MailApp", "Could not send email", e);
                                }
                            } catch (Exception e) {
                                Log.e("SendMail", e.getMessage(), e);
                            }
                        }else{
                            Toast.makeText(getApplicationContext(), "No network connection available. Can not send Email.", Toast.LENGTH_LONG).show();
                        }
                    //}


                }
            });

        } else {
            if (s_InputParameter.toString().toLowerCase().contains("phonecall:")) {
                s_phoneNumber = s_InputParameter.toString().substring(10, s_InputParameter.length());
                setPhoneNumber(s_phoneNumber);
                try {
                    if (isPermissionGranted()) {
                        call_action(s_phoneNumber);
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Your call has failed...", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else {
                if (s_InputParameter.toString().toLowerCase().contains("sendsms")) {
                    s_phoneNumber = s_InputParameter.toString().substring(8, s_InputParameter.length());
                    setPhoneNumber(s_phoneNumber);
                    if (isSMSPermissionGranted()) {
                        sendSMS(s_phoneNumber, "This test is awesome and should be displayed on the headunit with special chars 1234567890?=)(/&%$§\"!");
                    }
                }
                /*
                The next if case is for sending e-mails
                The email adress which should be the receip is the parameter which will be given via input (bat)
                example: email:dominik.kupka@recompli.de
                 */
                if (s_InputParameter.toString().toLowerCase().contains("email")) {
                    s_emailAdresse = s_InputParameter.toString().substring(6, s_InputParameter.length());
                    /*
                    Before executing the code for sending the mail the e-mail adress will be checked with the correct syntax.
                    Only valid e-mail adresses will  be sent / used.
                     */
                    if (isValidEmail(s_emailAdresse)) {
                        /*
                        E-c_Mail adress is valid and will be set.
                         */

                        setEMailAdresse(s_emailAdresse);

                        /*
                        We need to set the permittion for sending mails.
                        Also in AndroidManifest there is the network connection enabled.
                         */

                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                        /*
                        Before continuing with the sending of the email, check if a network connection is established.
                        Otherwise the email can not been sent.
                         */

                        b_isNetworkConnectionEstablished = isOnline();

                        if (b_isNetworkConnectionEstablished) {
                            try {
                                c_Mail m = new c_Mail("recompliceconnect@gmail.com", "re010417_EE61");
                                String[] array_s_toArr = {s_emailAdresse};
                                m.setTo(array_s_toArr);
                                m.setFrom("recompliceconnect@gmail.com");
                                m.setSubject("This is an email sent using my c_Mail JavaMail wrapper from an Android device.");
                                m.setBody("Email body.");
                                try {
                                    //m.addAttachment("/sdcard/filelocation");
                                    Toast.makeText(getApplicationContext(), "try to send ", Toast.LENGTH_SHORT).show();
                                    if (m.send()) {
                                        Toast.makeText(this, "Email was sent successfully.", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(this, "Email was not sent.", Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception e) {
                                    Log.e("MailApp", "Could not send email", e);
                                }
                            } catch (Exception e) {
                                Log.e("SendMail", e.getMessage(), e);
                            }
                        }else{
                            Toast.makeText(this, "No network connection available. Can not send Email.", Toast.LENGTH_LONG).show();
                        }
                    }

                    /*
                    App needs to wait after the email has been sent. Otherwise it will not work.
                     */

                    thread=  new Thread(){
                        @Override
                        public void run(){
                            try {
                                synchronized(this){
                                    wait(6000);
                                }
                            }
                            catch(InterruptedException ex){
                                System.err.println(ex);
                            }
                        }
                    };

                }
                if (s_InputParameter.toString().toLowerCase().equals("an") || s_InputParameter.toString().toLowerCase().equals("on") || s_InputParameter.toString().toLowerCase().equals("off") || s_InputParameter.toString().toLowerCase().equals("aus")) {
                    switchBluetoothState(s_InputParameter);
                } else {
                    if (s_InputParameter.toString().toLowerCase().equals("pairing")) {
                        mBluetoothAdapter.getProfileProxy(getApplicationContext(), mProfileListener, BluetoothProfile.A2DP);
                        mBluetoothAdapter.getProfileProxy(getApplicationContext(), mProfileListener, BluetoothProfile.HEADSET);
                        mBluetoothAdapter.getProfileProxy(getApplicationContext(), mProfileListener, BluetoothProfile.HEALTH);
                    }
                }
            }
           /* if (s_InputParameter.toString().toLowerCase().equals("accept_call")) {
                Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
                i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
                this.sendOrderedBroadcast(i, null);
            }*/
            if (s_InputParameter.toString().toLowerCase().equals("bluetooth_pairing")) {
                switchBluetoothState("on");
                if (mBluetoothAdapter.getScanMode() != android.bluetooth.BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    android.content.Intent discoverableIntent = new android.content.Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(android.bluetooth.BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300); // You are able to set how long it is discoverable.
                    startActivity(discoverableIntent);
                }
            }
            if (s_InputParameter.toString().toLowerCase().equals("start_music")) {
                if (Build.VERSION.SDK_INT >= 24) {
                    try {
                        Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                        m.invoke(null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                File f_file = new File(Environment.getExternalStorageDirectory().getPath() + "/beispielmusik/Sleep Away.mp3");
                intent.setDataAndType(Uri.fromFile(f_file), "audio/mp3");
                startActivity(intent);
            }
        }
    }

    private String getMyPhoneNumber(){
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getLine1Number();
    }

    private String getMy10DigitPhoneNumber(){
        String s = getMyPhoneNumber();
        return s != null && s.length() > 2 ? s.substring(2) : null;
    }



    public void EnableRuntimePermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.GET_ACCOUNTS, android.Manifest.permission.READ_PHONE_STATE}, RequestPermissionCode);
    }






  /*  public static void answerCall(Context context) {

        Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
        buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
        context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");

        Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
        buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
        context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
    }*/

    /*public void acceptCall() {
        if (Build.VERSION.SDK_INT >= 21) {
            Intent answerCalintent = new Intent(this, c_AcceptCallActivity.class);
            answerCalintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            answerCalintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(answerCalintent);
        } else {
            Intent intent = new Intent(this, c_AcceptCallActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
        }
    }*/

    /*
    The method acceptIncomingCall returns nothing and performs an instant phonecall.
     */
   /* private void acceptIncomingCall() {
        TelecomManager tm = (TelecomManager) this.getSystemService(Context.TELECOM_SERVICE);
        if (tm == null) {
            // whether you want to handle this is up to you really
            throw new NullPointerException("tm == null");
        }
        try {
            tm.getClass().getMethod("answerRingingCall").invoke(tm);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        c_PhonecallReceiver a = new c_PhonecallReceiver() {
            protected void onIncomingCallReceived(Context ctx, String number, Date start) {
                TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
                try {
                    if (tm == null) {
                        // this will be easier for debugging later on
                        throw new NullPointerException("tm == null");
                    }
                    // do reflection magic
                    tm.getClass().getMethod("answerRingingCall").invoke(tm);
                } catch (Exception e) {
                    Log.e(s_LOG_TAG, "Unable to use the Telephony Manager directly.", e);
                }
            }

            @Override
            protected void onIncomingCallAnswered(Context ctx, String number, Date start) {

            }

            @Override
            protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
            }

            @Override
            protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
            }

            @Override
            protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
            }

            @Override
            protected void onMissedCall(Context ctx, String number, Date start) {
            }
        };
    }*/

    /*
    Setter and getter for the PhoneNumber and the E-Mail address given by the bat who starts
     the application.
     */
    public void setPhoneNumber(String s_phoneNumber) {
        this.s_phoneNumber = s_phoneNumber;
    }

    public void setEMailAdresse(String s_emailAdresse) {
        this.s_emailAdresse = s_emailAdresse;
    }

    public String getPhoneNumber() {
        return s_phoneNumber;
    }

    public String getEMailAdresse() {
        return s_emailAdresse;
    }

    public void sendSMS(String s_phoneNo, String s_msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(s_phoneNo, null, s_msg, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage().toString(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

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

    /*
    This method sendEMail returns nothing and needs a String as inputparameter which will be the
    e-mail address who comes in the app via bat command from pc.
     */
    private void sendEmail(String s_emailAdresse) {
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{s_emailAdresse});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Automatisch gesendete E-Mail");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Best E-Mail text so far and nun etwas Sonderzeichen 1234567890^!°\"§$%&/()=?`");
        MainActivity.this.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    /*
    This method switchBluetoothState returns nothing and excepts an inputparameter who switches between the state "on / an" and "off / aus".
    The inputparameter is a string and is given by the bat, started from the pc. In case the parameter equals "an / on" the method will
    instantly start the bluetoothcore. If the incoming string equals "aus / off" the method will instantly switch off the bluetoothcore.
     */
    public void switchBluetoothState(String s_InputParameter) {
        if (s_InputParameter.toString().toLowerCase().equals("on") || s_InputParameter.toString().toLowerCase().equals("an")) {
            mBTAdapter = BluetoothAdapter.getDefaultAdapter();
            mBTAdapter.enable();
        }
        if (s_InputParameter.toString().toLowerCase().equals("off") || s_InputParameter.toString().toLowerCase().equals("aus")) {
            mBTAdapter = BluetoothAdapter.getDefaultAdapter();
            mBTAdapter.disable();
        }else{
            // do nothing
        }
    }

    /*
    This method ScanForBluetoothDevices returns nothing and has no inputparameter. It enables the bluetoothadapater
      and scans for available bluetooth devices.
     */
    public void ScanForBluetoothDevices() {
        mBTAdapter.enable();
        mBTAdapter.startDiscovery();
        Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
        mReceiver = new SingBroadcastReceiver();
        IntentFilter ifilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, ifilter);
    }

    /*
    Method pairDevice returns nothing and expects as inputparameter a BluetoothDevice from type BluetoothDevice.
    It will start pairing the mobile device with the found compatible bt device.
     */
    private void pairDevice(BluetoothDevice device) {
        try {
            Log.d("pairDevice()", "Start Pairing...");
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            Log.d("pairDevice()", "Pairing finished.");
        } catch (Exception e) {
            Log.e("pairDevice()", e.getMessage());
        }
    }

    /*
    This method isValidEmail retuns a boolean and needs a charsqeuence as inputparameter. The inputparameter will be
    an e-mail address which will come into this app via pc / bat command as string. The e-mail address needs to be checked
    before it should be started to send an e-mail. If this e-mail address which comes in as inputparameter is valid the
    method returns true and the next step in the sending of emails will be triggered, otherwise it will return false if
    the syntax of the e-mail adress is not valid and no e-mail will be sent.
     */
    public final static boolean isValidEmail(CharSequence c_target) {
        return !TextUtils.isEmpty(c_target) && android.util.Patterns.EMAIL_ADDRESS.matcher(c_target).matches();
    }

    private class SingBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String s_action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(s_action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a Toast
                String s_derp = device.getName() + " - " + device.getAddress();
                Toast.makeText(context, s_derp, Toast.LENGTH_LONG);
            }
        }
    }

    public void call_action(String s_phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + s_phoneNumber));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(callIntent);
    }

    /*
    This method isPermissionGranted returns a boolean and has no inputparameter. It will check the current
    device sdk version. If the version is greater or equal to version 23 it needs to check the device permissions
    for performing phonecalls and enable them. If the sdk version of the device is below 23 the permission is
    automatically granted.
     */
    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(s_LOG_TAG, "Permission is granted");
                return true;
            } else {
                Log.v(s_LOG_TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                return false;
            }
        } else {
            Log.v(s_LOG_TAG, "Permission is granted");
            return true;
        }
    }

    /*
    This method isPermissionGranted returns a boolean and has no inputparameter. It will check the current
    device sdk version. If the version is greater or equal to version 23 it needs to check the device permissions
    for performing sms and enable them. If the sdk version of the device is below 23 the permission is
    automatically granted.
     */

    public boolean isSMSPermissionGranted(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                Log.v(s_LOG_TAG, "Permission is granted");
                System.out.println("Permission is granted");
                return true;
            } else {
                Log.v(s_LOG_TAG, "Permission is revoked");
                System.out.println("Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, 0);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(s_LOG_TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:

                if (grantResults.length > 0) {

                    boolean GetAccountPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadPhoneStatePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (GetAccountPermission && ReadPhoneStatePermission) {

                        Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();

                    }
                }

                break;
        }
    }

    protected BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int i_profile, BluetoothProfile proxy) {
            if (i_profile == BluetoothProfile.A2DP) {
                mBluetoothA2DP = (BluetoothA2dp) proxy;
                try {
                    if (b_mIsConnect) {
                        Method connect = BluetoothA2dp.class.getDeclaredMethod("connect", BluetoothDevice.class);
                        connect.setAccessible(true);
                        connect.invoke(mBluetoothA2DP, mDevice);
                    } else {
                        Method disconnect = BluetoothA2dp.class.getDeclaredMethod("disconnect", BluetoothDevice.class);
                        disconnect.setAccessible(true);
                        disconnect.invoke(mBluetoothA2DP, mDevice);
                    }
                }catch (Exception e){
                } finally {
                }
            } else if (i_profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = (BluetoothHeadset) proxy;
                try {
                    if (b_mIsConnect) {
                        Method connect = BluetoothHeadset.class.getDeclaredMethod("connect", BluetoothDevice.class);
                        connect.setAccessible(true);
                        connect.invoke(mBluetoothHeadset, mDevice);
                    } else {
                        Method disconnect = BluetoothHeadset.class.getDeclaredMethod("disconnect", BluetoothDevice.class);
                        disconnect.setAccessible(true);
                        disconnect.invoke(mBluetoothHeadset, mDevice);
                    }
                }catch (Exception e){
                } finally {
                }
            } else if (i_profile == BluetoothProfile.HEALTH) {
                mBluetoothHealth = (BluetoothHealth) proxy;
                try {
                    if (b_mIsConnect) {
                        Method connect = BluetoothHealth.class.getDeclaredMethod("connect", BluetoothDevice.class);
                        connect.setAccessible(true);
                        connect.invoke(mBluetoothHealth, mDevice);
                    } else {
                        Method disconnect = BluetoothHealth.class.getDeclaredMethod("disconnect", BluetoothDevice.class);
                        disconnect.setAccessible(true);
                        disconnect.invoke(mBluetoothHealth, mDevice);
                    }
                }catch (Exception e){
                } finally {
                }
            }
        }
        public void onServiceDisconnected(int profile) {
        }


        protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
            if (requestCode == REQUEST_CODE_EMAIL && resultCode == RESULT_OK) {
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                Log.v(s_LOG_TAG, "email address is -> " + accountName);
                setEMailAdresse(accountName);
            }
        }
        protected String getUsername() {
            AccountManager manager = AccountManager.get(MainActivity.this);
            Account[] accounts = manager.getAccountsByType("com.google");
            String email = "";
            String[] parts = new String[10];
            List<String> possibleEmails = new LinkedList<String>();

            for (Account account : accounts) {
                // TODO: Check possibleEmail against an email regex or treat
                // account.name as an email address only for certain account.type values.
                possibleEmails.add(account.name);
            }

            if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
                email = possibleEmails.get(0);
                parts = email.split("@");

                if (parts.length > 1)
                    return parts[0];
            }
            Log.v(s_LOG_TAG, "komme ich hierhin? was gebe ich als email adresse zurueck? -> " + parts[0]);
            Log.v(s_LOG_TAG, "komme ich hierhin? was gebe ich als email adresse zurueck 2? -> " + email);
            setEMailAdresse(email);

            return null;
        }

    };
}