# JSP 화면의 Vue.js 전환 구현 계획

이 문서는 `src/main/webapp/WEB-INF/views`의 JSP 화면을 `client` 디렉터리의 Vue.js 화면으로 옮기는 단계별 계획이다. 구현은 [LIMIT.md](./LIMIT.md)의 TODO 09까지 기능 사용 범위 제한을 준수한다.

## 1. 적용 원칙

### 1.1 반드시 지킬 제한

- Vue 3 + Vite + Composition API + Pinia + Vue Router + Axios 기반으로 구현한다.
- 모든 Vue 컴포넌트는 `script setup` 방식으로 작성한다.
- 화면 상태는 `ref`, `computed`, 필요한 경우 `watch`로 관리한다.
- 부모-자식 통신은 props down, emits up 방식으로 처리한다.
- 화면 이동은 `router.push({ name, query })`와 `router-link`를 우선 사용한다.
- 검색 조건, 정렬 조건, 페이지 번호 같은 목록 상태는 route query에 둔다.
- API 호출은 컴포넌트에서 직접 하지 않고 `client/src/composables`의 API composable로 분리한다.
- 로그인 상태, 로그인 사용자, 공통 alert 메시지는 Pinia store에서 관리한다.
- 인증은 현재 백엔드의 세션 기반 인증을 그대로 사용한다.

### 1.2 사용하지 않을 기능

- JWT access token, refresh token, token refresh, Authorization header interceptor
- route meta 기반 인증 가드의 실제 적용
- per-route guard, navigation failure 처리
- slot, Teleport, Suspense, keep-alive, transition
- Options API
- UI component framework, form validation 라이브러리, query 캐싱 라이브러리
- 카카오 지도/주소, AI 채팅 등 TODO 10 이후 기능

### 1.3 현재 코드와 LIMIT.md의 차이

`LIMIT.md`는 Axios를 허용 기술로 전제하지만 현재 `client/package.json`에는 `axios` 의존성이 없다. 구현 시에는 다음 중 하나로 정리해야 한다.

- 교육 템플릿 누락으로 판단하면 Axios만 의존성 보정한다.
- “새 라이브러리 추가 금지”를 엄격히 우선하면 API 연동 단계는 보류하고 문서/정적 화면 전환까지만 진행한다.

이 계획은 `LIMIT.md`의 “Axios와 API Composable 사용 가능” 전제를 따른다. 따라서 Axios 보정은 새 기능 도입이 아니라 제한 문서와 클라이언트 골격의 불일치 해소로 본다.

## 2. 전환 대상 화면

| JSP | Vue 전환 대상 |
| --- | --- |
| `common/header.jspf`, `common/navbar.jspf`, `common/footer.jspf`, `common/flash.jspf` | `App.vue`, `components/common/*` |
| `home/index.jsp` | `views/HomeView.vue` |
| `auth/login.jsp` | `views/auth/LoginView.vue` |
| `auth/signup.jsp` | `views/auth/SignupView.vue` |
| `profile/index.jsp` | `views/profile/ProfileView.vue` |
| `meal/list.jsp` | `views/meal/MealListView.vue` |
| `meal/detail.jsp` | `views/meal/MealDetailView.vue` |
| `meal/form.jsp` | `views/meal/MealFormView.vue` |
| `coach/index.jsp` | `views/coach/CoachView.vue` |
| `community/index.jsp` | `views/community/CommunityView.vue` |
| `challenge/index.jsp` | `views/challenge/ChallengeView.vue` |
| `social/index.jsp` | `views/social/SocialView.vue` |
| `error/error.jsp` | `views/ErrorView.vue` |

## 3. API 전환 전략

### 3.1 이미 있는 REST API로 전환 가능한 영역

- 인증: `POST /api/v1/auth/login`, `POST /api/v1/auth/signup`, `POST /api/v1/auth/logout`
- 사용자: `GET /api/v1/users/me`, `PUT /api/v1/users/me`
- 식단: `GET/POST/PUT/DELETE /api/v1/meals`
- 음식 검색: `GET /api/v1/foods?keyword=...`
- 헬스 체크: `GET /api/v1/health`

### 3.2 REST API 보강이 필요한 영역

JSP 컨트롤러만 있고 JSON API가 없는 화면은 Vue 전환 전에 백엔드 API를 얇게 추가한다.

