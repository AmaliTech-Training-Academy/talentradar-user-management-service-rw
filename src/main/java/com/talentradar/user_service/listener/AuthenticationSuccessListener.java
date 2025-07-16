package com.talentradar.user_service.listener;

import com.talentradar.user_service.exception.UserNotFoundException;
import com.talentradar.user_service.model.Session;
import com.talentradar.user_service.model.User;
import com.talentradar.user_service.repository.SessionRepository;
import com.talentradar.user_service.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/* The class handles the event upon successful sign-in */
@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public AuthenticationSuccessListener(SessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        String email = ((UserDetails) event.getAuthentication().getPrincipal()).getUsername();
        // check whether username exists
        User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("A user with the email '%s' does not exist",
                                email))
                );

        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();

        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        String sessionId = request.getSession().getId();

        Session session = Session.builder()
                .sessionId(sessionId)
                .id(user.getId())
                .ipAddress(ip)
                .deviceInfo(userAgent)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();

        sessionRepository.save(session);
    }
}


