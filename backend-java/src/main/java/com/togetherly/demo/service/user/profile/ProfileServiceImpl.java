package com.togetherly.demo.service.user.profile;

import com.togetherly.demo.data.PageList;
import com.togetherly.demo.data.user.UserProfile;
import com.togetherly.demo.exception.UserDoesNotExist;
import com.togetherly.demo.model.auth.User;
import com.togetherly.demo.repository.user.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Profile service implementation.
 *
 * KEY CHANGE FROM ORIGINAL:
 * Uses UserProfile.from(user) factory method instead of new UserProfile(user).
 * Because UserProfile is a Java record, it doesn't have a User→UserProfile constructor.
 * The static factory method handles the conversion cleanly.
 *
 * HAND-WRITTEN.
 */
@Service
public class ProfileServiceImpl implements ProfileService {
    @Autowired private UserRepository userRepository;

    @Override
    public UserProfile getProfile(String userId) throws UserDoesNotExist {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserDoesNotExist("user is not exist !"));
        return UserProfile.from(user);
    }

    @Override
    public List<UserProfile> getAllUserProfiles() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getCreateAt))
                .map(UserProfile::from)
                .toList();
    }

    @Override
    public PageList<UserProfile> getAllUserProfilesWithPage(int page, int size) {
        if (page < 0 || size <= 0)
            throw new IllegalArgumentException("invalid page or size !");

        Page<User> paging = userRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createAt")));

        List<UserProfile> profiles = paging.getContent().stream()
                .map(UserProfile::from)
                .toList();

        return new PageList<>(
                paging.getTotalElements(),
                paging.getNumber(),
                paging.getTotalPages(),
                paging.getSize(),
                profiles);
    }
}
