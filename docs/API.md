# YumYumCoach API Reference

## 1. 공통 사항

- 기본 주소: `http://localhost:8080`
- 인증 방식: 세션 기반 로그인
- 세션 키: `loginUserId`
- 뷰 반환: 일반 사용자 화면 경로는 Vue SPA `index.html`로 forward, 기존 JSP MVC 컨트롤러는 `/legacy/**`에서 JSP View 또는 Redirect
- JSON 반환: Vue SPA 전환을 위해 `/api/v1/**` REST API가 확장됨
- `PATCH`, `DELETE`는 `spring.mvc.hiddenmethod.filter.enabled=true` 설정에 따라 HTML form의 hidden `_method` 방식 사용 가능

### 1.1 인증 정책

다음 경로를 제외하면 로그인 필요다.

- `/`
- `/home`
- `/auth/login`
- `/auth/signup`
- `/auth/logout`
- `/meals`
- `/meals/detail`
- `/meals/new`
- `/meals/edit`
- `/profile`
- `/coach`
- `/community`
- `/challenges`
- `/social`
- `/index.html`
- `/legacy/auth/**`
- `/assets/**`
- `/css/**`
- `/js/**`
- `/images/**`
- `/webjars/**`
- `/favicon.ico`
- `/error`

SPA 문서 route는 공개되어 있지만, 화면 데이터는 `/api/v1/**` 호출에서 세션 인증을 다시 받는다.

### 1.2 미인증 응답

- 일반 HTML 요청: 로그인 페이지로 리다이렉트
- JSON/AJAX 요청: `401` + JSON

```json
{
  "code": "AUTH_REQUIRED",
  "message": "로그인이 필요한 메뉴입니다."
}
```

### 1.3 주요 값 규칙

| 항목 | 값 |
| --- | --- |
| `mealType` | `breakfast`, `lunch`, `dinner`, `snack` |
| `goal` | `health`, `diet`, `muscle` |
| 커뮤니티 `category` | `review`, `expert`, `free`, `all` |
| 챌린지 membership status | `joined`, `completed` |
| 소셜 `action` | `follow`, `unfollow` |
| 프로필 `action` | `deactivate`, `delete` |

## 2. AuthController

기본 경로: `/auth`

| Method | Path | Auth | 파라미터 | 설명 | 응답 |
| --- | --- | --- | --- | --- | --- |
| GET | `/auth/login` | No | 없음 | 로그인 폼 표시 | `auth/login` |
| POST | `/auth/login` | No | `email`, `password` | 로그인 처리 | 성공 시 `redirect:/home`, 실패 시 `auth/login` |
| GET | `/auth/signup` | No | 없음 | 회원가입 폼 표시 | `auth/signup` |
| POST | `/auth/signup` | No | `email`, `password`, `nickname`, `gender`, `birthYear`, `height`, `weight`, `goal`, `healthNote` | 회원가입 처리 | 성공 시 `redirect:/home`, 실패 시 `auth/signup` |
| GET | `/auth/logout` | No | 없음 | 세션 로그아웃 | `redirect:/auth/login` |

## 3. HomeController

| Method | Path | Auth | 파라미터 | 설명 | 응답 |
| --- | --- | --- | --- | --- | --- |
| GET | `/` | Yes | 없음 | 홈 대시보드 표시 | `home/index` |
| GET | `/home` | Yes | 없음 | 홈 대시보드 표시 | `home/index` |

### 화면에 실리는 주요 데이터

- 최근 식단 3건
- 오늘 영양 요약
- 일일 목표 칼로리/단백질
- AI 코치 요약
- 활성 챌린지 일부
- 팔로잉/팔로워 수

## 4. ProfileController

