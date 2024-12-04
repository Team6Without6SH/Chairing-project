package com.sparta.chairingproject.domain.menu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sparta.chairingproject.domain.menu.entity.Menu;

public interface MenuRepository extends JpaRepository<Menu, Long> {
	@Query("SELECT m FROM Menu m WHERE m.id IN :menuIds AND m.store.id = :storeId")
	List<Menu> findAllByStoreIdAndMenuIds(@Param("storeId") Long storeId, @Param("menuIds") List<Long> menuIds);
}
