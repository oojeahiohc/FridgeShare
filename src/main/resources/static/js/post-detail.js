"use strict";

{
    const page = document.querySelector("#post-detail");
    const errorNode = document.querySelector("#error");
    const buttons = page.querySelectorAll("[data-action]");

    for (const button of buttons) {
        button.addEventListener("click", async () => {
            const {action, url, method, confirm} = button.dataset;
            if (confirm && !window.confirm(confirm)) return;

            // 예약 상태가 바뀌는 요청을 보내는 동안 다른 버튼의 중복 요청을 막는다.
            buttons.forEach(item => { item.disabled = true; });
            errorNode.textContent = "";
            try {
                const response = await requestJson(url, {method});
                if (action === "delete") {
                    location.assign(page.dataset.posts);
                } else if (action === "start") {
                    location.assign(page.dataset.rooms + "/" + encodeURIComponent(response.data.roomId));
                } else {
                    // 새 상태와 버튼은 상세 JSP에서 다시 렌더링한다.
                    location.reload();
                }
            } catch (error) {
                showError(errorNode, error);
                buttons.forEach(item => { item.disabled = false; });
            }
        });
    }
}
