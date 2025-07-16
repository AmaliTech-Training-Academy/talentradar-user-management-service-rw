package com.talentradar.user_service.mapper;

import com.talentradar.user_service.dto.SessionResponseDto;
import com.talentradar.user_service.model.Session;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SessionMapper {

    // map session + nested user info
    @Mapping(source = "user.fullName", target = "user.fullName")
    @Mapping(source = "user.email", target = "user.email")
    SessionResponseDto toDto(Session session);

    List<SessionResponseDto> toDtoList(List<Session> sessions);
}

