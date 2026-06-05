# TODO 09까지의 Vue.js 활용 제한

이 문서는 본 교육용 저장소에서 TODO 09번까지 진행했다고 가정했을 때, 학습자가 이미 배운 기술과 사용할 수 있는 Vue.js 기능의 범위를 정리한다. 기준은 현재 저장소의 `TODO:` 주석, Vue 파일 구조, composable/API 구현 흐름이다.

## 전제

- TODO 01부터 TODO 09까지 완료한 상태를 기준으로 한다.
- TODO 10 이후에 등장하는 기능은 아직 학습하지 않은 것으로 본다.
- 기존 JSP 화면을 Vue 화면과 컴포넌트로 옮기는 것이 주요 학습 목표다.
- Vue 3 + Vite + Composition API + Pinia + Vue Router + Axios 기반의 실습 범위 안에서 구현한다.

## TODO 09까지 배운 기술

### 프로젝트 기본 구조

- Vite 기반 Vue 3 프로젝트 실행 구조를 이해한다.
- `src/main.js`에서 Vue 앱을 생성하고 Pinia, Router를 등록한 뒤 `#app`에 mount하는 흐름을 이해한다.
- `src/App.vue`를 최상위 레이아웃으로 사용하고, 공통 헤더/푸터와 `router-view`로 화면을 구성한다.
- `@` alias로 `src` 하위 파일을 import하는 방식을 사용할 수 있다.

### Single File Component

- `.vue` 파일의 `template`, `script setup`, `style scoped` 구조를 사용할 수 있다.
- JSP 화면을 Vue 컴포넌트로 옮기면서 정적인 HTML을 Vue 템플릿 문법으로 바꿀 수 있다.
- Bootstrap class를 그대로 활용해 레이아웃과 기본 UI를 구성할 수 있다.
- `scoped` style과 전역 style의 차이를 이해하고 필요한 곳에 적용할 수 있다.

### Vue 템플릿 문법

사용 가능하다.

- `{{ }}` 보간으로 데이터를 출력한다.
- `v-if`, `v-else`, `v-for`를 사용해 조건부 렌더링과 반복 렌더링을 구현한다.
- `:class`, `:src`, `:disabled`, `:to` 같은 동적 바인딩을 사용한다.
- `@click`, `@submit.prevent`, `@input` 같은 이벤트 바인딩을 사용한다.
- `v-model`로 form input과 반응형 상태를 연결한다.
- `router-link`로 라우트 이동 링크를 만든다.

### Composition API 기초

사용 가능하다.

- `ref`로 form 데이터, API 결과, 로딩 상태, 에러 상태 같은 반응형 값을 관리한다.
- `computed`로 로그인 사용자 기준의 수정 가능 여부, 프로필 이미지 경로, 페이지 번호 목록 같은 파생 상태를 만든다.
- `watch`로 로그인 상태, 삭제 결과, 라우트 query 변경을 감시한다.
- `watchEffect`로 props 기반 local 상태를 동기화한다.
- `script setup`에서 import, 상태 선언, 함수 선언을 작성한다.

### Props와 Emits

사용 가능하다.

- `defineProps`로 부모가 전달하는 데이터를 받는다.
- props에는 `type`, `required`, `default` 정도의 기본 옵션을 설정할 수 있다.
- `defineEmits`로 자식 컴포넌트의 이벤트를 선언한다.
- 자식 컴포넌트에서 `emit`으로 검색 조건 변경, 페이지 변경, 프로필 변경 같은 이벤트를 부모에게 알린다.
- 부모는 `@update-condition`, `@change-page`, `@profile-update` 같은 이벤트 리스너로 자식 이벤트를 처리한다.

### Vue Router

사용 가능하다.

- `createRouter`, `createWebHistory`로 라우터를 구성한다.
- `/member`, `/auth` 하위에 children route를 구성한다.
- `name` 기반 라우팅을 사용한다.
- `component: () => import(...)` 형태의 lazy import를 사용할 수 있다.
- `useRouter`로 화면 이동을 처리한다.
- `useRoute`로 현재 route의 `query`를 읽는다.
- query string을 검색 조건, 현재 페이지, 회원 이메일 전달에 활용한다.
- `watch(() => route.query, ...)`로 같은 컴포넌트 안에서 query 변경에 따른 데이터 재조회 흐름을 구현한다.
- 기본적인 `beforeEach` 가드 구조를 살펴본 수준까지는 허용한다.

### Pinia 상태 관리

