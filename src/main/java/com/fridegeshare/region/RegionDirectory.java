package com.fridegeshare.region;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fridegeshare.common.ApiException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class RegionDirectory {
    public record Region(String code, String province, String district, String town) {
        // 시·군·구가 없는 지역의 표시명 조합
        public String label() {
            return "-".equals(district) ? province + " " + town : province + " " + district + " " + town;
        }
    }

    private final Map<String, Region> byCode = new LinkedHashMap<>();

    // TSV의 기준일 확인 후 회원가입과 동네 코드 검사에 쓸 목록 불러오기
    public RegionDirectory() throws IOException {
        var resource = new ClassPathResource("regions/neighborhoods.tsv");
        try (var reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String metadata = reader.readLine();
            if (metadata == null || !metadata.matches("# as-of=\\d{4}-\\d{2}-\\d{2}")) {
                throw new IllegalStateException("행정동 자료 기준일이 필요합니다.");
            }
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split("\\t", -1);
                if (columns.length != 4 || !columns[0].matches("[0-9]{10}")
                        || columns[1].isBlank() || columns[2].isBlank() || columns[3].isBlank()) {
                    throw new IllegalStateException("행정동 자료 형식이 올바르지 않습니다.");
                }
                var region = new Region(columns[0], columns[1], columns[2], columns[3]);
                if (byCode.putIfAbsent(region.code(), region) != null) {
                    throw new IllegalStateException("행정동 코드가 중복되었습니다.");
                }
            }
        }
        if (byCode.isEmpty()) {
            throw new IllegalStateException("행정동 자료가 비어 있습니다.");
        }
    }

    public List<Region> all() {
        return List.copyOf(byCode.values());
    }

    public Region require(String code) {
        Region region = byCode.get(code);
        if (region == null)
            throw ApiException.invalid("목록에서 활동 동네를 선택하세요.");
        return region;
    }

    public String label(String code) {
        Region region = byCode.get(code);
        return region == null ? code : region.label();
    }
}