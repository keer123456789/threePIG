package com.zrsy.threepig.Util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.admin.methods.response.PersonalListAccounts;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * 针对geth的personal接口的工具类
 */
@Component
public class EthereumUtil {
    @Value("${web3_url}")
    private String web3_url;

    @Value("${account_address}")
    private String from;

    /**
     * 创建新用户
     *
     * @param password 密码
     * @return address
     */
    public String createNewAccount(String password) {
        Admin web3j = Admin.build(new HttpService(web3_url));
        NewAccountIdentifier newAccountIdentifier = null;
        try {
            newAccountIdentifier = web3j.personalNewAccount(password).send();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return newAccountIdentifier.getAccountId();
    }

    /**
     * 获得系统内的所有用户地址
     * @return
     */
    public List<String> getAllAccount() {
        Admin web3j = Admin.build(new HttpService(web3_url));
        PersonalListAccounts listAccounts = null;
        try {
            listAccounts = web3j.personalListAccounts().send();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return listAccounts.getAccountIds();
    }

    /**
     * 解锁账户
     * @param address
     * @param password
     * @return
     */
    public Boolean UnlockAccount(String address,String password){
        Admin web3j = Admin.build(new HttpService(web3_url));
        PersonalUnlockAccount personalUnlockAccount=null;
        try {
            personalUnlockAccount=web3j.personalUnlockAccount(address,password).send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return personalUnlockAccount.accountUnlocked();
    }

    public Boolean UnlockAccount(){
        Admin web3j = Admin.build(new HttpService(web3_url));
        PersonalUnlockAccount personalUnlockAccount=null;
        try {
            personalUnlockAccount=web3j.personalUnlockAccount(from,"11111111").send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(personalUnlockAccount.accountUnlocked()==null){
            return true;
        }
        return personalUnlockAccount.accountUnlocked();
    }
    /**
     * 新账户转账100eth
     * @param to
     */
    public boolean sendTransaction(String to){
        Admin web3j = Admin.build(new HttpService(web3_url));
        UnlockAccount(from,"11111111");
        BigInteger value = Convert.toWei("100.0", Convert.Unit.ETHER).toBigInteger();
        Transaction transaction=  Transaction.createEtherTransaction(from,null,BigInteger.valueOf(1) ,BigInteger.valueOf(99999999),to,value);
        try {
            EthSendTransaction ethSendTransaction=web3j.personalSendTransaction(transaction,"11111111").send();
            String hash=ethSendTransaction.getTransactionHash();
            if(!hash.equals(null)){
                return true;
            }
            else{
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {

    }

}