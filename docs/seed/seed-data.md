# Training Service Seed Data 정리본

> 목적: 훈련 콘텐츠 원본 데이터를 DB seed로 넣기 위한 정리 문서입니다.  
> 범위: 사용자 수행 결과, 세션, 로그, 점수, 피드백 데이터는 포함하지 않습니다.  
> 대상 테이블: `social_scenarios`, `safety_scenarios`, `safety_scenes`, `safety_choices`, `document_questions`, `document_question_choices`

---

## 0. Seed 저장 원칙

### 0.1 원본 콘텐츠와 수행 결과 분리

이 문서의 데이터는 전부 **훈련 콘텐츠 원본**입니다.

따라서 아래 테이블에는 넣지 않습니다.

- `training_sessions`
- `social_dialog_logs`
- `safety_action_logs`
- `document_answer_logs`
- `training_scores`
- `training_feedbacks`
- `training_session_summaries`

### 0.2 Seed 코드 사용 기준

아래 문서의 `seed_code`는 실제 DB의 PK가 아니라, seed 데이터를 사람이 관리하기 쉽게 붙인 식별자입니다.

실제 DB 저장 시에는 다음 중 하나를 선택합니다.

1. `seed_code` 컬럼을 별도로 추가해서 관리한다.
2. seed insert script 내부에서 `seed_code -> UUID`를 매핑한다.
3. 운영 DB에는 UUID만 저장하고, seed 파일에서만 `seed_code`를 관리한다.

추천은 **1번**입니다.

---

# 1. 사회성 훈련 Seed Data

## 1.1 대상 테이블

```text
social_scenarios
```

## 1.2 추천 컬럼 구조

```text
scenario_id           -- UUID PK
seed_code             -- 사람이 관리하기 위한 seed 식별자, 예: SOC-OFF-AMB-001
job_type              -- OFFICE, LABOR
category_code         -- AMBIGUOUS_INSTRUCTION, MISTAKE_REPORT 등
title                 -- 시나리오 제목
background_text       -- 상황 카테고리 설명
situation_text        -- 실제 사용자에게 제시할 상황 문장
character_info        -- 사수님, 팀장님, 동료, 관리자님 등
difficulty            -- LEVEL_1 등
evaluation_point      -- 채점 포인트
example_answer        -- 모범 답변, 추후 보강 가능
is_active             -- 사용 여부
```

---

## 1.3 사회성 훈련 카테고리

| job_type | category_code | category_title |
|---|---|---|
| OFFICE | AMBIGUOUS_INSTRUCTION | 상사 또는 직장 동료의 지시가 모호해 이해를 제대로 하지 못했을 때 |
| OFFICE | MISTAKE_REPORT | 업무 중 실수를 해서 문제가 생겼을 때 얘기하는 법 |
| OFFICE | REFUSAL | 부당하거나 어려운 부탁을 받았을 때 거절하는 방법 |
| LABOR | FEEDBACK_ACCEPTANCE | 지적 및 피드백 수용 |
| LABOR | HELP_REQUEST | 도움 요청 및 조율 |
| LABOR | HEALTH_REPORT | 휴식 및 신체 상태 보고 |

---

## 1.4 사회성 훈련 시나리오 목록

### OFFICE / AMBIGUOUS_INSTRUCTION

| seed_code | job_type | category_code | situation_order | title | situation_text | character_info | difficulty | evaluation_point | example_answer |
|---|---|---:|---:|---|---|---|---|---|---|
| SOC-OFF-AMB-001 | OFFICE | AMBIGUOUS_INSTRUCTION | 1 | 복사 수량이 모호한 지시를 받았을 때 | 사수님이 서류를 가져와 책상에 툭 내려놓으며 "이거 넉넉히 복사해서 회의실에 갖다 둬요."라고 말하고 급하게 나갔습니다. 회의 인원이 몇 명인지 몰라 몇 장을 복사해야 할지 당황스러운 상황입니다. 어떻게 대처해야 할까요? | 사수님 | LEVEL_1 | 구체적인 숫자, 예를 들어 10장 또는 20장을 언급하며 수량을 확인했는가? | 사수님, 회의 인원이 몇 명인지 몰라서요. 몇 부 정도 복사하면 될까요? |
| SOC-OFF-AMB-002 | OFFICE | AMBIGUOUS_INSTRUCTION | 2 | 전화 전달에 필요한 연락처가 빠졌을 때 | 팀장님이 외근 중인데 사무실로 전화가 왔습니다. 상대방이 "나 박 사장인데, 팀장 오면 전화하라고 해요." 말하고 끊으려 합니다. 연락처를 모르면 팀장님께 보고할 수 없는 상황입니다. 어떻게 말해야 할까요? | 전화 상대방, 팀장님 | LEVEL_1 | 상대방의 정확한 성함과 다시 연락드릴 번호를 정중하게 요청했는가? | 죄송하지만 팀장님께 정확히 전달드릴 수 있도록 성함과 연락처를 한 번만 알려주실 수 있을까요? |
| SOC-OFF-AMB-003 | OFFICE | AMBIGUOUS_INSTRUCTION | 3 | 음료 구매 지시가 구체적이지 않을 때 | 대리님이 법인카드를 주며 "손님들 오시니까 탕비실에 음료수 좀 종류별로 사다 채워놔요. 센스 있게 알죠?"라고 합니다. 어떤 음료를 몇 개나 사야 할지 막막한 상황입니다. 뭐라고 물어봐야 할까요? | 대리님 | LEVEL_1 | 음료의 종류나 대략적인 구매 개수를 구체적으로 되물었는가? | 대리님, 커피나 주스처럼 어떤 종류를 몇 개 정도 사면 좋을까요? |

