package com.nizam.training.parentalcontrol;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

public class WifiReceiver extends BroadcastReceiver {
    String action = "";
    MyDB myDB;
    Context ct;

    @Override
    public void onReceive(Context aContext, Intent intent) {
        ct = aContext;
        action = intent.getAction();
        String pack_name = "";
        if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            try {
                TelephonyManager manager = (TelephonyManager) aContext.getSystemService(Context.TELEPHONY_SERVICE);
                //  TelecomManager telecomManager=(TelecomManager)aContext.getSystemService(TELECOM_SERVICE);
                //android.telecom.Call call=aContext.getSystemService(Call.class);
                //   Tele;
                // manager.

               /* Class c = Class.forName(manager.getClass().getName());
                Method m = c.getDeclaredMethod("getITelephony");
                m.setAccessible(true);*/
               /* ITelephony telephony = (ITelephony)m.invoke(manager);
                Log.i("Phone","blocked");
                telephony.endCall();t*/
            } catch (Exception e) {
                Log.d("mnkException", e.getMessage());
            }
        }
        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
            pack_name = "wifi";
        else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
            pack_name = "bluetooth";
        myDB = new MyDB(aContext);
        int isPerm = myDB.getData(AppSettingActivity.IsPermanent, pack_name);
        if (isPerm == 1)
            blockWifi();
        else {
            int HoursOfDay = myDB.getData(AppSettingActivity.HoursOfDay, pack_name);
            int MinutesOfHours = myDB.getData(AppSettingActivity.MinutesOfDay, pack_name);
            int timer = HoursOfDay * 60 + MinutesOfHours;
            int startHour = myDB.getData(AppSettingActivity.StartTimeInHours, pack_name);
            int startMinute = myDB.getData(AppSettingActivity.StartTimeInMinutes, pack_name);
            int endHour = myDB.getData(AppSettingActivity.EndTimeInHours, pack_name);
            int endMinute = myDB.getData(AppSettingActivity.EndTimeInMinutes, pack_name);
            int startTime = startHour * 60 + startMinute;
            int endTime = endHour * 60 + endMinute;
            int intweek1[] = AppSettingActivity.strToInt(myDB.getWeekData(AppSettingActivity.NoOfdayInWeek1, pack_name));
            int intweek2[] = AppSettingActivity.strToInt(myDB.getWeekData(AppSettingActivity.NoOfdayInWeek2, pack_name));
            Calendar calendar = Calendar.getInstance();
            int currentTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
            if (timer != 0) {
                for (int i1 : intweek1) {
                    if (i1 == calendar.get(Calendar.DAY_OF_WEEK)) {
                        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                            SharedPreferences sp = aContext.getSharedPreferences("BlockWifiTime", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.apply();
                            WifiManager wifi = (WifiManager) ct.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            if (wifi.isWifiEnabled()) {
                                int onTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);//55
                                editor.putInt("onTime", onTime).apply();
                            } else {
                                int offTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);//60
                                editor.putInt("timeUsed", offTime - sp.getInt("onTime", 0)).apply();
                            }
                            if (timer >= sp.getInt("timeUsed", 0)) {
                                blockWifi();
                            }
                        } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                            SharedPreferences sp = aContext.getSharedPreferences("BlockWifiTime", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.apply();

                        }
                    }
                }
            } else if (!(startTime == 0 && endTime == 0)) {
                for (int i1 : intweek2) {
                    if (i1 == calendar.get(Calendar.DAY_OF_WEEK)) {
                        if (startTime <= currentTime && endTime >= currentTime) {
                            blockWifi();
                        }
                    }
                }
            }
        }
    }

    public void blockWifi() {
        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            WifiManager wifi = (WifiManager) ct.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifi.isWifiEnabled())
                wifi.setWifiEnabled(false);
        } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled())
                bluetoothAdapter.disable();
        }
    }
}