package org.nmcpye.datarun.sharedkernal.feedback;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.text.MessageFormat;

@Getter
@Setter
public class ErrorMessage {
    private final ErrorCode errorCode;

    private final Object[] args;

    private final String message;

    public ErrorMessage(ErrorCode errorCode, Object... args) {
        this.errorCode = errorCode;
        this.args = args;
        this.message = MessageFormat.format(errorCode.getMessage(), this.args);
    }

    @JsonCreator
    public ErrorMessage(@JsonProperty("message") String message, @JsonProperty("errorCode") ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.args = null;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("[%s: '%s']", errorCode.name(), message);
    }
}