---

### OFFICE / MISTAKE_REPORT

| seed_code | job_type | category_code | situation_order | title | situation_text | character_info | difficulty | evaluation_point | example_answer |
|---|---|---:|---:|---|---|---|---|---|---|
| SOC-OFF-MIS-001 | OFFICE | MISTAKE_REPORT | 1 | 영수증 원본을 실수로 파쇄했을 때 | 파쇄기 옆에 놓인 종이 뭉치를 당연히 버리는 건 줄 알고 다 갈아버렸습니다. 그런데 알고 보니 사수님이 점심시간 직전에 정리해둔 영수증 원본들이었습니다. 이미 가루가 된 상황에서 어떻게 보고해야 할까요? | 사수님 | LEVEL_1 | "죄송합니다"라고 즉시 사과하고, 어떤 서류를 파쇄했는지 사실대로 말했는가? | 죄송합니다. 파쇄기 옆에 있던 영수증 원본을 버리는 서류로 착각해서 파쇄했습니다. |
| SOC-OFF-MIS-002 | OFFICE | MISTAKE_REPORT | 2 | 회의실 예약을 깜빡했을 때 | 오후 2시 회의를 위해 회의실에 갔더니 이미 다른 팀이 회의 중입니다. 알고 보니 내가 예약 시스템에 등록하는 걸 깜빡해서 우리 팀원들이 들어가지 못하고 있습니다. 팀장님께 이 상황을 어떻게 말해야 할까요? | 팀장님 | LEVEL_1 | 자신의 실수를 인정하고, 즉시 빈 회의실을 찾아보겠다는 대안을 제시했는가? | 팀장님, 제가 회의실 예약을 깜빡했습니다. 죄송합니다. 바로 사용 가능한 빈 회의실을 찾아보겠습니다. |
| SOC-OFF-MIS-003 | OFFICE | MISTAKE_REPORT | 3 | 지하철 고장으로 지각하게 되었을 때 | 출근하는 지하철이 터널 안에서 고장으로 멈췄습니다. 안내 방송에서는 "출발이 20분 이상 지연될 예정"이라고 합니다. 9시 정각까지 출근하기 불가능한 상황에서 사수님께 어떻게 연락해야 할까요? | 사수님 | LEVEL_1 | 지각 사유와 예상 도착 시간을 명확히 전달했는가? | 사수님, 지하철 고장으로 20분 이상 지연될 예정입니다. 도착 예상 시간은 확인되는 대로 다시 말씀드리겠습니다. |

---

### OFFICE / REFUSAL

| seed_code | job_type | category_code | situation_order | title | situation_text | character_info | difficulty | evaluation_point | example_answer |
|---|---|---:|---:|---|---|---|---|---|---|
| SOC-OFF-REF-001 | OFFICE | REFUSAL | 1 | 내 업무가 아닌 개인적인 부탁을 받았을 때 | 옆자리 선배가 자기 책상에 쌓인 빈 박스 더미를 내 밀차에 툭 던지며 "OO 씨는 이제 할 일 없지? 나 바쁘니까 이것도 가져가서 분리수거장에 좀 버리고 와요."라고 합니다. 원래 내 업무가 아닌데 당연하다는 듯 시키는 상황에서 뭐라고 대답해야 할까요? | 선배 | LEVEL_1 | 현재 내가 해야 할 업무가 있음을 밝히고, 개인적인 부탁은 어렵다고 정중히 거절했는가? | 죄송하지만 지금 제가 맡은 업무가 있어서 바로 도와드리기는 어렵습니다. |
| SOC-OFF-REF-002 | OFFICE | REFUSAL | 2 | 불편한 사생활 질문을 받았을 때 | 점심시간에 선배가 "OO 씨는 장애가 있어서 취직하기 편했겠어. 수당도 나오죠? 한 달에 얼마 받아요?"라며 기분 나쁜 사생활 질문을 합니다. 대답하고 싶지 않을 때 어떻게 상황을 넘겨야 할까요? | 선배 | LEVEL_1 | "그건 개인적인 일이라 말씀드리기 곤란합니다"처럼 정중하지만 명확하게 거절했는가? | 그건 개인적인 일이라 말씀드리기 곤란합니다. |
| SOC-OFF-REF-003 | OFFICE | REFUSAL | 3 | 위험한 작업 방식을 권유받았을 때 | 기계 작업을 하고 있는데 동료가 다가와 "야, 이거 그냥 손으로 대충 밀어 넣어. 기계 안 멈춰도 돼. 언제 일일이 끄고 해?"라며 위험한 방식을 권유합니다. 규칙대로 하지 않으면 동료가 나를 답답해할까 봐 걱정되는 상황입니다. 어떻게 말해야 할까요? | 동료 | LEVEL_1 | 규칙대로 기계를 멈추고 하겠다고 하거나, 다칠 수 있어 위험하다고 안전 원칙을 고수했는가? | 그렇게 하면 다칠 수 있어서 위험해요. 규칙대로 기계를 멈추고 하겠습니다. |

---

### LABOR / FEEDBACK_ACCEPTANCE

