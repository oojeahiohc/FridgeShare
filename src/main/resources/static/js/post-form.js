"use strict";

{
    const form = document.querySelector("#post-form");
    const errorNode = document.querySelector("#error");
    const button = form.querySelector("button");
    const editing = Boolean(form.dataset.id);
    const endpoint = form.dataset.api + (editing ? "/" + encodeURIComponent(form.dataset.id) : "");

    // 수정 화면의 기존 입력값 로딩 후 저장 버튼 활성화
    async function prepare() {
        try {
            if (editing) {
                const {data} = await requestJson(endpoint);
                for (const name of ["title", "foodName", "quantity", "expiryDate", "description"]) {
                    form.elements[name].value = data[name];
                }
            }
            button.disabled = false;
        } catch (error) {
            showError(errorNode, error);
        }
    }

    form.addEventListener("submit", async event => {
        event.preventDefault();
        button.disabled = true;
        errorNode.textContent = "";
        try {
            const {data} = await requestJson(endpoint, {
                method: editing ? "PUT" : "POST",
                body: Object.fromEntries(new FormData(form))
            });
            location.assign(form.dataset.posts + "/" + encodeURIComponent(data.id));
        } catch (error) {
            showError(errorNode, error);
            button.disabled = false;
        }
    });
    prepare();
}
