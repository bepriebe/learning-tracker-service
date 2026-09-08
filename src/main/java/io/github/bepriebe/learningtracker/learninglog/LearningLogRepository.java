package io.github.bepriebe.learningtracker.learninglog;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface LearningLogRepository extends JpaRepository<LearningLog, UUID> {
}