| 영역 | 필요한 API |
| --- | --- |
| 홈 | 최근 식단, 오늘 영양, 목표, 코치 조언, 챌린지 일부, 팔로우 수를 묶은 dashboard API |
| 코치 | 기존 `GET /coach/dashboard`를 `/api/v1/coach/dashboard`로 정리하거나 Vue에서 호출 가능한 JSON API로 유지 |
| 커뮤니티 | 게시글 목록/작성/수정/삭제, 댓글 작성/수정/삭제 API |
| 챌린지 | 목록/생성/참여/진행률 수정/탈퇴/삭제 API |
| 소셜 | 팔로잉/팔로워/추천/리더보드, 팔로우/언팔로우 API |
| 프로필 통계 | 식단 수, 팔로잉 수, 팔로워 수, 참여 챌린지 수 API |

API 보강은 기존 service/repository 로직을 재사용하고 DTO만 추가한다. JPA/MyBatis 도입, 인증 구조 변경, 고급 검증 프레임워크 도입은 하지 않는다.

## 4. 단계별 구현 계획과 커밋 메시지

### 1단계. 클라이언트 기본 골격 정리

작업 내용:

- `client/src/App.vue`를 공통 레이아웃으로 교체한다.
- `components/common/AppHeader.vue`, `AppNavbar.vue`, `AppFooter.vue`, `AppAlert.vue`를 만든다.
- Bootstrap class 중심으로 JSP의 공통 레이아웃을 옮긴다.
- Router 기본 route를 등록한다.
- Pinia store에 `loginUser`, `isLoggedIn`, `alertMsg` 상태와 기본 action을 만든다.
- Axios 의존성/공통 API 인스턴스 불일치를 확인하고, LIMIT.md 전제에 맞춰 최소 보정한다.

검증:

- `client` 개발 서버에서 홈 route가 렌더링되는지 확인한다.
- 공통 header/footer가 모든 route에 표시되는지 확인한다.

커밋 메시지:

```text
feat(client): Vue 공통 레이아웃과 라우터 골격 구성
```

### 2단계. API composable 기반 정리

작업 내용:

- `client/src/composables/useApi.js` 또는 현재 TypeScript 구조에 맞춘 `useApi.ts`를 만든다.
- `auth`, `user`, `meal`, `food` API composable을 만든다.
- Axios 공통 인스턴스는 `withCredentials: true`를 사용해 세션 쿠키를 유지한다.
- interceptor 기반 token 처리, refresh 처리, JWT 저장은 구현하지 않는다.
- API 로딩/에러 상태는 `useApi`의 `isLoading`, `error`로 통일한다.

검증:

- 로그인 실패/성공 응답을 composable 단위에서 확인한다.
- `/api/v1/users/me` 호출 시 세션 유무에 따른 결과를 확인한다.

커밋 메시지:

```text
feat(client): 세션 기반 API composable 추가
```

### 3단계. 인증 화면 전환

작업 내용:

- `auth/login.jsp`를 `LoginView.vue`로 옮긴다.
- `auth/signup.jsp`를 `SignupView.vue`로 옮긴다.
- `v-model`, `@submit.prevent`, `isLoading`, `error`를 사용해 form을 구성한다.
- 로그인/회원가입 성공 시 Pinia store의 로그인 사용자 상태를 갱신한다.
- 로그아웃은 버튼 클릭으로 `POST /api/v1/auth/logout` 호출 후 store를 초기화한다.
- JWT, token 저장, route meta guard는 사용하지 않는다.

검증:

- 로그인 성공 후 홈으로 이동하는지 확인한다.
- 로그아웃 후 로그인 사용자 표시가 사라지는지 확인한다.
- 회원가입 성공 후 세션이 생성되는지 확인한다.

커밋 메시지:

```text
feat(auth): 로그인과 회원가입 화면을 Vue로 전환
```

### 4단계. 식단 목록/상세 화면 전환

작업 내용:

- `meal/list.jsp`를 `MealListView.vue`로 옮긴다.
- 날짜, 식사 타입, 정렬 조건은 route query로 관리한다.
- `GET /api/v1/meals`를 호출해 목록을 렌더링한다.
- `meal/detail.jsp`를 `MealDetailView.vue`로 옮긴다.
- 상세 식단, 음식 목록, 영양 요약을 `GET /api/v1/meals/{mealId}`로 조회한다.
- 삭제 버튼은 `DELETE /api/v1/meals/{mealId}`를 호출하고 목록으로 이동한다.
- 차트 라이브러리 없이 Bootstrap progress/table/card class와 텍스트로 영양 정보를 표현한다.

검증:

