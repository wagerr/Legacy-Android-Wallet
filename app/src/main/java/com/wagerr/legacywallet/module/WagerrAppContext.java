package com.wagerr.legacywallet.module;

/**
 * Created by furszy on 6/4/17.
 */

public class WagerrAppContext {

    public static final String DEFAULT_RATE_COIN = "USD";
    public static final long RATE_UPDATE_TIME = 72000000;

    public static final String ENABLE_BIP44_APP_VERSION = "1.0.0";

    /** Wagerr android wallet repo first commit by LooorTor time */
    public static final long WAGERR_ANDROID_REPO_FIRST_COMMIT_BY_LOOOR_TOR_TIME = 1528762933;

    // report mail
    public static final String REPORT_EMAIL = "LooorTor@gmail.com";
    /** Subject line for manually reported issues. */
    public static final String REPORT_SUBJECT_ISSUE = "Reported issue";

    /** Donation address */
    public static final String DONATE_ADDRESS = "WZk3UecHPz48WJXM79oFX85PTme3EsbsJZ";


    /** Minimum memory */
    public static final int MEMORY_CLASS_LOWEND = 48;

    /** MAX TIME WAITED TO SAY THAT THE APP IS NOT SYNCHED ANYMORE.. in millis*/
    public static final long OUT_OF_SYNC_TIME = 60000; // 1 minute
}
