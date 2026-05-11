# Safety Training Scenario Seed Draft

이 문서는 실제 `safety_scenarios`, `safety_scenes`, `safety_choices` seed를 만들기 위한 원고 초안이다.

구조:

- `story-01`~`story-03`: 이야기 전개 장면. 선택지 없음.
- `story-04`: 최종 판단 장면. 선택지 3개.
- `correct`: 정답 선택 결과 문구와 이미지.
- `fail`: 오답 선택 결과 문구와 이미지.

프론트 동작:

- `choices`가 빈 배열이면 다음 버튼으로 `/advance-scene` 호출.
- `choices`가 있으면 선택지를 표시하고 `/next-scene` 호출.

---

## 1. 성 교육

### SAFE-SEX-001: 불편한 신체 접촉 거절하기

| field | value |
|---|---|
| category | `SEXUAL_EDUCATION` |
| title | 불편한 신체 접촉 거절하기 |
| description | 직장에서 상대방의 신체 접촉이 불편할 때 분명하게 거절하는 훈련 |
| assetPath | `/trainings/safety/sex-education/scenario-01` |

| scene | screen_info | situation_text | question_text | image_url |
|---|---|---|---|---|
| story-01 | 업무를 마치고 쉬는 장면 | 일을 마치고 잠깐 쉬고 있습니다. 사수가 다가와 오늘 고생이 많았다고 말합니다. |  | `/trainings/safety/sex-education/scenario-01/story-01.png` |
| story-02 | 사수가 가까이 다가오는 장면 | 사수가 내 뒤쪽으로 오더니 어깨 가까이에 손을 올립니다. 나는 조금 불편한 느낌이 듭니다. |  | `/trainings/safety/sex-education/scenario-01/story-02.png` |
| story-03 | 몸이 굳어지는 장면 | 사수가 장난처럼 웃으며 계속 가까이 있습니다. 나는 어떻게 말해야 할지 몰라 몸이 굳습니다. |  | `/trainings/safety/sex-education/scenario-01/story-03.png` |
| story-04 | 사수가 어깨를 주무르는 장면 | 사수가 어깨를 주무르며 "우리 OO 씨 고생하네~"라고 합니다. 나는 너무 당황해서 몸이 얼어버렸습니다. | 이럴 때 어떻게 해야 할까요? | `/trainings/safety/sex-education/scenario-01/story-04.png` |

| order | choice_text | is_correct | result_text | effect_text | feedback_image_url |
|---:|---|---|---|---|---|
| 1 | 기분이 나쁘지만 사수니까 꾹 참고 가만히 있는다. | false | X! 참으면 상대방은 괜찮다고 오해할 수 있어요. 불편할 때는 내 마음을 분명히 말해야 해요. | 불편한 상황이 계속됩니다. | `/trainings/safety/sex-education/scenario-01/fail.png` |
| 2 | "만지지 말아 주세요. 저는 불편합니다."라고 분명하게 말한다. | true | 잘했어요. 내 몸의 경계를 지키는 말은 예의 없는 말이 아니라 안전한 말이에요. | 사수가 당황하며 손을 떼고 사과합니다. | `/trainings/safety/sex-education/scenario-01/correct.png` |
| 3 | 나도 장난처럼 사수의 어깨를 만진다. | false | X! 똑같이 장난치면 상대방이 동의한 것으로 오해할 수 있어요. 불편하면 멈춰 달라고 말해야 해요. | 장난처럼 보이면서 상황이 더 헷갈려집니다. | `/trainings/safety/sex-education/scenario-01/fail.png` |

### SAFE-SEX-002: 사생활 질문 거절하기

| field | value |
|---|---|
| category | `SEXUAL_EDUCATION` |
| title | 사생활 질문 거절하기 |
| description | 직장 동료가 불편한 사생활 질문을 할 때 대답하지 않아도 된다는 것을 익히는 훈련 |
| assetPath | `/trainings/safety/sex-education/scenario-02` |

