package global;

import com.google.common.util.concurrent.ListenableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wagerrj.core.Address;
import org.wagerrj.core.Coin;
import org.wagerrj.core.InsufficientMoneyException;
import org.wagerrj.core.NetworkParameters;
import org.wagerrj.core.Peer;
import org.wagerrj.core.ScriptException;
import org.wagerrj.core.Sha256Hash;
import org.wagerrj.core.StoredBlock;
import org.wagerrj.core.Transaction;
import org.wagerrj.core.TransactionConfidence;
import org.wagerrj.core.TransactionInput;
import org.wagerrj.core.TransactionOutput;
import org.wagerrj.core.listeners.TransactionConfidenceEventListener;
import org.wagerrj.crypto.DeterministicKey;
import org.wagerrj.crypto.MnemonicException;
import org.wagerrj.script.Script;
import org.wagerrj.wallet.DeterministicKeyChain;
import org.wagerrj.wallet.SendRequest;
import org.wagerrj.wallet.Wallet;
import org.wagerrj.wallet.listeners.WalletCoinsReceivedEventListener;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import chain.BlockchainManager;
import global.exceptions.CantSweepBalanceException;
import global.exceptions.ContactAlreadyExistException;
import global.exceptions.NoPeerConnectedException;
import global.exceptions.UpgradeException;
import global.store.ContactsStoreDao;
import global.store.RateDbDao;
import global.wagerr.DefaultCoinSelector;
import global.wrappers.InputWrapper;
import global.wrappers.TransactionWrapper;
import wagerr.bet.BetActionKt;
import wagerr.bet.BetManager;
import wallet.WalletManager;
import wallet.exceptions.InsufficientInputsException;
import wallet.exceptions.TxNotFoundException;

import static global.WagerrCoreContext.NETWORK_PARAMETERS;

/**
 * Created by mati on 18/04/17.
 */

public class WagerrModuleImp implements WagerrModule {

    private static final Logger logger = LoggerFactory.getLogger(WagerrModuleImp.class);

    private ContextWrapper context;
    private WalletConfiguration walletConfiguration;
    private WalletManager walletManager;
    private BlockchainManager blockchainManager;
    private ContactsStoreDao contactsStore;
    private RateDbDao rateDb;
    private BetManager betManager;

    // cached balance --> todo: check this..
    private long availableBalance = 0;
    private BigDecimal pivInUsdHardcoded = new BigDecimal("1.5");

    // OS dependent helper.
    private BackupHelper backupHelper;

    public WagerrModuleImp(ContextWrapper contextWrapper, WalletConfiguration walletConfiguration, ContactsStoreDao contactsStore, RateDbDao rateDb, BackupHelper backupHelper) {
        this.context = contextWrapper;
        this.walletConfiguration = walletConfiguration;
        this.contactsStore = contactsStore;
        this.rateDb = rateDb;
        this.backupHelper = backupHelper;
        walletManager = new WalletManager(contextWrapper,walletConfiguration);
        blockchainManager = new BlockchainManager(context,walletManager,walletConfiguration);
        this.betManager = new BetManager(walletManager);
    }

    public void start() throws IOException{
        walletManager.init();
    }

    // todo: clean this.
    //public void setPivtrumPeergroup(PivtrumPeergroup peergroup){
        //peergroup.setAddressStore(addressStore);
        //peergroup.setWalletManager(walletManager);
        //this.peergroup = peergroup;
    //}

    @Override
    public void createWallet() {

    }

    @Override
    public boolean backupWallet(File backupFile, String password) throws IOException {
        return walletManager.backupWallet(backupFile,password);
    }

    private boolean backupWallet(Wallet wallet,File backupFile, String password) throws IOException {
        return walletManager.backupWallet(wallet,backupFile,password);
    }

    @Override
    public void restoreWallet(File backupFile) throws IOException {
        // restore wallet and launch the restart of the blockchain...
        walletManager.restoreWalletFromProtobuf(backupFile);
    }

