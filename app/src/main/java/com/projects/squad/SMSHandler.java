package com.projects.squad;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class SMSHandler extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        Toast.makeText(context,"Broadcast Received - oh yeh", Toast.LENGTH_LONG).show();
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                String incomingNumber = smsMessage.getOriginatingAddress();

                String messageBody = smsMessage.getMessageBody();
                Toast.makeText(context, "Broadcast Received " + messageBody, Toast.LENGTH_LONG).show();
                Log.d("Message received is :: ", "onReceive " + messageBody + "\n" + "INcoming number " + incomingNumber);
                Toast.makeText(context, "Received a new msg from " + incomingNumber, Toast.LENGTH_SHORT).show();

                messageParsing(context, messageBody, incomingNumber);
            }
        }
    }

    public void messageParsing(Context context, String message, String incomingNumber) {
        message = message.toLowerCase();
        if (message.contains("#ring")) {
            AudioManager mode = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            mode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            Toast.makeText(context, "Phone set on Ring mode", Toast.LENGTH_LONG).show();
//            sendSms("Done", incomingNumber);
        } else if (message.contains("#silent")) {
            AudioManager mode = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            mode.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            Toast.makeText(context, "Phone set on silent mode", Toast.LENGTH_LONG).show();
//            sendSms("Done", incomingNumber);
        } else if (message.contains("#locate")) {
            Location currentLocation = getCurrentLocation(context);
            double latitude = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();

            String locationSMS = "Last detected location of the Mobile is latitude - " + latitude +
                    " and longitude - " + longitude;
            Log.d("Location-BTP", locationSMS);
            sendSms(locationSMS, incomingNumber);
        }
    }

    public Location getCurrentLocation(Context context) {
        LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);


        @SuppressLint("MissingPermission") Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        @SuppressLint("MissingPermission") Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }


    public void sendSms(String msg, String contactNumber){
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> parts = smsManager.divideMessage(msg);
        smsManager.sendMultipartTextMessage(contactNumber, null, parts, null, null);
    }



}
