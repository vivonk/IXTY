package com.projects.squad;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MyJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        // runs on the main thread, so this Toast will appear
        Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
        // perform work here, i.e. network calls asynchronously
        ScheduledThreadPoolExecutor sch = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(5);
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(Utils.prefFileName, 0);

        String job = jobParameters.getExtras().getString("job");
        String jobFullKey = "location-" + job;
        if(!preferences.getBoolean(jobFullKey, false)){
            return true;
        }

        double histLat = Double.parseDouble(preferences.getString(jobFullKey + "-lat", "0"));
        double histLong = Double.parseDouble(preferences.getString(jobFullKey + "-long", "0"));

        final Location targetLocation = new Location("TargetLocation");
        targetLocation.setLongitude(histLong);
        targetLocation.setLatitude(histLat);
        final String jobStatic = job;

        Runnable task = new Runnable() {
            @Override
            public void run() {
                Log.d("RunningJob", "Job is running in 20 second delay");
                Location currentLocation = new Utils().getCurrentLocation(getApplicationContext());
                float distance = currentLocation.distanceTo(targetLocation);
                Log.d("LocationService", "run: Distance between is " + distance);
                if (distance <= 10){ // within 10 meter
                    if(jobStatic.contains("wifi")){
                        startWifi();
                    } else if(jobStatic.contains("unmute")){
                        Log.d("JobOnWork", "Need to unmute your mobile phone");
                        unmuteMobilePhone();
                    }
                }
            }
        };


        sch.scheduleAtFixedRate(task, 5, 20, TimeUnit.SECONDS);

        // returning false means the work has been done, return true if the job is being run asynchronously


        return true;
    }
    public void startWifi(){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

    }

    public void unmuteMobilePhone(){
        AudioManager mode = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }


    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}