package org.nmcpye.datarun.party.exceptions;

import org.nmcpye.datarun.sharedkernal.exceptions.IllegalQueryException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundObjectException extends IllegalQueryException {
    public NotFoundObjectException(String message) {
        super(message);
    }
}