| seed_code | job_type | category_code | situation_order | title | situation_text | character_info | difficulty | evaluation_point | example_answer |
|---|---|---:|---:|---|---|---|---|---|---|
| SOC-LAB-FBK-001 | LABOR | FEEDBACK_ACCEPTANCE | 1 | 청소 상태를 지적받았을 때 | 화장실 청소를 열심히 마쳤는데 관리자님이 오더니 "여기 구석에 머리카락 그대로잖아요! 눈에 보이는 것만 치울 거예요?"라며 사람들 앞에서 크게 화를 냅니다. 너무 속상하지만 일을 마무리하려면 뭐라고 대답해야 할까요? | 관리자님 | LEVEL_1 | 변명하기보다 지적된 곳을 확인하고 "즉시 다시 청소하겠습니다"라고 답했는가? | 네, 확인했습니다. 바로 다시 청소하겠습니다. |
| SOC-LAB-FBK-002 | LABOR | FEEDBACK_ACCEPTANCE | 2 | 작업 속도를 지적받았을 때 | 물류 박스를 접고 있는데 반장님이 오셔서 "OO 씨는 손이 너무 느려요. 옆 사람은 벌써 두 박스 다 채웠는데 언제 끝낼 거야?"라며 속도를 지적합니다. 마음이 급해지는 상황에서 뭐라고 말해야 할까요? | 반장님 | LEVEL_1 | 지적을 수용하고 "조금 더 속도를 내보겠습니다"라고 의지를 표현했는가? | 네, 알겠습니다. 조금 더 속도를 내보겠습니다. |
| SOC-LAB-FBK-003 | LABOR | FEEDBACK_ACCEPTANCE | 3 | 안전모 착용을 지적받았을 때 | 작업장이 너무 더워서 안전모를 잠깐 벗고 땀을 닦고 있었습니다. 그때 관리자님이 나타나 "안전 규칙 몰라요? 사고 나면 책임질 거야? 당장 써요!"라고 무섭게 지적합니다. 뭐라고 답변해야 할까요? | 관리자님 | LEVEL_1 | 규칙 위반을 인정하고 즉시 안전모를 착용하며 알겠다고 답했는가? | 죄송합니다. 바로 안전모를 착용하겠습니다. |

---

### LABOR / HELP_REQUEST

| seed_code | job_type | category_code | situation_order | title | situation_text | character_info | difficulty | evaluation_point | example_answer |
|---|---|---:|---:|---|---|---|---|---|---|
| SOC-LAB-HELP-001 | LABOR | HELP_REQUEST | 1 | 기계 이상 증상을 알릴 때 | 대형 세탁기에서 수건을 꺼내고 있는데 기계에서 '텅텅'거리는 쇳소리가 나더니 갑자기 멈췄습니다. 혼자 해결하려다 사고가 날 것 같아 무서운 상황입니다. 관리자님께 어떻게 알려야 할까요? | 관리자님 | LEVEL_1 | 기계의 이상 증상, 즉 소음과 멈춤을 설명하고 도움을 요청했는가? | 관리자님, 세탁기에서 텅텅거리는 소리가 나더니 갑자기 멈췄습니다. 확인을 도와주실 수 있을까요? |
| SOC-LAB-HELP-002 | LABOR | HELP_REQUEST | 2 | 무거운 박스를 함께 들어달라고 요청할 때 | 주방에서 식자재 박스 5개를 창고로 옮겨야 합니다. 박스가 너무 크고 무거워서 혼자 들다가 허리를 다칠 것 같은 상황입니다. 옆에 있는 동료에게 어떻게 도움을 청해야 할까요? | 동료 | LEVEL_1 | "박스가 너무 무거워서 그런데 같이 들어주실 수 있나요?"라고 구체적으로 요청했는가? | 박스가 너무 무거워서 그런데 같이 들어주실 수 있나요? |
| SOC-LAB-HELP-003 | LABOR | HELP_REQUEST | 3 | 작업 순서가 기억나지 않을 때 | 어제 배운 나사 조립 작업인데, 오늘 아침 기계 앞에 서니 어디서부터 시작해야 할지 순서가 하나도 기억나지 않습니다. 가만히 있으면 일이 밀리는 상황입니다. 사수님께 어떻게 말해야 할까요? | 사수님 | LEVEL_1 | 기억이 나지 않음을 솔직히 말하고 다시 한번 알려달라고 재교육을 요청했는가? | 죄송합니다. 어제 배운 작업 순서가 정확히 기억나지 않습니다. 다시 한번만 알려주실 수 있을까요? |

---

### LABOR / HEALTH_REPORT

| seed_code | job_type | category_code | situation_order | title | situation_text | character_info | difficulty | evaluation_point | example_answer |
|---|---|---:|---:|---|---|---|---|---|---|
| SOC-LAB-HEALTH-001 | LABOR | HEALTH_REPORT | 1 | 화장실에 급히 가야 할 때 | 마트 진열 업무를 하던 중 갑자기 배가 너무 아파서 당장 화장실에 가야 할 것 같습니다. 하지만 내 자리를 비우면 손님 안내를 할 사람이 없는 상황입니다. 관리자님께 뭐라고 말해야 할까요? | 관리자님 | LEVEL_1 | 화장실에 다녀와야 하는 상황임을 알리고 자리를 잠시 비워도 되는지 허락을 구했는가? | 관리자님, 배가 너무 아파서 화장실에 잠시 다녀와야 할 것 같습니다. 잠깐 자리를 비워도 될까요? |
| SOC-LAB-HEALTH-002 | LABOR | HEALTH_REPORT | 2 | 더위로 어지러워 휴식이 필요할 때 | 한여름 뙤약볕에서 주차 안내를 하는데 갑자기 어지럽고 식은땀이 나며 쓰러질 것 같습니다. 계속 서 있으면 위험할 것 같은 상황입니다. 반장님께 휴식을 요청하려면 뭐라고 말해야 할까요? | 반장님 | LEVEL_1 | 어지러운 신체 상태를 설명하고 "10분만 쉬어도 될까요?"처럼 구체적인 시간을 요청했는가? | 반장님, 갑자기 어지럽고 식은땀이 납니다. 10분만 쉬어도 될까요? |
| SOC-LAB-HEALTH-003 | LABOR | HEALTH_REPORT | 3 | 커터 칼에 손을 베었을 때 | 박스를 뜯다가 커터 칼에 손가락이 깊게 베여서 피가 많이 나고 손이 떨립니다. 너무 아파서 일을 계속할 수 없는 상황일 때 상사에게 어떻게 보고해야 할까요? | 상사 | LEVEL_1 | 부상 사실을 즉시 알리고 치료가 필요함을 요청했는가? | 손가락을 깊게 베어서 피가 많이 납니다. 보건실에 가서 치료를 받아야 할 것 같습니다. |