    @Override
    public void restoreWalletFromEncrypted(File file, String password) throws IOException {
        walletManager.restoreWalletFromEncrypted(file,password);
    }

    @Override
    public void restoreWallet(List<String> mnemonic, long timestamp,boolean bip44) throws IOException, MnemonicException {
        walletManager.restoreWalletFrom(mnemonic,timestamp,bip44);
    }

    @Override
    public void cleanupWallet() {
        walletManager.cleanupWallet();
    }

    @Override
    public boolean isWalletCreated() {
        return false;
    }

    @Override
    public Address getReceiveAddress() {
        return walletManager.getCurrentAddress();
    }

    @Override
    public boolean isTransactionRelatedToWatchedAddress(Transaction tx) {
        return walletManager.isTransactionOnlyRelatedToWatchedAddress(tx);
    }

    @Override
    public List<Transaction> getWatchedSpent() {
        return walletManager.getWatchedSpent();
    }

    @Override
    public List<Transaction> getMineSpent() {
        return walletManager.getMineSpent();
    }

    @Override
    public Address getFreshNewAddress(){
        return walletManager.newFreshReceiveAddress();
    }

    @Override
    public boolean isAddressUsed(Address address) {
        return walletManager.isAddressMine(address);
    }

    @Override
    public long getAvailableBalance() {
        return walletManager.getAvailableBalance().longValue();//availableBalance;
    }

    @Override
    public Coin getAvailableBalanceCoin() {
        return walletManager.getAvailableBalance();//availableBalance;
    }

    @Override
    public Coin getUnnavailableBalanceCoin() {
        return walletManager.getUnspensableBalance();
    }

    @Override
    public boolean isWalletWatchOnly(){
        return walletManager.isWatchOnly();
    }

    @Override
    public BigDecimal getAvailableBalanceLocale() {
        return pivInUsdHardcoded.multiply(new BigDecimal(availableBalance));
    }

    @Override
    public List<AddressLabel> getContacts(){
        return contactsStore.list();
    }

    @Override
    public AddressLabel getAddressLabel(String address) {
        return contactsStore.getContact(address);
    }

    @Override
    public void saveContact(AddressLabel addressLabel) throws ContactAlreadyExistException {
        if (contactsStore.getContact(addressLabel.getAddresses().get(0))!=null) throw new ContactAlreadyExistException();
        contactsStore.insert(addressLabel);
    }
    @Override
    public void saveContactIfNotExist(AddressLabel addressLabel){
        if (contactsStore.getContact(addressLabel.getAddresses().get(0))!=null) return;
        contactsStore.insert(addressLabel);
    }

    @Override
    public void deleteAddressLabel(AddressLabel data) {
        contactsStore.delete(data);
    }

    @Override
    public boolean chechAddress(String addressBase58) {
        boolean result = false;
        try {
            Address.fromBase58(NETWORK_PARAMETERS, addressBase58);
            result = true;
        }catch (Exception e){
            // nothing..
        }
        return result;
    }

    @Override
    public Transaction buildSendTx(String addressBase58, Coin amount, String memo,Address changeAddress) throws InsufficientMoneyException {
        return buildSendTx(addressBase58,amount,null,memo,changeAddress);
    }
    @Override
    public Transaction buildSendTx(String addressBase58, Coin amount,Coin feePerKb, String memo,Address changeAddress) throws InsufficientMoneyException{
        Address address = Address.fromBase58(WagerrCoreContext.NETWORK_PARAMETERS, addressBase58);

        SendRequest sendRequest = SendRequest.to(address,amount);
        sendRequest.memo = memo;
        sendRequest.signInputs = true;
        sendRequest.shuffleOutputs = false; // don't shuffle outputs to know the contact
        if (feePerKb!=null)
            sendRequest.feePerKb = feePerKb;
        if (changeAddress!=null){
            sendRequest.changeAddress = changeAddress;
        }
        walletManager.completeSend(sendRequest);

        return sendRequest.tx;
    }

