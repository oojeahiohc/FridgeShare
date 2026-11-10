"use strict";

{
    const page = document.querySelector("#posts-list");
    const form = document.querySelector("#search");
    const items = document.querySelector("#items");
    const empty = document.querySelector("#empty");
    const errorNode = document.querySelector("#error");
    const statusText = {AVAILABLE: "모집 중", RESERVED: "예약 중", COMPLETED: "나눔 완료"};

    async function loadPosts() {
        form.querySelector("button").disabled = true;
        errorNode.textContent = "";
        const keyword = form.elements.keyword.value.trim();
        const suffix = keyword ? "?" + new URLSearchParams({keyword}) : "";
        try {
            const {data} = await requestJson(page.dataset.api + suffix);
            items.replaceChildren();
            for (const post of data) {
                const row = items.insertRow();
                const link = document.createElement("a");
                link.href = page.dataset.posts + "/" + encodeURIComponent(post.id);
                link.textContent = post.title;
                row.insertCell().append(link);
                row.insertCell().textContent = post.foodName;
                row.insertCell().textContent = post.expiryDate;
                row.insertCell().textContent = statusText[post.status];
            }
            empty.hidden = data.length > 0;
        } catch (error) {
            items.replaceChildren();
            empty.hidden = true;
            showError(errorNode, error);
        } finally {
            form.querySelector("button").disabled = false;
        }
    }

    form.addEventListener("submit", event => {
        event.preventDefault();
        loadPosts();
    });
    loadPosts();
}
