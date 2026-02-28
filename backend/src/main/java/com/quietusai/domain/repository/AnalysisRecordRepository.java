package com.quietusai.domain.repository;

import com.quietusai.domain.entity.AnalysisRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnalysisRecordRepository extends JpaRepository<AnalysisRecord, UUID> {
}