    @Override
    public Transaction completeTx(Transaction transaction,Address changeAddress,Coin feePerKb) throws InsufficientMoneyException {
        SendRequest sendRequest = SendRequest.forTx(transaction);
        if (transaction.getInputs()!=null && !transaction.getInputs().isEmpty()){
            List<TransactionOutput> unspent = new ArrayList<>();
            for (TransactionInput input : transaction.getInputs()) {
                unspent.add(input.getConnectedOutput());
            }
            sendRequest.coinSelector = new DefaultCoinSelector(unspent);
        }
        sendRequest.signInputs = true;
        sendRequest.shuffleOutputs = false; // don't shuffle outputs to know the contact
        if (changeAddress!=null){
            sendRequest.changeAddress = changeAddress;
        }
        if (feePerKb!=null)
            sendRequest.feePerKb = feePerKb;
        //sendRequest.changeAddress -> add the change address with address that i know instead of give this job to the wallet.
        walletManager.completeSend(sendRequest);

        return sendRequest.tx;
    }

    public Transaction completeTx(Transaction transaction) throws InsufficientMoneyException{
        SendRequest sendRequest = SendRequest.forTx(transaction);
        sendRequest.changeAddress = null;
        sendRequest.signInputs = true;
        sendRequest.shuffleOutputs = false;
        walletManager.completeSend(sendRequest);
        //walletManager.getWallet().signTransaction(sendRequest);
        return sendRequest.tx;
    }

    @Override
    public Transaction completeTxWithCustomFee(Transaction transaction,Coin fee) throws InsufficientMoneyException{
        SendRequest sendRequest = SendRequest.forTx(transaction);
        sendRequest.signInputs = true;
        sendRequest.shuffleOutputs = false; // don't shuffle outputs to know the contact
        sendRequest.feePerKb = fee;
        sendRequest.changeAddress = walletManager.newFreshReceiveAddress();
        walletManager.completeSend(sendRequest);
        // if the fee is different to the custom fee and the tx size is lower than 1kb (1000 bytes in wagerr core)
        /*if(!sendRequest.tx.getFee().equals(fee) && sendRequest.tx.unsafeBitcoinSerialize().length<1000){
            // re acomodate outputs to include the selected fee
            List<TransactionOutput> oldOutputs = sendRequest.tx.getOutputs();
            sendRequest.tx.clearOutputs();
            for (TransactionOutput oldOutput : oldOutputs) {
                if (oldOutput.getScriptPubKey().getToAddress(getConf().getNetworkParams()).equals(sendRequest.changeAddress)){
                    //nothing
                }else {
                    sendRequest.tx.addOutput(oldOutput);
                }
            }


        }*/

        return sendRequest.tx;
    }

    @Override
    public Coin getUnspentValue(Sha256Hash parentTransactionHash, int index) {
        return walletManager.getUnspentValue(parentTransactionHash,index);
    }

    @Override
    public boolean isAnyPeerConnected() {
        List<Peer> peers = blockchainManager.getConnectedPeers();
        return (blockchainManager != null && peers != null) && !peers.isEmpty();
    }

    @Override
    public long getConnectedPeerHeight() {
        List<Peer> peers = blockchainManager.getConnectedPeers();
        if (blockchainManager!=null &&  peers!=null && !peers.isEmpty()){
            Peer peer = peers.get(0);
            if (peer!=null)
                return peer.getBestHeight();
            else
                return -1;
        }else
            return -1;
    }

    @Override
    public int getProtocolVersion() {
        return WagerrCoreContext.NETWORK_PARAMETERS.getProtocolVersionNum(NetworkParameters.ProtocolVersion.CURRENT);
    }

    @Override
    public void checkMnemonic(List<String> mnemonic) throws MnemonicException {
        walletManager.checkMnemonic(mnemonic);
    }

    @Override
    public WalletConfiguration getConf() {
        return walletConfiguration;
    }

