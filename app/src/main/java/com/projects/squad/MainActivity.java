package com.projects.squad;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.PersistableBundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SMSHandler broadcastSms;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        requestReadAndSendSmsPermission();

        Log.d("BTP", "Simple logger message to check");
        final List<String> operationsSMS = new ArrayList<>();
        operationsSMS.add("#Ring \nThis will enable the Ring service. When your mobile phone is far from you, just send a SMS containing #ring from some other phone, and it will set mode to ring mode");
        operationsSMS.add("#Locate \nThis will enable locate service. When you lost your mobile and don't know where, just send #locate SMS to your number and it will send your latest location of mobile back");

        final List<String> operationsGPS = new ArrayList<>();
        operationsGPS.add("#Unmute \nThis will enable Unmute service. Automatically unmute your Android when you get back home");
        operationsGPS.add("#Wifi \nTurn on Wifi and turn off mobile data when I enter home/office");

        Toast.makeText(this, "Registered Broadcast service", Toast.LENGTH_SHORT).show();
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.lv_item, operationsSMS){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View itemView = inflater.inflate(R.layout.lv_item,null,true);

                String desc = operationsSMS.get(position);
                CardView cardView = itemView.findViewById(R.id.card);

                LinearLayout linearLayout = cardView.findViewById(R.id.lv_item_ll);
                TextView tv = linearLayout.findViewById(R.id.lv_item_ll_tv);
                Switch swt = linearLayout.findViewById(R.id.lv_item_ll_sw);
                swt.setId(position);

                tv.setText(desc);

                swt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        int positionId = compoundButton.getId();
//                        Toast.makeText(MainActivity.this, "Check changed " + b + " " + compoundButton.getId(), Toast.LENGTH_SHORT).show();
                        if(positionId == 0){
                            Toast.makeText(MainActivity.this, "Setting up ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                return itemView;
            }
        };

        ListView listView = findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        ArrayAdapter adapterGPS = new ArrayAdapter<String>(this, R.layout.lv_item, operationsGPS){
            @NonNull
            @Override
            public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View itemView = inflater.inflate(R.layout.lv_item,null,true);

                String desc = operationsGPS.get(position);
                CardView cardView = itemView.findViewById(R.id.card);

                LinearLayout linearLayout = cardView.findViewById(R.id.lv_item_ll);
                TextView tv = linearLayout.findViewById(R.id.lv_item_ll_tv);
                Switch swt = linearLayout.findViewById(R.id.lv_item_ll_sw);
                swt.setId(position);

                tv.setText(desc);

                swt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        SharedPreferences preferences = getApplicationContext().getSharedPreferences(Utils.prefFileName, 0);
                        SharedPreferences.Editor editor = preferences.edit();

                        int positionId = compoundButton.getId();
//                        Toast.makeText(MainActivity.this, "Check changed " + b + " " + compoundButton.getId(), Toast.LENGTH_SHORT).show();
                        String service = "";
                        switch (position){
                            case 0: service = "unmute"; break;
                            case 1: service = "wifi"; break;
                        }
                        if(b){
                            Location currentLocation = new Utils().getCurrentLocation(MainActivity.this);
                            String TAG = "LocationServiceBTP";
                            Log.d(TAG, "onCheckedChanged - location " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
                            Log.d(TAG, "onCheckedChanged - location " + (float) currentLocation.getLatitude() + ", " + (float) currentLocation.getLongitude());
                            editor.putString("location-" + service + "-lat", String.valueOf(currentLocation.getLatitude()));
                            editor.putString("location-" + service + "-long", String.valueOf(currentLocation.getLongitude()));
                            if(positionId == 0){
                                Toast.makeText(MainActivity.this, "Setting up Un mute service", Toast.LENGTH_SHORT).show();

                            } else if(positionId == 1){
                                Toast.makeText(MainActivity.this, "Setting up Wifi service", Toast.LENGTH_SHORT).show();
                            }
                            editor.putBoolean("location-" + service, true);
                            editor.apply();
                            launchLocationJob(service);
//                            editor.commit();
                        } else{
                            editor.putBoolean("location-" + service, false);
                            editor.apply();
//                            editor.commit();
                        }
                        editor.commit();
                    }
                });

                return itemView;
            }


        };

        ListView lvGPS = findViewById(R.id.list_view_gps);
        lvGPS.setAdapter(adapterGPS);
        lvGPS.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(MainActivity.this, "Item clicked - " + i + " " + l, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void launchLocationJob(String whichJob){
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("job", whichJob);

        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo jobInfo = new JobInfo.Builder(10001, new ComponentName(getApplicationContext(), MyJobService.class))
                .setPersisted(true)
                .setExtras(bundle)
                .setRequiresCharging(false)
                .setOverrideDeadline(0)
                .build();
        int result = 0;

        try {
            result = scheduler.schedule(jobInfo);
        } catch (Exception e){
            e.printStackTrace();
        }

        if (result == JobScheduler.RESULT_SUCCESS){
            Toast.makeText(this, "Successfully scheduled job", Toast.LENGTH_SHORT).show();
        } else{
            Toast.makeText(this, "Failed to scheduled job", Toast.LENGTH_SHORT).show();
        }
    }


    private void requestReadAndSendSmsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_SMS)) {
            Log.d("Permission", "shouldShowRequestPermissionRationale(), no permission requested");
            return;
        }
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS, Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                , Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE},
                0);
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !notificationManager.isNotificationPolicyAccessGranted()) {

            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

            startActivity(intent);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
