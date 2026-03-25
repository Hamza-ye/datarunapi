package org.nmcpye.datarun.party.exceptions;

import org.nmcpye.datarun.sharedkernal.exceptions.IllegalQueryException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class InvalidBindingException extends IllegalQueryException {
    public InvalidBindingException(String message) {
        super(message);
    }
}
