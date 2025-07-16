package com.talentradar.user_service.controller;

import com.talentradar.user_service.dto.SessionResponseDto;
import com.talentradar.user_service.exception.UnauthorizedException;
import com.talentradar.user_service.listener.AuthenticationSuccessListener;
import com.talentradar.user_service.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Session Controller", description = "Manage all user's session api")
public class SessionController {
    private static final Logger logger = LoggerFactory.getLogger(SessionController.class);
    private final SessionService sessionService;

    @GetMapping(name = "fetchActiveSessions", path = "/sessions")
    @Operation(summary = "Fetch all active session",
            description = "This end point allow only admin to view all of the active session")
    public ResponseEntity<Page<SessionResponseDto>> viewProjects(HttpServletRequest request, Pageable pageable){
        String userRole = request.getHeader("X-User-Role");
        if (userRole == null || !userRole.equalsIgnoreCase("ADMIN")) {
            logger.info("Unauthorized user tried to access session data");
            throw new UnauthorizedException("Only admin has access to this data!");
        }
        Page<SessionResponseDto> sessionsList = sessionService.getActiveSessions(pageable);
        return ResponseEntity.ok(sessionsList);
    }
}
