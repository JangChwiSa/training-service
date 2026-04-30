
# Git Strategy

## 1. Purpose

??臾몄꽌??Training Service 媛쒕컻???꾪븳 Git 釉뚮옖移??꾨왂, 而ㅻ컠 洹쒖튃, Pull Request 洹쒖튃, 蹂묓빀 湲곗????뺤쓽?쒕떎.

Training Service??API 紐낆꽭, DB 紐낆꽭, ?쒗???ㅼ씠?닿렇?⑥쓣 湲곗??쇰줈 援ы쁽?섎?濡?Git ?묒뾽 怨쇱젙?먯꽌??紐낆꽭? 肄붾뱶???뺥빀?깆쓣 ?좎??댁빞 ?쒕떎.

愿??臾몄꽌:

```text
docs/api/api-spec.md
docs/api/training-api-spec.md
docs/database/db-spec.md
docs/database/training-db-spec.md
docs/architecture/sequence-diagrams.md
docs/architecture/overall-architecture.md
```

---

## 2. Branch Strategy

???꾨줈?앺듃??`main`, `develop`, ?묒뾽 釉뚮옖移섎? 湲곗??쇰줈 愿由ы븳??

```text
main
- ?덉젙 釉뚮옖移?
- ??긽 諛고룷 媛?ν븳 ?곹깭瑜??좎??쒕떎
- 吏곸젒 push?섏? ?딅뒗??
- release ?먮뒗 hotfix瑜??듯빐?쒕쭔 蹂寃쏀븳??

develop
- 媛쒕컻 ?듯빀 釉뚮옖移?
- 湲곕뒫 媛쒕컻 釉뚮옖移섍? 蹂묓빀?섎뒗 ??곸씠??
- ?뚯뒪?멸? ?듦낵?섍퀬 由대━利?以鍮꾧? ?섎㈃ main?쇰줈 蹂묓빀?쒕떎
```

---

## 3. Working Branch Types