---

# 2. 안전 훈련 Seed Data

## 2.1 대상 테이블

```text
safety_scenarios
safety_scenes
safety_choices
```

## 2.2 추천 컬럼 구조

### safety_scenarios

```text
scenario_id
seed_code
title
category              -- SEXUAL_EDUCATION, INFECTIOUS_DISEASE, COMMUTE_SAFETY
description
is_active
created_at
```

### safety_scenes

```text
scene_id
seed_code
scenario_id
scene_order
screen_info
situation_text
question_text
is_end_scene
```

### safety_choices

```text
choice_id
seed_code
scene_id
choice_order
choice_text
next_scene_id
is_correct
result_text
effect_text
```

---

## 2.3 안전 훈련 시나리오 목록

| seed_code | title | category | description |
|---|---|---|---|
| SAFE-SEX-001 | 나의 경계 지키기 | SEXUAL_EDUCATION | 직장 내 신체 접촉, 사생활 질문, 사진 요구, 2차 피해 상황에서 나의 경계를 지키는 훈련 |
| SAFE-INF-001 | 함께 만드는 안심 일터 | INFECTIOUS_DISEASE | 손 씻기, 기침 예절, 아플 때 보고 및 휴식 등 감염병 예방 행동을 익히는 훈련 |
| SAFE-COM-001 | 무사히 회사까지 | COMMUTE_SAFETY | 보행, 횡단보도, 버스 정류장, 버스 안 안전 행동을 익히는 출퇴근길 안전 훈련 |

---

## 2.4 SAFE-SEX-001: 나의 경계 지키기

### Scene 목록

| scene_seed_code | scenario_seed_code | scene_order | screen_info | situation_text | question_text | is_end_scene |
|---|---|---:|---|---|---|---|
| SAFE-SEX-001-S01 | SAFE-SEX-001 | 1 | 사수가 어깨를 주무르는 장면 | 사수가 어깨를 주무르며 "우리 OO 씨 고생하네~"라고 합니다. 나는 너무 당황해서 몸이 얼어버렸습니다. | 이럴 때 어떻게 해야 할까요? | false |
| SAFE-SEX-001-S02 | SAFE-SEX-001 | 2 | 점심시간에 동료가 사생활 질문을 하는 장면 | 점심시간, 동료가 옆에 앉더니 "남자(여자)친구랑 진도 어디까지 나갔어?"라고 묻습니다. | 이럴 때 어떻게 해야 할까요? | false |
| SAFE-SEX-001-S03 | SAFE-SEX-001 | 3 | 퇴근 후 동료가 사진을 요구하는 메시지를 보내는 장면 | 퇴근 후, 동료가 메시지로 "OO 씨 사진 한 장만 보내줘. 우리끼리만 볼게"라고 합니다. | 이럴 때 어떻게 해야 할까요? | false |
| SAFE-SEX-001-S04 | SAFE-SEX-001 | 4 | 성희롱 신고 후 동료가 2차 피해성 발언을 하는 장면 | 당신은 용기를 내어 직장 내 성희롱 사실을 회사에 알렸습니다. 그런데 평소 친하게 지내던 옆 부서 동료가 다가와 "OO 씨, 정말 그런 일이 있었다고? 솔직히 네 말이 사실인지 모르겠다. 네가 좀 부풀려서 말한 거 아냐? 그냥 좋게 해결하자."라고 말합니다. | 이럴 때 어떻게 해야 할까요? | true |

### Choice 목록

