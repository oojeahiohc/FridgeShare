"use strict";

// 변경 요청의 CSRF 헤더 주입 및 JSON 오류 변환
async function requestJson(path, { method = "GET", body } = {}) {
    const headers = new Headers({ Accept: "application/json" });
    if (body !== undefined) headers.set("Content-Type", "application/json");
    if (["POST", "PUT", "PATCH", "DELETE"].includes(method)) {
        const header = document.querySelector('meta[name="csrf-header"]').content;
        const token = document.querySelector('meta[name="csrf-token"]').content;
        headers.set(header, token);
    }
    const response = await fetch(path, {
        method,
        headers,
        body: body === undefined ? undefined : JSON.stringify(body),
        credentials: "same-origin",
        cache: "no-store"
    });
    // 204 응답은 본문이 비어 있으므로 null 반환
    if (response.status === 204) return null;
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        const error = new Error(data.message || "요청을 처리하지 못했습니다.");
        error.code = data.code;
        error.status = response.status;
        throw error;
    }
    return data;
}

function showError(node, error) {
    node.textContent = error.status === 401
        ? "로그인이 만료되었습니다. 로그인한 뒤 다시 시도하세요."
        : error.message;
}