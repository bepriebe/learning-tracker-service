package io.github.bepriebe.learningtracker.goal;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface GoalRepository extends JpaRepository<Goal, UUID> {
}
