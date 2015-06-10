package com.tysovsky.app.chesshelper;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;


public class MainActivity extends Activity implements View.OnClickListener {
    BluetoothSPP bt;
    Vibrator vibrator;
    public SharedPreferences sharedPreferences;

    Button btnConnect;
    Button btnSend;
    Button btnSettings;

    EditText etLetter1;
    EditText etNumber1;
    EditText etLetter2;
    EditText etNumber2;

    public static String preferences = "preferences";

    long VIBRATION_LONG = 4000;
    long VIBRATION_SHORT = 1000;
    long PAUSE_LONG = 4000;
    long PAUSE_SHORT = 1000;

    Map<String, long[]> signals = new HashMap<String, long[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt = new BluetoothSPP(this);
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);


        initializeViews();
        setupSignals();

        //If Bluetooth is not available
        if(!bt.isBluetoothAvailable()){
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            //Exit the application
            finish();
        }

        //Listener for when data is recieved
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] bytes, String s) {
                vibrator.vibrate(getSignalPattern(bytes), -1);
            }
        });

        //Bluetooth status listener
        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            @Override
            public void onDeviceConnected(String deviceName, String deviceAdress) {
                Toast.makeText(getApplicationContext(), "Connected to " + deviceName + "\n" + deviceAdress, Toast.LENGTH_SHORT).show();
                btnConnect.setText("Disconnect");
            }

            @Override
            public void onDeviceDisconnected() {
                //Notify the user that bluetooth connection was lost
                Toast.makeText(getApplicationContext(), "Connection Lost", Toast.LENGTH_SHORT).show();
                //Change the connect button text
                btnConnect.setText("Connect");
            }

            @Override
            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext(), "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        //Check is bluetooth is turned on. If not ask the user's permission to turn it on
        if(!bt.isBluetoothEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        }
        //Start the bluetooth service
        else{
            if(!bt.isServiceAvailable()){
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        setupSignals();

    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        //Turn of the bluetooth service when app closes
        bt.stopService();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnConnect:
                if(bt.getServiceState() == BluetoothState.STATE_CONNECTED){
                    bt.disconnect();
                }
                else{
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
                break;
            case R.id.btnSend:
                String msgToSend = etLetter1.getText().toString()+etNumber1.getText().toString()
                        + etLetter1.getText().toString() + etNumber2.getText().toString();
                bt.send(msgToSend, true);

                if(sharedPreferences.getBoolean("AUTO_DELETE", false))
                {
                    etLetter1.setText("");
                    etLetter2.setText("");
                    etNumber1.setText("");
                    etNumber2.setText("");
                }

                break;
            case R.id.btnSettings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                MainActivity.this.startActivity(intent);
                break;
        }
    }

    private void initializeViews(){
        btnConnect = (Button)findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(this);
        btnSend = (Button)findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        btnSettings = (Button)findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(this);

        etLetter1 = (EditText)findViewById(R.id.etLetter1);
        etLetter2 = (EditText)findViewById(R.id.etLetter2);
        etNumber1 = (EditText)findViewById(R.id.etNumber1);
        etNumber2 = (EditText)findViewById(R.id.etNumber2);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE){
            if(resultCode == Activity.RESULT_OK){
                bt.connect(data);
            }
        }

        else if(requestCode == BluetoothState.REQUEST_ENABLE_BT){
            if(resultCode == Activity.RESULT_OK){
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Bluetooth was not enabled", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void setupSignals(){

        sharedPreferences = getSharedPreferences(preferences, 0);
        VIBRATION_SHORT = sharedPreferences.getLong("VIBRATION_SHORT", 1000);
        VIBRATION_LONG = sharedPreferences.getLong("VIBRATION_LONG", 4000);
        PAUSE_SHORT = sharedPreferences.getLong("PAUSE_SHORT", 1000);
        PAUSE_LONG = (long)(sharedPreferences.getLong("PAUSE_LONG", 4000)/2);

        signals.clear();

        Log.i("Info", "Map size before: " + signals.size());

        signals.put("A", new long[]{PAUSE_LONG, VIBRATION_SHORT, PAUSE_LONG});
        signals.put("B", new long[]{PAUSE_LONG, VIBRATION_SHORT, PAUSE_SHORT, VIBRATION_SHORT, PAUSE_LONG});
        signals.put("C", new long[]{PAUSE_LONG, VIBRATION_SHORT, PAUSE_SHORT, VIBRATION_SHORT, PAUSE_SHORT, VIBRATION_SHORT, PAUSE_LONG});
        signals.put("D", new long[]{PAUSE_LONG, VIBRATION_SHORT, PAUSE_SHORT, VIBRATION_SHORT, PAUSE_SHORT, VIBRATION_SHORT, PAUSE_SHORT, VIBRATION_SHORT, PAUSE_LONG});
        signals.put("E", new long[]{PAUSE_LONG, VIBRATION_LONG, PAUSE_LONG});
        signals.put("F", new long[]{PAUSE_LONG, VIBRATION_LONG, PAUSE_SHORT, VIBRATION_LONG, PAUSE_LONG});
        signals.put("G", new long[]{PAUSE_LONG, VIBRATION_LONG, PAUSE_SHORT, VIBRATION_LONG, PAUSE_SHORT, VIBRATION_LONG, PAUSE_LONG});
        signals.put("H", new long[]{PAUSE_LONG, VIBRATION_LONG, PAUSE_SHORT, VIBRATION_LONG, PAUSE_SHORT, VIBRATION_LONG, PAUSE_SHORT, VIBRATION_LONG, PAUSE_LONG});

        signals.put("1", new long[]{PAUSE_LONG, VIBRATION_SHORT, PAUSE_LONG});
        signals.put("2", new long[]{PAUSE_LONG, VIBRATION_SHORT, PAUSE_SHORT, VIBRATION_SHORT, PAUSE_LONG});
        signals.put("3", new long[]{PAUSE_LONG, VIBRATION_SHORT, PAUSE_SHORT, VIBRATION_SHORT, PAUSE_SHORT, VIBRATION_SHORT, PAUSE_LONG});
        signals.put("4", new long[]{PAUSE_LONG, VIBRATION_SHORT, PAUSE_SHORT, VIBRATION_SHORT, PAUSE_SHORT, VIBRATION_SHORT, PAUSE_SHORT, VIBRATION_SHORT, PAUSE_LONG});
        signals.put("5", new long[]{PAUSE_LONG, VIBRATION_LONG, PAUSE_LONG});
        signals.put("6", new long[]{PAUSE_LONG, VIBRATION_LONG, PAUSE_SHORT, VIBRATION_LONG, PAUSE_LONG});
        signals.put("7", new long[]{PAUSE_LONG, VIBRATION_LONG, PAUSE_SHORT, VIBRATION_LONG, PAUSE_SHORT, VIBRATION_LONG, PAUSE_LONG});
        signals.put("8", new long[]{PAUSE_LONG, VIBRATION_LONG, PAUSE_SHORT, VIBRATION_LONG, PAUSE_SHORT, VIBRATION_LONG, PAUSE_SHORT, VIBRATION_LONG, PAUSE_LONG});

        Log.i("Info", "Map size after: " + signals.size());
    }

    //Hacky, but it works
    private long[] getSignalPattern(byte[] bytes){

        int size1 = signals.get(""+(char)bytes[0]).length;
        int size2 = signals.get(""+(char)bytes[1]).length;
        int size3 = signals.get(""+(char)bytes[2]).length;
        int size4 = signals.get(""+(char)bytes[3]).length;

        long[] array = new long[size1 + size2 + size3 + size4];

        for(int i = 0; i < size1;i++ ){
            array[i] = signals.get(""+(char)bytes[0])[i];
        }

        for(int i = 0; i < size2;i++ ){
            array[size1 +i -1] = signals.get(""+(char)bytes[1])[i];
        }

        for(int i = 0; i < size3;i++ ){
            array[size1 + size2 + i -2] = signals.get(""+(char)bytes[2])[i];
        }

        for(int i = 0; i < size4;i++ ){
            array[size1 + size2 + size3 + i - 3] = signals.get(""+(char)bytes[3])[i];
        }

        return array;
    }
}