    @Override
    public  TransactionWrapper getTxWrapper(String txId){
        Transaction transaction = getTx(Sha256Hash.wrap(txId));
        boolean isSendFromMe = walletManager.isMine(transaction);
        boolean isStaking = false;
        Map<Integer, AddressLabel> outputsLabeled = new HashMap<>();
        Map<Integer, AddressLabel> inputsLabeled = new HashMap<>();
        Address address = null;
        try {
            for (TransactionOutput transactionOutput : transaction.getOutputs()) {
                Script script = transactionOutput.getScriptPubKey();
                if (script.isSentToAddress() || script.isPayToScriptHash()) {
                    try {
                        address = script.getToAddress(WagerrCoreContext.NETWORK_PARAMETERS, true);
                        // if the tx is mine i know that the first output address is the sent and the second one is the change address
                        outputsLabeled.put(transactionOutput.getIndex(), contactsStore.getContact(address.toBase58()));
                    } catch (ScriptException e) {
                        logger.warn("unknown tx output, " + script.toString() + ", is tx coinbase: " + transaction.isCoinBase());
                        e.printStackTrace();
                    }
                } else if (script.isSentToRawPubKey()) {
                    // is the staking reward
                    address = script.getToAddress(WagerrCoreContext.NETWORK_PARAMETERS, true);
                    // if the tx is mine i know that the first output address is the sent and the second one is the change address
                    outputsLabeled.put(transactionOutput.getIndex(), contactsStore.getContact(address.toBase58()));
                } else {
                    logger.warn("unknown tx output, " + script.toString() + ", is tx coinbase: " + transaction.isCoinBase());
                }
            }

                    /*for (TransactionInput transactionInput : transaction.getInputs()) {
                        try {
                            address = transactionInput.getScriptSig().getToAddress(getConf().getNetworkParams());
                            // if the tx is mine i know that the first output address is the sent and the second one is the change address
                            inputsLabeled.put((int) transactionInput.getOutpoint().getIndex(), contactsStore.getAddressLabel(address.toBase58()));
                        }catch (ScriptException e){
                            e.printStackTrace();
                        }
                    }*/

        } catch (Exception e) {
            e.printStackTrace();
            //swallow this for now..
        }

        TransactionWrapper wrapper;
        if (transaction.isCoinStake()) {
            if (transaction.getOutputs().get(1).isMine(getWallet())) {
                wrapper = new TransactionWrapper(
                        transaction,
                        inputsLabeled,
                        outputsLabeled,
                        walletManager.getValueSentToMe(transaction),
                        TransactionWrapper.TransactionUse.STAKE
                );
            } else {
                //no masternode reward here
                wrapper = new TransactionWrapper(
                        transaction,
                        inputsLabeled,
                        outputsLabeled,
                        walletManager.getValueSentToMe(transaction),
                        TransactionWrapper.TransactionUse.BET_REWARD,
                        betManager.getBetFullDataByRewardTransaction(transaction)
                );
            }

        } else if (transaction.isZerocoinSpend()) {
            wrapper = new TransactionWrapper(
                    transaction,
                    inputsLabeled,
                    outputsLabeled,
                    isSendFromMe ? getValueSentFromMe(transaction, true) : walletManager.getValueSentToMe(transaction),
                    TransactionWrapper.TransactionUse.ZC_SPEND
            );

        } else if(BetActionKt.isBetAction(transaction)){
            wrapper = new TransactionWrapper(
                    transaction,
                    inputsLabeled,
                    outputsLabeled,
                    getValueSentFromMe(transaction, true),
                    TransactionWrapper.TransactionUse.BET_ACTION,
                    betManager.getBetActionDataByActionTransaction(transaction)
            );
        }else{
            wrapper = new TransactionWrapper(
                    transaction,
                    inputsLabeled,
                    outputsLabeled,
                    isSendFromMe ? getValueSentFromMe(transaction, true) : walletManager.getValueSentToMe(transaction),
                    isSendFromMe ? TransactionWrapper.TransactionUse.SENT_SINGLE : TransactionWrapper.TransactionUse.RECEIVE
            );
        }
        return wrapper;
    }

