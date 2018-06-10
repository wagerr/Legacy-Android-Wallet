package chain;

import org.wagerrj.core.PeerGroup;

import java.util.Set;

public interface BlockchainManagerListener {

    void peerGroupInitialized(PeerGroup peerGroup);

    void onBlockchainOff(Set<Impediment> impediments);

    void checkStart();

    void checkEnd();

}