- query 변경 시 목록이 재조회되는지 확인한다.
- 식단 상세 이동과 삭제 후 목록 이동이 정상인지 확인한다.

커밋 메시지:

```text
feat(meal): 식단 목록과 상세 화면을 Vue로 전환
```

### 5단계. 식단 등록/수정 화면 전환

작업 내용:

- `meal/form.jsp`를 `MealFormView.vue`로 옮긴다.
- 음식 검색은 `GET /api/v1/foods?keyword=...`를 사용한다.
- 선택 음식 목록은 `ref` 배열로 관리하고, 섭취량은 `v-model`로 연결한다.
- 등록은 `POST /api/v1/meals`, 수정은 `PUT /api/v1/meals/{mealId}`를 호출한다.
- 검색 조건과 수정 대상 식단 ID는 route query를 사용한다.
- 고급 form validation 라이브러리 없이 서비스 오류 메시지와 기본 입력 제어만 사용한다.

검증:

- 음식 검색, 음식 추가/제거, gram 변경이 동작하는지 확인한다.
- 등록/수정 후 상세 화면으로 이동하는지 확인한다.

커밋 메시지:

```text
feat(meal): 식단 등록과 수정 화면을 Vue로 전환
```

### 6단계. 프로필 화면 전환

작업 내용:

- `profile/index.jsp`를 `ProfileView.vue`로 옮긴다.
- `GET /api/v1/users/me`로 사용자 정보를 조회한다.
- `PUT /api/v1/users/me`로 프로필을 수정한다.
- 식단 수, 팔로워 수, 팔로잉 수, 참여 챌린지 수는 별도 통계 API가 필요하면 백엔드에 최소 DTO/API를 추가한다.
- 계정 비활성화 기능은 기존 서비스 흐름을 재사용하는 API가 없으면 API를 추가한다.
- 프로필 이미지 업로드가 현재 백엔드에 없다면 LIMIT.md의 허용 범위와 백엔드 지원 범위를 분리해 후순위로 둔다.

검증:

- 프로필 조회/수정 후 store의 로그인 사용자 정보가 갱신되는지 확인한다.
- 통계 API가 없을 때 화면이 깨지지 않고 기본값을 보여주는지 확인한다.

커밋 메시지:

```text
feat(profile): 사용자 프로필 화면을 Vue로 전환
```

### 7단계. 홈과 코치 화면 전환

작업 내용:

- `home/index.jsp`를 `HomeView.vue`로 옮긴다.
- 홈 대시보드용 REST API를 추가하거나 기존 service 조합으로 JSON DTO를 반환한다.
- `coach/index.jsp`를 `CoachView.vue`로 옮긴다.
- 기존 `GET /coach/dashboard`는 JSP 경로와 섞이지 않도록 `/api/v1/coach/dashboard`로 추가하는 방식을 우선한다.
- 코치 조언은 기존 규칙 기반 결과를 그대로 표시한다.
- AI 채팅, 외부 AI 호출, streaming UI는 구현하지 않는다.

검증:

- 로그인 사용자 기준 홈 요약이 렌더링되는지 확인한다.
- 코치 대시보드 JSON 결과가 Vue 화면에 표시되는지 확인한다.

커밋 메시지:

```text
feat(dashboard): 홈과 코치 화면을 Vue로 전환
```

### 8단계. 커뮤니티 화면과 API 전환

작업 내용:

- 커뮤니티 REST API를 추가한다.
- `community/index.jsp`를 `CommunityView.vue`로 옮긴다.
- 게시글 목록은 category query로 필터링한다.
- 게시글/댓글 작성과 수정은 같은 화면 안에서 `v-if`로 form 상태를 전환한다.
- 수정 가능 여부는 로그인 사용자 ID와 작성자 ID 비교 결과를 `computed`로 처리한다.
- modal, slot, dynamic component 없이 단순 조건부 렌더링으로 구성한다.

검증:

- 카테고리 필터, 게시글 작성/수정/삭제가 동작하는지 확인한다.
- 댓글 작성/수정/삭제가 동작하는지 확인한다.

커밋 메시지:

```text
feat(community): 커뮤니티 화면과 API를 Vue 기준으로 전환
```

### 9단계. 챌린지 화면과 API 전환

작업 내용:

- 챌린지 REST API를 추가한다.
- `challenge/index.jsp`를 `ChallengeView.vue`로 옮긴다.
- 챌린지 목록, 생성, 참여, 진행률 수정, 탈퇴, 삭제를 구현한다.
- 진행률 form은 `v-model`과 기본 숫자 input으로 처리한다.
- 메모리 저장소 한계는 유지하고, DB 영속화는 이 전환 범위에 포함하지 않는다.

