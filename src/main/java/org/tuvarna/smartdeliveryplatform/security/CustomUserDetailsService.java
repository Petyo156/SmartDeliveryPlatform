package org.tuvarna.smartdeliveryplatform.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.tuvarna.smartdeliveryplatform.shared.constants.ErrorMessages;
import org.tuvarna.smartdeliveryplatform.user.model.User;
import org.tuvarna.smartdeliveryplatform.user.repository.UserRepository;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(ErrorMessages.USER_WITH_EMAIL_NOT_FOUND.formatted(email)));

        return new AuthenticationMetadata(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.getStatus()
        );
    }
}
