package com.guoguang.testwificonnect;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.guoguang.wifi.utils.WifiAdmin;

import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    public static final String TAG = "MainActivity";
    private ToggleButton wifiBt;
    private TextView showConnectedWifi, wifiState;
    private ListView mlistView;
    protected WifiAdmin mWifiAdmin;
    private List<ScanResult> mWifiList;
    protected String ssid;
    private String tempSSID;
    SharedPreferences preferences;
    int type = 1;
    int i = 0;
    Context mContext;

    //更新wifi列表
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            List<ScanResult> mlist = (List<ScanResult>) msg.obj;
            mlistView.setAdapter(new WifiAdapter(mContext, mlist));

        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWifiAdmin = new WifiAdmin(MainActivity.this);

        mContext = MainActivity.this;
        preferences = getSharedPreferences("wifi_password", Context.MODE_PRIVATE);
        initView();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        registerReceiver(wifiChangeReceiver, filter);

        //判断初始化wifi状态
        boolean isConnected = mWifiAdmin.checkState(mContext).equals("Wifi已经开启");
        if (isConnected) {
            wifiBt.setChecked(true);
            wifiState.setText("打开");
            scanWifi(mContext);
        } else {
            wifiBt.setChecked(false);
            wifiState.setText("关闭");
        }

        //ToggleButton监听事件
        wifiBt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //打开wifi
                    Toast.makeText(mContext, "请稍等，wifi正在打开", Toast.LENGTH_SHORT).show();
                    mWifiAdmin.openWifi(mContext);
                    Thread scanThread = new FirstScanThread(mWifiAdmin);
                    scanThread.start();
                    wifiState.setText("打开");
                } else {
                    //关闭wifi
                    mWifiAdmin.closeWifi(mContext);
                    mlistView.setAdapter(null);
                    showConnectedWifi.setVisibility(View.GONE);
                    wifiState.setText("关闭");
                }
            }
        });


    }

    @Override
    protected void onResume() {
        ssid = mWifiAdmin.getSSID();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(wifiChangeReceiver);
        super.onDestroy();
    }

    /**
     * 控件初始化
     */
    private void initView() {
        mlistView = (ListView) findViewById(R.id.wifi_list);
        showConnectedWifi = (TextView) findViewById(R.id.wifi_show_connect);
        wifiBt = (ToggleButton) findViewById(R.id.wifi_bt);
        wifiState = (TextView) findViewById(R.id.wifi_state);

        showConnectedWifi.setOnClickListener(this);
        mlistView.setOnItemClickListener(this);

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //wifi列表点击连接事件
        final EditText password = new EditText(MainActivity.this);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        final ScanResult scanResult = mWifiList.get(position);
        tempSSID = scanResult.SSID;
        type = 1;
        //判断加密方式
        String capabilities = scanResult.capabilities;
        if (capabilities.contains("WEP")) {
            type = 2;
            password.setText("");
            alertDialog.setView(password);
        } else if (capabilities.contains("PSK")) {
            type = 3;
            password.setText("");
            alertDialog.setView(password);
        }
        alertDialog.setTitle(tempSSID);
        if (type != 1) {
            alertDialog.setMessage("请输入密码");
        } else {
            alertDialog.setMessage("是否进行连接？");
            password.setText("");
        }

        alertDialog.setPositiveButton("连接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ConnectivityManager connectMgr = (ConnectivityManager) mContext
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifiNetInfo = connectMgr
                        .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (wifiNetInfo.isConnected()) {
                    mWifiAdmin.updateWifiInfo();
                    ssid = mWifiAdmin.getSSID();
                    mWifiAdmin.removeConnectedWifi(ssid);
                }

                String pwd = password.getText().toString();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(tempSSID, pwd);   //保存密码
                editor.commit();

                //连接wifi
                boolean flag = mWifiAdmin.wifiConnect(tempSSID, pwd, type);
                ssid = tempSSID;
                Toast.makeText(mContext, "正在连接，请稍后", Toast.LENGTH_SHORT).show();
                if (flag == true) {
                    scanWifi(mContext);
                }

            }
        });
        alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.create();
        alertDialog.show();

    }

    @Override
    public void onClick(View v) {
        String state = mWifiAdmin.checkState(mContext);
        Log.d(TAG, "WIFI state=" + state);
        switch (v.getId()) {
            case R.id.wifi_show_connect:
                //已连接wifi删除提示框
                AlertDialog.Builder deleteDialog = new AlertDialog.Builder(mContext);
                deleteDialog.setMessage("是否删除该网络连接");
                deleteDialog.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mWifiAdmin.updateWifiInfo();
                        ssid = mWifiAdmin.getSSID();
                        Log.d(TAG, "ssid=" + ssid);
                        mWifiAdmin.removeConnectedWifi(ssid);
                        //mWifiAdmin.updateWifiInfo();
                        // mWifiAdmin.removeWifi(mWifiAdmin.getNetworkId());
                        // Log.d(TAG, "ID=" + mWifiAdmin.getNetworkId());
                        // mWifiAdmin.saveConnectedConf();
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.remove(ssid);
                        editor.commit();
                    }
                });
                deleteDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                deleteDialog.create();
                deleteDialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.updateWifi:
                scanWifi(mContext);
        }
        return true;
    }

    /**
     * wifi打开后延迟扫描线程
     */
    private class FirstScanThread extends Thread {
        WifiAdmin threadWifiAdmin;

        public FirstScanThread(WifiAdmin wifiAdmin) {
            threadWifiAdmin = wifiAdmin;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean isConnected = threadWifiAdmin.checkState(mContext).equals("Wifi已经开启");
            Log.d(TAG, "isConnected" + isConnected);
            if (isConnected) {
                threadWifiAdmin.startScan(mContext);
                mWifiList = threadWifiAdmin.selectWifiList();
                Log.d(TAG, "mWifiList" + mWifiList);
                if (mWifiList != null) {
                    Message message = new Message();
                    message.obj = mWifiList;
                    handler.sendMessage(message);
                }
            }

        }
    }

    /**
     * 扫描可连接wifi
     *
     * @param context
     * @return
     */
    private boolean scanWifi(Context context) {
        boolean isConnected = (mWifiAdmin.checkState(context).equals("Wifi已经开启"));
        Log.d(TAG, "isconnceted=" + isConnected);
        if (isConnected) {
            mWifiAdmin.startScan(context);
            mWifiList = mWifiAdmin.selectWifiList();
            Log.d(TAG, "mWifiList=" + mWifiList);
            if (mWifiList != null) {
                mlistView.setAdapter(new WifiAdapter(this, mWifiList));
            }
            return true;
        }
        return false;
    }

    /**
     * 扫描可连接wifi
     *
     * @param context
     * @return
     */
    private boolean scanWholeWifi(Context context) {
        boolean isConnected = (mWifiAdmin.checkState(context).equals("Wifi已经开启"));
        Log.d(TAG, "isconnceted=" + isConnected);
        if (isConnected) {
            mWifiAdmin.startScan(context);
            //mWifiList = mWifiAdmin.selectWifiList();
            Log.d(TAG, "mWifiList=" + mWifiList);
            if (mWifiList != null) {
                mlistView.setAdapter(new WifiAdapter(this, mWifiList));
            }
            return true;
        }
        return false;
    }


    /*private boolean scanWifi(Context context) {
        long startTime = System.currentTimeMillis();
        boolean isOutTime = false;
        boolean isConnected = (mWifiAdmin.checkState(context).equals("Wifi已经开启"));
        Log.d(TAG, "isConnected1=" + isConnected);
        while (!isConnected && !isOutTime) {
            long endTime = System.currentTimeMillis();
            Log.d(TAG, "isConnected2=" + isConnected);
            if (mWifiAdmin.checkState(context).equals("Wifi已经开启")) {
                isConnected = true;
                break;
            }
            if (endTime - startTime >= 5000) {
                isOutTime = true;
                break;
            }
        }
        Log.d(TAG, "isOutTime=" + isConnected);

        if (isOutTime) {
            Toast.makeText(mContext, "wifi连接失败，请连接wifi", Toast.LENGTH_SHORT).show();
        }

        if (isConnected) {
            mWifiAdmin.startScan(context);
            mWifiList = mWifiAdmin.selectWifiList();
            if (mWifiList != null) {
                mlistView.setAdapter(new WifiAdapter(this, mWifiList));
            }
            return true;
        }
        return false;
    }*/

    /**
     * wifi状态改变广播
     */
    private BroadcastReceiver wifiChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "WIFI state changed");
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                //获取联网状态的NetworkInfo对象
                NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (info != null) {
                    //如果当前的网络连接成功并且网络连接可用
                    if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                            //网络为wifi连接
                            mWifiAdmin.updateWifiInfo();
                            String connnectedInfo = mWifiAdmin.initShowConn();
                            showConnectedWifi.setText("已连接：" + connnectedInfo);
                            Toast.makeText(mContext, "WIFI状态：连接", Toast.LENGTH_SHORT).show();
                            showConnectedWifi.setVisibility(View.VISIBLE);
                            return;
                        }
                    } else {
                        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                            showConnectedWifi.setText("");
                            Toast.makeText(mContext, "WIFI状态：断开", Toast.LENGTH_SHORT).show();
                            showConnectedWifi.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }

            //密码出错广播
        if (intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                int wifiResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 123);
                if (wifiResult == WifiManager.ERROR_AUTHENTICATING) {
                    Toast.makeText(mContext, "身份验证出现问题，正尝试重新连接", Toast.LENGTH_SHORT).show();
                    mWifiAdmin.removeConnectedWifi("\"" + tempSSID + "\"");
                    scanWholeWifi(mContext);

                }
            }
        }
    };
}