사용 가능하다.

- `defineStore`의 setup store 형태를 사용할 수 있다.
- `ref`로 내부 상태를 만들고, `computed` getter로 외부에 공개한다.
- 로그인 여부, 로그인 사용자, 공통 alert 메시지를 store에서 관리한다.
- store action 성격의 일반 함수를 작성해 상태를 변경한다.
- `pinia-plugin-persistedstate`를 등록하고 sessionStorage 기반 persist 설정을 살펴본 범위까지 사용할 수 있다.

다만 TODO 09 기준에서는 JWT 토큰 저장과 갱신 로직은 학습 범위가 아니다. `_tokens`, `tokens`, `login`, `logout`, `refresh`처럼 TODO 21 이후를 위한 구조는 존재하더라도 실제 구현 대상으로 보지 않는다.

### Axios와 API Composable

사용 가능하다.

- `axios.create`로 공통 API 인스턴스를 만든다.
- `baseURL`을 `import.meta.env` 환경 변수에서 읽는다.
- 공통 `useApi(apiFunction)` composable로 `data`, `error`, `isLoading`, `execute`를 관리한다.
- 기능별 API composable을 만들어 컴포넌트에서 호출한다.
- `GET`, `POST`, `PATCH`, `DELETE` 요청을 사용할 수 있다.
- query parameter는 `params`로 전달한다.
- 회원가입, 이메일 중복 체크, 회원 목록 조회, 회원 삭제, 프로필 수정 API를 구현할 수 있다.
- 파일 업로드에는 `FormData`를 사용할 수 있다.
- API 호출 중 버튼이나 table을 disabled 처리하는 정도의 로딩 UI를 구현할 수 있다.

### TODO 09까지 구현 가능한 화면과 기능

구현 가능하다.

- 공통 헤더, 푸터, 최상위 레이아웃
- 홈 화면
- 로그인 화면
- 로그아웃 처리
- 회원가입 화면
- 이메일 중복 체크
- 회원 목록 화면
- 검색 조건 컴포넌트
- 페이지네이션 컴포넌트
- 회원 목록 아이템 컴포넌트
- 회원 상세 정보 중 기본 회원 정보 영역
- 로그인 사용자 또는 ADMIN 여부에 따른 수정 가능 버튼 노출
- 회원 삭제
- 프로필 이미지 표시
- 프로필 이미지 변경
- 삭제 또는 로그인 상태 변경 후 라우터 이동

## 사용하면 안 되는 기능

아래 항목은 TODO 09까지의 학습 범위를 넘어서므로 사용하지 않는다.

### TODO 10 이후 기능

- 카카오 주소 검색 또는 주소-좌표 변환 API
- `useKakaoApis.js`
- `AppKakaoMap.vue`
- 지도 렌더링, 마커 표시, 주소 기반 지도 갱신
- 회원 주소 목록 표시, 주소 추가, 주소 삭제
- 회원 상세 화면 전체를 주소/지도까지 통합하는 TODO 12 범위
- 회원 수정 폼 구현과 회원 정보 수정 API
- JWT 로그인 API 연동
- access token, refresh token 저장
- request interceptor에서 Authorization header 자동 추가
- 401 응답 처리와 token refresh
- 세션 만료 처리
- AI 채팅 화면 구현

### 고급 Vue 기능

TODO 09까지의 실습에서는 다음 기능을 사용하지 않는다.

- Options API 기반 `data`, `methods`, `created`, `mounted` 작성
- `provide` / `inject`
- 커스텀 directive
- render function, JSX
- `defineExpose`
- `defineModel`
- `Teleport`
- `Suspense`
- async component를 직접 제어하는 고급 패턴
- dynamic component의 `is` 속성 기반 화면 전환
- transition, transition-group
- keep-alive
- slot, named slot, scoped slot
- composable 내부에서 과도하게 복잡한 전역 상태를 만드는 패턴

### 고급 Router 기능

다음은 사용하지 않는다.

- route meta 기반 인증 가드의 실제 적용
- nested layout route를 이용한 복잡한 레이아웃 분기
- per-route guard
- navigation failure 처리
- history state 직접 조작
- 동적 path parameter 중심 설계

TODO 09까지는 query 기반 화면 상태 관리와 name 기반 이동만 사용한다.

### 고급 상태 관리

다음은 사용하지 않는다.

