package com.quietusai.domain.repository;

import com.quietusai.domain.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
}
