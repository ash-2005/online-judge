package com.oj.api.repository;

import com.oj.common.entity.WarRoomParticipant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarRoomParticipantRepository extends JpaRepository<WarRoomParticipant, Long> {

    List<WarRoomParticipant> findByWarRoomId(Long warRoomId);

    Optional<WarRoomParticipant> findByWarRoomIdAndUserId(Long warRoomId, Long userId);

    boolean existsByWarRoomIdAndUserId(Long warRoomId, Long userId);

    long countByWarRoomId(Long warRoomId);
}
