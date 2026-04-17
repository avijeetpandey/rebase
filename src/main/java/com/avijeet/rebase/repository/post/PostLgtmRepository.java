package com.avijeet.rebase.repository.post;

import com.avijeet.rebase.entities.PostLgtm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLgtmRepository extends JpaRepository<PostLgtm, Long> {
    Optional<PostLgtm> findByPostIdAndUserId(Long postId, Long userId);
    boolean existsByPostIdAndUserId(Long postId, Long userId);
}

