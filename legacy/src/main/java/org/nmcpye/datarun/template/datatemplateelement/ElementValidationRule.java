package org.nmcpye.datarun.template.datatemplateelement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Hamza Assada 30/05/2025 (7amza.it@gmail.com)
 */
@NoArgsConstructor
@Accessors(chain = true)
@Setter
@Getter
public class ElementValidationRule implements Serializable {
    /**
     * validation's expression
     */
    private String expression;

    /**
     * validation's message when expression is met
     */
    private Map<String, String> validationMessage;
}