| Method | Path | Auth | 파라미터 | 설명 | 응답 |
| --- | --- | --- | --- | --- | --- |
| GET | `/profile` | Yes | 없음 | 내 프로필 화면 표시 | `profile/index` |
| POST | `/profile` | Yes | `action`, `email`, `nickname`, `password`, `gender`, `birthYear`, `height`, `weight`, `goal`, `healthNote` | 프로필 수정 또는 계정 비활성화 | 성공 시 `redirect:/profile` 또는 `redirect:/auth/login`, 실패 시 `profile/index` |

### 특이사항

- `action=deactivate` 또는 `action=delete`이면 실제 삭제가 아니라 `active=false` 처리 후 로그아웃한다.
- `password`는 비어 있으면 변경하지 않는다.

## 5. MealController

기본 경로: `/meals`

| Method | Path | Auth | 파라미터 | 설명 | 응답 |
| --- | --- | --- | --- | --- | --- |
| GET | `/meals` | Yes | `startDate`, `endDate`, `mealType`, `sortKey` | 식단 목록 조회 및 필터링 | `meal/list` |
| GET | `/meals/detail` | Yes | `mealId` | 식단 상세 및 분석 조회 | `meal/detail` |
| GET | `/meals/new` | Yes | `keyword`, `mealType` | 식단 등록 폼 표시 | `meal/form` |
| GET | `/meals/edit` | Yes | `mealId`, `keyword` | 식단 수정 폼 표시 | `meal/form` |
| POST | `/meals/new` | Yes | `mealDate`, `mealType`, `memo`, `foodCode[]`, `grams_{foodCode}` | 식단 생성 | 성공 시 `redirect:/meals`, 실패 시 `meal/form` |
| POST | `/meals/edit` | Yes | `mealId`, `mealDate`, `mealType`, `memo`, `foodCode[]`, `grams_{foodCode}` | 식단 수정 | 성공 시 `redirect:/meals/detail?mealId=...`, 실패 시 `meal/form` |
| POST | `/meals/delete` | Yes | `mealId` | 식단 삭제 | `redirect:/meals` |

### `sortKey` 값

| 값 | 의미 |
| --- | --- |
| `dateDesc` | 날짜 내림차순 기본값 |
| `dateAsc` | 날짜 오름차순 |
| `energyDesc` | 칼로리 내림차순 |
| `scoreDesc` | 분석 점수 내림차순 |

### 식단 생성/수정 요청 형식 메모

음식은 다음 형태로 함께 전송된다.

- `foodCode=food_oat`
- `foodCode=food_egg`
- `grams_food_oat=180`
- `grams_food_egg=100`

## 6. CommunityController

기본 경로: `/community`

| Method | Path | Auth | 파라미터 | 설명 | 응답 |
| --- | --- | --- | --- | --- | --- |
| GET | `/community` | Yes | `category` | 커뮤니티 메인 화면 및 카테고리 필터 | `community/index` |
| GET | `/community/posts/{postId}/edit` | Yes | `category` | 특정 게시글 편집 상태로 화면 렌더링 | `community/index` 또는 Redirect |
| GET | `/community/comments/{commentId}/edit` | Yes | `category` | 특정 댓글 편집 상태로 화면 렌더링 | `community/index` 또는 Redirect |
| POST | `/community/posts` | Yes | `category`, `linkedMealId`, `title`, `content` | 게시글 생성 | `redirect:/community...` |
| PATCH | `/community/posts/{postId}` | Yes | `category`, `linkedMealId`, `title`, `content` | 게시글 수정 | `redirect:/community...` |
| DELETE | `/community/posts/{postId}` | Yes | `redirectCategory` | 게시글 삭제 | `redirect:/community...` |
| POST | `/community/posts/{postId}/comments` | Yes | `commentContent`, `redirectCategory` | 댓글 생성 | `redirect:/community...` |
| PATCH | `/community/comments/{commentId}` | Yes | `commentContent`, `redirectCategory` | 댓글 수정 | `redirect:/community...` |
| DELETE | `/community/comments/{commentId}` | Yes | `redirectCategory` | 댓글 삭제 | `redirect:/community...` |

### 특이사항

