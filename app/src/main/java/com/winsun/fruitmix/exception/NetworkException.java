package com.winsun.fruitmix.exception;

/**
 * Created by Administrator on 2017/8/30.
 */

public class NetworkException extends Exception {

    private int httpErrorCode;

    /**
     * Constructs a new {@code Exception} with the current stack trace and the
     * specified detail message.
     *
     * @param detailMessage the detail message for this exception.
     */
    public NetworkException(String detailMessage, int httpErrorCode) {
        super(detailMessage);
        this.httpErrorCode = httpErrorCode;
    }

    public int getHttpErrorCode() {
        return httpErrorCode;
    }
}
