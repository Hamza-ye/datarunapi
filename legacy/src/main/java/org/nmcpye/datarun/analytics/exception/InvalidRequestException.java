package org.nmcpye.datarun.analytics.exception;

import org.nmcpye.datarun.sharedkernal.exceptions.IllegalQueryException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Hamza Assada
 * @since 29/08/2025
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidRequestException extends IllegalQueryException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
