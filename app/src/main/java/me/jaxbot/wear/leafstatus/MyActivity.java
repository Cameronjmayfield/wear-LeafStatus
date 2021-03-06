package me.jaxbot.wear.leafstatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MyActivity extends ActionBarActivity {
    String DISCLAIMER = "A project by Jonathan Warner (@Jaxbot). Not affiliated with or supported by Nissan.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences settings = getSharedPreferences("U", 0);

        if (settings.getString("username", "").equals("")) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_my);



        SurfaceView view = (SurfaceView) findViewById(R.id.surfaceView2);

        final Context context = this;

        final Spinner spinner = (Spinner) findViewById(R.id.spinner_chargelevel);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.charge_levels, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setSelection(settings.getInt("defaultChargeLevel", 0));

        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences("U", 0);
                SharedPreferences.Editor editor = settings.edit();

                int interval = ((SeekBar) findViewById(R.id.seekBar)).getProgress();
                boolean autoUpdate = ((CheckBox) findViewById(R.id.checkBox)).isChecked();
                boolean showPermanent = ((CheckBox) findViewById(R.id.permanent)).isChecked();
                boolean useMetric = ((CheckBox) findViewById(R.id.metric)).isChecked();

                if (showPermanent && !settings.getBoolean("showPermanent", false)) {
                    new AlertDialog.Builder(context)
                        .setTitle("Wear Warning")
                        .setMessage("Undismissible notifications will not display on connected Wear devices due to current limitations.")
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // eh
                            }
                        })
                        .show();
                }

                editor.putInt("interval", interval);
                editor.putBoolean("autoupdate", autoUpdate);
                editor.putBoolean("showPermanent", showPermanent);
                editor.putBoolean("useMetric", useMetric);
                editor.putInt("defaultChargeLevel", spinner.getSelectedItemPosition());

                editor.commit();

                if (autoUpdate)
                    AlarmSetter.setAlarm(context);
                else
                {
                    AlarmSetter.cancelAlarm(context);
                }
                updateCarStatusAsync();

                button.setEnabled(false);

                showToast("Saved, updating vehicle status...");
            }
        });

        int interval = settings.getInt("interval", 30);
        final SeekBar seekbar = (SeekBar)findViewById(R.id.seekBar);

        seekbar.setProgress(interval);
        setProgressText(interval);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setProgressText(i);
                findViewById(R.id.button).setEnabled(true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ((CheckBox)(findViewById(R.id.permanent))).setChecked(settings.getBoolean("showPermanent", false));
        ((CheckBox)(findViewById(R.id.metric))).setChecked(settings.getBoolean("useMetric", false));
        CheckBox checkbox = ((CheckBox)(findViewById(R.id.checkBox)));
        checkbox.setChecked(settings.getBoolean("autoupdate", true));
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                findViewById(R.id.button).setEnabled(true);
                seekbar.setEnabled(b);
            }
        });


        Carwings carwings = new Carwings(this);
        if (carwings.lastUpdateTime.equals("")) {
            updateCarStatusAsync();
        } else {
            updateCarStatusUI(carwings);
            LeafNotification.sendNotification(context, carwings);
        }
    }

    void updateCarStatusAsync()
    {
        (findViewById(R.id.progressBar)).setVisibility(View.VISIBLE);

        final Context context = this;
        final Activity activity = this;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                final Carwings carwings = new Carwings(context);

                carwings.update();
                LeafNotification.sendNotification(context, carwings);

                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        updateCarStatusUI(carwings);
                    }
                });
                return null;
            }
        }.execute(null, null, null);
    }

    void updateCarStatusUI(Carwings carwings)
    {
        (findViewById(R.id.surfaceView2)).setBackgroundColor(Color.parseColor(carwings.currentBattery == 12 ? "#8bc34a" : carwings.charging ? "#ff9800" : carwings.currentBattery > 2 ? "#03a9f4" : "#e51c23"));
        (findViewById(R.id.progressBar)).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.battery_bars)).setText(carwings.currentBattery + " of 12");
        ((TextView) findViewById(R.id.chargetime)).setText(carwings.charging ? "Charging, " + carwings.chargeTime + "till full [" + carwings.chargerType + "]" : carwings.chargeTime + "to charge [" + carwings.chargerType + "]");
        ((TextView) findViewById(R.id.range)).setText(carwings.range);
        ((TextView) findViewById(R.id.lastupdated)).setText(carwings.lastUpdateTime);
        ((Button) findViewById(R.id.button)).setEnabled(true);

        try {
            String versionName = this.getPackageManager()
                .getPackageInfo(this.getPackageName(), 0).versionName;
            ((TextView) findViewById(R.id.disclaimer)).setText(DISCLAIMER + " V" + versionName);
        }
        catch (Exception e)
        {
        }
    }

    private void setProgressText(int interval) {
        ((TextView) findViewById(R.id.txtMinutes)).setText("Update every " + (interval + 15) + " minutes");
    }

    void showToast(String text)
    {
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_signoff) {
            SharedPreferences settings = getSharedPreferences("U", 0);
            SharedPreferences.Editor editor = settings.edit();

            editor.putString("username", "");
            editor.putString("password", "");

            editor.commit();

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

