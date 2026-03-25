package org.nmcpye.datarun.sharedkernal.apiquery;

import org.nmcpye.datarun.sharedkernal.exceptions.ErrorCodeException;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorCode;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorMessage;

public class QueryRequestValidationException extends ErrorCodeException {

    public QueryRequestValidationException(ErrorMessage message) {
        super(message);
    }

    public QueryRequestValidationException(String message) {
        super(new ErrorMessage(ErrorCode.E2050, message));
    }
}

