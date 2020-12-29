package com.example.arudino_bluetooth;


import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.UUID;


import android.bluetooth.BluetoothAdapter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;


import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

public class PurposeActivity extends AppCompatActivity {
    private static final String TAG = "bluetooth2";

    TextView tvSmokeCount, tvRestCount, tvGoalCount, textView;
    Button btnSmoke, btnCharge, btnReset, btnClose;
    LineChart lineChart;

    LineDataSet dataSet;
    LineData lineData;

    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private ConnectedThread mConnectedThread;


    private final String DEVICE_ADDRESS = "98:D3:C1:FD:3F:87"; //MAC Address of Bluetooth Module
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    SharedPreferences pref;

    ArrayList<String> labels = new ArrayList<>();
    ArrayList<Entry> entries = new ArrayList<>(Arrays.asList(new Entry[24]));

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purpose);

        pref = getSharedPreferences("pref",MODE_PRIVATE);

        btnSmoke = findViewById(R.id.btn_smoke);
        btnCharge = findViewById(R.id.btn_charge);
        btnReset = findViewById(R.id.btn_reset);
        btnClose = findViewById(R.id.btn_close);
        lineChart = findViewById(R.id.lineChart);
        tvSmokeCount = findViewById(R.id.tv_smoke_count);
        tvRestCount = findViewById(R.id.tv_rest_count);
        tvGoalCount = findViewById(R.id.tv_goal_count);
        textView = findViewById(R.id.textView2);

        for(int i = 0; i < 24; i++){
            labels.add(i + "시");
        }

        tvSmokeCount.setText(pref.getString("smoke_count","0"));
        tvRestCount.setText(pref.getString("rest_count","0"));
        tvGoalCount.setText(pref.getString("goal_count","0"));

        //entries 데이터들을 sharedPreference 에서 받아온다.
        //가져올 때 시간 데이터를 가져오는데 이 데이터들 초기화.

        for(int i = 0; i < 24; i++){
            float count = Float.parseFloat(pref.getString(String.valueOf(i),"0"));
            entries.set(i,new Entry(count, i));
        }

        dataSet = new LineDataSet(entries,"시간당 핀 개수");
        dataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> String.valueOf((int) value));
        lineData = new LineData(labels, dataSet);
        lineChart.setData(lineData);
        lineChart.setDescription("");

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setAxisMaxValue(30);
        yAxisLeft.setAxisMinValue(0);

        YAxis yAxisRight = lineChart.getAxisRight(); // y축의 오른쪽 면 설정
        yAxisRight.setDrawLabels(false);
        yAxisRight.setDrawAxisLine(false);
        yAxisRight.setDrawGridLines(false);

        btnSmoke.setOnClickListener(v->{
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);

            int smokeCount = Integer.parseInt(tvSmokeCount.getText().toString());
            int restCount = Integer.parseInt(tvRestCount.getText().toString());
            int goalCount = Integer.parseInt(pref.getString("goal_count","0"));

            Log.d("goalCount", String.valueOf(goalCount));

            //예외처리
            if(restCount == 0){
                Toast.makeText(getApplicationContext(),"필 수 있는 담배가 부족합니다.",Toast.LENGTH_SHORT);
            } else if(smokeCount > goalCount || smokeCount == goalCount){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("위험, 더 이상 필 수 없습니다.");
                builder.setView(getLayoutInflater().inflate(R.layout.dialog_custom, null));
                builder.setPositiveButton("확인", (dialogInterface, i) -> {});

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else {
                mConnectedThread.write("6");
                tvSmokeCount.setText(String.valueOf(smokeCount + 1));
                tvRestCount.setText(String.valueOf(restCount + -1));
                entries.set(hour, new Entry(entries.get(hour).getVal() + 1, hour));
                refreshGraph();
            }
        });

        btnClose.setOnClickListener(v -> {
            mConnectedThread.write("8");
        });

        btnCharge.setOnClickListener(v -> {
            mConnectedThread.write("7");
            int restCount = Integer.parseInt(tvRestCount.getText().toString());
            tvRestCount.setText(String.valueOf(restCount + 1));
        });

        btnReset.setOnClickListener(v ->{
            SharedPreferences.Editor editor = pref.edit();

            for(int i = 0; i < 24; i++){
                editor.remove(String.valueOf(i));
            }

            editor.remove("smoke_count");

            tvSmokeCount.setText("0");

            ArrayList<Entry> tempEntries = new ArrayList<>();

            for(int i = 0; i < 24; i++){
                tempEntries.add(i, new Entry(0,i));
            }

            entries = tempEntries;
            dataSet = new LineDataSet(entries, "시간당 핀 개수");
            lineData = new LineData(labels, dataSet);
            lineChart.setData(lineData);

            refreshGraph();

            editor.apply();
        });
    }

    @Override
    protected void onStart(){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        //checkBtSate();

        super.onStart();
    }

    private void refreshGraph(){
        dataSet = new LineDataSet(entries, "시간당 핀 개수");
        dataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> String.valueOf((int) value));
        lineData = new LineData(labels, dataSet);
        lineChart.setData(lineData);
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException{
        try{
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, PORT_UUID);
        } catch (Exception e){
            Log.d(TAG, "Could not create Insecure RFComm Connection", e);
        }
        return device.createRfcommSocketToServiceRecord(PORT_UUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        tvSmokeCount.setText(pref.getString("smoke_count", "0"));
        tvRestCount.setText(pref.getString("rest_count", "0"));
        tvGoalCount.setText(pref.getString("goal_count", "0"));

        //entries 데이터들을 sharedPreference 에서 받아온다.
        //가져올 때 시간 데이터를 가져오는데 이 데이터들 초기화.
        for (int i = 0; i < 24; i++) {
            float count = Float.parseFloat(pref.getString(String.valueOf(i), "0"));
            entries.set(i, new Entry(count, i));
        }

        BluetoothDevice device = btAdapter.getRemoteDevice(DEVICE_ADDRESS);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
        }

        btAdapter.cancelDiscovery();

        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btnCharge.setEnabled(false);
                btnSmoke.setEnabled(false);
                textView.setText("아두이노의 블루투스 통신 모듈을 확인해 주세요.");
                Toast.makeText(getApplicationContext(), "아두이노의 블루투스 통신 모듈을 확인해 주세요", Toast.LENGTH_SHORT).show();

                errorExit("오류", "아두이노와 블루투스 연동에 실패했습니다.");
                btSocket.close();
            } catch (IOException e2) {
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause(){
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        SharedPreferences.Editor editor = pref.edit();

        //sharedPreferences 에 저장
        editor.putString("smoke_count", tvSmokeCount.getText().toString());
        editor.putString("rest_count", tvRestCount.getText().toString());
        editor.putString("goal_count", tvGoalCount.getText().toString());

        for(int i = 0; i < 24; i++){
            editor.putString(String.valueOf(i), String.valueOf(entries.get(i).getVal())).apply();
        }

        try{
            btSocket.close();
        }catch (IOException e2){
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTSate(){
        // 블루투스 사용 가능한가?
        // 불가능하면 null값 반환

        if (btAdapter == null){
            TextView textView = findViewById(R.id.textView2);

            btnCharge.setEnabled(false);
            btnSmoke.setEnabled(false);

            textView.setText("블루투스가 불가능한 기종입니다. 다른 기기에서 실행해 주세요.");
            Toast.makeText(getApplicationContext(), "블루투스가 불가능한 기종입니다. 다른 기기에서 실행해주세요.", Toast.LENGTH_SHORT).show();
            errorExit("오류", "블루투스 불가 기종입니다.");
        }
        else{
            if(btAdapter.isEnabled()){
                Log.d(TAG, "...Bluetooth On...");
            }else{
                //사용자에게 블루투스를 키라는  intent.
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent,1);
                errorExit("오류", "블루투스를 키고 실행해주세요.");
            }
        }
    }
    private void errorExit(String title, String message){
        Toast.makeText(getApplicationContext(), title + " - "+ message, Toast.LENGTH_LONG).show();
    }

    private class ConnectedThread extends Thread{
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            OutputStream tmpOut = null;

            // Temp 개체를 사용하여 출력 스트림 가져오기

            try{
                tmpOut = socket.getOutputStream();
            } catch (IOException e){
            }

            mmOutStream = tmpOut;
        }

        //데이터를 전송하려면 activity에서 이 메소드 호출

        void write(String message){
            Log.d(TAG, "...Data to send: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try{
                mmOutStream.write(msgBuffer);
            } catch (IOException e){
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
            }
        }

    }
}