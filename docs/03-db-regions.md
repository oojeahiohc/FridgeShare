### MariaDB 초기 설정과 행정동 데이터 준비
- [`compose.yaml`](../compose.yaml) Docker 컨테이너를 어떻게 실행할지 정하는 Docker Compose 설정 파일
    - MariaDB 11.4를 빈 볼륨에서 처음 시작하면 `MARIADB_DATABASE`와 `MARIADB_USER` 설정으로 DB와 접속 계정을 만들고 `db/schema.sql`로 테이블 5개를 생성한다.
    - 개발용 `mariadb_data`와 테스트용 `mariadb_test_data` 볼륨을 사용한다. 기존 MySQL 컨테이너가 같은 포트를 사용 중이면 먼저 중지한다.
    - 개발용 MariaDB는 호스트의 `3308`에 열리고 Spring Boot도 기본으로 `localhost:3308`에 연결한다. 포트를 바꾸려면 실행 전에 `DB_PORT`를 지정한다.
    - Git Bash에서 `DB_PASSWORD`를 설정한다. `DB_USERNAME`의 기본값은 `fridegeshare`이며, 다른 값을 쓰려면 MariaDB를 처음 시작하기 전에 지정한다.

    ```bash
    # Docker 명령 실행
    # 실행 전 Docker Desktop을 켜고 프로젝트 루트에 compose.yaml과 db/schema.sql이 있는 지 확인한다.

    read -rsp "MariaDB 비밀번호 입력: " DB_PASSWORD
    printf '\n'
    export DB_PASSWORD

    # 개발용 MariaDB 실행 및 준비 대기
    docker compose -p fridegeshare up -d --wait db

    # 공통 테이블 5개 확인
    docker compose -p fridegeshare exec db sh -c 'mariadb --user="$MARIADB_USER" --password="$MARIADB_PASSWORD" "$MARIADB_DATABASE" --execute="SHOW TABLES;"'
    ```

- `PostBranchTest`는 기본으로 별도 MariaDB(`localhost:3309`)에 연결한다. 접속 주소를 직접 지정하려면 `DB_URL`을 사용한다.

    ```bash
    docker compose -p fridegeshare --profile test up -d --wait db-test
    RUN_DB_TESTS=true ./gradlew test --tests 'com.fridegeshare.post.PostBranchTest'
    ```
---
- [`import-regions.py`](../scripts/import-regions.py)
    - 행정안전부 원본 ZIP에서 읍·면·동 데이터를 추출해 neighborhoods.tsv로 변환하는 Python 스크립트
    ```
    # 스크립트 실행
    python -X utf8 scripts/import-regions.py "scripts/jscode20261026.zip" 2026-10-26

    # 생성된 TSV의 처음 4줄 확인
    head -n 4 src/main/resources/regions/neighborhoods.tsv
    ```
---
- [`RegionDirectory.java`](../src/main/java/com/fridegeshare/region/RegionDirectory.java) neighborhoods.tsv를 읽어서 행정동 목록을 조회하는 클래스
    - 회원가입 화면에 시·도, 시·군·구, 읍·면·동 선택 목록을 제공
    - 회원가입 요청에 들어온 행정동 코드가 목록에 있는지 확인
    - 저장된 행정동 코드로 화면에 표시할 동네 이름을 찾는다.
