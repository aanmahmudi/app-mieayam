package com.app.mie.ayam.ordering;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppOrderRepository extends JpaRepository<AppOrder, Long> {
	List<AppOrder> findAllByUserUsernameOrderByCreatedAtDesc(String username);
	Optional<AppOrder> findByIdAndUserUsername(Long id, String username);
	List<AppOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);

	interface WeeklyAdminStatsRow {
		java.sql.Timestamp getWeekStart();
		long getTotalOrders();
		long getPaidOrders();
		long getPaidTotal();
	}

	@Query(value = """
		select
			date_trunc('week', o.created_at) as week_start,
			count(*) as total_orders,
			coalesce(sum(case when p.id is not null and (p.confirmed is null or p.confirmed = true) then 1 else 0 end), 0) as paid_orders,
			coalesce(sum(case when p.id is not null and (p.confirmed is null or p.confirmed = true) then o.total else 0 end), 0) as paid_total
		from app_order o
		left join app_order_payment p on p.order_id = o.id
		where o.created_at >= :from
		group by week_start
		order by week_start desc
		""", nativeQuery = true)
	List<WeeklyAdminStatsRow> findWeeklyAdminStats(@Param("from") java.time.Instant from);
}