| scene | screen_info | situation_text | question_text | image_url |
|---|---|---|---|---|
| story-01 | 점심시간에 앉아 있는 장면 | 점심시간이 되어 식당에 앉았습니다. 조용히 밥을 먹으며 쉬고 싶습니다. |  | `/trainings/safety/sex-education/scenario-02/story-01.png` |
| story-02 | 동료가 옆에 앉는 장면 | 평소 친하지 않은 동료가 내 옆에 앉아 이것저것 묻기 시작합니다. |  | `/trainings/safety/sex-education/scenario-02/story-02.png` |
| story-03 | 질문이 점점 개인적으로 변하는 장면 | 동료의 질문이 점점 개인적인 이야기로 바뀝니다. 나는 대답하기 싫고 마음이 불편합니다. |  | `/trainings/safety/sex-education/scenario-02/story-03.png` |
| story-04 | 동료가 사생활 질문을 하는 장면 | 점심시간, 동료가 옆에 앉더니 "남자친구나 여자친구랑 진도 어디까지 나갔어?"라고 묻습니다. | 이럴 때 어떻게 해야 할까요? | `/trainings/safety/sex-education/scenario-02/story-04.png` |

| order | choice_text | is_correct | result_text | effect_text | feedback_image_url |
|---:|---|---|---|---|---|
| 1 | 당황해서 얼굴이 빨개진 채 대답한다. | false | X! 사생활 질문에는 대답할 의무가 없어요. 불편한 질문은 거절해도 됩니다. | 동료가 더 자세히 묻기 시작합니다. | `/trainings/safety/sex-education/scenario-02/fail.png` |
| 2 | "그건 사적인 질문이라 대답하지 않겠습니다."라고 말한다. | true | 잘했어요. 내 사생활은 내가 지킬 수 있고, 불편한 질문에는 선을 그어도 됩니다. | 동료가 머쓱해하며 질문을 멈춥니다. | `/trainings/safety/sex-education/scenario-02/correct.png` |
| 3 | 화를 내며 식판을 엎는다. | false | X! 불편하다고 해서 물건을 던지거나 크게 화를 내면 갈등이 커질 수 있어요. 차분히 거절하는 것이 좋아요. | 주변 사람들이 놀라며 상황이 복잡해집니다. | `/trainings/safety/sex-education/scenario-02/fail.png` |

### SAFE-SEX-003: 사진 요구 거절하기

| field | value |
|---|---|
| category | `SEXUAL_EDUCATION` |
| title | 사진 요구 거절하기 |
| description | 개인 사진을 요구받았을 때 개인정보와 안전을 지키는 훈련 |
| assetPath | `/trainings/safety/sex-education/scenario-03` |

| scene | screen_info | situation_text | question_text | image_url |
|---|---|---|---|---|
| story-01 | 퇴근 후 휴대폰을 보는 장면 | 퇴근 후 집에 가는 길입니다. 휴대폰에 직장 동료의 메시지가 도착합니다. |  | `/trainings/safety/sex-education/scenario-03/story-01.png` |
| story-02 | 동료가 친근하게 말을 거는 장면 | 동료는 장난스러운 말투로 말을 걸며 나에게 더 가까운 사이처럼 행동합니다. |  | `/trainings/safety/sex-education/scenario-03/story-02.png` |
| story-03 | 사진 이야기가 나오는 장면 | 동료가 내 사진을 보고 싶다는 말을 합니다. 나는 왜 필요한지 몰라 불안합니다. |  | `/trainings/safety/sex-education/scenario-03/story-03.png` |
| story-04 | 동료가 사진을 요구하는 메시지를 보내는 장면 | 퇴근 후, 동료가 메시지로 "OO 씨 사진 한 장만 보내줘. 우리끼리만 볼게"라고 합니다. | 이럴 때 어떻게 해야 할까요? | `/trainings/safety/sex-education/scenario-03/story-04.png` |

| order | choice_text | is_correct | result_text | effect_text | feedback_image_url |
|---:|---|---|---|---|---|
| 1 | 한 장만 찍어서 보낸다. | false | X! 내 사진은 소중한 개인정보예요. 함부로 보내면 원하지 않는 곳에 쓰일 수 있어요. | 사진이 다른 사람에게 퍼질 위험이 생깁니다. | `/trainings/safety/sex-education/scenario-03/fail.png` |
| 2 | "이런 사진을 요구하지 마세요."라고 답한다. | true | 잘했어요. 내 사진을 지키는 것은 내 안전을 지키는 일이에요. | 상대방이 당황하며 요구를 멈춥니다. | `/trainings/safety/sex-education/scenario-03/correct.png` |
| 3 | 욕설 메시지를 보낸다. | false | X! 거절은 필요하지만 욕설로 답하면 갈등이 커질 수 있어요. 단호하고 짧게 거절하세요. | 대화가 싸움으로 번질 수 있습니다. | `/trainings/safety/sex-education/scenario-03/fail.png` |

