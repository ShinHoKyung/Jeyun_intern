package com.example.jw.magnetic;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    DBHelper dbHelper;
    SQLiteDatabase db;
    TextView magX, magY, magZ, angle, result, wifi;
    Button bCount;

    static boolean flag = false, done = true;
    WifiManager wifiManager;
    WifiReciver reciver;
    List<ScanResult> ScanResult;
    MagClass magClass[] = new MagClass[361];

    SensorManager sensorManager;

    int blockCnt = 0;

    float[] rota = new float[9];
    float[] result_data = new float[3];
    float[] mag_data = new float[3];
    float[] acc_data = new float[3];

    int wifiresult = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
        }
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        magX = (TextView) findViewById(R.id.magX);
        magY = (TextView) findViewById(R.id.magY);
        magZ = (TextView) findViewById(R.id.magZ);
        angle = (TextView) findViewById(R.id.angle);
        result = (TextView) findViewById(R.id.result);
        wifi = (TextView) findViewById(R.id.wifi);
        bCount = (Button) findViewById(R.id.blockCnt);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        reciver = new WifiReciver();
        registerReceiver(reciver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        CalClass calClass = new CalClass(db);
        wifiresult = calClass.calWifi(new String[][]{{"00:26:66:a0:88:a2", "-41.0"},
                {"f8:32:e4:50:94:08", "-54.0"},
                {"64:e5:99:90:fe:30", "-61.0"},
                {"f8:32:e4:50:94:0c", "-69.0"},
                {"00:26:66:94:95:0c", "-78.0"},
                {"88:36:6c:24:09:a0", "-80.0"},
                {"02:07:88:e9:db:da", "-83.0"},
                {"00:08:52:2d:bb:c6", "-85.0"},
                {"06:07:88:e9:db:da", "-85.0"},
                {"88:36:6c:b0:eb:b0", "-85.0"}});
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(reciver);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(mSensorListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(reciver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        int delay = SensorManager.SENSOR_DELAY_UI;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(mSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), delay);
        sensorManager.registerListener(mSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), delay);
    }


    SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
                return;

            float[] v = event.values;

            switch (event.sensor.getType()) {
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mag_data = event.values.clone();

                    magX.setText("" + v[0]);
                    magY.setText("" + v[1]);
                    magZ.setText("" + v[2]);

                    if (flag == true) {
//                        Log.i("magnetic class", (Integer.parseInt(angle.getText().toString()) + 180) + " " + magClass[Integer.parseInt(angle.getText().toString()) + 180].getSize());
//                        if (magClass[Integer.parseInt(angle.getText().toString()) + 180].getSize() < 10)
//                            magClass[Integer.parseInt(angle.getText().toString()) + 180].addValue(v[0], v[1], v[2]);
                    }
                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    acc_data = event.values.clone();
                    break;
            }

            if (mag_data != null && acc_data != null) {
                SensorManager.getRotationMatrix(rota, null, acc_data, mag_data);
                SensorManager.getOrientation(rota, result_data);
                result_data[0] = (float) Math.toDegrees(result_data[0]);
                result_data[1] = (float) Math.toDegrees(result_data[1]);
                result_data[2] = (float) Math.toDegrees(result_data[2]);
                if (result_data[0] < 0)
                    result_data[0] += 360;

                angle.setText(result_data[0] + " " + result_data[1] + " " + result_data[2]);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    class CustomTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {
            if (strings[0].equals("measure")) {

//                for (int i = 0; i < 361; i++)
//                    magClass[i] = new MagClass();
//                while (!done) {
//                    int i, count = 0;
//                    for (i = 0; i < 360; i++) {
//                        if (magClass[i].getSize() == 10 && !magClass[i].isPrinted()) {
//                            magClass[i].setPrinted(true);
//                            count++;
//                            String temp = String.valueOf((int) Math.ceil(count / 360));
//                            result.setText(temp);
//                        } else if (magClass[i].getSize() < 10)
//                            continue;
//                    }
//                    if (count == 360)
//                        done = true;
//                }

                flag = false;
            } else if (strings[0].equals("Msave")) {
//                for (int i = 0; i < 360; i++) {
//                    String sql = "INSERT INTO mValue(magX, magY, magZ) VALUES (" + magClass[i].getValue()[0] + ", " + magClass[i].getValue()[1] + ", " + magClass[i].getValue()[2] + ");";
//                    db.execSQL(sql);
//                }
//                done = false;
                double x, y, z, t;
                x = Double.parseDouble(magX.getText().toString());
                y = Double.parseDouble(magY.getText().toString());
                z = Double.parseDouble(magZ.getText().toString());
                t = Math.sqrt(x * x + y * y + z * z);

                String sql = "INSERT INTO mValue(magX, magY, magZ, magT, blockNum) VALUES (" + magX.getText().toString() + ", " + magY.getText().toString() + ", " + magZ.getText().toString() + ", " + String.valueOf(t) + ", " + bCount.getText().toString() + ");";
                db.execSQL(sql);


                Log.i("sqlQuery", sql);
            } else if (strings[0].equals("Wsave")) {
                String temp = wifi.getText().toString();
                String sql = "";

                int i;
                for (i = 0; i < 9; i++) {
                    if (50 + 38 * i <= temp.length()) {
                        sql = "INSERT INTO wValue(macId, wifi, blockNum) VALUES ('" + temp.substring(22 + 38 * i, 39 + 38 * i) + "', " + temp.substring(47 + 38 * i, 50 + 38 * i) + ", " + String.valueOf(blockCnt) + ");";
                        db.execSQL(sql);
                        Log.i("sqlQuery", temp.substring(22 + (38 * i), 39 + (38 * i)) + " " + temp.substring(47 + (38 * i), 50 + (38 * i)));
                    } else
                        break;
                }
                if (i == 9) {
                    if (50 + 38 * i <= temp.length()) {
                        sql = "INSERT INTO wValue(macId, wifi, blockNum) VALUES ('" + temp.substring(365, 382) + "', " + temp.substring(390, 393) + ", " + String.valueOf(blockCnt) + ");";
                        db.execSQL(sql);
                        Log.i("sqlQuery", temp.substring(365, 382) + " " + temp.substring(390, 393));
                    }
                }
            }
            return null;
        }
    }

    public void onClickButton(View v) {
        switch (v.getId()) {
            case R.id.btnMeasure:
                CustomTask measureTask = new CustomTask();
                flag = true;
                done = false;
                result.setText("측정 중");
                wifiManager.startScan();
                wifi.setText("Start");
                measureTask.execute("measure");

                Log.i("asdasdasd", ""+wifiresult);

                break;
            case R.id.btnMsave:
                // if (done) {
                CustomTask msaveTask = new CustomTask();
                msaveTask.execute("Msave");
//                } else {
//                    Toast.makeText(this, "측정을 해야합니다", Toast.LENGTH_SHORT).show();
//                }

                break;
            case R.id.btnWsave:
                //unregisterReceiver(reciver);
                CustomTask wsaveTask = new CustomTask();
                wsaveTask.execute("Wsave");
                break;
            case R.id.blockCnt:
                blockCnt++;
                bCount.setText(String.valueOf(blockCnt));
                break;
        }
    }

    class WifiReciver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intnet) {
            ScanResult = wifiManager.getScanResults();
            wifi.setText("Current Wifi\n");
            for (int i = 0; i < ScanResult.size(); i++) {
                wifi.append((i + 1) + " .MAC : " + (ScanResult.get(i)).BSSID + " RRSI : " + (ScanResult.get(i)).level + "\n");
            }
            result.setText("측정 끝");
        }

    }
}
