package org.nmcpye.datarun.party.exceptions;

import org.nmcpye.datarun.sharedkernal.exceptions.IllegalQueryException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DeletionConflictException extends IllegalQueryException {
    public DeletionConflictException(String message) {
        super(message);
    }
}
