package global;

import java.util.ArrayList;
import java.util.List;

import pivtrum.PivtrumPeerData;

/**
 * Created by furszy on 7/2/17.
 */

public class PivtrumGlobalData {

    public static final String[] TRUSTED_TEST_NODES = new String[]{"35.189.104.248"};

    public static final String[] TRUSTED_NODES = new String[]{"46.166.148.3"};

    public static final List<PivtrumPeerData> listTrustedHosts() {
        List<PivtrumPeerData> list = new ArrayList<>();
        for (String trustedNode : TRUSTED_NODES) {
            list.add(new PivtrumPeerData(trustedNode, 55002, 55552));
        }
        return list;
    }

    public static final List<PivtrumPeerData> listTrustedTestHosts() {
        List<PivtrumPeerData> list = new ArrayList<>();
        for (String trustedNode : TRUSTED_TEST_NODES) {
            list.add(new PivtrumPeerData(trustedNode, 55004, 55552));
        }
        return list;
    }
}