### SAFE-SEX-004: 2차 피해 발언 대응하기

| field | value |
|---|---|
| category | `SEXUAL_EDUCATION` |
| title | 2차 피해 발언 대응하기 |
| description | 신고나 상담 후 주변의 의심과 압박에 흔들리지 않고 내 경험을 지키는 훈련 |
| assetPath | `/trainings/safety/sex-education/scenario-04` |

| scene | screen_info | situation_text | question_text | image_url |
|---|---|---|---|---|
| story-01 | 상담을 결심하는 장면 | 나는 직장에서 겪은 불편한 일을 혼자 참지 않기로 했습니다. 도움을 받을 수 있는 곳에 상담하기로 마음먹습니다. |  | `/trainings/safety/sex-education/scenario-04/story-01.png` |
| story-02 | 상담을 마친 장면 | 상담을 마치고 나니 조금 안심되지만, 주변 사람이 어떻게 볼지 걱정도 됩니다. |  | `/trainings/safety/sex-education/scenario-04/story-02.png` |
| story-03 | 동료가 다가오는 장면 | 평소 친하게 지내던 동료가 다가옵니다. 동료는 내 이야기를 들었다며 조심스럽게 말을 꺼냅니다. |  | `/trainings/safety/sex-education/scenario-04/story-03.png` |
| story-04 | 동료가 2차 피해성 발언을 하는 장면 | 동료가 "OO 씨, 정말 그런 일이 있었어? 솔직히 네 말이 사실인지 모르겠다. 그냥 좋게 해결하자."라고 말합니다. | 이럴 때 어떻게 해야 할까요? | `/trainings/safety/sex-education/scenario-04/story-04.png` |

| order | choice_text | is_correct | result_text | effect_text | feedback_image_url |
|---:|---|---|---|---|---|
| 1 | "내가 너무 예민했나?" 생각하며 신고를 취소한다. | false | X! 주변 사람이 믿어주지 않거나 참으라고 하는 것은 2차 피해가 될 수 있어요. 내 권리를 포기하지 마세요. | 나는 더 위축되고 도움을 받기 어려워집니다. | `/trainings/safety/sex-education/scenario-04/fail.png` |
| 2 | "제 경험은 사실입니다. 의심하지 말아주세요."라고 말한다. | true | 잘했어요. 내 경험을 차분히 말하고 보호받을 권리를 지키는 것은 안전한 행동입니다. | 동료가 더 이상 압박하지 못하고 물러납니다. | `/trainings/safety/sex-education/scenario-04/correct.png` |
| 3 | "너 가해자랑 한패지?"라며 화를 내고 욕설을 한다. | false | X! 화가 나도 욕설로 대응하면 갈등이 커질 수 있어요. 내 경험은 사실이라고 차분히 말하는 것이 좋아요. | 말다툼이 커지고 내 마음도 더 힘들어집니다. | `/trainings/safety/sex-education/scenario-04/fail.png` |

---

## 2. 감염병 대응

### SAFE-INF-001: 손 씻기 지키기

| field | value |
|---|---|
| category | `INFECTIOUS_DISEASE` |
| title | 손 씻기 지키기 |
| description | 다른 사람이 대충 씻고 나가도 올바른 손 씻기를 실천하는 훈련 |
| assetPath | `/trainings/safety/infectious-disease-response/scenario-01` |

| scene | screen_info | situation_text | question_text | image_url |
|---|---|---|---|---|
| story-01 | 화장실에 들어가는 장면 | 근무 중 화장실에 갔습니다. 곧 다시 일하러 돌아가야 해서 시간이 많지 않습니다. |  | `/trainings/safety/infectious-disease-response/scenario-01/story-01.png` |
| story-02 | 옆 사람이 손을 대충 씻는 장면 | 옆 사람이 물만 살짝 묻히고 금방 나갑니다. 나도 빨리 나가야 할 것 같은 마음이 듭니다. |  | `/trainings/safety/infectious-disease-response/scenario-01/story-02.png` |
| story-03 | 세면대 앞에서 망설이는 장면 | 세면대 앞에 섰습니다. 비누를 써야 할지, 물로만 씻어도 될지 고민됩니다. |  | `/trainings/safety/infectious-disease-response/scenario-01/story-03.png` |
| story-04 | 화장실에서 손을 씻는 장면 | 화장실에 갔습니다. 옆 사람이 물로만 3초 만에 씻고 나가는 걸 보니 나도 마음이 급해집니다. | 이럴 때 어떻게 해야 할까요? | `/trainings/safety/infectious-disease-response/scenario-01/story-04.png` |

