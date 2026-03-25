package org.nmcpye.datarun.web.rest.util;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

public final class PaginationUtil {
    private PaginationUtil() {
    }

    public static <T> HttpHeaders generatePaginationHttpHeaders(UriComponentsBuilder uriBuilder, Page<T> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", Long.toString(page.getTotalElements()));
        StringBuilder link = new StringBuilder();
        if ((page.getNumber() + 1) < page.getTotalPages()) {
            link.append("<").append(generateUri(uriBuilder, page.getNumber() + 1, page.getSize()))
                    .append(">; rel=\"next\",");
        }
        if ((page.getNumber()) > 0) {
            link.append("<").append(generateUri(uriBuilder, page.getNumber() - 1, page.getSize()))
                    .append(">; rel=\"prev\",");
        }
        int lastPage = 0;
        if (page.getTotalPages() > 0) {
            lastPage = page.getTotalPages() - 1;
        }
        link.append("<").append(generateUri(uriBuilder, lastPage, page.getSize())).append(">; rel=\"last\",");
        link.append("<").append(generateUri(uriBuilder, 0, page.getSize())).append(">; rel=\"first\"");
        headers.add(HttpHeaders.LINK, link.toString());
        return headers;
    }

    private static String generateUri(UriComponentsBuilder uriBuilder, int page, int size) {
        return uriBuilder.replaceQueryParam("page", page).replaceQueryParam("size", size).toUriString();
    }
}