- Pinia plugin 직접 작성
- store 간 복잡한 의존 구조
- `$patch`, `$subscribe`, `$onAction` 중심 구현
- JWT token을 decode해서 로그인 상태를 복원하는 기능
- localStorage/sessionStorage를 직접 조작하는 인증 로직

### 외부 라이브러리 추가

새 라이브러리를 추가하지 않는다. TODO 09까지는 현재 `package.json`에 있는 Vue, Vue Router, Pinia, Axios, NProgress, Bootstrap class 활용 수준으로 충분하다.

특히 다음 목적의 라이브러리는 사용하지 않는다.

- form validation 전용 라이브러리
- UI component framework
- 상태 관리 대체 라이브러리
- fetch/query 캐싱 라이브러리
- 지도, 주소 검색, geocoding 라이브러리
- JWT 인증 자동화 라이브러리

## 구현 방식 제한

### 컴포넌트 작성 방식

- 모든 Vue 컴포넌트는 `script setup` 방식으로 작성한다.
- 상태는 `ref`와 `computed`를 우선 사용한다.
- 부모-자식 통신은 props down, emits up 방식으로 처리한다.
- 화면 이동은 `router.push({ name, query })` 형태를 우선 사용한다.
- DOM 직접 접근은 TODO 09의 profile dialog/file input처럼 필요한 경우에만 제한적으로 사용한다.

### API 작성 방식

- 컴포넌트에서 axios를 직접 호출하지 않는다.
- API 호출은 `src/composables/useMemberApis.js`의 기능별 composable로 분리한다.
- 각 composable은 공통 `useApi`를 반환해 `data`, `error`, `execute`, `isLoading` 패턴을 유지한다.
- API 응답 payload 구조는 기존 코드 흐름을 따른다.
- 인증 token이 필요한 API라는 전제를 코드에 추가하지 않는다.

### 화면 상태 관리 방식

- 검색 조건과 페이지 번호는 route query에 둔다.
- API 로딩 상태는 `isLoading`으로 처리한다.
- 공통 alert 메시지는 Pinia member store의 `alertMsg`를 사용한다.
- 로그인 사용자의 이름, 이메일, 권한은 Pinia member store의 `loginUser`를 사용한다.

## 판단 기준

기능 사용 여부가 애매할 때는 다음 기준으로 판단한다.

- TODO 01-09 주석이 있는 파일에서 이미 사용된 문법이면 사용할 수 있다.
- TODO 10 이상의 주석에 처음 등장하는 기능이면 사용하지 않는다.
- 현재 컴포넌트 구조를 단순히 반복, 조합, 확장하는 정도면 사용할 수 있다.
- 인증, 지도, 주소, 회원수정, AI, token refresh와 연결되면 사용하지 않는다.
- 학습자가 JSP를 Vue로 변환하는 데 필요한 기본 Vue 기능이면 사용할 수 있다.
- 문제 해결은 가능하지만 고급 Vue 패턴으로 코드를 크게 우회하게 만드는 기능은 사용하지 않는다.

## 현재 Vue 전환 구현의 준수 결과

2026-06-05 기준 JSP 화면 Vue 전환 구현은 위 제한을 다음 방식으로 준수한다.

- 패키지 실행과 빌드는 `pnpm` 기준으로 수행한다.
- Vue 화면은 모두 `script setup` 방식으로 작성했다.
- 화면 상태는 `ref`, `computed`, `watch` 중심으로 관리한다.
- 화면 이동은 `router-link`와 `router.push({ name, query })`를 사용한다.
- 식단 목록 필터, 식단 수정 대상, 음식 검색어, 커뮤니티 카테고리처럼 화면 상태 보존이 필요한 값은 route query를 사용한다.
- API 호출은 `client/src/composables`의 기능별 composable에서만 수행하고, Vue 컴포넌트에서 `axios`를 직접 호출하지 않는다.
- Axios 공통 인스턴스는 세션 쿠키 유지를 위해 `withCredentials: true`만 사용한다.
- 로그인 상태와 공통 alert는 Pinia session store에서 관리한다.
- JWT access token, refresh token, Authorization header interceptor, token refresh 로직은 구현하지 않았다.
- route meta 기반 인증 가드와 per-route guard는 구현하지 않았다.
- UI component framework, form validation 라이브러리, query 캐싱 라이브러리, 지도/주소/AI 채팅 기능은 추가하지 않았다.

Axios 의존성은 이 문서의 허용 범위와 `docs/PLAN.md`의 전제에 맞춰 추가했다. 그 외 새 런타임 라이브러리는 추가하지 않았다.
