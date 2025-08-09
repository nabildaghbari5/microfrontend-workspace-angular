package com.talentcloud.auth.repository;

import com.talentcloud.auth.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, UUID> {

    Mono<User> findByUsername(String username);

    Mono<User> findByEmail(String email);

    Mono<User> findByKeycloakId(String keycloakId);

    Mono<Boolean> existsByUsernameOrEmail(String username, String email);
}