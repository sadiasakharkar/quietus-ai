package com.quietusai.application.service;

import com.quietusai.api.dto.SessionResponse;
import com.quietusai.api.dto.StartSessionResponse;
import com.quietusai.domain.entity.Session;
import com.quietusai.domain.entity.User;
import com.quietusai.domain.model.SessionStatus;
import com.quietusai.domain.repository.SessionRepository;
import com.quietusai.domain.repository.UserRepository;
import com.quietusai.infrastructure.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public SessionService(SessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public StartSessionResponse startSession(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "User not found"));

        Session session = new Session();
        session.setUser(user);
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(OffsetDateTime.now());

        Session saved = sessionRepository.save(session);
        return new StartSessionResponse(saved.getId(), saved.getStatus().name(), saved.getStartedAt());
    }

    @Transactional
    public SessionResponse endSession(UUID userId, UUID sessionId) {
        Session session = findOwnedSession(userId, sessionId);
        if (session.getStatus() == SessionStatus.ENDED) {
            return toResponse(session);
        }

        session.setStatus(SessionStatus.ENDED);
        session.setEndedAt(OffsetDateTime.now());
        Session saved = sessionRepository.save(session);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SessionResponse getSession(UUID userId, UUID sessionId) {
        return toResponse(findOwnedSession(userId, sessionId));
    }

    private Session findOwnedSession(UUID userId, UUID sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "Session not found"));
        if (!session.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "SESSION_ACCESS_DENIED", "Session does not belong to user");
        }
        return session;
    }

    private SessionResponse toResponse(Session session) {
        return new SessionResponse(
                session.getId(),
                session.getUser().getId(),
                session.getStatus().name(),
                session.getStartedAt(),
                session.getEndedAt()
        );
    }
}
