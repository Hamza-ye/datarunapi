package org.nmcpye.datarun.web.common;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.data.domain.Page;

import java.util.List;

@JsonSerialize(using = DynamicFieldSerializer.class)
public class PagedResponse<T> {
    private final Page<T> page;
    //    private int page;
//    private int size;
//    private long totalElements;
//    private boolean paged;
//    private boolean flatten;
//    private int totalPages;
//    private boolean first;
//    private boolean last;
//    private boolean empty;
    private String nextPage;
    //    private List<T> items;
    private String entityName;

    public PagedResponse(Page<T> page, String contentName, String nextPage) {
//        this.items = page.getContent();
//        this.totalElements = page.getTotalElements();
//        this.totalPages = page.getTotalPages();
//        this.page = page.getNumber();
        this.page = page;
//        this.size = page.getSize();
//        this.first = page.isFirst();
//        this.last = page.isLast();
//        this.empty = page.isEmpty();
        this.entityName = contentName;
        this.nextPage = nextPage;
    }


    public int getPage() {
        return page.getNumber();
    }

//    public void setPage(int page) {
//        this.page = page;
//    }

    public int getSize() {
        return page.getSize();
    }

//    public void setSize(int size) {
//        this.size = size;
//    }

    public long getTotalElements() {
        return page.getTotalElements();
    }

//    public void setTotalElements(long totalElements) {
//        this.totalElements = totalElements;
//    }

    public int getTotalPages() {
        return page.getTotalPages();
    }

//    public void setTotalPages(int totalPages) {
//        this.totalPages = totalPages;
//    }

    public boolean isFirst() {
        return page.isFirst();
    }

//    public void setFirst(boolean first) {
//        this.first = first;
//    }

    public boolean isLast() {
        return page.isLast();
    }

//    public void setLast(boolean last) {
//        this.last = last;
//    }

    public boolean isEmpty() {
        return page.isEmpty();
    }

//    public void setEmpty(boolean empty) {
//        this.empty = empty;
//    }

    public String getNextPage() {
        return nextPage;
    }

    public void setNextPage(String nextPage) {
        this.nextPage = nextPage;
    }

    public List<T> getItems() {
        return page.getContent();
    }

//    public void setItems(List<T> items) {
//        this.items = items;
//    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Boolean isPaged() {
        return page.getPageable().isPaged();
    }
}