| choice_seed_code | scene_seed_code | choice_order | choice_text | next_scene_seed_code | is_correct | result_text | effect_text |
|---|---|---:|---|---|---|---|---|
| SAFE-SEX-001-S01-C01 | SAFE-SEX-001-S01 | 1 | 기분이 나쁘지만 사수니까 꾹 참고 가만히 있는다. | SAFE-SEX-001-S01 | false | X! 참거나 똑같이 장난치면 상대방은 동의한 것으로 오해해요. 불쾌할 땐 단호히 말해야 해요! | 배경이 어두워지며 사이렌 소리가 들립니다. |
| SAFE-SEX-001-S01-C02 | SAFE-SEX-001-S01 | 2 | 만지지 말라고 분명하게 말한다. | SAFE-SEX-001-S02 | true | 사수가 당황하며 손을 뗍니다. "미안해요, 내가 실수했네." | 사수가 손을 떼고 다음 상황으로 이동합니다. |
| SAFE-SEX-001-S01-C03 | SAFE-SEX-001-S01 | 3 | 똑같이 사수의 어깨를 만진다. | SAFE-SEX-001-S01 | false | X! 참거나 똑같이 장난치면 상대방은 동의한 것으로 오해해요. 불쾌할 땐 단호히 말해야 해요! | 배경이 어두워지며 사이렌 소리가 들립니다. |
| SAFE-SEX-001-S02-C01 | SAFE-SEX-001-S02 | 1 | 당황해서 얼굴이 빨개진 채 대답한다. | SAFE-SEX-001-S02 | false | X! 사생활 질문에 답할 의무는 없어요! | 동료가 더 무례한 질문을 이어갑니다. |
| SAFE-SEX-001-S02-C02 | SAFE-SEX-001-S02 | 2 | "그건 예의가 없는 질문입니다. 대답하지 않겠습니다."라고 한다. | SAFE-SEX-001-S03 | true | 동료가 머쓱해하며 자리를 뜹니다. | 동료가 물러나고 다음 상황으로 이동합니다. |
| SAFE-SEX-001-S02-C03 | SAFE-SEX-001-S02 | 3 | 화를 내며 식판을 엎는다. | SAFE-SEX-001-S02 | false | X! 사생활 질문에 답할 의무는 없어요! | 동료가 더 무례한 질문을 이어갑니다. |
| SAFE-SEX-001-S03-C01 | SAFE-SEX-001-S03 | 1 | 한 장만 찍어서 보낸다. | SAFE-SEX-001-S03 | false | X! 내 사진은 소중한 개인정보예요. 함부로 보내면 나쁜 일에 쓰일 수 있어요! | 내 사진이 인터넷에 떠도는 아찔한 상상 연출이 나옵니다. |
| SAFE-SEX-001-S03-C02 | SAFE-SEX-001-S03 | 2 | "이런 사진을 요구하지 마세요."라고 얘기한다. | SAFE-SEX-001-S04 | true | 상대방이 당황하며 "농담이었어..."라고 답장을 보냅니다. 나의 사진을 지키는 것은 나의 안전을 지키는 일이에요. 정말 잘했어요! | 상대방이 물러나고 다음 상황으로 이동합니다. |
| SAFE-SEX-001-S03-C03 | SAFE-SEX-001-S03 | 3 | 욕설 메시지를 보낸다. | SAFE-SEX-001-S03 | false | X! 내 사진은 소중한 개인정보예요. 함부로 보내면 나쁜 일에 쓰일 수 있어요! | 내 사진이 인터넷에 떠도는 아찔한 상상 연출이 나옵니다. |
| SAFE-SEX-001-S04-C01 | SAFE-SEX-001-S04 | 1 | "내가 너무 예민했나?" 생각하며 신고를 취소한다. | SAFE-SEX-001-S04 | false | X! 주변 사람들이 믿어주지 않거나 참으라고 하는 것은 명백한 2차 피해예요. 나의 소중한 권리를 포기하지 마세요! | 위축되고 어두운 분위기의 연출이 나옵니다. |
| SAFE-SEX-001-S04-C02 | SAFE-SEX-001-S04 | 2 | "제 경험은 사실입니다. 의심하지 말아주세요."라고 얘기한다. | NULL | true | 나의 경험을 당당하게 말하는 것은 나의 안전을 지키는 일이에요. 주변의 의심을 용기 있게 디딤으로써 더 건강한 직장 생활을 할 수 있습니다. 정말 잘했어요! | 성공 엔딩 연출이 나옵니다. |
| SAFE-SEX-001-S04-C03 | SAFE-SEX-001-S04 | 3 | "너 가해자랑 한패지?"라며 화를 내고 욕설을 한다. | SAFE-SEX-001-S04 | false | X! 주변 사람들이 믿어주지 않거나 참으라고 하는 것은 명백한 2차 피해예요. 나의 소중한 권리를 포기하지 마세요! | 갈등이 커지는 연출이 나옵니다. |

---

## 2.5 SAFE-INF-001: 함께 만드는 안심 일터

### Scene 목록

| scene_seed_code | scenario_seed_code | scene_order | screen_info | situation_text | question_text | is_end_scene |
|---|---|---:|---|---|---|---|
| SAFE-INF-001-S01 | SAFE-INF-001 | 1 | 화장실에서 손을 씻는 장면 | 화장실에 갔습니다. 옆 사람이 물로만 3초 만에 씻고 나가는 걸 보니 나도 마음이 급해집니다. | 이럴 때 어떻게 해야 할까요? | false |
| SAFE-INF-001-S02 | SAFE-INF-001 | 2 | 회의 중 기침이 나오려는 장면 | 중요한 회의 중 갑자기 기침이 터져 나오려 합니다. 마스크가 없는 순간이라면? | 이럴 때 어떻게 해야 할까요? | false |
| SAFE-INF-001-S03 | SAFE-INF-001 | 3 | 일하는 중 열이 나고 몸이 무거운 장면 | 일을 하던 중 갑자기 열이 나고 몸이 너무 무겁습니다. 일이 조금 남았는데 어떡할까요? | 이럴 때 어떻게 해야 할까요? | true |

### Choice 목록