| order | choice_text | is_correct | result_text | effect_text | feedback_image_url |
|---:|---|---|---|---|---|
| 1 | 나도 물로만 대충 헹군다. | false | X! 비누 없이 대충 씻으면 세균이 잘 줄어들지 않아요. | 손에 세균이 남아 다른 곳으로 옮겨갈 수 있습니다. | `/trainings/safety/infectious-disease-response/scenario-01/fail.png` |
| 2 | 비누로 거품을 내어 30초간 꼼꼼히 씻는다. | true | 잘했어요. 비누로 꼼꼼히 씻으면 감염을 예방하는 데 도움이 됩니다. | 손이 깨끗해지고 안심하고 일터로 돌아갑니다. | `/trainings/safety/infectious-disease-response/scenario-01/correct.png` |
| 3 | 마른 수건으로 손을 세게 닦기만 한다. | false | X! 마른 수건으로 닦기만 해서는 손의 세균을 충분히 줄일 수 없어요. | 손이 깨끗해지지 않은 채 밖으로 나가게 됩니다. | `/trainings/safety/infectious-disease-response/scenario-01/fail.png` |

### SAFE-INF-002: 기침 예절 지키기

| field | value |
|---|---|
| category | `INFECTIOUS_DISEASE` |
| title | 기침 예절 지키기 |
| description | 마스크가 없을 때도 기침 예절을 지켜 주변 사람을 보호하는 훈련 |
| assetPath | `/trainings/safety/infectious-disease-response/scenario-02` |

| scene | screen_info | situation_text | question_text | image_url |
|---|---|---|---|---|
| story-01 | 회의에 참석한 장면 | 중요한 회의가 시작되었습니다. 여러 사람이 가까이 앉아 이야기를 듣고 있습니다. |  | `/trainings/safety/infectious-disease-response/scenario-02/story-01.png` |
| story-02 | 목이 간질거리는 장면 | 갑자기 목이 간질거리고 기침이 나올 것 같습니다. 주변 사람들도 가까이 있습니다. |  | `/trainings/safety/infectious-disease-response/scenario-02/story-02.png` |
| story-03 | 마스크가 없는 것을 확인하는 장면 | 주머니를 확인했지만 마스크가 없습니다. 기침을 참기 어려워지고 있습니다. |  | `/trainings/safety/infectious-disease-response/scenario-02/story-03.png` |
| story-04 | 회의 중 기침이 나오려는 장면 | 중요한 회의 중 갑자기 기침이 터져 나오려 합니다. 마스크가 없는 순간이라면? | 이럴 때 어떻게 해야 할까요? | `/trainings/safety/infectious-disease-response/scenario-02/story-04.png` |

| order | choice_text | is_correct | result_text | effect_text | feedback_image_url |
|---:|---|---|---|---|---|
| 1 | 손바닥으로 가리고 기침한다. | false | X! 손바닥으로 가리면 세균이 손에 묻어 다른 사람에게 옮겨갈 수 있어요. | 손에 묻은 세균이 책상과 물건에 퍼집니다. | `/trainings/safety/infectious-disease-response/scenario-02/fail.png` |
| 2 | 옷소매 안쪽으로 입과 코를 완전히 가린다. | true | 잘했어요. 옷소매로 입과 코를 가리면 주변 사람을 보호할 수 있어요. | 회의 중에도 세균이 퍼지는 것을 줄입니다. | `/trainings/safety/infectious-disease-response/scenario-02/correct.png` |
| 3 | 참다가 공중에 대고 기침한다. | false | X! 공중에 기침하면 주변 사람에게 침방울이 퍼질 수 있어요. | 회의실 안으로 세균이 퍼질 수 있습니다. | `/trainings/safety/infectious-disease-response/scenario-02/fail.png` |

### SAFE-INF-003: 아플 때 보고하고 쉬기

| field | value |
|---|---|
| category | `INFECTIOUS_DISEASE` |
| title | 아플 때 보고하고 쉬기 |
| description | 몸이 아플 때 억지로 일하지 않고 관리자에게 알리는 훈련 |
| assetPath | `/trainings/safety/infectious-disease-response/scenario-03` |

