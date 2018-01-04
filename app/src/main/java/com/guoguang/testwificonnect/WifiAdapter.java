package com.guoguang.testwificonnect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by wangjr on 2017/12/3.
 */

public class WifiAdapter extends BaseAdapter {
    private static final String TAG="WifiAdapter";

    LayoutInflater inflater;
    List<ScanResult> list;
    private TextView wifiSSID;
    private ImageView wifiLevel;
    public int level;

    public WifiAdapter(Context context, List<ScanResult> list){
        this.inflater=LayoutInflater.from(context);
        this.list=list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"ViewHolder","InflateParams"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View mView=null;
        mView=inflater.inflate(R.layout.wifi_list,null);
        wifiSSID=(TextView)mView.findViewById(R.id.wifi_ssid);
        wifiLevel=(ImageView)mView.findViewById(R.id.wifi_level);
        ScanResult scanResult=list.get(position);
        wifiSSID.setText(scanResult.SSID);
        Log.d(TAG,"ScanResult.SSID="+scanResult.SSID);

        level= WifiManager.calculateSignalLevel(scanResult.level,5);//信号强度
        setWifiImg(scanResult,level);
        return mView;
    }

    //选择信号对应的图标
    private void setWifiImg(ScanResult scanResult, int level){
        if(scanResult.capabilities.contains("WEP")||scanResult.capabilities.contains("PSK")||
                scanResult.capabilities.contains("EAP")){
                if(level<=0&&level>=-50){
                    wifiLevel.setImageResource(R.drawable.ic_wifi_lock_signal_1_light);
                }else if(level<-50&&level>=-70){
                    wifiLevel.setImageResource(R.drawable.ic_wifi_lock_signal_2_light);
                }else if(level<-70&&level>=-80){
                    wifiLevel.setImageResource(R.drawable.ic_wifi_lock_signal_3_light);
                }else {
                    wifiLevel.setImageResource(R.drawable.ic_wifi_lock_signal_4_light);
                }
        }else{
            if(level<=0&&level>=-50){
                wifiLevel.setImageResource(R.drawable.ic_wifi_signal_1_light);
            }else if(level<-50&&level>=-70){
                wifiLevel.setImageResource(R.drawable.ic_wifi_signal_2_light);
            }else if(level<-70&&level>=-80){
                wifiLevel.setImageResource(R.drawable.ic_wifi_signal_3_light);
            }else {
                wifiLevel.setImageResource(R.drawable.ic_wifi_signal_4_light);
            }

        }
    }
}