| choice_seed_code | scene_seed_code | choice_order | choice_text | next_scene_seed_code | is_correct | result_text | effect_text |
|---|---|---:|---|---|---|---|---|
| SAFE-INF-001-S01-C01 | SAFE-INF-001-S01 | 1 | 나도 물로만 대충 헹군다. | SAFE-INF-001-S01 | false | X! 비누가 없으면 세균이 죽지 않아요! | 손바닥에 세균 괴물이 가득한 연출이 나옵니다. |
| SAFE-INF-001-S01-C02 | SAFE-INF-001-S01 | 2 | 비누로 거품을 내어 30초간 꼼꼼히 씻는다. | SAFE-INF-001-S02 | true | 손에서 반짝반짝 빛이 나는 연출. | 손이 깨끗해지고 다음 상황으로 이동합니다. |
| SAFE-INF-001-S01-C03 | SAFE-INF-001-S01 | 3 | 마른 수건으로 손을 세게 닦기만 한다. | SAFE-INF-001-S01 | false | X! 비누가 없으면 세균이 죽지 않아요! | 손바닥에 세균 괴물이 가득한 연출이 나옵니다. |
| SAFE-INF-001-S02-C01 | SAFE-INF-001-S02 | 1 | 손바닥으로 가리고 기침한다. | SAFE-INF-001-S02 | false | X! 손으로 가리면 세균이 손에 묻어 다른 사람에게 병을 옮길 수 있어요! | 회의 테이블 위로 세균이 튀는 연출이 나옵니다. |
| SAFE-INF-001-S02-C02 | SAFE-INF-001-S02 | 2 | 옷소매 안쪽으로 입과 코를 완전히 가린다. | SAFE-INF-001-S03 | true | 세균을 완벽히 차단했습니다. | 올바른 기침 예절 후 다음 상황으로 이동합니다. |
| SAFE-INF-001-S02-C03 | SAFE-INF-001-S02 | 3 | 참다가 공중에 대고 기침한다. | SAFE-INF-001-S02 | false | X! 손으로 가리면 세균이 손에 묻어 다른 사람에게 병을 옮길 수 있어요! | 회의 테이블 위로 세균이 튀는 연출이 나옵니다. |
| SAFE-INF-001-S03-C01 | SAFE-INF-001-S03 | 1 | 끝까지 참고 앉아서 일을 마친다. | SAFE-INF-001-S03 | false | X! 아픈 것을 참고 일하면 내 병이 깊어질 뿐만 아니라, 소중한 동료들까지 위험해질 수 있어요! | 주인공이 책상에 엎드려 끙끙 앓고, 주변 동료들도 하나둘씩 기침을 하기 시작하는 우울한 연출이 나옵니다. |
| SAFE-INF-001-S03-C02 | SAFE-INF-001-S03 | 2 | 관리자에게 몸 상태를 보고하고 퇴근해 쉰다. | NULL | true | 매니저님이 "쉬어야 빨리 낫죠"라고 격려하며 퇴근을 도와주는 훈훈한 엔딩. | 성공 엔딩 연출이 나옵니다. |
| SAFE-INF-001-S03-C03 | SAFE-INF-001-S03 | 3 | 화장실에 숨어서 쉰다. | SAFE-INF-001-S03 | false | X! 아픈 것을 참고 일하면 내 병이 깊어질 뿐만 아니라, 소중한 동료들까지 위험해질 수 있어요! | 주인공이 책상에 엎드려 끙끙 앓고, 주변 동료들도 하나둘씩 기침을 하기 시작하는 우울한 연출이 나옵니다. |

---

## 2.6 SAFE-COM-001: 무사히 회사까지

### Scene 목록

| scene_seed_code | scenario_seed_code | scene_order | screen_info | situation_text | question_text | is_end_scene |
|---|---|---:|---|---|---|---|
| SAFE-COM-001-S01 | SAFE-COM-001 | 1 | 인도에서 스마트폰 알림이 오는 장면 | 인도를 걷는데 스마트폰이 울립니다. 친구가 보낸 재미있는 영상 메시지가 궁금해서 자꾸 눈이 갑니다. | 이럴 때 어떻게 해야 할까요? | false |
| SAFE-COM-001-S02 | SAFE-COM-001 | 2 | 횡단보도 신호등이 깜빡이는 장면 | 버스를 놓칠 것 같은데 신호등이 깜빡거리기 시작합니다. 지금 전력 질주하면 건널 수 있을 것 같습니다. | 이럴 때 어떻게 해야 할까요? | false |
| SAFE-COM-001-S03 | SAFE-COM-001 | 3 | 버스 정류장에서 사람들이 차도 쪽으로 몰리는 장면 | 버스 정류장에 내가 탈 버스가 보입니다. 사람들이 먼저 타려고 차도 쪽으로 우르르 몰려나갑니다. | 이럴 때 어떻게 해야 할까요? | false |
| SAFE-COM-001-S04 | SAFE-COM-001 | 4 | 버스 안에서 갑자기 크게 흔들리는 장면 | 버스 안에 자리가 없어 서서 가는데, 갑자기 버스가 크게 흔들립니다. | 이럴 때 어떻게 해야 할까요? | true |

### Choice 목록

