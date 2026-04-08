package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndIsActiveTrue(String email);

    Optional<User> findByIdAndIsActiveTrue(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIsActiveTrue(String email);
}
