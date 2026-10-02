package com.ecommerce.backend.infrastructure.repository;

import com.ecommerce.backend.domain.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /** Varsayılan önce, sonra en yeni. */
    List<Address> findByUserIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtDescIdDesc(Long userId);

    /** En yeni önce (varsayılan silinince yerine geçecek adres için). */
    List<Address> findByUserIdAndIsActiveTrueOrderByCreatedAtDescIdDesc(Long userId);

    Optional<Address> findByIdAndUserIdAndIsActiveTrue(Long id, Long userId);

    List<Address> findByUserIdAndIsDefaultTrueAndIsActiveTrue(Long userId);

    boolean existsByUserIdAndIsActiveTrue(Long userId);
}
