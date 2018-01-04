package com.guoguang.wifi.utils;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangjr on 2017/11/30.
 */

public class WifiAdmin {
    // 定义WifiManager对象
    private WifiManager mWifiManager;
    // 定义WifiInfo对象
    private WifiInfo mWifiInfo;
    // 扫描出的网络连接列表
    private List<ScanResult> mWifiList;
    // 网络连接列表
    private List<WifiConfiguration> mWifiConfiguration;
    // 定义一个WifiLock
    WifiManager.WifiLock mWifiLock;

    private static final String TAG="WIFIAdmin";

    // 构造器
    public WifiAdmin(Context context) {
        // 取得WifiManager对象
        mWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        // 取得WifiInfo对象
        mWifiInfo = mWifiManager.getConnectionInfo();

    }

    // 打开WIFI
    public void openWifi(Context context) {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
            Toast.makeText(context,"打开成功", Toast.LENGTH_SHORT).show();
        }else if (mWifiManager.getWifiState() == 2) {
            Toast.makeText(context,"亲，Wifi正在开启，不用再开了", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context,"亲，Wifi已经开启,不用再开了", Toast.LENGTH_SHORT).show();
        }
    }

    // 关闭WIFI
    public void closeWifi(Context context) {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
            Toast.makeText(context,"关闭成功", Toast.LENGTH_SHORT).show();
        }else if(mWifiManager.getWifiState() == 1){
            Toast.makeText(context,"亲，Wifi已经关闭，不用再关了", Toast.LENGTH_SHORT).show();
        }else if (mWifiManager.getWifiState() == 0) {
            Toast.makeText(context,"亲，Wifi正在关闭，不用再关了", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context,"请重新关闭", Toast.LENGTH_SHORT).show();
        }
    }

    // 检查当前WIFI状态
    public String checkState(Context context) {
        String state="没有获取到WiFi状态";
        if (mWifiManager.getWifiState() == 0) {
            state="Wifi正在关闭";
        } else if (mWifiManager.getWifiState() == 1) {
            state="Wifi已经关闭";
        } else if (mWifiManager.getWifiState() == 2) {
            state="Wifi正在开启";
        } else if (mWifiManager.getWifiState() == 3) {
            state="Wifi已经开启";
        }
        return state;
    }

    // 锁定WifiLock
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    // 解锁WifiLock
    public void releaseWifiLock() {
        // 判断时候锁定
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    // 创建一个WifiLock
    public void creatWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("Test");
    }

    // 得到配置好的网络
    public List<WifiConfiguration> getConfiguration() {
        return mWifiConfiguration;
    }

    // 指定配置好的网络进行连接
    public void connectConfiguration(int index) {
        // 索引大于配置好的网络索引返回
        if (index > mWifiConfiguration.size()) {
            return;
        }
        // 连接配置好的指定ID的网络
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId,
                true);
    }

    //扫描wifi
    public void startScan(Context context) {
        mWifiManager.startScan();
        //得到扫描结果
        List<ScanResult> results = mWifiManager.getScanResults();
        // 得到配置好的网络连接
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();
        if (results == null) {
            if(mWifiManager.getWifiState()==3){
                Toast.makeText(context,"当前区域没有无线网络",Toast.LENGTH_SHORT).show();
            }else if(mWifiManager.getWifiState()==2){
                Toast.makeText(context,"wifi正在开启，请稍后扫描", Toast.LENGTH_SHORT).show();
            }else{Toast.makeText(context,"WiFi没有开启", Toast.LENGTH_SHORT).show();
            }
        } else {
            mWifiList = new ArrayList();
            for(ScanResult result : results){
                if (result.SSID == null || result.SSID.length() == 0 || result.capabilities.contains("[IBSS]")||result.SSID.equals(mWifiManager.getConnectionInfo().getSSID())) {
                    continue;
                }
                boolean found = false;
                for(ScanResult item:mWifiList){
                    if(item.SSID.equals(result.SSID)&&item.capabilities.equals(result.capabilities)){
                        found = true;break;
                    }
                }
                if(!found){
                    mWifiList.add(result);
                }
            }
        }
    }

    // 得到网络列表
    public List<ScanResult> getWifiList() {
        return mWifiList;
    }

    // 查看扫描结果
    public StringBuilder lookUpScan() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mWifiList.size(); i++) {
            stringBuilder
                    .append("Index_" + new Integer(i + 1).toString() + ":");
            // 将ScanResult信息转换成一个字符串包
            // 其中把包括：BSSID、SSID、capabilities、frequency、level
            stringBuilder.append((mWifiList.get(i)).toString());
            stringBuilder.append("/n");
        }
        return stringBuilder;
    }

    // 得到MAC地址
    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    // 得到接入点的BSSID
    public String getBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    public String getSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
    }


    // 得到IP地址
    public int getIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    // 得到连接的ID
    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    // 得到WifiInfo的所有信息包
    public String getWifiInfo() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }

    public int getWifiState(){
        return mWifiManager.getWifiState();
    }


    // 添加一个网络并连接
    public boolean addNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        boolean b =  mWifiManager.enableNetwork(wcgID, true);
        return b;
    }

    // 断开指定ID的网络
    public void disconnectWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }
    public void removeWifi(int netId) {
        disconnectWifi(netId);
        mWifiManager.removeNetwork(netId);
    }


    //IP转换
    public String ipIntToString(int ip) {
        try {
            byte[] bytes = new byte[4];
            bytes[0] = (byte) (0xff & ip);
            bytes[1] = (byte) ((0xff00 & ip) >> 8);
            bytes[2] = (byte) ((0xff0000 & ip) >> 16);
            bytes[3] = (byte) ((0xff000000 & ip) >> 24);
            return Inet4Address.getByAddress(bytes).getHostAddress();
        } catch (Exception e) {
            return "";
        }
    }

    //配置wifi，无密码，WEP、WPA密码
    public WifiConfiguration createWifiInfo(String SSID, String Password, int Type)
    {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        WifiConfiguration temConfig=this.isExsits(SSID);
        if(temConfig!=null){
            mWifiManager.removeNetwork(temConfig.networkId);
        }

        if(Type == 1) //WIFICIPHER_NOPASS
        {
            //config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            //config.wepTxKeyIndex = 0;
        }
        if(Type == 2) //WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0]= "\""+Password+"\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if(Type == 3) //WIFICIPHER_WPA_psk
        {
            config.preSharedKey = "\""+Password+"\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    //判断是否已保存配置
    private WifiConfiguration isExsits(String SSID)
    {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs)
        {
            if (existingConfig.SSID.equals("\""+SSID+"\""))
            {
                return existingConfig;
            }
        }
        return null;
    }

    //保存配置
    public void saveConnectedConf(){
        mWifiManager.saveConfiguration();
    }


    /**
     * 更新wifiInfo
     */
    public void updateWifiInfo(){
        mWifiInfo=mWifiManager.getConnectionInfo();
    }

    /**
     * 显示连接信息
     * @return
     */
    public String initShowConn() {

//		//打印wifiInfo
//		StringBuffer sb = new StringBuffer();
//		sb.append("wifiInfo.getBSSID()::"+wifiInfo.getBSSID())
//		.append("\ngetHiddenSSID()::"+wifiInfo.getHiddenSSID())
//		.append("\ngetIpAddress()::"+wifiInfo.getIpAddress())
//		.append("\ngetLinkSpeed()::"+wifiInfo.getLinkSpeed())
//		.append("\ngetNetworkId()::"+wifiInfo.getNetworkId())
//		.append("\ngetRssi()::"+wifiInfo.getRssi())
//		.append("\ngetSSID()::"+wifiInfo.getSSID())
//		.append("\ndescribeContents()::"+wifiInfo.describeContents());
//		System.out.println(sb.toString());
        String s = mWifiInfo.getSSID() + "    IP地址:" + ipIntToString(mWifiInfo.getIpAddress()) + "    Mac地址：" + mWifiInfo.getMacAddress();
        return s;
    }

    /**
     * wifi连接
     * @param SSID ssid
     * @param pwd 密码
     * @param type 加密类型
     * @return
     */
    public boolean wifiConnect(String SSID, String pwd, int type ){
        boolean flag=false;
        final WifiConfiguration wifiConfiguration=isExsits(SSID);
        if(type!=1){
            if(wifiConfiguration==null){
                flag=addNetwork(createWifiInfo(SSID, pwd, type));
            }else {
                flag=mWifiManager.enableNetwork(wifiConfiguration.networkId, true);
            }
        }else {
            flag=addNetwork(createWifiInfo(SSID, pwd, type));
        }
        Log.d(TAG,"flag="+flag);
        if(flag==true){
            saveConnectedConf();
        }

        return flag;
    }



    /**
     * 筛选wifi列表（不含已连接的wifi）
     * @return
     */
    public List<ScanResult> selectWifiList(){
        int wifiState=mWifiManager.getWifiState();
        if(wifiState==WifiManager.WIFI_STATE_ENABLED){
            updateWifiInfo();
            for(ScanResult result:mWifiList){
                String connectedSSID=mWifiInfo.getSSID();
                String subConnectedSSID=connectedSSID.substring(1,connectedSSID.length()-1);
                if(subConnectedSSID.equals(result.SSID)){
                    mWifiList.remove(result);
                    break;
                }
            }
        }
        return mWifiList;
    }

    /**
     * 断开已连接wifi
     * @param ssid
     */
    public void removeConnectedWifi(String ssid) {
        List<WifiConfiguration> configurations = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration configuration : configurations) {
            //configuration.SSID.equals("\"" + ssid + "\"")
            Log.d(TAG,"configuration.SSID="+configuration.SSID);
            Log.d(TAG,"configuration  SSID="+ssid);
            if (configuration.SSID.equals( ssid )) {
                mWifiManager.removeNetwork(configuration.networkId);
            }
        }
    }



}


