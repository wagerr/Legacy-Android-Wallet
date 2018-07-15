package network;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by furszy on 7/2/17.
 */

public class PeerGlobalData {

    public static final String[] TRUSTED_TEST_NODES = new String[]{"35.227.74.136"};

    public static final String[] TRUSTED_NODES = new String[]{"46.166.148.3"};

    public static final List<PeerData> listTrustedHosts() {
        List<PeerData> list = new ArrayList<>();
        for (String trustedNode : TRUSTED_NODES) {
            list.add(new PeerData(trustedNode, 55002, 55552));
        }
        return list;
    }

    public static final List<PeerData> listTrustedTestHosts() {
        List<PeerData> list = new ArrayList<>();
        for (String trustedNode : TRUSTED_TEST_NODES) {
            list.add(new PeerData(trustedNode, 55004, 55552));
        }
        return list;
    }
}
