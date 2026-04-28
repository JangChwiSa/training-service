
# Git Strategy

## 1. Purpose

이 문서는 Training Service 개발을 위한 Git 브랜치 전략, 커밋 규칙, Pull Request 규칙, 병합 기준을 정의한다.

Training Service는 API 명세, DB 명세, 시퀀스 다이어그램을 기준으로 구현되므로 Git 작업 과정에서도 명세와 코드의 정합성을 유지해야 한다.

관련 문서:

```text
docs/api/api-spec.md
docs/api/training-api-spec.md
docs/database/database-spec.md
docs/database/training-db-spec.md
docs/architecture/sequence-diagrams.md
docs/architecture/overall-architecture.md
```

---

## 2. Branch Strategy

이 프로젝트는 `main`, `develop`, 작업 브랜치를 기준으로 관리한다.

```text
main
- 안정 브랜치
- 항상 배포 가능한 상태를 유지한다
- 직접 push하지 않는다
- release 또는 hotfix를 통해서만 변경한다

develop
- 개발 통합 브랜치
- 기능 개발 브랜치가 병합되는 대상이다
- 테스트가 통과하고 릴리즈 준비가 되면 main으로 병합한다
```

---

## 3. Working Branch Types

작업 브랜치는 목적에 따라 prefix를 구분한다.

```text
feature/{short-description}
fix/{short-description}
docs/{short-description}
refactor/{short-description}
test/{short-description}
chore/{short-description}
hotfix/{short-description}
```

### Examples

```text
feature/social-session-start
feature/social-training-complete
feature/safety-next-scene
feature/focus-session-complete
feature/document-answer-submit

fix/session-ownership-validation
fix/duplicate-training-completion

docs/training-api-spec
docs/training-db-spec
docs/git-strategy

refactor/training-domain-package
refactor/progress-update-service

test/social-completion-service
test/session-ownership-validation

chore/update-ci-config
chore/add-env-example

hotfix/session-completion-error
```

---

## 4. Branch Rules

```text
- main 브랜치에 직접 push하지 않는다.
- develop 브랜치에도 원칙적으로 직접 push하지 않는다.
- 기능, 수정, 문서, 리팩토링, 테스트 작업은 각각 별도 브랜치에서 진행한다.
- 하나의 브랜치에는 하나의 목적만 담는다.
- API 변경과 DB 변경은 반드시 관련 명세 문서 변경을 함께 포함한다.
- 관련 없는 리팩토링은 기능 PR에 섞지 않는다.
- 문서만 수정하는 경우 docs/* 브랜치를 사용한다.
```

---

## 5. Merge Flow

기본 병합 흐름은 다음과 같다.

```text
feature/*   -> develop
fix/*       -> develop
docs/*      -> develop
refactor/*  -> develop
test/*      -> develop
chore/*     -> develop

develop     -> main
```

운영 중 긴급 수정이 필요한 경우에는 `hotfix/*` 브랜치를 사용한다.

```text
main -> hotfix/{short-description}
hotfix/{short-description} -> main
hotfix/{short-description} -> develop
```

---

## 6. Commit Message Rules

커밋 메시지는 짧고 명령형으로 작성한다.

권장 형식:

```text
<type>: <summary>
```

사용 가능한 type:

```text
feat      새로운 기능 추가
fix       버그 수정
docs      문서 수정
refactor  동작 변경 없는 코드 구조 개선
test      테스트 추가 또는 수정
chore     빌드, 설정, 의존성, 유지보수 작업
```

### Good Examples

```text
feat: add social training session creation
feat: implement safety next scene API
feat: add focus training completion flow

fix: validate training session ownership
fix: prevent duplicate training completion

docs: add training API specification
docs: add training database specification
docs: add git strategy

refactor: separate training progress service
refactor: reorganize training domain package

test: add social training completion tests
test: add session ownership validation tests

chore: add local environment example
chore: update CI configuration
```

### Bad Examples

```text
update
fix
wip
stuff
수정
작업함
```

---

## 7. Pull Request Rules

모든 코드 변경은 Pull Request를 통해 병합한다.

PR 제목은 커밋 메시지와 비슷하게 명확하게 작성한다.

```text
feat: add social training session creation
fix: validate training session ownership
docs: add training database specification
```

PR 본문에는 다음 내용을 포함한다.

```text
- 변경 요약
- 관련 Training Service 도메인
- 관련 API 명세 위치
- 관련 DB 명세 위치
- 테스트 결과
- 남은 TODO 또는 리스크
```

### PR Template

