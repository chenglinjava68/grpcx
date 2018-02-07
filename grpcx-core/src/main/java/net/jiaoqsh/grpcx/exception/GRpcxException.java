package net.jiaoqsh.grpcx.exception;

/**
 * @file: GRpcxException
 * @author: jiaoqsh
 * @since: 2018/02/07
 */
public class GRpcxException extends RuntimeException {

    public GRpcxException() {
        super();
    }

    public GRpcxException(String message) {
        super(message);
    }

    public GRpcxException(String message, Throwable cause) {
        super(message, cause);
    }

    public GRpcxException(Throwable cause) {
        super(cause);
    }
}
