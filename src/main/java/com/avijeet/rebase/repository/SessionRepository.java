package com.avijeet.rebase.repository;

import com.avijeet.rebase.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> { }
