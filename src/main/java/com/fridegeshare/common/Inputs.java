package com.fridegeshare.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public final class Inputs {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private Inputs() {
    }

    public static LocalDate today() {
        return LocalDate.now(SEOUL);
    }

    // DB 지역 시각에 서울 오프셋 부여
    public static OffsetDateTime timestamp(LocalDateTime value) {
        return value.atZone(SEOUL).toOffsetDateTime();
    }

    // 앞뒤 공백 제거 및 유니코드 글자 수 검증
    public static String text(String value, String label, int min, int max) {
        String normalized = value == null ? "" : value.strip();
        int length = normalized.codePointCount(0, normalized.length());
        if (length < min || length > max) {
            throw ApiException.invalid(label + " 길이는 " + min + "~" + max + "자여야 합니다.");
        }
        return normalized;
    }
}