검증:

- 챌린지 생성/참여/진행률 수정/탈퇴/삭제 흐름을 확인한다.
- 서버 재시작 시 데이터가 사라지는 현재 한계를 문서와 화면 흐름에서 혼동하지 않는지 확인한다.

커밋 메시지:

```text
feat(challenge): 챌린지 화면과 API를 Vue 기준으로 전환
```

### 10단계. 소셜 화면과 API 전환

작업 내용:

- 소셜 REST API를 추가한다.
- `social/index.jsp`를 `SocialView.vue`로 옮긴다.
- 팔로잉, 팔로워, 추천 사용자, 리더보드를 탭이 아니라 단순 버튼/조건부 영역으로 구성한다.
- 팔로우/언팔로우 후 목록을 재조회한다.
- 메모리 저장소 한계는 유지하고, DB 영속화는 별도 과제로 둔다.

검증:

- 팔로우/언팔로우 후 추천 목록과 팔로잉 목록이 갱신되는지 확인한다.
- 리더보드가 팔로워 수 기준으로 표시되는지 확인한다.

커밋 메시지:

```text
feat(social): 소셜 화면과 API를 Vue 기준으로 전환
```

### 11단계. 라우팅, 정적 리소스, Spring 연동 정리

작업 내용:

- Vue 개발 서버와 Spring API 서버의 개발 실행 방식을 정리한다.
- 운영 빌드 산출물을 Spring 정적 리소스로 서빙할지, 프론트/백엔드 분리 배포로 둘지 결정한다.
- Vue Router history mode 새로고침 대응을 Spring fallback 설정으로 처리한다.
- 기존 JSP route와 Vue route가 충돌하지 않도록 점진 전환 경로를 정리한다.
- `/api/v1/**`, `/batch/**`, `/swagger-ui/**`는 fallback 대상에서 제외한다.

검증:

- Vue route 직접 접근과 새로고침이 동작하는지 확인한다.
- REST API와 Swagger UI 접근이 Vue fallback에 잡히지 않는지 확인한다.

커밋 메시지:

```text
chore(web): Vue 라우팅과 Spring 정적 리소스 연동 정리
```

### 12단계. JSP 제거 준비와 문서 갱신

작업 내용:

- Vue 전환이 끝난 JSP 화면을 목록화한다.
- 아직 유지해야 하는 JSP와 제거 가능한 JSP를 분리한다.
- `README.md`, `docs/RESEARCH.md`, `docs/API.md`에 Vue 전환 결과와 API 변경 사항을 반영한다.
- `docs/LIMIT.md`에서 실제 구현 중 확인한 제한 준수 여부와 예외 판단을 업데이트한다.
- JSP 파일 삭제는 모든 Vue route와 API 검증 후 별도 커밋으로 진행한다.

검증:

- 사용자 핵심 흐름을 브라우저에서 수동 검증한다.
- 백엔드 테스트와 클라이언트 build/type-check/lint를 실행한다.

커밋 메시지:

```text
docs(plan): Vue 전환 결과와 남은 JSP 정리 기준 문서화
```

## 5. 권장 구현 순서 요약

1. 공통 레이아웃과 라우터
2. API composable과 세션 store
3. 인증
4. 식단 목록/상세
5. 식단 등록/수정
6. 프로필
7. 홈/코치
8. 커뮤니티
9. 챌린지
10. 소셜
11. Spring 정적 리소스/fallback 연동
12. 문서 갱신과 JSP 제거 준비

이 순서는 이미 REST API가 있는 영역을 먼저 전환해 Vue 구조를 안정화하고, 이후 API 보강이 필요한 화면으로 확장하는 방식이다.

## 6. 최종 검증 체크리스트

- `client`에서 Vue route가 모두 렌더링된다.
- 모든 API 호출은 composable을 통해 이뤄진다.
- 컴포넌트에서 axios를 직접 호출하지 않는다.
- JWT/token/interceptor refresh 로직이 없다.
- route meta 인증 가드를 실제 적용하지 않는다.
- 검색/정렬/페이지 상태는 route query로 유지된다.
- 새 외부 UI/validation/query 라이브러리를 추가하지 않는다.
- Spring 세션 쿠키 기반 로그인/로그아웃이 유지된다.
- JSP와 Vue route 충돌이 없다.
- 기존 JSP 기반 핵심 기능과 Vue 전환 후 기능 범위가 일치한다.
