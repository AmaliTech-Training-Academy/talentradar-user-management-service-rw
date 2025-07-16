package com.talentradar.user_service.controller;

import com.talentradar.user_service.dto.SessionResponseDto;
import com.talentradar.user_service.exception.UnauthorizedException;
import com.talentradar.user_service.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;

    @GetMapping(name = "fetchActiveSessions", path = "/sessions")
    public ResponseEntity<Page<SessionResponseDto>> viewProjects(HttpServletRequest request, Pageable pageable){
        String userRole = request.getHeader("X-User-Role");
        if (userRole == null || !userRole.equalsIgnoreCase("ADMIN")) {
            throw new UnauthorizedException("Only admin has access to this data!");
        }
        Page<SessionResponseDto> sessionsList = sessionService.getActiveSessions(pageable);
        return ResponseEntity.ok(sessionsList);
    }
}
