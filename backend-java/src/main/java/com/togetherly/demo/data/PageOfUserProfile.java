package com.togetherly.demo.data;

import com.togetherly.demo.data.user.UserProfile;
import java.util.List;

/**
 * Paginated list of user profiles returned to the client.
 * Wraps PageList<UserProfile> into a concrete type for clean API responses.
 */
public record PageOfUserProfile(
        long totalItems,
        int currentPage,
        int totalPages,
        int pageSize,
        List<UserProfile> profiles) {

    public static PageOfUserProfile from(PageList<UserProfile> pageList) {
        return new PageOfUserProfile(
                pageList.totalItems(),
                pageList.currentPage(),
                pageList.totalPages(),
                pageList.pageSize(),
                pageList.list());
    }
}