    @Override
    public List<TransactionWrapper> listTx() {
        List<TransactionWrapper> list = new ArrayList<>();
        for (Transaction transaction : walletManager.listTransactions()) {
            boolean isSendFromMe = walletManager.isMine(transaction);
            boolean isStaking = false;
            Map<Integer, AddressLabel> outputsLabeled = new HashMap<>();
            Map<Integer, AddressLabel> inputsLabeled = new HashMap<>();
            Address address = null;
            try {
                for (TransactionOutput transactionOutput : transaction.getOutputs()) {
                    Script script = transactionOutput.getScriptPubKey();
                    if (script.isSentToAddress() || script.isPayToScriptHash()) {
                        try {
                            address = script.getToAddress(WagerrCoreContext.NETWORK_PARAMETERS, true);
                            // if the tx is mine i know that the first output address is the sent and the second one is the change address
                            outputsLabeled.put(transactionOutput.getIndex(), contactsStore.getContact(address.toBase58()));
                        } catch (ScriptException e) {
                            logger.warn("unknown tx output, " + script.toString() + ", is tx coinbase: " + transaction.isCoinBase());
                            e.printStackTrace();
                        }
                    } else if (script.isSentToRawPubKey()) {
                        // is the staking reward
                        address = script.getToAddress(WagerrCoreContext.NETWORK_PARAMETERS, true);
                        // if the tx is mine i know that the first output address is the sent and the second one is the change address
                        outputsLabeled.put(transactionOutput.getIndex(), contactsStore.getContact(address.toBase58()));
                    } else {
                        logger.warn("unknown tx output, " + script.toString() + ", is tx coinbase: " + transaction.isCoinBase());
                    }
                }

                    /*for (TransactionInput transactionInput : transaction.getInputs()) {
                        try {
                            address = transactionInput.getScriptSig().getToAddress(getConf().getNetworkParams());
                            // if the tx is mine i know that the first output address is the sent and the second one is the change address
                            inputsLabeled.put((int) transactionInput.getOutpoint().getIndex(), contactsStore.getAddressLabel(address.toBase58()));
                        }catch (ScriptException e){
                            e.printStackTrace();
                        }
                    }*/

            } catch (Exception e) {
                e.printStackTrace();
                //swallow this for now..
            }

            TransactionWrapper wrapper;
            if (transaction.isCoinStake()) {
                if (transaction.getOutputs().get(1).isMine(getWallet())) {
                    wrapper = new TransactionWrapper(
                            transaction,
                            inputsLabeled,
                            outputsLabeled,
                            walletManager.getValueSentToMe(transaction),
                            TransactionWrapper.TransactionUse.STAKE
                    );
                } else {
                    //no masternode reward here
                    wrapper = new TransactionWrapper(
                            transaction,
                            inputsLabeled,
                            outputsLabeled,
                            walletManager.getValueSentToMe(transaction),
                            TransactionWrapper.TransactionUse.BET_REWARD,
                            betManager.getBetFullDataByRewardTransaction(transaction)
                    );
                }

            } else if (transaction.isZerocoinSpend()) {
                wrapper = new TransactionWrapper(
                        transaction,
                        inputsLabeled,
                        outputsLabeled,
                        isSendFromMe ? getValueSentFromMe(transaction, true) : walletManager.getValueSentToMe(transaction),
                        TransactionWrapper.TransactionUse.ZC_SPEND
                );

            } else if(BetActionKt.isBetAction(transaction)){
                wrapper = new TransactionWrapper(
                        transaction,
                        inputsLabeled,
                        outputsLabeled,
                        getValueSentFromMe(transaction, true),
                        TransactionWrapper.TransactionUse.BET_ACTION,
                        betManager.getBetActionDataByActionTransaction(transaction)
                );
            }else{
                wrapper = new TransactionWrapper(
                        transaction,
                        inputsLabeled,
                        outputsLabeled,
                        isSendFromMe ? getValueSentFromMe(transaction, true) : walletManager.getValueSentToMe(transaction),
                        isSendFromMe ? TransactionWrapper.TransactionUse.SENT_SINGLE : TransactionWrapper.TransactionUse.RECEIVE
                );
            }

            //no need to add oracle transations
            if (!walletManager.isTransactionOnlyRelatedToWatchedAddress(transaction)) {
                list.add(wrapper);
            }
        }
        return list;
    }

