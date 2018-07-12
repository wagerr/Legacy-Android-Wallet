package global;

/**
 * Created by furszy on 6/4/17.
 */

public interface WalletConfiguration {


    String getTrustedNodeHost();

    void saveTrustedNode(String host,int port);

    void saveScheduleBlockchainService(long time);

    long getScheduledBLockchainService();


}