- 수정 화면 전용 별도 JSP가 아니라 동일한 `community/index` 화면에 `editPost` 또는 `editComment` 상태를 실어 렌더링한다.
- `linkedMealId`는 식단 리뷰 게시글과 식단 기록을 연결할 때 사용된다.

## 7. ChallengeController

기본 경로: `/challenges`

| Method | Path | Auth | 파라미터 | 설명 | 응답 |
| --- | --- | --- | --- | --- | --- |
| GET | `/challenges` | Yes | 없음 | 챌린지 목록/내 참여 상태 조회 | `challenge/index` |
| POST | `/challenges` | Yes | `title`, `description`, `category`, `targetCount`, `endDate` | 챌린지 생성 | `redirect:/challenges` |
| POST | `/challenges/{challengeId}/memberships` | Yes | 없음 | 현재 사용자 챌린지 참여 | `redirect:/challenges` |
| PATCH | `/challenges/{challengeId}/memberships/me` | Yes | `progress` | 현재 사용자 진행률 수정 | `redirect:/challenges` |
| DELETE | `/challenges/{challengeId}/memberships/me` | Yes | 없음 | 현재 사용자 챌린지 탈퇴 | `redirect:/challenges` |
| DELETE | `/challenges/{challengeId}` | Yes | 없음 | 생성자 본인의 챌린지 삭제 | `redirect:/challenges` |

### 특이사항

- 현재 로그인 사용자 정보는 `LoginCheckFilter`가 request attribute `currentUser`로 주입한다.
- 진행률은 `0`과 `targetCount` 사이로 보정된다.
- 완료 조건을 만족하면 membership status가 `completed`로 바뀐다.

## 8. CoachController

기본 경로: `/coach`

| Method | Path | Auth | 파라미터 | 설명 | 응답 |
| --- | --- | --- | --- | --- | --- |
| GET | `/coach` | Yes | 없음 | AI 코치 페이지 화면 표시 | `coach/index` |
| GET | `/coach/dashboard` | Yes | 없음 | 대시보드 데이터를 JSON으로 조회 | `200 OK` JSON |

### `/coach/dashboard` 응답 구조

```json
{
  "summary": "string",
  "recovery": "string",
  "todaySummary": {
    "calories": 0,
    "protein": 0
  },
  "dailyGoal": {
    "calories": 0,
    "protein": 0
  },
  "todayPct": 0,
  "sessions": [
    {
      "title": "string",
      "detail": "string",
      "intensity": "string"
    }
  ],
  "nextActions": [
    "string"
  ],
  "recentAnalyses": [
    {
      "headline": "string",
      "nextAction": "string",
      "grade": "A",
      "score": 0
    }
  ],
  "challenges": [
    {
      "id": "string",
      "title": "string",
      "description": "string",
      "progress": 0,
      "targetCount": 0
    }
  ]
}
```

## 9. SocialController

기본 경로: `/social`

| Method | Path | Auth | 파라미터 | 설명 | 응답 |
| --- | --- | --- | --- | --- | --- |
| GET | `/social` | Yes | 없음 | 팔로잉, 팔로워, 추천 사용자, 리더보드 조회 | `social/index` |
| POST | `/social` | Yes | `action`, `targetUserId` | 팔로우 또는 언팔로우 처리 | `redirect:/social` |

### `action` 값

| 값 | 의미 |
| --- | --- |
| `follow` | 대상 사용자 팔로우 |
| `unfollow` | 대상 사용자 언팔로우 |

## 10. Vue SPA REST API

Vue SPA는 세션 쿠키 기반으로 다음 JSON API를 사용한다.

