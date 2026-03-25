package org.nmcpye.datarun.party.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.nmcpye.datarun.party.entities.PartySet.PartySetSpec;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.IOException;
import java.time.Instant;

@Getter
@Setter
@Accessors(chain = true)
public class PagedRequest {
    @Schema(description = "Flag to enable fetching paged or not paged content")
    private boolean paged = true;

    @Schema(description = "Page number (default: 0)")
    private int page = 0;

    @Schema(description = "Page size (default: 20)")
    private int size = 20;

    @Schema(description = "Flag to only include elements update since a point of time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant since;

    ///
    public static String specToString(PartySetSpec partySet) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(partySet);
    }

    public Instant getSince() {
        return since != null ? since : Instant.EPOCH;
    }

    public Pageable getPageable() {
        if (!isPaged()) {
            return Pageable.unpaged();
        }

        return PageRequest.of(getPage(), getSize());
    }

    public int getSize() {
        if (!isPaged()) {
            return Integer.MAX_VALUE;
        }
        return size;
    }

    public int getPage() {
        if (!isPaged()) {
            return 0;
        }
        return page;
    }
}
