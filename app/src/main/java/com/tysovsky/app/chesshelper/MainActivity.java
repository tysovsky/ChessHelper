package com.tysovsky.app.chesshelper;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
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

    Button btnConnect;
    Button btnSend;

    EditText etLetter1;
    EditText etNumber1;
    EditText etLetter2;
    EditText etNumber2;

    public byte[] msg;
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
                msg = bytes;
                vibrator.vibrate(signals.get("" + (char)bytes[1]), -1);
                //vibrator.vibrate(signals.get("" + (char)bytes[1]), -1);
                //vibrator.vibrate(signals.get("" + (char)bytes[2]), -1);
                //vibrator.vibrate(signals.get("" + (char)bytes[3]), -1);
                //handleSignal.run();
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
                break;
        }
    }

    private void initializeViews(){
        btnConnect = (Button)findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(this);
        btnSend = (Button)findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

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

    Thread handleSignal = new Thread(new Runnable() {
        @Override
        public void run() {
            switch (msg[0]){
                case 'A':
                    long[] pattern = {0, 1000, 1000};
                    vibrator.vibrate(pattern, -1);
            }
        }
    });

    private void setupSignals(){
        signals.put("A", new long[]{0, 3000, 1000});
        signals.put("B", new long[]{0, 3000, 1000, 1000, 1000});
        signals.put("C", new long[]{0, 3000, 1000, 1000, 1000, 1000, 1000});
        signals.put("D", new long[]{0, 3000, 1000, 1000, 1000, 1000, 1000, 1000, 1000});
        signals.put("E", new long[]{0, 3000, 3000});
        signals.put("F", new long[]{0, 3000, 3000, 1000, 3000});
        signals.put("G", new long[]{0, 3000, 3000, 1000, 3000, 1000, 3000});
        signals.put("H", new long[]{0, 3000, 3000, 1000, 3000, 1000, 3000, 1000, 3000});

        signals.put("1", new long[]{0, 3000, 1000});
        signals.put("2", new long[]{0, 3000, 1000, 1000, 1000});
        signals.put("3", new long[]{0, 3000, 1000, 1000, 1000, 1000, 1000});
        signals.put("4", new long[]{0, 3000, 1000, 1000, 1000, 1000, 1000, 1000, 1000});
        signals.put("5", new long[]{0, 3000, 3000});
        signals.put("6", new long[]{0, 3000, 3000, 1000, 3000});
        signals.put("7", new long[]{0, 3000, 3000, 1000, 3000, 1000, 3000});
        signals.put("8", new long[]{0, 3000, 3000, 1000, 3000, 1000, 3000, 1000, 3000});
    }
}
