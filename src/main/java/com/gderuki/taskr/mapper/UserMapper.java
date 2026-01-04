package com.gderuki.taskr.mapper;

import com.gderuki.taskr.entity.User;
import com.gderuki.taskr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

/**
 * Helper mapper for converting user IDs to usernames.
 * Used by TaskMapper for audit field mapping.
 */
@Component
@RequiredArgsConstructor
public class UserMapper {

    private final UserRepository userRepository;

    @Named("userIdToUsername")
    public String userIdToUsername(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId)
                .map(User::getUsername)
                .orElse(null);
    }
}
