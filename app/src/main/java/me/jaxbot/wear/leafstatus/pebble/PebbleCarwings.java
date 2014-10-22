package me.jaxbot.wear.leafstatus.pebble;

import com.getpebble.android.kit.util.PebbleDictionary;

import me.jaxbot.wear.leafstatus.Carwings;

public class PebbleCarwings{

    private static final int CURRENT_BATTERY = 2;
    private static final int RANGE = 3;
    private static final int CHARGE_TIME = 4;
    private static final int CURRENT_HVAC = 5;
    private static final int LAST_UPDATE_TIME = 6;
    private static final int CHARGER_TYPE = 7;
    private static final int CHARGING = 8;
    private static final int USE_METRIC = 9;

    public static PebbleDictionary getDictionary(Carwings carwings){
        PebbleDictionary data = new PebbleDictionary();
        data.addInt8(CURRENT_BATTERY, (byte) carwings.currentBattery);
        data.addString(RANGE, carwings.range);
        data.addString(CHARGE_TIME, carwings.chargeTime);
        data.addInt8(CURRENT_HVAC, carwings.currentHvac ? (byte) 1 : 0);
        data.addString(LAST_UPDATE_TIME, carwings.lastUpdateTime);
        data.addString(CHARGER_TYPE, carwings.chargerType);
        data.addInt8(CHARGING, carwings.charging ? (byte) 1 : 0);
        data.addInt8(USE_METRIC, carwings.useMetric ? (byte) 1 : 0);
        return data;
    }
}
