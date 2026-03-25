package org.nmcpye.datarun.sharedkernal.repository;

import org.springframework.security.access.AccessDeniedException;

public class DeleteAccessDeniedException extends AccessDeniedException {

    public DeleteAccessDeniedException(String msg) {
        super(msg);
    }
}