?묒뾽 釉뚮옖移섎뒗 紐⑹쟻???곕씪 prefix瑜?援щ텇?쒕떎.

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
- main 釉뚮옖移섏뿉 吏곸젒 push?섏? ?딅뒗??
- develop 釉뚮옖移섏뿉???먯튃?곸쑝濡?吏곸젒 push?섏? ?딅뒗??
- 湲곕뒫, ?섏젙, 臾몄꽌, 由ы뙥?좊쭅, ?뚯뒪???묒뾽? 媛곴컖 蹂꾨룄 釉뚮옖移섏뿉??吏꾪뻾?쒕떎.
- ?섎굹??釉뚮옖移섏뿉???섎굹??紐⑹쟻留??대뒗??
- API 蹂寃쎄낵 DB 蹂寃쎌? 諛섎뱶??愿??紐낆꽭 臾몄꽌 蹂寃쎌쓣 ?④퍡 ?ы븿?쒕떎.
- 愿???녿뒗 由ы뙥?좊쭅? 湲곕뒫 PR???욎? ?딅뒗??
- 臾몄꽌留??섏젙?섎뒗 寃쎌슦 docs/* 釉뚮옖移섎? ?ъ슜?쒕떎.
```

---

## 5. Merge Flow

湲곕낯 蹂묓빀 ?먮쫫? ?ㅼ쓬怨?媛숇떎.

```text
feature/*   -> develop
fix/*       -> develop
docs/*      -> develop
refactor/*  -> develop
test/*      -> develop
chore/*     -> develop

develop     -> main
```

?댁쁺 以?湲닿툒 ?섏젙???꾩슂??寃쎌슦?먮뒗 `hotfix/*` 釉뚮옖移섎? ?ъ슜?쒕떎.

```text
main -> hotfix/{short-description}
hotfix/{short-description} -> main
hotfix/{short-description} -> develop
```

---

## 6. Commit Message Rules

而ㅻ컠 硫붿떆吏??吏㏐퀬 紐낅졊?뺤쑝濡??묒꽦?쒕떎.

沅뚯옣 ?뺤떇:

```text
<type>: <summary>
```

?ъ슜 媛?ν븳 type:

```text
feat      ?덈줈??湲곕뒫 異붽?
fix       踰꾧렇 ?섏젙
docs      臾몄꽌 ?섏젙
refactor  ?숈옉 蹂寃??녿뒗 肄붾뱶 援ъ“ 媛쒖꽑
test      ?뚯뒪??異붽? ?먮뒗 ?섏젙
chore     鍮뚮뱶, ?ㅼ젙, ?섏〈?? ?좎?蹂댁닔 ?묒뾽
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
?섏젙
?묒뾽??
```

---

## 7. Pull Request Rules

紐⑤뱺 肄붾뱶 蹂寃쎌? Pull Request瑜??듯빐 蹂묓빀?쒕떎.

PR ?쒕ぉ? 而ㅻ컠 硫붿떆吏? 鍮꾩듂?섍쾶 紐낇솗?섍쾶 ?묒꽦?쒕떎.

```text
feat: add social training session creation
fix: validate training session ownership
docs: add training database specification
```

PR 蹂몃Ц?먮뒗 ?ㅼ쓬 ?댁슜???ы븿?쒕떎.

```text
- 蹂寃??붿빟
- 愿??Training Service ?꾨찓??
- 愿??API 紐낆꽭 ?꾩튂
- 愿??DB 紐낆꽭 ?꾩튂
- ?뚯뒪??寃곌낵
- ?⑥? TODO ?먮뒗 由ъ뒪??
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

API 紐낆꽭? DB 紐낆꽭??援ы쁽 怨꾩빟?대떎.

```text
- API 寃쎈줈瑜?蹂寃쏀븯硫?諛섎뱶??API 紐낆꽭???섏젙?쒕떎.
- Request/Response ?꾨뱶瑜?蹂寃쏀븯硫?諛섎뱶??API 紐낆꽭???섏젙?쒕떎.
- ?뚯씠釉? 而щ읆, Enum, ?쒖빟議곌굔??蹂寃쏀븯硫?諛섎뱶??DB 紐낆꽭???섏젙?쒕떎.
- ?쒗???먮쫫??蹂寃쏀븯硫?諛섎뱶???쒗???ㅼ씠?닿렇?⑤룄 ?섏젙?쒕떎.
- ?꾩껜 ?ㅽ럺怨?Training Service 諛쒖톸 ?ㅽ럺??異⑸룎?섎㈃ ?꾩껜 ?ㅽ럺???곗꽑?쒕떎.
- 異⑸룎??諛쒓껄?섎㈃ ?꾩쓽濡?援ы쁽?섏? ?딄퀬 癒쇱? 異⑸룎 ?댁슜??湲곕줉?쒕떎.
```

?ㅽ럺 蹂寃쎌씠 ?ы븿??PR? ?꾨옒 臾몄꽌瑜??뺤씤?댁빞 ?쒕떎.

```text
docs/api/api-spec.md
docs/api/training-api-spec.md
docs/database/db-spec.md
docs/database/training-db-spec.md
docs/architecture/sequence-diagrams.md
```

---

## 9. Testing Rules Before Merge

develop?쇰줈 蹂묓빀?섍린 ???ㅼ쓬 ??ぉ???뺤씤?쒕떎.

```text
- 愿???⑥쐞 ?뚯뒪?멸? ?듦낵?덈뒗媛
- 愿???듯빀 ?뚯뒪?멸? ?듦낵?덈뒗媛
- sessionId ?뚯쑀沅?寃利앹씠 源⑥?吏 ?딆븯?붽?
- userId瑜??붿껌 諛붾뵒??荑쇰━ ?뚮씪誘명꽣濡?諛쏆? ?딅뒗媛
- API 紐낆꽭? ?ㅼ젣 援ы쁽???쇱튂?섎뒗媛
- DB 紐낆꽭? ?ㅼ젣 ?뷀떚??留덉씠洹몃젅?댁뀡???쇱튂?섎뒗媛
```

---

## 10. Recommended Development Flow

?쇰컲 湲곕뒫 媛쒕컻 ?먮쫫:

```bash
git checkout develop
git pull origin develop
git checkout -b feature/social-session-start
```

?묒뾽 ??

```bash
git status
git add .
git commit -m "feat: add social training session creation"
git push origin feature/social-session-start
```

洹??ㅼ쓬 GitHub?먯꽌 `feature/social-session-start` ??`develop` 諛⑺뼢?쇰줈 Pull Request瑜??앹꽦?쒕떎.

---

## 11. Documentation-Only Flow

臾몄꽌留??섏젙?섎뒗 寃쎌슦:

```bash
git checkout develop
git pull origin develop
git checkout -b docs/training-api-spec
```

?묒뾽 ??

```bash
git add docs/
git commit -m "docs: update training API specification"
git push origin docs/training-api-spec
```

---

## 12. Release Flow

由대━利?以鍮꾧? ?꾨즺?섎㈃ `develop`?먯꽌 `main`?쇰줈 PR???앹꽦?쒕떎.

```text
develop -> main
```

由대━利?PR?먮뒗 ?ㅼ쓬 ?댁슜???ы븿?쒕떎.

```text
- ?ы븿??湲곕뒫 紐⑸줉
- ?섏젙??踰꾧렇 紐⑸줉
- 蹂寃쎈맂 API
- 蹂寃쎈맂 DB
- ?뚯뒪??寃곌낵
- 諛고룷 ??二쇱쓽?ы빆
```

由대━利??쒓렇???꾩슂 ???ㅼ쓬 ?뺤떇???ъ슜?쒕떎.

```text
v0.1.0
v0.2.0
v1.0.0
```

---

## 13. Hotfix Flow

?댁쁺 以?湲닿툒 ?섏젙???꾩슂??寃쎌슦 `main`?먯꽌 hotfix 釉뚮옖移섎? ?앹꽦?쒕떎.

```bash
git checkout main
git pull origin main
git checkout -b hotfix/session-completion-error
```

?섏젙 ??

```bash
git add .
git commit -m "fix: handle session completion error"
git push origin hotfix/session-completion-error
```

蹂묓빀 ?쒖꽌:

```text
hotfix/session-completion-error -> main
hotfix/session-completion-error -> develop
```

---

## 14. Codex Usage Rules

Codex瑜??ъ슜???뚮룄 ?숈씪??Git ?꾨왂???곕Ⅸ??

```text
- Codex ?묒뾽 ???꾩옱 釉뚮옖移섎? ?뺤씤?쒕떎.
- Codex?먭쾶 ?묒뾽?쒗궗 ??愿??臾몄꽌 寃쎈줈瑜?紐낆떆?쒕떎.
- Codex媛 API 寃쎈줈, Request/Response, DB 而щ읆???꾩쓽濡?蹂寃쏀븯吏 ?딅룄濡??쒕떎.
- Codex媛 肄붾뱶瑜??섏젙?섍린 ??援ы쁽 怨꾪쉷??癒쇱? ?쒖떆?섍쾶 ?쒕떎.
- Codex ?묒뾽 ??git diff瑜?諛섎뱶???뺤씤?쒕떎.
```

沅뚯옣 ?붿껌 ?덉떆:

```text
docs/api/training-api-spec.md? docs/database/training-db-spec.md瑜?湲곗??쇰줈
?ы쉶???덈젴 ?몄뀡 ?쒖옉 API瑜?援ы쁽?댁쨾.

API 寃쎈줈, Request/Response, DB ?꾨뱶???꾩쓽濡?諛붽씀吏 留?
癒쇱? 援ы쁽 怨꾪쉷???ㅻ챸?섍퀬, 洹??ㅼ쓬 肄붾뱶瑜??섏젙?댁쨾.
```

---

## 15. Important Reminder

????μ냼??Training Service 援ы쁽???꾪븳 ??μ냼?대떎.

```text
User Service = ?몄쬆/濡쒓렇???ъ슜???뺣낫 愿由?
Voice Service = ?뚯꽦/AI ?곹샇?묒슜 泥섎━
Training Service = ?덈젴 湲곕줉, ?먯닔, ?쇰뱶諛? 吏꾪뻾 ?곹깭 愿由?
Report Service = ?덈젴 寃곌낵 ?댁꽍 諛?由ы룷??愿由?
```

Git ?묒뾽?먯꽌????寃쎄퀎瑜??좎??쒕떎.

