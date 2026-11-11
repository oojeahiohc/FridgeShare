"use strict";

{
    const page = document.querySelector("#chat-room");
    const form = document.querySelector("#chat-form");
    const input = form.elements.content;
    const button = form.querySelector("button");
    const list = document.querySelector("#messages");
    const notice = document.querySelector("#chat-status");
    const errorNode = document.querySelector("#error");
    let afterId = 0;
    let closed = false;
    let stopped = false;
    let sending = false;
    let loaded = false;

    // 채팅 종료·권한 상실·전송 중 상태의 입력 제어
    function syncInput() {
        input.disabled = !loaded || closed || stopped;
        button.disabled = input.disabled || sending;
        if (closed) notice.textContent = "종료된 예약의 대화는 읽기 전용입니다.";
        else if (loaded) notice.textContent = "";
    }

    // 마지막으로 받은 메시지 이후만 조회하고 응답이 끝난 뒤 다음 조회를 예약한다.
    async function poll() {
        try {
            const feed = await requestJson(page.dataset.api + "?" + new URLSearchParams({afterId}));
            for (const message of feed.data) {
                const item = document.createElement("li");
                const speaker = String(message.senderId) === page.dataset.myId ? "나" : "상대방";
                item.textContent = speaker + " · " + message.createdAt + "\n" + message.content;
                list.append(item);
                afterId = message.id;
            }
            loaded = true;
            if (!feed.canSend) closed = true;
            syncInput();
        } catch (error) {
            showError(errorNode, error);
            if ([401, 403, 404].includes(error.status)) {
                stopped = true;
                syncInput();
            }
        } finally {
            if (!stopped) setTimeout(poll, 3000);
        }
    }

    form.addEventListener("submit", async event => {
        event.preventDefault();
        if (button.disabled) return;
        sending = true;
        syncInput();
        errorNode.textContent = "";
        try {
            await requestJson(page.dataset.api, {method: "POST", body: {content: input.value}});
            input.value = "";
        } catch (error) {
            showError(errorNode, error);
            // 조회 사이에 예약이 종료된 경우 전송 응답으로도 읽기 전용 상태를 반영한다.
            if (error.code === "CHAT_NOT_AVAILABLE") closed = true;
        } finally {
            sending = false;
            syncInput();
        }
    });
    poll();
}
