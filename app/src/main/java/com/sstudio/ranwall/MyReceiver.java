package com.sstudio.ranwall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class MyReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Intent startIntent = new Intent(context, RanWallService.class);
            startIntent.setAction(Constants.ACTION.DOWNLOAD_IMAGE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startIntent);
            }else {
                context.startService(startIntent);
            }
        }
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent startIntent = new Intent(context, RanWallService.class);
            //startIntent.setAction(Constants.ACTION.DOWNLOAD_IMAGE);
            startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startIntent);
            }else {
                context.startService(startIntent);
            }
        }
    }
}