| choice_seed_code | scene_seed_code | choice_order | choice_text | next_scene_seed_code | is_correct | result_text | effect_text |
|---|---|---:|---|---|---|---|---|
| SAFE-COM-001-S01-C01 | SAFE-COM-001-S01 | 1 | 걸으면서 영상을 본다. | SAFE-COM-001-S01 | false | X! 스마트폰에 눈이 팔리면 다가오는 차나 킥보드를 못 봐서 크게 다칠 수 있어요! | 화면이 흔들리며 전동 킥보드 경적 소리가 납니다. |
| SAFE-COM-001-S01-C02 | SAFE-COM-001-S01 | 2 | 영상을 보고 싶지만 참고 스마트폰을 가방에 넣는다. | SAFE-COM-001-S02 | true | 앞을 보며 걷는 당신은 아주 멋진 직장인입니다. 이제 무사히 횡단보도 앞에 도착했습니다. | 안전하게 걷고 다음 상황으로 이동합니다. |
| SAFE-COM-001-S01-C03 | SAFE-COM-001-S01 | 3 | 이어폰 볼륨을 키우고 앞만 보며 걷는다. | SAFE-COM-001-S01 | false | X! 스마트폰에 눈이 팔리면 다가오는 차나 킥보드를 못 봐서 크게 다칠 수 있어요! | 화면이 흔들리며 전동 킥보드 경적 소리가 납니다. |
| SAFE-COM-001-S02-C01 | SAFE-COM-001-S02 | 1 | 가방을 고쳐 매고 전력 질주한다. | SAFE-COM-001-S02 | false | X! 깜빡이는 신호에 뛰는 건 아주 위험해요. 늦더라도 안전이 먼저예요! | 건너는 도중 빨간불로 바뀌고 차들이 위태롭게 멈추는 연출이 나옵니다. |
| SAFE-COM-001-S02-C02 | SAFE-COM-001-S02 | 2 | 무리하게 뛰지 않고 멈춰 서서 다음 신호를 기다린다. | SAFE-COM-001-S03 | true | 차분하게 숨을 고르며 다음 신호를 기다립니다. 무사히 길을 건너 버스 정류장에 도착했습니다! | 안전하게 길을 건너고 다음 상황으로 이동합니다. |
| SAFE-COM-001-S02-C03 | SAFE-COM-001-S02 | 3 | 차가 오는지 슬쩍 보고 무단횡단을 한다. | SAFE-COM-001-S02 | false | X! 깜빡이는 신호에 뛰는 건 아주 위험해요. 늦더라도 안전이 먼저예요! | 건너는 도중 빨간불로 바뀌고 차들이 위태롭게 멈추는 연출이 나옵니다. |
| SAFE-COM-001-S03-C01 | SAFE-COM-001-S03 | 1 | 나도 차도로 내려가서 기다린다. | SAFE-COM-001-S03 | false | X! 차도로 내려가는 건 아주 위험해요. 안전선은 생명선입니다! | 버스 바퀴가 내 발 앞까지 오는 아찔한 연출이 나옵니다. |
| SAFE-COM-001-S03-C02 | SAFE-COM-001-S03 | 2 | 노란색 안전선 안쪽에서 버스가 정차할 때까지 기다린다. | SAFE-COM-001-S04 | true | 버스가 완전히 멈춘 후 질서 있게 탑승합니다. | 안전하게 버스에 탑승하고 다음 상황으로 이동합니다. |
| SAFE-COM-001-S03-C03 | SAFE-COM-001-S03 | 3 | 버스 문이 열리기 전에 차 쪽으로 뛰어간다. | SAFE-COM-001-S03 | false | X! 차도로 내려가는 건 아주 위험해요. 안전선은 생명선입니다! | 버스 바퀴가 내 발 앞까지 오는 아찔한 연출이 나옵니다. |
| SAFE-COM-001-S04-C01 | SAFE-COM-001-S04 | 1 | 두 손으로 손잡이를 꼭 잡는다. | NULL | true | 무사히 회사 앞에 도착해 출근 카드를 찍는 성공 엔딩! | 성공 엔딩 연출이 나옵니다. |
| SAFE-COM-001-S04-C02 | SAFE-COM-001-S04 | 2 | 중심을 잡으며 폰을 본다. | SAFE-COM-001-S04 | false | X! 버스 안에서 손잡이를 잡지 않으면 급정거할 때 크게 다칠 수 있어요. 폰보다 손잡이가 먼저! | 버스가 급정거하며 몸이 앞으로 쏠려 바닥에 넘어지는 연출이 나옵니다. |
| SAFE-COM-001-S04-C03 | SAFE-COM-001-S04 | 3 | 문에 몸을 기대어 서 있는다. | SAFE-COM-001-S04 | false | X! 버스 안에서 손잡이를 잡지 않으면 급정거할 때 크게 다칠 수 있어요. 폰보다 손잡이가 먼저! | 버스가 급정거하며 몸이 앞으로 쏠려 바닥에 넘어지는 연출이 나옵니다. |

---

# 3. 문서 이해 훈련 Seed Data

## 3.1 대상 테이블

```text
document_questions
document_question_choices
```

## 3.2 추천 컬럼 구조

### document_questions

```text
question_id
seed_code
title
document_text
question_text
question_type          -- MULTIPLE_CHOICE
difficulty             -- LEVEL_1 ~ LEVEL_5
correct_answer
explanation
correct_feedback
wrong_feedback
is_active
```

### document_question_choices

```text
choice_id
seed_code
question_id
choice_order
choice_text
is_correct
```

---

## 3.3 문서 이해 문제 목록

### DOC-L1-001: 신입사원 워크숍 안내

| column | value |
|---|---|
| seed_code | DOC-L1-001 |
| title | 신입사원 워크숍 안내 |
| difficulty | LEVEL_1 |
| question_type | MULTIPLE_CHOICE |
| document_text | 이번 신입사원 워크숍은 5월 10일 강원도 속초에서 진행됩니다. 모든 참가자는 당일 오전 8시까지 회사 정문 앞 대형 버스에 탑승해야 합니다. 개인 세면도구와 운동화는 개별적으로 준비하시기 바랍니다. 숙소 내에서 취사는 금지되어 있으며, 저녁 식사는 인근 식당에서 단체로 진행될 예정입니다. |
| question_text | 위 글의 내용과 일치하지 않는 것은 무엇인가요? |
| correct_answer | 숙소 안에서 직접 음식을 만들어 먹을 수 있다. |
| explanation | 숙소 내에서 취사는 금지되어 있으므로, 직접 음식을 만들어 먹을 수 있다는 내용은 글과 일치하지 않습니다. |
| wrong_feedback | X! 글의 마지막 문장에서 '취사는 금지'라고 적혀 있어요. 다시 확인해 볼까요? |
| correct_feedback | 정답입니다! 세부 사항을 꼼꼼하게 읽으셨네요! |

| choice_seed_code | question_seed_code | choice_order | choice_text | is_correct |
|---|---|---:|---|---|
| DOC-L1-001-C01 | DOC-L1-001 | 1 | 워크숍 장소는 강원도 속초이다. | false |
| DOC-L1-001-C02 | DOC-L1-001 | 2 | 오전 8시까지 버스에 타야 한다. | false |
| DOC-L1-001-C03 | DOC-L1-001 | 3 | 숙소 안에서 직접 음식을 만들어 먹을 수 있다. | true |

---

