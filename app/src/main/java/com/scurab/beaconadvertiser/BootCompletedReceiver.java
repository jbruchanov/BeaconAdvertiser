package com.scurab.beaconadvertiser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by jiri.bruchanov on 09/12/2014.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    private boolean ENABLED = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BootCompletedReceiver", "onReceive:action" + intent.getAction());
        if (ENABLED && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootCompletedReceiver", "Start ScannerService");
            context.startService(new Intent(context, ScannerService.class));
        }
    }
}
