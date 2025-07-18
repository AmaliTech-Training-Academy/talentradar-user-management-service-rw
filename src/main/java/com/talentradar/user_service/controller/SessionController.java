package com.talentradar.user_service.controller;

import com.talentradar.user_service.dto.ResponseDto;
import com.talentradar.user_service.dto.SessionResponseDto;
import com.talentradar.user_service.exception.UnauthorizedException;
import com.talentradar.user_service.listener.AuthenticationSuccessListener;
import com.talentradar.user_service.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<ResponseDto> viewActiveSession(Pageable pageable){
        Page<SessionResponseDto> sessionsList = sessionService.getActiveSessions(pageable);
        ResponseDto response = ResponseDto.builder()
                .status(true)
                .message("Fetch session list")
                .errors(List.of())
                .data(sessionsList)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(name = "revokeASingleSession", path = "/sessions/{sessionId}")
    @Operation(summary = "Delete a single session",
            description = "This end point allow only admin to delete/revoke a session using its id")
    public ResponseEntity<?> deleteRestaurant(@PathVariable String sessionId, HttpServletRequest request){
        HttpSession session = request.getSession(false); // get current session
        this.sessionService.revokeSessionById(sessionId, session);

        ResponseDto response = ResponseDto.builder()
                .status(true)
                .message("DeleteSession")
                .errors(List.of())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping(name = "filterSessions", path = "/sessions/filter")
    @Operation(summary = "Filter all active session",
            description = "This end point allow only admin to filter by userId and date")
    public ResponseEntity<ResponseDto> filterSession(@RequestParam(required = false) UUID userId,
                                                     @RequestParam(required = false) String date,
                                                     Pageable pageable){
        Page<SessionResponseDto> sessionsList = sessionService.filterSessions(userId, date, pageable);
        ResponseDto response = ResponseDto.builder()
                .status(true)
                .message("Filter session")
                .errors(List.of())
                .data(sessionsList)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
