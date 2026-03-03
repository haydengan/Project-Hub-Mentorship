package com.togetherly.demo.data;

import java.util.List;

/**
 * Generic wrapper for paginated query results.
 * Used internally between Service → Controller layers.
 *
 * WHY NOT A RECORD? Records support generics, so this COULD be a record.
 * But it's a good example of when either approach works. We use a record here.
 *
 * @param <E> the type of items in the list
 */
public record PageList<E>(
        long totalItems,
        int currentPage,
        int totalPages,
        int pageSize,
        List<E> list) {}
