package com.talentradar.user_service.listener;

import com.talentradar.user_service.exception.NotFoundUserException;
import com.talentradar.user_service.model.Session;
import com.talentradar.user_service.model.User;
import com.talentradar.user_service.repository.UserSessionRepository;
import com.talentradar.user_service.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* The class handles the event upon successful sign-in */
@Service
@RequiredArgsConstructor
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationSuccessListener.class);
    private final UserSessionRepository userSessionRepository;
    private final UserRepository userRepository;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {

        String email = ((UserDetails) event.getAuthentication().getPrincipal()).getUsername();
        logger.info("User '{}' logged in successfully", email);
        // check whether email exists
        User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundUserException(
                        String.format("A user with the email '%s' does not exist",
                                email))
                );

        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();

        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        String sessionId = request.getSession().getId();

        // Check if session with this sessionId already exists
        Session existing = userSessionRepository.findBySessionId(sessionId).orElse(null);

        if (existing == null) {
            logger.info("Create new session for user {}", email);
            // create new
            Session session = Session.builder()
                    .id(null)
                    .sessionId(sessionId)
                    .user(user)
                    .ipAddress(ip)
                    .deviceInfo(userAgent)
                    .createdAt(LocalDateTime.now())
                    .isActive(true)
                    .build();
            userSessionRepository.save(session);
        } else {
            logger.info("Update user {} session", email);
            // update fields
            existing.setIpAddress(ip);
            existing.setDeviceInfo(userAgent);
            existing.setActive(true);
            existing.setCreatedAt(LocalDateTime.now());
            userSessionRepository.save(existing);
        }

    }
}