    @Override
    public Coin getValueSentFromMe(Transaction transaction, boolean excludeChangeAddress) {
        if (excludeChangeAddress){
            return transaction.getOutput(0).getValue();
        }else
            return walletManager.getValueSentFromMe(transaction);
    }

    @Override
    public void commitTx(Transaction transaction) {
        walletManager.commitTx(transaction);
    }

    @Override
    public List<Peer> listConnectedPeers() {
        return blockchainManager.listConnectedPeers();
    }

    @Override
    public int getChainHeight() {
        return blockchainManager.getChainHeadHeight();
    }

    @Override
    public StoredBlock getChainHead() {
        return blockchainManager.getChainHead();
    }

    public BlockchainManager getBlockchainManager() {
        return blockchainManager;
    }

    public void addCoinsReceivedEventListener(WalletCoinsReceivedEventListener coinReceiverListener) {
        walletManager.addCoinsReceivedEventListener(coinReceiverListener);
    }

    public void removeCoinsReceivedEventListener(WalletCoinsReceivedEventListener coinReceiverListener) {
        walletManager.removeCoinsReceivedEventListener(coinReceiverListener);
    }

    public void addOnTransactionConfidenceChange(TransactionConfidenceEventListener transactionConfidenceEventListener) {
        walletManager.addOnTransactionsConfidenceChange(transactionConfidenceEventListener);
    }

    public void removeTransactionsConfidenceChange(TransactionConfidenceEventListener transactionConfidenceEventListener) {
        walletManager.removeTransactionConfidenceChange(transactionConfidenceEventListener);
    }

    public WagerrRate getRate(String coin) {
        return rateDb.getRate(coin);
    }

    @Override
    public Wallet getWallet() {
        return walletManager.getWallet();
    }

    @Override
    public List<InputWrapper> listUnspentWrappers() {
        List<InputWrapper> inputWrappers = new ArrayList<>();
        for (TransactionOutput transactionOutput : walletManager.listUnspent()) {
            Address address = transactionOutput.getScriptPubKey().getToAddress(WagerrCoreContext.NETWORK_PARAMETERS,true);
            AddressLabel addressLabel = contactsStore.getContact(address.toBase58());
            inputWrappers.add(
                    new InputWrapper(
                            transactionOutput,
                            addressLabel
                    )

            );
        }
        return inputWrappers;
    }

    @Override
    public Set<InputWrapper> convertFrom(List<TransactionInput> list) throws TxNotFoundException {
        Set<InputWrapper> ret = new HashSet<>();
        for (TransactionInput input : list) {
            TransactionOutput transactionOutput = input.getConnectedOutput();
            if (transactionOutput==null){
                transactionOutput = getUnspent(input.getOutpoint().getHash(), (int) input.getOutpoint().getIndex());
            }
            Address address = transactionOutput.getScriptPubKey().getToAddress(WagerrCoreContext.NETWORK_PARAMETERS,true);
            AddressLabel addressLabel = contactsStore.getContact(address.toBase58());
            ret.add(
                    new InputWrapper(
                            transactionOutput,
                            addressLabel
                            )
            );
        }
        return ret;
    }

    @Override
    public Transaction getTx(Sha256Hash txId) {
        return walletManager.getTransaction(txId);
    }

    @Override
    public List<String> getMnemonic() {
        return walletManager.getMnemonic();
    }

    @Override
    public String getWatchingPubKey() {
        return walletManager.getExtPubKey();
    }

    @Override
    public DeterministicKey getWatchingKey() {
        return walletManager.getWatchingPubKey();
    }

