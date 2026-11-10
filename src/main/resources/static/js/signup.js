"use strict";

{
    const form = document.querySelector("#signup");
    const province = document.querySelector("#province");
    const district = document.querySelector("#district");
    const town = document.querySelector("#town");
    // 서버가 렌더링한 행정동 목록을 사용해 추가 요청 없이 지역을 좁힌다.
    const catalog = [...document.querySelector("#region-data").content.querySelectorAll("option")];
    const errorNode = document.querySelector("#error");
    const button = form.querySelector("button");

    // 상위 지역 변경 시 하위 선택값 초기화
    function fill(select, prompt, entries) {
        select.replaceChildren(new Option(prompt, ""));
        entries.forEach(([label, value]) => select.add(new Option(label, value)));
        select.disabled = entries.length === 0;
    }

    const provinces = [...new Set(catalog.map(item => item.dataset.province))];
    fill(province, "선택", provinces.map(name => [name, name]));
    fill(district, "선택", []);
    fill(town, "선택", []);

    province.addEventListener("change", () => {
        const districts = [...new Set(catalog.filter(item => item.dataset.province === province.value)
            .map(item => item.dataset.district))];
        fill(district, "선택", districts.map(name => [name === "-" ? "해당 없음" : name, name]));
        fill(town, "선택", []);
    });

    district.addEventListener("change", () => {
        const towns = catalog.filter(item => item.dataset.province === province.value
            && item.dataset.district === district.value);
        fill(town, "선택", towns.map(item => [item.textContent, item.value]));
    });

    form.addEventListener("submit", async event => {
        event.preventDefault();
        if (!town.value) {
            errorNode.textContent = "활동 동네를 선택하세요.";
            return;
        }
        // 가입 응답을 기다리는 동안 중복 제출을 막는다.
        button.disabled = true;
        errorNode.textContent = "";
        try {
            await requestJson(form.dataset.api, { method: "POST", body: Object.fromEntries(new FormData(form)) });
            location.assign(form.dataset.next);
        } catch (error) {
            showError(errorNode, error);
            button.disabled = false;
        }
    });
}