### DOC-L2-001: 사내 복사기 사용 및 고장 대처법

| column | value |
|---|---|
| seed_code | DOC-L2-001 |
| title | 사내 복사기 사용 및 고장 대처법 |
| difficulty | LEVEL_2 |
| question_type | MULTIPLE_CHOICE |
| document_text | 복사기를 사용하기 전에는 먼저 종이가 충분한지 확인해야 합니다. 만약 종이가 걸렸을 경우에는 강제로 잡아당기지 마십시오. 먼저 앞 덮개를 열고 걸린 종이를 천천히 제거한 뒤, 다시 덮개를 닫고 '재시작' 버튼을 누르세요. 만약 이 방법으로도 해결되지 않는다면, 즉시 관리부(내선 102번)로 연락하여 수리를 요청해야 합니다. 직접 기계를 분해하는 행동은 위험하므로 절대 금합니다. |
| question_text | 종이가 걸렸을 때 가장 먼저 해야 할 행동은 무엇인가요? |
| correct_answer | 복사기 앞 덮개 열기 |
| explanation | 종이가 걸렸을 경우 먼저 앞 덮개를 열고 걸린 종이를 천천히 제거해야 합니다. |
| wrong_feedback | 아쉬워요! 문제 해결의 '순서'를 찾는 것이 중요해요. '먼저'라는 글자 뒤를 찾아보세요. |
| correct_feedback | 훌륭합니다! 업무 매뉴얼의 순서를 완벽히 파악하셨습니다. |

| choice_seed_code | question_seed_code | choice_order | choice_text | is_correct |
|---|---|---:|---|---|
| DOC-L2-001-C01 | DOC-L2-001 | 1 | 관리부에 전화하기 | false |
| DOC-L2-001-C02 | DOC-L2-001 | 2 | 복사기 앞 덮개 열기 | true |
| DOC-L2-001-C03 | DOC-L2-001 | 3 | 걸린 종이 강제로 잡아당기기 | false |

---

### DOC-L3-001: 비품 절약 및 관리 협조 요청

| column | value |
|---|---|
| seed_code | DOC-L3-001 |
| title | 비품 절약 및 관리 협조 요청 |
| difficulty | LEVEL_3 |
| question_type | MULTIPLE_CHOICE |
| document_text | 임직원 여러분, 최근 사무용품 소모량이 예년에 비해 30% 이상 증가했습니다. 특히 종이컵과 A4 용지의 무분별한 사용이 예산 낭비의 주요 원인이 되고 있습니다. 따라서 오늘부터 회의 시 개인 컵 사용을 권장하며, 이면지 활용을 생활화해주시기 바랍니다. 작은 실천이 모여 우리 회사의 환경을 보호하고 예산을 절감할 수 있습니다. 여러분의 적극적인 협조 부탁드립니다. |
| question_text | 이 메일을 보낸 가장 큰 목적은 무엇인가요? |
| correct_answer | 사무용품을 아껴 쓰자고 제안하기 위해 |
| explanation | 글쓴이는 사무용품 사용 증가로 인한 예산 낭비를 줄이기 위해 개인 컵 사용과 이면지 활용을 제안하고 있습니다. |
| wrong_feedback | X! 글쓴이가 이 글을 왜 썼는지 '따라서' 뒤에 나오는 결론을 확인해 보세요. |
| correct_feedback | 완벽해요! 글의 핵심 주제를 정확하게 짚어내셨습니다. |

| choice_seed_code | question_seed_code | choice_order | choice_text | is_correct |
|---|---|---:|---|---|
| DOC-L3-001-C01 | DOC-L3-001 | 1 | 종이컵의 종류를 알려주기 위해 | false |
| DOC-L3-001-C02 | DOC-L3-001 | 2 | 사무용품을 아껴 쓰자고 제안하기 위해 | true |
| DOC-L3-001-C03 | DOC-L3-001 | 3 | 새로운 회의 장소를 안내하기 위해 | false |

---

# 4. 전체 Seed 수량 요약

| 영역 | 테이블 | seed 수량 |
|---|---|---:|
| 사회성 훈련 | social_scenarios | 18 |
| 안전 훈련 | safety_scenarios | 3 |
| 안전 장면 | safety_scenes | 11 |
| 안전 선택지 | safety_choices | 33 |
| 문서 이해 문제 | document_questions | 3 |
| 문서 이해 선택지 | document_question_choices | 9 |

---

# 5. DB Insert 변환 시 주의사항

## 5.1 safety_choices.next_scene_id 처리

Markdown에서는 `next_scene_seed_code`로 표기했습니다.

실제 DB insert에서는 다음 순서로 처리합니다.

1. `safety_scenarios` insert
2. `safety_scenes` insert
3. `scene_seed_code -> scene_id` 매핑 생성
4. `safety_choices` insert 시 `next_scene_seed_code`를 실제 `next_scene_id`로 변환

## 5.2 종료 선택지 처리

성공 엔딩으로 끝나는 선택지는 `next_scene_seed_code = NULL`입니다.

실제 DB에서는 `next_scene_id`를 `NULL`로 저장합니다.

## 5.3 사회성 훈련 채점 처리

사회성 훈련은 선택지가 없으므로 `evaluation_point`를 기준으로 AI 평가 또는 룰 기반 평가를 수행합니다.

`example_answer`는 사용자에게 직접 노출할 수도 있고, 내부 평가 기준으로만 사용할 수도 있습니다.

## 5.4 문서 이해 선택지 처리

현재 기본 DB 명세의 `document_questions`에는 선택지 저장 구조가 부족하므로, 객관식 문제를 정상적으로 관리하려면 `document_question_choices` 테이블을 추가하는 것을 권장합니다.