| Method | Path | 설명 |
| --- | --- | --- |
| POST | `/api/v1/auth/login` | 로그인 후 세션 생성 |
| POST | `/api/v1/auth/signup` | 회원가입 후 세션 생성 |
| POST | `/api/v1/auth/logout` | 세션 종료 |
| GET | `/api/v1/users/me` | 내 프로필 조회 |
| GET | `/api/v1/users/me/dashboard` | 프로필 화면용 목표/활동 통계 조회 |
| PUT | `/api/v1/users/me` | 내 프로필 수정 |
| POST | `/api/v1/users/me/deactivate` | 내 계정 비활성화 |
| DELETE | `/api/v1/users/me` | 내 계정 영구 삭제 |
| GET | `/api/v1/meals` | 내 식단 목록 조회 |
| GET | `/api/v1/meals/{mealId}` | 식단 상세, 분석, 일일 목표 조회 |
| POST | `/api/v1/meals` | 식단 생성 |
| PUT | `/api/v1/meals/{mealId}` | 식단 수정 |
| DELETE | `/api/v1/meals/{mealId}` | 식단 삭제 |
| GET | `/api/v1/foods?keyword=...` | 음식 검색 |
| GET | `/api/v1/dashboard/home` | 홈 대시보드 조회 |
| GET | `/api/v1/coach/dashboard` | 코치 대시보드 조회 |
| GET | `/api/v1/community?category=...` | 커뮤니티 게시글/댓글 목록 조회 |
| POST | `/api/v1/community/posts` | 게시글 작성 |
| PATCH | `/api/v1/community/posts/{postId}` | 게시글 수정 |
| DELETE | `/api/v1/community/posts/{postId}` | 게시글 삭제 |
| POST | `/api/v1/community/posts/{postId}/comments` | 댓글 작성 |
| PATCH | `/api/v1/community/comments/{commentId}` | 댓글 수정 |
| DELETE | `/api/v1/community/comments/{commentId}` | 댓글 삭제 |
| GET | `/api/v1/challenges` | 챌린지 보드 조회 |
| POST | `/api/v1/challenges` | 챌린지 생성 |
| POST | `/api/v1/challenges/{challengeId}/memberships` | 챌린지 참여 |
| PATCH | `/api/v1/challenges/{challengeId}/memberships/me` | 내 진행률 수정 |
| DELETE | `/api/v1/challenges/{challengeId}/memberships/me` | 챌린지 탈퇴 |
| DELETE | `/api/v1/challenges/{challengeId}` | 내가 만든 챌린지 삭제 |
| GET | `/api/v1/social` | 소셜 대시보드 조회 |
| POST | `/api/v1/social/following/{targetUserId}` | 팔로우 |
| DELETE | `/api/v1/social/following/{targetUserId}` | 언팔로우 |
| GET | `/api/v1/health` | API 상태 확인 |

개발 중 `client` Vite 서버는 `/api`, `/batch`, `/swagger-ui`, `/v3/api-docs`를 Spring Boot 서버(`http://localhost:8080`)로 proxy한다. `pnpm build`는 Vue SPA 산출물을 `src/main/resources/static`에 생성하므로 Spring Boot가 `index.html`과 정적 asset을 함께 서빙할 수 있다.

## 11. 예외 및 오류 응답

### 화면 오류

다음 예외는 `error/error.jsp`로 렌더링된다.

| 예외 | 상태 코드 | 설명 |
| --- | --- | --- |
| `CustomException` | 예외 내부 status 사용 | 인증 실패, 권한 문제, 잘못된 요청 등 |
| `IllegalStateException` | 500 | 리포지토리/상태 오류 |
| 기타 `Exception` | 500 | 일반 처리 오류 |

### 실제 사용 패턴

- 일반 화면 요청은 오류 화면 렌더링
- 일부 컨트롤러는 예외 대신 플래시 메시지 + 리다이렉트 처리

## 12. 빠른 요약

이 프로젝트는 일반 사용자 화면을 `client` Vue SPA로 전환하고, 같은 기능을 사용할 수 있도록 `/api/v1/**` REST API를 확장한 상태다. 기존 JSP 화면/폼 컨트롤러는 레거시 확인용으로 `/legacy/**` 경로에 남아 있다.
