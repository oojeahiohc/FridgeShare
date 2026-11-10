"use strict";

{
    const page = document.querySelector("#post-detail");
    const errorNode = document.querySelector("#error");
    const remove = document.querySelector("#delete");
    const endpoint = page.dataset.api + "/" + encodeURIComponent(page.dataset.id);

    remove?.addEventListener("click", async () => {
        if (!window.confirm("이 게시글을 삭제할까요?")) return;
        remove.disabled = true;
        try {
            await requestJson(endpoint, {method: "DELETE"});
            location.assign(page.dataset.posts);
        } catch (error) {
            showError(errorNode, error);
            remove.disabled = false;
        }
    });
}
