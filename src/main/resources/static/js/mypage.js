"use strict";

{
    const page = document.querySelector("#activity");
    const errorNode = document.querySelector("#error");
    const postState = {AVAILABLE: "모집 중", RESERVED: "예약 중", COMPLETED: "나눔 완료"};
    const reservationState = {RESERVED: "예약 확정", CANCELLED: "취소", COMPLETED: "나눔 완료"};

    function link(path, id, label) {
        const item = document.createElement("a");
        item.href = path + "/" + encodeURIComponent(id);
        item.textContent = label;
        return item;
    }

    // 마이페이지 목록의 공통 빈 상태 표시
    function rows(id, data, draw) {
        const list = document.getElementById(id);
        list.replaceChildren();
        if (data.length === 0) {
            const item = document.createElement("li");
            item.textContent = "내역이 없습니다.";
            list.append(item);
            return;
        }
        for (const row of data) {
            const item = document.createElement("li");
            draw(item, row);
            list.append(item);
        }
    }

    async function loadActivity() {
        const [profile, posts, reservations, history] = await Promise.all([
            requestJson(page.dataset.me),
            requestJson(page.dataset.me + "/posts"),
            requestJson(page.dataset.me + "/reservations"),
            requestJson(page.dataset.me + "/chat-rooms")
        ]);
        document.querySelector("#profile").textContent = profile.data.nickname + " · " + profile.data.neighborhood;
        rows("my-posts", posts.data, (item, post) => {
            item.append(link(page.dataset.posts, post.id, post.title), " · " + postState[post.status]);
        });
        rows("my-reservations", reservations.data, (item, row) => {
            item.append(row.foodName + " · " + reservationState[row.reservationStatus] + " · " + postState[row.postStatus]);
            // 논리 삭제된 글은 예약 이력에 표시하되 상세 링크는 숨긴다.
            if (!row.postDeleted) item.append(" ", link(page.dataset.posts, row.postId, "상세"));
            if (row.chatRoomId != null) item.append(" ", link(page.dataset.rooms, row.chatRoomId, "채팅"));
            if (row.reservationStatus === "RESERVED" && row.postStatus === "RESERVED") {
                const cancel = document.createElement("button");
                cancel.type = "button";
                cancel.textContent = "예약 취소";
                cancel.addEventListener("click", async () => {
                    cancel.disabled = true;
                    errorNode.textContent = "";
                    try {
                        await requestJson(page.dataset.api + "/reservations/" + encodeURIComponent(row.reservationId)
                            + "/cancel", {method: "PATCH"});
                        await loadActivity();
                    } catch (error) {
                        showError(errorNode, error);
                        cancel.disabled = false;
                    }
                });
                item.append(" ", cancel);
            }
        });
        rows("previous-chats", history.data, (item, row) => {
            item.append(row.foodName + " · " + reservationState[row.reservationStatus] + " · " + row.counterpartNickname,
                " ", link(page.dataset.rooms, row.roomId, "대화 열기"));
        });
    }

    loadActivity().catch(error => showError(errorNode, error));
}
