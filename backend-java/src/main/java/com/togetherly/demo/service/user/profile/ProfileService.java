package com.togetherly.demo.service.user.profile;

import com.togetherly.demo.data.PageList;
import com.togetherly.demo.data.user.UserProfile;
import com.togetherly.demo.exception.AlreadyExist;
import com.togetherly.demo.exception.UserDoesNotExist;
import java.util.List;

/**
 * Read-only service for fetching user profiles.
 *
 * Profiles are a safe, public-facing view of user data
 * (excludes password, login attempts, etc.).
 *
 * HAND-WRITTEN interface.
 */
public interface ProfileService {

    /** Get a single user's profile by their ID. */
    UserProfile getProfile(String userId) throws UserDoesNotExist;

    /** Get all user profiles (no pagination). */
    List<UserProfile> getAllUserProfiles();

    /** Get user profiles with pagination. */
    PageList<UserProfile> getAllUserProfilesWithPage(int page, int size);

    /** Update a user's username. */
    void updateUsername(String userId, String newUsername)
            throws UserDoesNotExist, AlreadyExist;
}
