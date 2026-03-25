package org.nmcpye.datarun.sharedkernal.exceptions;

import org.nmcpye.datarun.sharedkernal.feedback.ErrorCode;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorMessage;

/**
 * Exception representing an illegal query.
 *
 * @author Hamza Assada 17/03/2025 (7amza.it@gmail.com)
 */
public class IllegalQueryException
    extends ErrorCodeException {
    /**
     * Constructor.
     *
     * @param message the exception message.
     */
    public IllegalQueryException(String message) {
        super(message);
    }

    /**
     * Constructor. Sets the message based on the error code message.
     *
     * @param errorCode the {@link ErrorCode}.
     */
    public IllegalQueryException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Constructor. Sets the message and error code based on the error message.
     *
     * @param errorMessage the {@link ErrorMessage}.
     */
    public IllegalQueryException(ErrorMessage errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructor. Sets the message based on the error code and arguments.
     *
     * @param errorCode the {@link ErrorCode}.
     * @param args      the message format arguments.
     */
    public IllegalQueryException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}
