package org.nmcpye.datarun.web.errors;

public class PathUpdateException extends RuntimeException {

    public PathUpdateException(String message) {
        super(message);
    }

    public PathUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}

