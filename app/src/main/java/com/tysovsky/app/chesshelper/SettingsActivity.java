package com.tysovsky.app.chesshelper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;


public class SettingsActivity extends Activity implements View.OnClickListener {

    SharedPreferences sharedPreferences;

    Button btnSave;
    EditText etVibrationShort;
    EditText etVibrationLong;
    EditText etPauseShort;
    EditText etPauseLong;
    CheckBox cbAutoDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(MainActivity.preferences, 0);

        setupViews();




    }

    public void setupViews()
    {
        btnSave = (Button)findViewById(R.id.btnSettingsSave);
        btnSave.setOnClickListener(this);

        etVibrationShort = (EditText)findViewById(R.id.etSettingsShortVibration);
        etVibrationShort.setText(""+sharedPreferences.getLong("VIBRATION_SHORT", 1000));
        etVibrationLong = (EditText)findViewById(R.id.etSettingsLongVibration);
        etVibrationLong.setText(""+sharedPreferences.getLong("VIBRATION_LONG", 4000));
        etPauseShort = (EditText)findViewById(R.id.etSettingsShortPause);
        etPauseShort.setText(""+sharedPreferences.getLong("PAUSE_SHORT", 1000));
        etPauseLong = (EditText)findViewById(R.id.etSettingsLongPause);
        etPauseLong.setText(""+sharedPreferences.getLong("PAUSE_LONG", 4000));
        cbAutoDelete = (CheckBox)findViewById(R.id.cbAutoDelete);
        cbAutoDelete.setChecked(sharedPreferences.getBoolean("AUTO_DELETE", false));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSettingsSave:
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("VIBRATION_SHORT", Long.decode(etVibrationShort.getText().toString()));
                editor.putLong("VIBRATION_LONG", Long.decode(etVibrationLong.getText().toString()));
                editor.putLong("PAUSE_SHORT", Long.decode(etPauseShort.getText().toString()));
                editor.putLong("PAUSE_LONG", Long.decode(etPauseLong.getText().toString()));
                editor.putBoolean("AUTO_DELETE", cbAutoDelete.isChecked());
                editor.commit();
                finish();
                break;
        }
    }
}
