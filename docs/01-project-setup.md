### Spring Boot 프로젝트 초기 구성과 빌드
- Spring Initializr을 사용하지 않고 빈 폴더에서 직접 구성한다.
- 사전 준비: JDK 17, Git, VSCode java 확장, Docker Desktop
1. VSCode에서 빈 FridegeShare 폴더 열기
2. VSCode 터미널 기본 프로필을 Git Bash로 설정
    - 설정 경로: 명령 팔레트(Ctrl + Shift + P)에서 Terminal: Select Default Profile → Git Bash
3. Gradle Wrapper 파일 다운로드
    - Gradle 공식 저장소의 v8.14.3 태그에서 gradlew, gradlew.bat, gradle/wrapper/gradle-wrapper.jar를 내려받는다.
```
(
  set -e
  java -version
  git --version
  mkdir -p gradle/wrapper
  source_url='https://raw.githubusercontent.com/gradle/gradle/v8.14.3'
  for file in gradlew gradlew.bat gradle/wrapper/gradle-wrapper.jar; do
    curl -fsSL "$source_url/$file" -o "$file"
  done
  chmod +x gradlew
)
```
- 용어
    | 파일 | 역할 |
    | --- | --- |
    | `gradlew` | Git Bash에서 Gradle 실행<br> ./gradlew build처럼 실행 |
    | `gradlew.bat` | Windows PowerShell에서 Gradle 실행<br> .\gradlew.bat build처럼 실행  |
    | `gradle/wrapper/gradle-wrapper.jar` | 두 스크립트가 공통으로 호출하는 Java 프로그램.<br> 설정 파일을 읽고, 지정된 Gradle 버전이 없으면 내려받은 뒤 실행 |
4. [`gradle-wrapper.properties`](../gradle/wrapper/gradle-wrapper.properties)
    - distributionUrl에 Gradle 8.14.3 배포본을 지정(프로젝트를 같은 Gradle 버전으로 빌드하기 위해)
- 설정 후 Git Bash에서 `./gradlew --version`로 확인한다.
    - 버전이 뜨면 완료
5. [`settings.gradle`](../settings.gradle)
    - Gradle 루트 프로젝트 이름
    ```
    rootProject.name = 'FridegeShare'
    ```
6. [`build.gradle`](../build.gradle)
    - Java 17을 기준으로 빌드하고 실행형 WAR를 구성
    - 의존성 추가: Spring Web, Spring Security, MyBatis Framework, MySQL Driver, Tomcat Jasper, JSTL API 및 구현체
7. .gitignore
8. .gitattributes
9. [`application.yml`](../src/main/resources/application.yml)
    - JSP, 데이터소스, 세션, 포트 설정
10. [`FridegeShareApplication.java`](../src/main/java/com/fridegeshare/FridegeShareApplication.java)
    - Spring Boot 실행 진입점
---
### 기본 빌드 설정 검증
- Git Bash에서 `./gradlew bootWar`를 실행하여 BUILD SUCCESSFUL 확인
