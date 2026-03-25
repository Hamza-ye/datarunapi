package org.nmcpye.datarun.sharedkernal.exceptions;

import org.nmcpye.datarun.sharedkernal.feedback.ErrorCode;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorMessage;

/**
 * Runtime exception which references an {@link ErrorCode}.
 *
 * @author Lars Helge Overland
 */
public class ErrorCodeException
    extends RuntimeException {
    private final ErrorCode errorCode;

    /**
     * Deprecated, use {@link ErrorCode} or {@ErrorMessage} constructors
     * instead.
     */
    public ErrorCodeException(String message) {
        super(message);
        this.errorCode = null;
    }

    /**
     * Constructor.
     *
     * @param errorCode the {@link ErrorCode}.
     */
    public ErrorCodeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * Constructor.
     *
     * @param errorMessage the {@link ErrorMessage}.
     */
    public ErrorCodeException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorCode = errorMessage.getErrorCode();
    }

    /**
     * Constructor.
     *
     * @param errorCode the {@link ErrorCode}.
     * @param args      the message format arguments.
     */
    public ErrorCodeException(ErrorCode errorCode, Object... args) {
        this(new ErrorMessage(errorCode, args));
    }

    /**
     * Returns the {@link ErrorCode} of the exception.
     *
     * @return the {@link ErrorCode} of the exception.
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
