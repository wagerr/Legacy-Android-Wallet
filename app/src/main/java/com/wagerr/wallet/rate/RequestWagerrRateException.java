package com.wagerr.wallet.rate;

/**
 * Created by furszy on 7/5/17.
 */
public class RequestWagerrRateException extends Exception {
    public RequestWagerrRateException(String message) {
        super(message);
    }

    public RequestWagerrRateException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestWagerrRateException(Exception e) {
        super(e);
    }
}
