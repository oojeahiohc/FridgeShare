"use strict";

{
    const page = document.querySelector("#posts-list");
    const form = document.querySelector("#search");
    const items = document.querySelector("#items");
    const empty = document.querySelector("#empty");
    const errorNode = document.querySelector("#error");
    const pageStatus = document.querySelector("#page-status");
    const previous = document.querySelector("#previous");
    const next = document.querySelector("#next");
    const searchButton = form.querySelector('button[type="submit"]');
    const statusText = {AVAILABLE: "모집 중", RESERVED: "예약 중", COMPLETED: "나눔 완료"};
    let currentPage = 1;
    let activeKeyword = "";

    async function loadPosts(pageNumber, keyword) {
        if (searchButton.disabled) return;
        const previousWasDisabled = previous.disabled;
        const nextWasDisabled = next.disabled;
        searchButton.disabled = previous.disabled = next.disabled = true;
        errorNode.textContent = "";
        const params = new URLSearchParams();
        if (keyword) params.set("keyword", keyword);
        try {
            let data;
            do {
                params.set("page", String(pageNumber));
                ({data} = await requestJson(page.dataset.api + "?" + params));
                pageNumber = data.totalPages;
            } while (data.totalPages > 0 && data.page > data.totalPages);

            items.replaceChildren();
            for (const post of data.posts) {
                const row = items.insertRow();
                const link = document.createElement("a");
                link.href = page.dataset.posts + "/" + encodeURIComponent(post.id);
                link.textContent = post.title;
                row.insertCell().append(link);
                row.insertCell().textContent = post.foodName;
                row.insertCell().textContent = post.expiryDate;
                row.insertCell().textContent = statusText[post.status];
            }
            currentPage = data.page;
            activeKeyword = keyword;
            empty.hidden = data.posts.length > 0;
            pageStatus.textContent = data.totalPages === 0
                ? "총 0건, 0페이지"
                : `${data.page} / ${data.totalPages}페이지, 총 ${data.totalCount}건`;
            previous.disabled = !data.hasPrevious;
            next.disabled = !data.hasNext;
        } catch (error) {
            previous.disabled = previousWasDisabled;
            next.disabled = nextWasDisabled;
            showError(errorNode, error);
        } finally {
            searchButton.disabled = false;
        }
    }

    form.addEventListener("submit", event => {
        event.preventDefault();
        loadPosts(1, form.elements.keyword.value.trim());
    });
    previous.addEventListener("click", () => loadPosts(currentPage - 1, activeKeyword));
    next.addEventListener("click", () => loadPosts(currentPage + 1, activeKeyword));
    loadPosts(1, activeKeyword);
}
