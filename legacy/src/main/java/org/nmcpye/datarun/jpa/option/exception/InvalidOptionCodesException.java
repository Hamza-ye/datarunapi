package org.nmcpye.datarun.jpa.option.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * @author Hamza Assada
 * @since 14/08/2025
 */
@Getter
public class InvalidOptionCodesException extends RuntimeException {
    List<String> missingCodes;

    public InvalidOptionCodesException(List<String> missingCodes) {
        super("Missing option codes: " + String.join(", ", missingCodes));
        this.missingCodes = missingCodes == null ? Collections.emptyList() : List.copyOf(missingCodes);
    }
}