| scene | screen_info | situation_text | question_text | image_url |
|---|---|---|---|---|
| story-01 | 근무 중 몸이 무거운 장면 | 일을 하던 중 몸이 평소보다 무겁고 집중이 잘 되지 않습니다. |  | `/trainings/safety/infectious-disease-response/scenario-03/story-01.png` |
| story-02 | 열이 나는 것 같은 장면 | 이마가 뜨겁고 기운이 없습니다. 그래도 해야 할 일이 조금 남아 있습니다. |  | `/trainings/safety/infectious-disease-response/scenario-03/story-02.png` |
| story-03 | 주변 동료를 걱정하는 장면 | 가까운 자리에는 동료들이 일하고 있습니다. 내가 아프면 다른 사람에게도 영향을 줄 수 있습니다. |  | `/trainings/safety/infectious-disease-response/scenario-03/story-03.png` |
| story-04 | 일하는 중 열이 나고 몸이 무거운 장면 | 일을 하던 중 갑자기 열이 나고 몸이 너무 무겁습니다. 일이 조금 남았는데 어떡할까요? | 이럴 때 어떻게 해야 할까요? | `/trainings/safety/infectious-disease-response/scenario-03/story-04.png` |

| order | choice_text | is_correct | result_text | effect_text | feedback_image_url |
|---:|---|---|---|---|---|
| 1 | 끝까지 참고 앉아서 일을 마친다. | false | X! 아픈 것을 참고 일하면 내 몸도 더 힘들어지고 동료에게도 위험할 수 있어요. | 몸 상태가 더 나빠지고 주변 동료들도 걱정합니다. | `/trainings/safety/infectious-disease-response/scenario-03/fail.png` |
| 2 | 관리자에게 몸 상태를 보고하고 퇴근해 쉰다. | true | 잘했어요. 아플 때는 알리고 쉬는 것이 나와 동료 모두를 지키는 행동입니다. | 관리자가 쉬어도 된다고 안내하고 회복할 시간을 줍니다. | `/trainings/safety/infectious-disease-response/scenario-03/correct.png` |
| 3 | 화장실에 숨어서 쉰다. | false | X! 몸 상태를 숨기면 도움을 받기 어렵고 상황이 늦게 알려질 수 있어요. | 아무도 내 상태를 몰라 필요한 도움을 받지 못합니다. | `/trainings/safety/infectious-disease-response/scenario-03/fail.png` |

---

## 3. 출퇴근 안전

### SAFE-COM-001: 걸으며 휴대폰 보지 않기

| field | value |
|---|---|
| category | `COMMUTE_SAFETY` |
| title | 걸으며 휴대폰 보지 않기 |
| description | 보행 중 휴대폰 알림이 와도 주변을 보고 안전하게 걷는 훈련 |
| assetPath | `/trainings/safety/commute-safety/scenario-01` |

| scene | screen_info | situation_text | question_text | image_url |
|---|---|---|---|---|
| story-01 | 출근길 인도를 걷는 장면 | 아침 출근길입니다. 인도에는 사람과 자전거, 전동 킥보드가 지나갑니다. |  | `/trainings/safety/commute-safety/scenario-01/story-01.png` |
| story-02 | 휴대폰 알림이 울리는 장면 | 휴대폰에서 알림 소리가 납니다. 친구가 보낸 영상 메시지가 도착했습니다. |  | `/trainings/safety/commute-safety/scenario-01/story-02.png` |
| story-03 | 앞을 보지 못하는 위험 장면 | 휴대폰 화면을 보면 앞에서 오는 사람이나 킥보드를 늦게 볼 수 있습니다. |  | `/trainings/safety/commute-safety/scenario-01/story-03.png` |
| story-04 | 인도에서 스마트폰 알림이 오는 장면 | 인도를 걷는데 스마트폰이 울립니다. 친구가 보낸 재미있는 영상 메시지가 궁금해서 자꾸 눈이 갑니다. | 이럴 때 어떻게 해야 할까요? | `/trainings/safety/commute-safety/scenario-01/story-04.png` |

