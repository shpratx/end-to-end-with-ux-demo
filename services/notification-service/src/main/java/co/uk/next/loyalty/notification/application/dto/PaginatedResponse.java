package co.uk.Dunelm.loyalty.notification.application.dto;

import java.util.List;

public record PaginatedResponse<T>(
        List<T> data,
        Meta meta
) {
    public PaginatedResponse(List<T> data, int pageNumber, int pageSize, long totalItems, int totalPages) {
        this(data, new Meta(pageNumber, pageSize, totalItems, totalPages, pageNumber < totalPages, pageNumber > 1));
    }

    public record Meta(int pageNumber, int pageSize, long totalItems, int totalPages, boolean hasDunelmPage, boolean hasPreviousPage) {}
}
