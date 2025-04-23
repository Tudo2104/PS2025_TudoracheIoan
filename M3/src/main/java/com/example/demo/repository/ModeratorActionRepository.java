package com.example.demo.repository;

import com.example.demo.entity.ModeratorAction;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ModeratorActionRepository extends JpaRepository<ModeratorAction,Long> {

    Optional<ModeratorAction> findByTargetUserIdAndBlocked(Long targetUserId, boolean blocked);

    List<ModeratorAction> findByTargetUserIdAndReadStatus(Long targetUserId, boolean status);
}