| order | choice_text | is_correct | result_text | effect_text | feedback_image_url |
|---:|---|---|---|---|---|
| 1 | 걸으면서 영상을 본다. | false | X! 스마트폰에 눈이 팔리면 다가오는 차나 킥보드를 못 봐서 크게 다칠 수 있어요. | 앞을 보지 못해 위험한 상황이 가까워집니다. | `/trainings/safety/commute-safety/scenario-01/fail.png` |
| 2 | 영상을 보고 싶지만 참고 스마트폰을 가방에 넣는다. | true | 잘했어요. 걸을 때는 앞과 주변을 보는 것이 안전합니다. | 안전하게 걸어 다음 장소로 이동합니다. | `/trainings/safety/commute-safety/scenario-01/correct.png` |
| 3 | 이어폰 볼륨을 키우고 앞만 보며 걷는다. | false | X! 앞만 봐도 주변 소리를 듣지 못하면 위험을 늦게 알아차릴 수 있어요. | 경고음이나 주변 소리를 놓칠 수 있습니다. | `/trainings/safety/commute-safety/scenario-01/fail.png` |

### SAFE-COM-002: 깜빡이는 신호에서 멈추기

| field | value |
|---|---|
| category | `COMMUTE_SAFETY` |
| title | 깜빡이는 신호에서 멈추기 |
| description | 횡단보도 신호가 곧 바뀔 때 무리하게 뛰지 않고 기다리는 훈련 |
| assetPath | `/trainings/safety/commute-safety/scenario-02` |

| scene | screen_info | situation_text | question_text | image_url |
|---|---|---|---|---|
| story-01 | 횡단보도 앞에 도착한 장면 | 회사로 가는 길에 횡단보도 앞에 도착했습니다. 길 건너편에는 버스 정류장이 보입니다. |  | `/trainings/safety/commute-safety/scenario-02/story-01.png` |
| story-02 | 버스가 곧 올 것 같은 장면 | 버스가 곧 올 것 같아 마음이 급해집니다. 빨리 건너야 할 것 같은 생각이 듭니다. |  | `/trainings/safety/commute-safety/scenario-02/story-02.png` |
| story-03 | 신호가 깜빡이는 장면 | 보행자 신호가 깜빡이기 시작합니다. 차들도 조금씩 움직일 준비를 하고 있습니다. |  | `/trainings/safety/commute-safety/scenario-02/story-03.png` |
| story-04 | 횡단보도 신호등이 깜빡이는 장면 | 버스를 놓칠 것 같은데 신호등이 깜빡거리기 시작합니다. 지금 전력 질주하면 건널 수 있을 것 같습니다. | 이럴 때 어떻게 해야 할까요? | `/trainings/safety/commute-safety/scenario-02/story-04.png` |

| order | choice_text | is_correct | result_text | effect_text | feedback_image_url |
|---:|---|---|---|---|---|
| 1 | 가방을 고쳐 매고 전력 질주한다. | false | X! 깜빡이는 신호에 뛰는 건 아주 위험해요. 늦더라도 안전이 먼저예요. | 건너는 도중 신호가 바뀌어 차와 가까워집니다. | `/trainings/safety/commute-safety/scenario-02/fail.png` |
| 2 | 무리하게 뛰지 않고 멈춰 서서 다음 신호를 기다린다. | true | 잘했어요. 급해도 멈춰 기다리는 것이 가장 안전합니다. | 차분하게 다음 신호를 기다린 뒤 안전하게 건넙니다. | `/trainings/safety/commute-safety/scenario-02/correct.png` |
| 3 | 차가 오는지 슬쩍 보고 무단횡단을 한다. | false | X! 차가 없어 보여도 무단횡단은 매우 위험합니다. | 예상하지 못한 차가 가까이 올 수 있습니다. | `/trainings/safety/commute-safety/scenario-02/fail.png` |

### SAFE-COM-003: 버스 정류장 안전선 지키기

| field | value |
|---|---|
| category | `COMMUTE_SAFETY` |
| title | 버스 정류장 안전선 지키기 |
| description | 버스가 도착할 때 차도로 내려가지 않고 안전선 안쪽에서 기다리는 훈련 |
| assetPath | `/trainings/safety/commute-safety/scenario-03` |

