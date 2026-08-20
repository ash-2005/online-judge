package com.oj.api.repository;

import com.oj.common.entity.WarRoom;
import com.oj.common.enums.WarRoomStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarRoomRepository extends JpaRepository<WarRoom, Long> {

    Optional<WarRoom> findByRoomCode(String roomCode);

    List<WarRoom> findByStatus(WarRoomStatus status);

    long countByStatusIn(List<WarRoomStatus> statuses);
}
