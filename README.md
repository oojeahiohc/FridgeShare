# FridgeShare

Java 17, Spring MVC, JSP, MyBatis, MariaDB 기반 식재료 나눔 서비스입니다. Gradle로 실행형 WAR를 빌드합니다.

## 로컬 실행

JDK 17과 Docker Desktop을 준비한 뒤, 프로젝트 루트에서 Git Bash를 열고 실행합니다.

```bash
read -rsp 'MariaDB 비밀번호: ' DB_PASSWORD
printf '\n'
export DB_PASSWORD
export DB_USERNAME="${DB_USERNAME:-fridegeshare}"

docker compose -p fridegeshare up -d --wait db
./gradlew bootWar
java -Duser.timezone=Asia/Seoul -jar build/libs/FridegeShare.war
```

- 회원가입 화면은 [http://localhost:8080/signup](http://localhost:8080/signup)에서 확인합니다.
- 개발 DB의 기본 주소는 `localhost:3308`입니다.
- 행정동 데이터는 `src/main/resources/regions/neighborhoods.tsv`에 있으며 기준일은 `2026-10-26`입니다.

## 통합 테스트

로컬 실행에서 설정한 환경변수를 사용합니다. 테스트 DB는 `localhost:3309`에서 실행됩니다.

```bash
docker compose -p fridegeshare --profile test up -d --wait db-test

DB_URL='jdbc:mariadb://localhost:3309/fridegeshare' \
  RUN_DB_TESTS=true ./gradlew clean test bootWar
```

## Docker 이미지 검증

앞에서 실행한 앱이 아직 켜져 있다면, 해당 터미널에서 Ctrl+C를 눌러 종료합니다.  
아래 명령으로 앱을 Docker에서 실행하면, 앱이 Docker에 띄운 개발용 MariaDB(db:3306)에 연결됩니다.

```bash
docker compose -p fridegeshare up -d --wait db
docker build -t fridegeshare:local .

docker run --rm --network fridegeshare_default -p 8080:8080 \
  --env DB_USERNAME --env DB_PASSWORD \
  --env DB_URL=jdbc:mariadb://db:3306/fridegeshare \
  fridegeshare:local
```

## Cloudtype 배포

로컬에서는 MariaDB 컨테이너가 빈 데이터 볼륨으로 처음 시작할 때 `compose.yaml` 설정에 따라 데이터베이스와 앱 계정을 자동으로 만들고 `db/schema.sql`을 적용합니다. Cloudtype에 처음 배포할 때는 MariaDB 서비스에서 데이터베이스와 테이블을 직접 만들고 앱 전용 계정에 권한을 부여합니다.

### MariaDB 서비스 준비

1. 앱과 같은 배포환경에 MariaDB 서비스를 생성합니다. 영구 디스크와 `root` 비밀번호를 설정한 뒤 배포합니다. [Cloudtype 데이터베이스 문서](https://docs.cloudtype.dev/ko/developers/database)
2. MariaDB 서비스의 터미널을 엽니다. 셸이 열리면 `mariadb -u root -p`를 실행하고 `root` 비밀번호를 입력합니다. `MariaDB>` 프롬프트에서 아래 SQL을 실행합니다. [Cloudtype 터미널 문서](https://docs.cloudtype.dev/ko/developers/log)

   ```sql
   CREATE DATABASE IF NOT EXISTS fridegeshare CHARACTER SET utf8mb4;
   USE fridegeshare;
   ```

3. 같은 터미널에 [`db/schema.sql`](db/schema.sql)의 전체 내용을 붙여 넣어 실행합니다. `SHOW TABLES;`로 `users`, `food_posts`, `reservations`, `chat_rooms`, `chat_messages` 테이블이 생성됐는지 확인합니다.
4. 아래 SQL의 `REPLACE_WITH_APP_PASSWORD`를 앱 전용 계정의 비밀번호로 바꿔 실행합니다. `SHOW GRANTS` 결과에서 `fridegeshare` 데이터베이스에 `SELECT`, `INSERT`, `UPDATE` 권한이 부여됐는지 확인합니다. [MariaDB 계정 생성 문서](https://mariadb.com/docs/server/reference/sql-statements/account-management-sql-statements/create-user)

   ```sql
   CREATE USER 'fridegeshare_app'@'%' IDENTIFIED BY 'REPLACE_WITH_APP_PASSWORD';

   GRANT SELECT, INSERT, UPDATE ON fridegeshare.* TO 'fridegeshare_app'@'%';

   SHOW GRANTS FOR 'fridegeshare_app'@'%';
   ```

### 앱 배포

1. 앱 서비스에서 GitHub 저장소의 `main` 브랜치와 `Dockerfile`을 선택하고 포트를 `8080`, 인스턴스를 `1개`로 설정합니다.
2. MariaDB 서비스의 **리소스 → 내부 연결**에서 호스트와 포트를 확인한 뒤 아래 값을 앱 서비스의 환경변수에 입력하고 배포합니다.

   | 환경변수 | 값 |
   | --- | --- |
   | `DB_URL` | `jdbc:mariadb://<내부호스트>:<내부포트>/fridegeshare` |
   | `DB_USERNAME` | `fridegeshare_app` |
   | `DB_PASSWORD` | 앱 전용 계정을 만들 때 설정한 비밀번호 |

3. 배포된 HTTPS 주소에서 회원가입, 로그인, 검색, 10건 단위 페이지 이동, 예약, 채팅, 예약 완료·취소, 이전 대화 조회를 확인합니다.