| scene | screen_info | situation_text | question_text | image_url |
|---|---|---|---|---|
| story-01 | 버스 정류장에 도착한 장면 | 횡단보도를 건너 버스 정류장에 도착했습니다. 사람들이 버스를 기다리고 있습니다. |  | `/trainings/safety/commute-safety/scenario-03/story-01.png` |
| story-02 | 버스가 멀리 보이는 장면 | 내가 탈 버스가 멀리서 보입니다. 몇몇 사람이 앞으로 움직이기 시작합니다. |  | `/trainings/safety/commute-safety/scenario-03/story-02.png` |
| story-03 | 사람들이 차도 쪽으로 몰리는 장면 | 사람들이 먼저 타려고 차도 가까이로 나갑니다. 나도 따라가야 할지 고민됩니다. |  | `/trainings/safety/commute-safety/scenario-03/story-03.png` |
| story-04 | 버스 정류장에서 사람들이 차도 쪽으로 몰리는 장면 | 버스 정류장에 내가 탈 버스가 보입니다. 사람들이 먼저 타려고 차도 쪽으로 우르르 몰려나갑니다. | 이럴 때 어떻게 해야 할까요? | `/trainings/safety/commute-safety/scenario-03/story-04.png` |

| order | choice_text | is_correct | result_text | effect_text | feedback_image_url |
|---:|---|---|---|---|---|
| 1 | 나도 차도로 내려가서 기다린다. | false | X! 차도로 내려가는 건 아주 위험해요. 안전선은 생명선입니다. | 버스 바퀴와 너무 가까워집니다. | `/trainings/safety/commute-safety/scenario-03/fail.png` |
| 2 | 노란색 안전선 안쪽에서 버스가 정차할 때까지 기다린다. | true | 잘했어요. 버스가 완전히 멈춘 뒤 질서 있게 타는 것이 안전합니다. | 안전선 뒤에서 기다렸다가 차례대로 탑승합니다. | `/trainings/safety/commute-safety/scenario-03/correct.png` |
| 3 | 버스 문이 열리기 전에 차 쪽으로 뛰어간다. | false | X! 버스가 완전히 멈추기 전에 가까이 가면 크게 다칠 수 있어요. | 버스와 너무 가까워져 위험합니다. | `/trainings/safety/commute-safety/scenario-03/fail.png` |

### SAFE-COM-004: 버스 안에서 손잡이 잡기

| field | value |
|---|---|
| category | `COMMUTE_SAFETY` |
| title | 버스 안에서 손잡이 잡기 |
| description | 버스 안에서 자리가 없을 때 손잡이를 잡고 몸의 균형을 지키는 훈련 |
| assetPath | `/trainings/safety/commute-safety/scenario-04` |

| scene | screen_info | situation_text | question_text | image_url |
|---|---|---|---|---|
| story-01 | 버스에 탑승한 장면 | 버스에 탔지만 빈자리가 없습니다. 나는 서서 가야 합니다. |  | `/trainings/safety/commute-safety/scenario-04/story-01.png` |
| story-02 | 버스가 출발하는 장면 | 버스가 출발하자 몸이 조금 흔들립니다. 주변에는 손잡이와 봉이 있습니다. |  | `/trainings/safety/commute-safety/scenario-04/story-02.png` |
| story-03 | 휴대폰을 보고 싶은 장면 | 휴대폰 알림이 와서 보고 싶지만, 버스가 움직이고 있어 균형을 잡아야 합니다. |  | `/trainings/safety/commute-safety/scenario-04/story-03.png` |
| story-04 | 버스 안에서 갑자기 크게 흔들리는 장면 | 버스 안에 자리가 없어 서서 가는데, 갑자기 버스가 크게 흔들립니다. | 이럴 때 어떻게 해야 할까요? | `/trainings/safety/commute-safety/scenario-04/story-04.png` |

| order | choice_text | is_correct | result_text | effect_text | feedback_image_url |
|---:|---|---|---|---|---|
| 1 | 두 손으로 손잡이를 꼭 잡는다. | true | 잘했어요. 버스 안에서는 손잡이를 잡아 몸의 균형을 지키는 것이 안전합니다. | 무사히 회사 앞에 도착합니다. | `/trainings/safety/commute-safety/scenario-04/correct.png` |
| 2 | 중심을 잡으며 휴대폰을 본다. | false | X! 버스 안에서는 휴대폰보다 손잡이가 먼저예요. 급정거하면 크게 다칠 수 있어요. | 몸이 앞으로 쏠려 넘어질 위험이 생깁니다. | `/trainings/safety/commute-safety/scenario-04/fail.png` |
| 3 | 문에 몸을 기대어 서 있는다. | false | X! 문에 기대어 있으면 급정거하거나 문이 열릴 때 위험할 수 있어요. | 몸이 흔들리고 문 쪽으로 기울어집니다. | `/trainings/safety/commute-safety/scenario-04/fail.png` |
