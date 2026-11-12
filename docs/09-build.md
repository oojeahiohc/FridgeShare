### 배포
- .dockerignore
    - Docker 빌드에 로컬 빌드 결과물과 환경변수 파일이 들어가지 않도록 한다.
- Dockerfile
    - build.gradle에서 bootWar 산출물 이름과 같은 이름으로 COPY 경로에 사용한다.