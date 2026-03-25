package org.nmcpye.datarun.web.rest.v1.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author Hamza Assada 11/03/2026 (7amza.it@gmail.com)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class IdentifiableEntityDto {
    private String id;
    private String uid;
    private String code;
    private String name;
}
