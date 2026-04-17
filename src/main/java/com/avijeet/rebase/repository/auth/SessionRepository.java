package com.avijeet.rebase.repository.auth;

import com.avijeet.rebase.entities.Session;
import com.avijeet.rebase.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
	@EntityGraph(attributePaths = "user")
	Optional<Session> findByIdAndActiveTrue(Long id);

	@EntityGraph(attributePaths = "user")
	Optional<Session> findByRefreshTokenAndActiveTrue(String refreshToken);

	@EntityGraph(attributePaths = "user")
	List<Session> findByUserAndActiveTrue(User user);
}
