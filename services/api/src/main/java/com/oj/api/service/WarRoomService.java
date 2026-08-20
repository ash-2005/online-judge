package com.oj.api.service;

import com.oj.api.dto.ApiDtos.CreateWarRoomRequest;
import com.oj.api.dto.ApiDtos.WarRoomDto;
import com.oj.api.dto.ApiDtos.WarRoomEvent;
import com.oj.api.exception.ApiException;
import com.oj.api.repository.ProblemRepository;
import com.oj.api.repository.UserRepository;
import com.oj.api.repository.WarRoomParticipantRepository;
import com.oj.api.repository.WarRoomRepository;
import com.oj.api.security.AuthSupport;
import com.oj.common.entity.Problem;
import com.oj.common.entity.User;
import com.oj.common.entity.WarRoom;
import com.oj.common.entity.WarRoomParticipant;
import com.oj.common.enums.WarRoomStatus;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WarRoomService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final WarRoomRepository warRoomRepository;
    private final WarRoomParticipantRepository participantRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final WarRoomEventPublisher eventPublisher;

    public WarRoomService(
            WarRoomRepository warRoomRepository,
            WarRoomParticipantRepository participantRepository,
            ProblemRepository problemRepository,
            UserRepository userRepository,
            WarRoomEventPublisher eventPublisher
    ) {
        this.warRoomRepository = warRoomRepository;
        this.participantRepository = participantRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public WarRoomDto create(CreateWarRoomRequest request) {
        if (request.maxParticipants() < 2) {
            throw new ApiException("maxParticipants must be at least 2");
        }
        Problem problem = problemRepository.findById(request.problemId())
                .orElseThrow(() -> new ApiException("Problem not found"));
        User user = userRepository.findById(AuthSupport.currentUserId())
                .orElseThrow(() -> new ApiException("User not found"));

        WarRoom room = WarRoom.builder()
                .roomCode(generateUniqueCode())
                .problem(problem)
                .maxParticipants(request.maxParticipants())
                .status(WarRoomStatus.WAITING)
                .build();
        room = warRoomRepository.save(room);

        participantRepository.save(WarRoomParticipant.builder()
                .warRoom(room)
                .user(user)
                .build());

        eventPublisher.publish(new WarRoomEvent(
                "JOIN",
                room.getId(),
                room.getRoomCode(),
                user.getId(),
                user.getUsername(),
                user.getUsername() + " created the room",
                Instant.now()
        ));

        return toDto(room);
    }

    @Transactional(readOnly = true)
    public List<WarRoomDto> list(WarRoomStatus status) {
        List<WarRoom> rooms = status == null
                ? warRoomRepository.findAll()
                : warRoomRepository.findByStatus(status);
        return rooms.stream().map(this::toDto).toList();
    }

    @Transactional
    public WarRoomDto join(String code) {
        WarRoom room = warRoomRepository.findByRoomCode(code.toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException("War room not found"));
        if (room.getStatus() == WarRoomStatus.FINISHED) {
            throw new ApiException("War room already finished");
        }

        User user = userRepository.findById(AuthSupport.currentUserId())
                .orElseThrow(() -> new ApiException("User not found"));

        if (participantRepository.existsByWarRoomIdAndUserId(room.getId(), user.getId())) {
            return toDto(room);
        }

        long count = participantRepository.countByWarRoomId(room.getId());
        if (count >= room.getMaxParticipants()) {
            throw new ApiException("War room is full");
        }

        participantRepository.save(WarRoomParticipant.builder()
                .warRoom(room)
                .user(user)
                .build());

        if (room.getStatus() == WarRoomStatus.WAITING && count + 1 >= room.getMaxParticipants()) {
            room.setStatus(WarRoomStatus.IN_PROGRESS);
            room.setStartedAt(Instant.now());
            warRoomRepository.save(room);
            eventPublisher.publish(new WarRoomEvent(
                    "STATUS",
                    room.getId(),
                    room.getRoomCode(),
                    null,
                    null,
                    "IN_PROGRESS",
                    Instant.now()
            ));
        }

        eventPublisher.publish(new WarRoomEvent(
                "JOIN",
                room.getId(),
                room.getRoomCode(),
                user.getId(),
                user.getUsername(),
                user.getUsername() + " joined",
                Instant.now()
        ));

        return toDto(room);
    }

    @Transactional(readOnly = true)
    public WarRoomDto getByCode(String code) {
        WarRoom room = warRoomRepository.findByRoomCode(code.toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException("War room not found"));
        return toDto(room);
    }

    private WarRoomDto toDto(WarRoom room) {
        return new WarRoomDto(
                room.getId(),
                room.getRoomCode(),
                room.getProblem().getId(),
                room.getProblem().getTitle(),
                room.getMaxParticipants(),
                room.getStatus(),
                room.getWinnerId(),
                room.getStartedAt(),
                room.getEndedAt(),
                room.getCreatedAt(),
                participantRepository.countByWarRoomId(room.getId())
        );
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < 20; attempt++) {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
            }
            String code = sb.toString();
            if (warRoomRepository.findByRoomCode(code).isEmpty()) {
                return code;
            }
        }
        throw new ApiException("Could not generate room code");
    }
}
