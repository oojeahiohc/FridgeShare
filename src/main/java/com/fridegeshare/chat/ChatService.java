package com.fridegeshare.chat;

import java.time.OffsetDateTime;
import java.util.List;

import com.fridegeshare.common.ApiException;
import com.fridegeshare.common.Inputs;
import com.fridegeshare.common.SessionUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {
    public record RoomId(long roomId, long reservationId) {}
    public record Opened(RoomId room, boolean created) {}
    public record Message(long id, long chatRoomId, long senderId, String content, OffsetDateTime createdAt) {}
    public record Sent(long id, long senderId, String content, OffsetDateTime createdAt) {}
    public record Feed(List<Message> data, boolean canSend) {}
    public record Room(long roomId, long postId, boolean postDeleted, String foodName, String counterpartNickname) {}
    public record History(long roomId, long reservationId, String reservationStatus, long postId,
                          boolean postDeleted, String foodName, String counterpartNickname) {}

    private final ChatMapper chat;

    ChatService(ChatMapper chat) { this.chat = chat; }

    // 같은 예약의 방 생성 요청을 직렬화해 재요청에 기존 방을 반환한다.
    @Transactional
    public Opened open(SessionUser user, long reservationId) {
        String status = chat.lockReservation(reservationId);
        if (status == null) throw ApiException.missing();
        var context = chat.byReservation(reservationId);
        if (context.authorId() != user.id()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_POST_OWNER", "작성자만 채팅을 시작할 수 있습니다.");
        }
        requireActive(status);
        if (context.roomId() != null) return new Opened(new RoomId(context.roomId(), reservationId), false);
        chat.createRoom(reservationId);
        return new Opened(new RoomId(chat.insertedId(), reservationId), true);
    }

    // 증분 조회 결과가 비어 있어도 예약 종료 여부를 화면에 전달한다.
    @Transactional(readOnly = true)
    public Feed messages(SessionUser user, long roomId, long afterId) {
        if (afterId < 0) throw ApiException.invalid("메시지 ID를 확인하세요.");
        var room = requireRoom(user, roomId);
        return new Feed(chat.messages(roomId, afterId).stream().map(this::toMessage).toList(),
            "RESERVED".equals(room.status()));
    }

    // 예약을 잠근 뒤 최신 상태를 확인해 취소·완료와 전송이 엇갈리지 않도록 한다.
    @Transactional
    public Sent send(SessionUser user, long roomId, String raw) {
        var room = requireRoom(user, roomId);
        requireActive(chat.lockReservation(room.reservationId()));
        chat.insertMessage(roomId, user.id(), Inputs.text(raw, "메시지", 1, 1000));
        var message = chat.insertedMessage();
        return new Sent(message.id(), message.senderId(), message.content(), Inputs.timestamp(message.createdAt()));
    }

    public Room room(SessionUser user, long roomId) {
        var room = requireRoom(user, roomId);
        return new Room(roomId, room.postId(), room.postDeleted(), room.foodName(),
            room.authorId() == user.id() ? room.applicantNickname() : room.ownerNickname());
    }

    public List<History> history(SessionUser user) {
        return chat.history(user.id()).stream()
            .map(row -> new History(row.roomId(), row.reservationId(), row.reservationStatus(), row.postId(),
                row.postDeleted(), row.foodName(), row.counterpartNickname())).toList();
    }

    private ChatMapper.ContextRow requireRoom(SessionUser user, long roomId) {
        var context = chat.byRoom(roomId);
        if (context == null) throw ApiException.missing();
        if (context.authorId() != user.id() && context.applicantId() != user.id()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_CHAT_PARTICIPANT", "채팅방 참여자만 접근할 수 있습니다.");
        }
        return context;
    }

    private void requireActive(String status) {
        if (!"RESERVED".equals(status)) {
            throw new ApiException(HttpStatus.CONFLICT, "CHAT_NOT_AVAILABLE", "종료된 예약은 읽기 전용입니다.");
        }
    }

    private Message toMessage(ChatMapper.MessageRow row) {
        return new Message(row.id(), row.chatRoomId(), row.senderId(), row.content(), Inputs.timestamp(row.createdAt()));
    }
}
