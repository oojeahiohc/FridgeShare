package com.fridegeshare.reservation;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import com.fridegeshare.common.ApiException;
import com.fridegeshare.common.Inputs;
import com.fridegeshare.common.SessionUser;
import com.fridegeshare.region.RegionDirectory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {
    public record Created(long reservationId, long postId, String status) {}
    public record Changed(long reservationId, String status) {}
    public record History(long reservationId, long postId, boolean postDeleted, String foodName,
                          String postStatus, String reservationStatus, Long chatRoomId, OffsetDateTime createdAt) {}

    private final ReservationMapper reservations;

    private final RegionDirectory regions;

    ReservationService(ReservationMapper reservations, RegionDirectory regions) {
        this.reservations = reservations;
        this.regions = regions;
    }

    // 조건부 선점과 예약 저장의 단일 커밋
    @Transactional
    public Created claim(SessionUser user, long postId) {
        var neighborhoods = regions.districtCodes(user.neighborhoodCode());
        var today = Inputs.today();
        if (neighborhoods.isEmpty() || reservations.claim(postId, user.id(), neighborhoods, today) == 0) {
            throw unavailable(reservations.lockPost(postId), user, today);
        }
        reservations.insert(postId, user.id());
        return new Created(reservations.insertedId(), postId, "RESERVED");
    }

    @Transactional
    public Changed cancel(SessionUser user, long reservationId) {
        return transition(user, reservationId, false);
    }

    @Transactional
    public Changed complete(SessionUser user, long reservationId) {
        return transition(user, reservationId, true);
    }

    // 취소, 완료의 중복 전이 방지를 위한 게시글 우선 잠금
    private Changed transition(SessionUser user, long id, boolean complete) {
        Long postId = reservations.postId(id);
        if (postId == null) throw ApiException.missing();

        var post = reservations.lockPost(postId);
        var reservation = reservations.lockReservation(id);
        boolean owner = post.authorId() == user.id();
        if (complete && !owner) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_POST_OWNER", "작성자만 나눔을 완료할 수 있습니다.");
        }
        if (!owner && reservation.applicantId() != user.id()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_RESERVATION_PARTICIPANT", "해당 예약을 취소할 권한이 없습니다.");
        }

        String reservationStatus = complete ? "COMPLETED" : "CANCELLED";
        String postStatus = complete ? "COMPLETED" : "AVAILABLE";
        if (reservations.changeReservation(id, reservationStatus) != 1
                || reservations.changePost(postId, postStatus) != 1) {
            throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "이미 처리된 예약입니다.");
        }
        return new Changed(id, reservationStatus);
    }

    public List<History> mine(SessionUser user) {
        return reservations.mine(user.id()).stream()
            .map(row -> new History(row.reservationId(), row.postId(), row.postDeleted(), row.foodName(),
                row.postStatus(), row.reservationStatus(), row.chatRoomId(), Inputs.timestamp(row.createdAt())))
            .toList();
    }

    // 선점 실패 원인의 권한, 기한, 마감 오류 분류
    private ApiException unavailable(ReservationMapper.PostLock post, SessionUser user, LocalDate today) {
        if (post == null || post.deletedAt() != null) return ApiException.missing();
        if (post.authorId() == user.id()) {
            return new ApiException(HttpStatus.FORBIDDEN, "SELF_RESERVATION", "본인 글은 예약할 수 없습니다.");
        }
        if (!regions.sameDistrict(post.neighborhood(), user.neighborhoodCode())) {
            return new ApiException(HttpStatus.FORBIDDEN, "REGION_MISMATCH", "같은 시·도와 시·군·구의 글만 예약할 수 있습니다.");
        }
        if (post.expiryDate().isBefore(today)) {
            return new ApiException(HttpStatus.CONFLICT, "FOOD_EXPIRED", "소비기한이 지났습니다.");
        }
        return new ApiException(HttpStatus.CONFLICT, "RESERVATION_CLOSED", "예약이 마감되었습니다.");
    }
}