```md
## Summary

-

## Related Domain

-

## Related Specs

- API:
- DB:
- Sequence:

## Changes

-

## Test Evidence

-

## Risks / TODOs

-
```

---

## 8. Spec Change Rules

API 명세와 DB 명세는 구현 계약이다.

```text
- API 경로를 변경하면 반드시 API 명세도 수정한다.
- Request/Response 필드를 변경하면 반드시 API 명세도 수정한다.
- 테이블, 컬럼, Enum, 제약조건을 변경하면 반드시 DB 명세도 수정한다.
- 시퀀스 흐름을 변경하면 반드시 시퀀스 다이어그램도 수정한다.
- 전체 스펙과 Training Service 발췌 스펙이 충돌하면 전체 스펙을 우선한다.
- 충돌을 발견하면 임의로 구현하지 않고 먼저 충돌 내용을 기록한다.
```

스펙 변경이 포함된 PR은 아래 문서를 확인해야 한다.

```text
docs/api/api-spec.md
docs/api/training-api-spec.md
docs/database/database-spec.md
docs/database/training-db-spec.md
docs/architecture/sequence-diagrams.md
```

---

## 9. Testing Rules Before Merge

develop으로 병합하기 전 다음 항목을 확인한다.

```text
- 관련 단위 테스트가 통과했는가
- 관련 통합 테스트가 통과했는가
- sessionId 소유권 검증이 깨지지 않았는가
- userId를 요청 바디나 쿼리 파라미터로 받지 않는가
- Training Service가 user_db 또는 report_db에 직접 접근하지 않는가
- API 명세와 실제 구현이 일치하는가
- DB 명세와 실제 엔티티/마이그레이션이 일치하는가
```

---

## 10. Recommended Development Flow

일반 기능 개발 흐름:

```bash
git checkout develop
git pull origin develop
git checkout -b feature/social-session-start
```

작업 후:

```bash
git status
git add .
git commit -m "feat: add social training session creation"
git push origin feature/social-session-start
```

그 다음 GitHub에서 `feature/social-session-start` → `develop` 방향으로 Pull Request를 생성한다.

---

## 11. Documentation-Only Flow

문서만 수정하는 경우:

```bash
git checkout develop
git pull origin develop
git checkout -b docs/training-api-spec
```

작업 후:

```bash
git add docs/
git commit -m "docs: update training API specification"
git push origin docs/training-api-spec
```

---

## 12. Release Flow

릴리즈 준비가 완료되면 `develop`에서 `main`으로 PR을 생성한다.

```text
develop -> main
```

릴리즈 PR에는 다음 내용을 포함한다.

```text
- 포함된 기능 목록
- 수정된 버그 목록
- 변경된 API
- 변경된 DB
- 테스트 결과
- 배포 시 주의사항
```

릴리즈 태그는 필요 시 다음 형식을 사용한다.

```text
v0.1.0
v0.2.0
v1.0.0
```

---

## 13. Hotfix Flow

운영 중 긴급 수정이 필요한 경우 `main`에서 hotfix 브랜치를 생성한다.

```bash
git checkout main
git pull origin main
git checkout -b hotfix/session-completion-error
```

수정 후:

```bash
git add .
git commit -m "fix: handle session completion error"
git push origin hotfix/session-completion-error
```

병합 순서:

```text
hotfix/session-completion-error -> main
hotfix/session-completion-error -> develop
```

---

## 14. Codex Usage Rules

Codex를 사용할 때도 동일한 Git 전략을 따른다.

```text
- Codex 작업 전 현재 브랜치를 확인한다.
- Codex에게 작업시킬 때 관련 문서 경로를 명시한다.
- Codex가 API 경로, Request/Response, DB 컬럼을 임의로 변경하지 않도록 한다.
- Codex가 코드를 수정하기 전 구현 계획을 먼저 제시하게 한다.
- Codex 작업 후 git diff를 반드시 확인한다.
```

권장 요청 예시:

```text
docs/api/training-api-spec.md와 docs/database/training-db-spec.md를 기준으로
사회성 훈련 세션 시작 API를 구현해줘.

API 경로, Request/Response, DB 필드는 임의로 바꾸지 마.
먼저 구현 계획을 설명하고, 그 다음 코드를 수정해줘.
```

---

## 15. Important Reminder

이 저장소는 Training Service 구현을 위한 저장소이다.

```text
User Service = 인증/로그인/사용자 정보 관리
Voice Service = 음성/AI 상호작용 처리
Training Service = 훈련 기록, 점수, 피드백, 진행 상태 관리
Report Service = 훈련 결과 해석 및 리포트 관리
```

Git 작업에서도 이 경계를 유지한다.
