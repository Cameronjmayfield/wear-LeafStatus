package me.jaxbot.wear.leafstatus.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import me.jaxbot.wear.leafstatus.Carwings;
import me.jaxbot.wear.leafstatus.LeafNotification;

public class StartACTask extends AsyncTask<Void, Void, Void>{

    final static String TAG = "StartAC";
    private final Context mContext;
    private final boolean state;

    public StartACTask(Context context, boolean state) {
        this.mContext = context;
        this.state = state;
    }

    @Override
        protected Void doInBackground(Void... params) {
            Carwings carwings = new Carwings(mContext);

            carwings.currentHvac = state;
            LeafNotification.sendNotification(mContext, carwings, false);

            if (carwings.startAC(state)) {
                Log.i(TAG, "AC started.");
            } else {
                Log.i(TAG, "StartAC failed, likely due to login.");
            }

            LeafNotification.sendNotification(mContext, carwings);
            return null;
        }
}
