package com.app.mie.ayam.ordering;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppOrderRepository extends JpaRepository<AppOrder, Long> {
	List<AppOrder> findAllByUserUsernameOrderByCreatedAtDesc(String username);
	Optional<AppOrder> findByIdAndUserUsername(Long id, String username);
	List<AppOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
