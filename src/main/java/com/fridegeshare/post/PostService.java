package com.fridegeshare.post;

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
public class PostService {
    public record PostForm(String title, String foodName, String quantity, LocalDate expiryDate, String description) {}
    public record SavedPost(long id, String status) {}
    public record Mine(long id, String title, String status, OffsetDateTime createdAt) {}
    public record Post(long id, long authorId, String authorNickname, String title, String foodName, String quantity,
                       LocalDate expiryDate, String description, String neighborhood, String neighborhoodCode,
                       String status, OffsetDateTime createdAt) {}
    public record Detail(
        @com.fasterxml.jackson.annotation.JsonUnwrapped Post post,
        String viewerRole, Long reservationId, Long chatRoomId) {}

    private final PostMapper posts;
    private final RegionDirectory regions;

    PostService(PostMapper posts, RegionDirectory regions) {
        this.posts = posts;
        this.regions = regions;
    }

    public List<Post> list(SessionUser user, String keyword) {
        String search = keyword == null ? null : Inputs.text(keyword, "검색어", 1, 100);
        var neighborhoods = regions.districtCodes(user.neighborhoodCode());
        if (neighborhoods.isEmpty()) return List.of();
        return posts.list(neighborhoods, search).stream().map(this::toPost).toList();
    }

    public List<Mine> mine(SessionUser user) {
        return posts.mine(user.id()).stream()
            .map(row -> new Mine(row.id(), row.title(), row.status(), Inputs.timestamp(row.createdAt()))).toList();
    }

    // 조회자 역할 판정 및 참여자에게만 예약, 채팅 ID 공개
    public Detail detail(SessionUser user, long id) {
        var row = requireInDistrict(user, id);
        String role = "OTHER";
        if (row.authorId() == user.id()) role = "OWNER";
        else if (row.applicantId() != null && row.applicantId() == user.id()) role = "APPLICANT";
        boolean participant = !role.equals("OTHER");
        return new Detail(toPost(row), role,
            participant ? row.reservationId() : null, participant ? row.chatRoomId() : null);
    }

    private PostMapper.PostRow requireInDistrict(SessionUser user, long id) {
        var row = require(id);
        // 조회 범위 밖의 글은 주소로 접근해도 존재 여부를 공개하지 않는다.
        if (!regions.sameDistrict(user.neighborhoodCode(), row.neighborhoodCode())) {
            throw ApiException.missing();
        }
        return row;
    }

    // LAST_INSERT_ID()가 INSERT와 같은 DB 연결을 사용하도록 트랜잭션으로 묶는다.
    @Transactional
    public SavedPost create(SessionUser user, PostForm raw) {
        posts.insert(user.id(), user.neighborhoodCode(), validate(raw));
        return new SavedPost(posts.insertedId(), "AVAILABLE");
    }

    public SavedPost update(SessionUser user, long id, PostForm raw) {
        requireEditable(user, id);
        // 조회 후 상태가 바뀌었을 수 있으므로 변경 행이 없으면 권한과 상태를 다시 확인한다.
        if (posts.update(id, user.id(), validate(raw)) == 0) requireEditable(user, id);
        return new SavedPost(id, "AVAILABLE");
    }

    public void delete(SessionUser user, long id) {
        requireEditable(user, id);
        if (posts.delete(id, user.id()) == 0) requireEditable(user, id);
    }

    // 작성자 및 모집 중 상태 확인
    public void requireEditable(SessionUser user, long id) {
        var row = require(id);
        if (row.authorId() != user.id()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_POST_OWNER", "작성자만 수정하거나 삭제할 수 있습니다.");
        }
        if (!"AVAILABLE".equals(row.status())) {
            throw new ApiException(HttpStatus.CONFLICT, "POST_NOT_EDITABLE", "모집 중인 글만 수정하거나 삭제할 수 있습니다.");
        }
    }

    private PostMapper.PostRow require(long id) {
        var row = posts.byId(id);
        if (row == null) throw ApiException.missing();
        return row;
    }

    private Post toPost(PostMapper.PostRow row) {
        return new Post(row.id(), row.authorId(), row.authorNickname(), row.title(), row.foodName(), row.quantity(),
            row.expiryDate(), row.description(), regions.label(row.neighborhoodCode()), row.neighborhoodCode(),
            row.status(), Inputs.timestamp(row.createdAt()));
    }

    // 서울 기준 소비기한 및 게시글 필수값 검증
    private PostForm validate(PostForm raw) {
        if (raw.expiryDate() == null || raw.expiryDate().isBefore(Inputs.today())) {
            throw ApiException.invalid("소비기한은 서울 기준 오늘 이후의 날짜를 선택하세요.");
        }
        return new PostForm(Inputs.text(raw.title(), "제목", 1, 100),
            Inputs.text(raw.foodName(), "식재료명", 1, 100),
            Inputs.text(raw.quantity(), "수량", 1, 50), raw.expiryDate(),
            Inputs.text(raw.description(), "설명", 1, 2000));
    }
}
