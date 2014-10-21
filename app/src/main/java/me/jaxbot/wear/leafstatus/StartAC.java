package me.jaxbot.wear.leafstatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import me.jaxbot.wear.leafstatus.async.StartACTask;

/**
 * Created by jonathan on 9/21/14.
 */
public class StartAC extends BroadcastReceiver {
    final static String TAG = "StartAC";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "Starting AC...");

        final boolean state = intent.getBooleanExtra("desiredState", false);

        Log.i(TAG, "Desired: " + String.valueOf(state));

        new StartACTask(context, state).execute(null, null, null);
    }
}