    @Override
    public DeterministicKey getKeyPairForAddress(Address address) {
        return walletManager.getKeyPairForAddress(address);
    }

    @Override
    public TransactionOutput getUnspent(Sha256Hash parentTxHash, int index) throws TxNotFoundException {
        return walletManager.getUnspent(parentTxHash,index);
    }

    @Override
    public List<TransactionOutput> getRandomUnspentNotInListToFullCoins(List<TransactionInput> inputs, Coin amount) throws InsufficientInputsException {
        return walletManager.getRandomListUnspentNotInListToFullCoins(inputs,amount);
    }

   @Override
   public boolean isSyncWithNode() throws NoPeerConnectedException {
       boolean isSync = false;
       if (isAnyPeerConnected()) {
           long peerHeight = getConnectedPeerHeight();
           if (peerHeight!=-1){
               if (getChainHeight()+10>peerHeight) {
                   isSync = true;
               }
           }
       }else {
          throw new NoPeerConnectedException();
       }
       return isSync;
   }

    @Override
    public void watchOnlyMode(String xpub, DeterministicKeyChain.KeyChainType keyChainType) throws IOException {
        walletManager.watchOnlyMode(xpub,keyChainType);
    }

    @Override
    public boolean isBip32Wallet() {
        return walletManager.isBip32Wallet();
    }

    @Override
    public boolean sweepBalanceToNewSchema() throws InsufficientMoneyException, CantSweepBalanceException {
        try {
            logger.info("sweepBalanceToNewSchema");

            // backup the current wallet first
            File backupFileOld = backupHelper.determineBackupFile("old");
            backupWallet(backupFileOld,"");

            // new wallet
            Wallet newWallet = walletManager.generateRandomWallet();
            Address sweepAddress = newWallet.freshReceiveAddress();
            logger.info("sweep address: "+sweepAddress);
            // sweep old wallet balance
            Transaction transaction = walletManager.createCleanWalletTx(sweepAddress);
            logger.info("sweep tx: "+transaction);

            // backup the new wallet
            File backupFile = backupHelper.determineBackupFile("upgrade");
            backupWallet(newWallet,backupFile,"");

            // broadcast
            ListenableFuture<Transaction> future = blockchainManager.broadcastTransaction(transaction);
            transaction = future.get();
            logger.info("sweep done: "+future.isDone());

            // wait until the tx is confirmed with 2 blocks
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            ListenableFuture<TransactionConfidence> confidenceFuture = transaction.getConfidence().getDepthFuture(2,executorService);
            TransactionConfidence confidence = confidenceFuture.get();

            if (confidence.getDepthInBlocks()>1){
                logger.info("Upgrade wallet tx confidence accepted by the network");

            }else {
                logger.error("ERROR, Upgrade wallet tx confidence not accepted by the network {}",confidence);
            }
            // change wallet
            walletManager.replaceWallet(newWallet);
            return true;
        }catch (InterruptedException e) {
            e.printStackTrace();
            throw new CantSweepBalanceException(e.getMessage(),e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new CantSweepBalanceException(e.getMessage(),e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CantSweepBalanceException(e.getMessage(),e);
        }
    }

    @Override
    public boolean upgradeWallet(String upgradeCode) throws UpgradeException {
        try {
            return sweepBalanceToNewSchema();
        } catch (InsufficientMoneyException e) {
            e.printStackTrace();
            throw new UpgradeException(e.getMessage(),e);
        } catch (CantSweepBalanceException e) {
            throw new UpgradeException(e.getMessage(),e);
        }
    }

    @Override
    public List<WagerrRate> listRates() {
        return rateDb.list();
    }

    @Override
    public List<String> getAvailableMnemonicWordsList() {
        return walletManager.getAvailableMnemonicWordsList();
    }


    public void saveRate(WagerrRate wagerrRate){
        rateDb.insertOrUpdateIfExist(wagerrRate);
    }

    @Override
    public BetManager getBetManager() {
        return betManager;
    }
}
