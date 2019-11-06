package com.wagerr.legacywallet.utils;

import android.content.SharedPreferences;

import network.PeerData;

import static com.wagerr.legacywallet.module.WagerrAppContext.DEFAULT_RATE_COIN;

/**
 * Created by furszy on 6/8/17.
 */

public class AppConf extends Configurations {

    public static final String PREFERENCE_NAME = "app_conf";
    private static final String IS_APP_INIT = "is_app_init";
    private static final String PINCODE = "pincode";
    private static final String TRUSTED_NODE_HOST = "trusted_node_host";
    private static final String TRUSTED_NODE_TCP = "trusted_node_tcp";
    private static final String TRUSTED_NODE_SSL = "trusted_node_ssl";

    private static final String SELECTED_RATE_COIN = "selected_rate_coin";
    private static final String USER_HAS_BACKUP = "user_has_backup";
    private static final String LAST_BEST_CHAIN_BLOCK_TIME = "last_best_chain_block_time";
    private static final String SHOW_REPORT_ON_START = "show_report";


    public AppConf(SharedPreferences prefs) {
        super(prefs);
    }

    public void setAppInit(boolean v){
        save(IS_APP_INIT,v);
    }

    public boolean isAppInit(){
        return getBoolean(IS_APP_INIT,false);
    }


    public void savePincode(String pincode) {
        save(PINCODE,pincode);
    }

    public String getPincode(){
        return getString(PINCODE,null);
    }

    public void saveTrustedNode(PeerData peerData){
        save(TRUSTED_NODE_HOST, peerData.getHost());
        save(TRUSTED_NODE_TCP, peerData.getTcpPort());
        save(TRUSTED_NODE_SSL, peerData.getSslPort());
    }
    public PeerData getTrustedNode(){
        String host = getString(TRUSTED_NODE_HOST,null);
        if (host!=null){
            int tcp = getInt(TRUSTED_NODE_TCP,-1);
            int ssl = getInt(TRUSTED_NODE_TCP,-1);
            return new PeerData(host,tcp,ssl);
        }else
            return null;
    }

    public void setSelectedRateCoin(String coin){
        save(SELECTED_RATE_COIN,coin);
    }

    public String getSelectedRateCoin(){
        return getString(SELECTED_RATE_COIN,DEFAULT_RATE_COIN);
    }

    public boolean hasBackup() {
        return getBoolean(USER_HAS_BACKUP,false);
    }

    public void setHasBackup(boolean hasBackup){
        save(USER_HAS_BACKUP,hasBackup);
    }


    public void setLastBestChainBlockTime(long lastBestChainBlockTime) {
        save(LAST_BEST_CHAIN_BLOCK_TIME,lastBestChainBlockTime);
    }

    public long getLastBestChainBlockTime(){
        return getLong(LAST_BEST_CHAIN_BLOCK_TIME,0);
    }

    public void saveShowReportScreenOnStart(boolean flag) {
        save(SHOW_REPORT_ON_START,flag);
    }

    public boolean getShowReportOnStart(){
        return getBoolean(SHOW_REPORT_ON_START,false);
    }

}
