UPDATE social_scenarios SET seed_code = 'SOC-OFF-AMB-001', category_code = 'AMBIGUOUS_INSTRUCTION', situation_order = 1, evaluation_point = '구체적인 숫자, 예를 들어 10장 또는 20장을 언급하며 수량을 확인했는가?', example_answer = '사수님, 회의 인원이 몇 명인지 몰라서요. 몇 부 정도 복사하면 될까요?' WHERE scenario_id = 1;
UPDATE social_scenarios SET seed_code = 'SOC-OFF-AMB-002', category_code = 'AMBIGUOUS_INSTRUCTION', situation_order = 2, evaluation_point = '상대방의 정확한 성함과 다시 연락드릴 번호를 정중하게 요청했는가?', example_answer = '죄송하지만 팀장님께 정확히 전달드릴 수 있도록 성함과 연락처를 한 번만 알려주실 수 있을까요?' WHERE scenario_id = 2;
UPDATE social_scenarios SET seed_code = 'SOC-OFF-AMB-003', category_code = 'AMBIGUOUS_INSTRUCTION', situation_order = 3, evaluation_point = '음료의 종류나 대략적인 구매 개수를 구체적으로 되물었는가?', example_answer = '대리님, 커피나 주스처럼 어떤 종류를 몇 개 정도 사면 좋을까요?' WHERE scenario_id = 3;
UPDATE social_scenarios SET seed_code = 'SOC-OFF-MIS-001', category_code = 'MISTAKE_REPORT', situation_order = 1, evaluation_point = '"죄송합니다"라고 즉시 사과하고, 어떤 서류를 파쇄했는지 사실대로 말했는가?', example_answer = '죄송합니다. 파쇄기 옆에 있던 영수증 원본을 버리는 서류로 착각해서 파쇄했습니다.' WHERE scenario_id = 4;
UPDATE social_scenarios SET seed_code = 'SOC-OFF-MIS-002', category_code = 'MISTAKE_REPORT', situation_order = 2, evaluation_point = '자신의 실수를 인정하고, 즉시 빈 회의실을 찾아보겠다는 대안을 제시했는가?', example_answer = '팀장님, 제가 회의실 예약을 깜빡했습니다. 죄송합니다. 바로 사용 가능한 빈 회의실을 찾아보겠습니다.' WHERE scenario_id = 5;
UPDATE social_scenarios SET seed_code = 'SOC-OFF-MIS-003', category_code = 'MISTAKE_REPORT', situation_order = 3, evaluation_point = '지각 사유와 예상 도착 시간을 명확히 전달했는가?', example_answer = '사수님, 지하철 고장으로 20분 이상 지연될 예정입니다. 도착 예상 시간은 확인되는 대로 다시 말씀드리겠습니다.' WHERE scenario_id = 6;
UPDATE social_scenarios SET seed_code = 'SOC-OFF-REF-001', category_code = 'REFUSAL', situation_order = 1, evaluation_point = '현재 내가 해야 할 업무가 있음을 밝히고, 개인적인 부탁은 어렵다고 정중히 거절했는가?', example_answer = '죄송하지만 지금 제가 맡은 업무가 있어서 바로 도와드리기는 어렵습니다.' WHERE scenario_id = 7;
UPDATE social_scenarios SET seed_code = 'SOC-OFF-REF-002', category_code = 'REFUSAL', situation_order = 2, evaluation_point = '"그건 개인적인 일이라 말씀드리기 곤란합니다"처럼 정중하지만 명확하게 거절했는가?', example_answer = '그건 개인적인 일이라 말씀드리기 곤란합니다.' WHERE scenario_id = 8;
UPDATE social_scenarios SET seed_code = 'SOC-OFF-REF-003', category_code = 'REFUSAL', situation_order = 3, evaluation_point = '규칙대로 기계를 멈추고 하겠다고 하거나, 다칠 수 있어 위험하다고 안전 원칙을 고수했는가?', example_answer = '그렇게 하면 다칠 수 있어서 위험해요. 규칙대로 기계를 멈추고 하겠습니다.' WHERE scenario_id = 9;
UPDATE social_scenarios SET seed_code = 'SOC-LAB-FBK-001', category_code = 'FEEDBACK_ACCEPTANCE', situation_order = 1, evaluation_point = '변명하기보다 지적된 곳을 확인하고 "즉시 다시 청소하겠습니다"라고 답했는가?', example_answer = '네, 확인했습니다. 바로 다시 청소하겠습니다.' WHERE scenario_id = 10;
UPDATE social_scenarios SET seed_code = 'SOC-LAB-FBK-002', category_code = 'FEEDBACK_ACCEPTANCE', situation_order = 2, evaluation_point = '지적을 수용하고 "조금 더 속도를 내보겠습니다"라고 의지를 표현했는가?', example_answer = '네, 알겠습니다. 조금 더 속도를 내보겠습니다.' WHERE scenario_id = 11;
UPDATE social_scenarios SET seed_code = 'SOC-LAB-FBK-003', category_code = 'FEEDBACK_ACCEPTANCE', situation_order = 3, evaluation_point = '규칙 위반을 인정하고 즉시 안전모를 착용하며 알겠다고 답했는가?', example_answer = '죄송합니다. 바로 안전모를 착용하겠습니다.' WHERE scenario_id = 12;
UPDATE social_scenarios SET seed_code = 'SOC-LAB-HELP-001', category_code = 'HELP_REQUEST', situation_order = 1, evaluation_point = '기계의 이상 증상, 즉 소음과 멈춤을 설명하고 도움을 요청했는가?', example_answer = '관리자님, 세탁기에서 텅텅거리는 소리가 나더니 갑자기 멈췄습니다. 확인을 도와주실 수 있을까요?' WHERE scenario_id = 13;
UPDATE social_scenarios SET seed_code = 'SOC-LAB-HELP-002', category_code = 'HELP_REQUEST', situation_order = 2, evaluation_point = '"박스가 너무 무거워서 그런데 같이 들어주실 수 있나요?"라고 구체적으로 요청했는가?', example_answer = '박스가 너무 무거워서 그런데 같이 들어주실 수 있나요?' WHERE scenario_id = 14;
UPDATE social_scenarios SET seed_code = 'SOC-LAB-HELP-003', category_code = 'HELP_REQUEST', situation_order = 3, evaluation_point = '기억이 나지 않음을 솔직히 말하고 다시 한번 알려달라고 재교육을 요청했는가?', example_answer = '죄송합니다. 어제 배운 작업 순서가 정확히 기억나지 않습니다. 다시 한번만 알려주실 수 있을까요?' WHERE scenario_id = 15;
UPDATE social_scenarios SET seed_code = 'SOC-LAB-HEALTH-001', category_code = 'HEALTH_REPORT', situation_order = 1, evaluation_point = '화장실에 다녀와야 하는 상황임을 알리고 자리를 잠시 비워도 되는지 허락을 구했는가?', example_answer = '관리자님, 배가 너무 아파서 화장실에 잠시 다녀와야 할 것 같습니다. 잠깐 자리를 비워도 될까요?' WHERE scenario_id = 16;
UPDATE social_scenarios SET seed_code = 'SOC-LAB-HEALTH-002', category_code = 'HEALTH_REPORT', situation_order = 2, evaluation_point = '어지러운 신체 상태를 설명하고 "10분만 쉬어도 될까요?"처럼 구체적인 시간을 요청했는가?', example_answer = '반장님, 갑자기 어지럽고 식은땀이 납니다. 10분만 쉬어도 될까요?' WHERE scenario_id = 17;
UPDATE social_scenarios SET seed_code = 'SOC-LAB-HEALTH-003', category_code = 'HEALTH_REPORT', situation_order = 3, evaluation_point = '부상 사실을 즉시 알리고 치료가 필요함을 요청했는가?', example_answer = '손가락을 깊게 베어서 피가 많이 납니다. 보건실에 가서 치료를 받아야 할 것 같습니다.' WHERE scenario_id = 18;

UPDATE safety_scenarios SET seed_code = 'SAFE-SEX-001' WHERE scenario_id = 1;
UPDATE safety_scenarios SET seed_code = 'SAFE-INF-001' WHERE scenario_id = 2;
UPDATE safety_scenarios SET seed_code = 'SAFE-COM-001' WHERE scenario_id = 3;

UPDATE safety_scenes SET seed_code = 'SAFE-SEX-001-S01' WHERE scene_id = 1;
UPDATE safety_scenes SET seed_code = 'SAFE-SEX-001-S02' WHERE scene_id = 2;
UPDATE safety_scenes SET seed_code = 'SAFE-SEX-001-S03' WHERE scene_id = 3;
UPDATE safety_scenes SET seed_code = 'SAFE-SEX-001-S04' WHERE scene_id = 4;
UPDATE safety_scenes SET seed_code = 'SAFE-INF-001-S01' WHERE scene_id = 5;
UPDATE safety_scenes SET seed_code = 'SAFE-INF-001-S02' WHERE scene_id = 6;
UPDATE safety_scenes SET seed_code = 'SAFE-INF-001-S03' WHERE scene_id = 7;
UPDATE safety_scenes SET seed_code = 'SAFE-COM-001-S01' WHERE scene_id = 8;
UPDATE safety_scenes SET seed_code = 'SAFE-COM-001-S02' WHERE scene_id = 9;
UPDATE safety_scenes SET seed_code = 'SAFE-COM-001-S03' WHERE scene_id = 10;
UPDATE safety_scenes SET seed_code = 'SAFE-COM-001-S04' WHERE scene_id = 11;

UPDATE safety_choices SET seed_code = 'SAFE-SEX-001-S01-C01', choice_order = 1, result_text = 'X! 참거나 똑같이 장난치면 상대방은 동의한 것으로 오해해요. 불쾌할 땐 단호히 말해야 해요!', effect_text = '배경이 어두워지며 사이렌 소리가 들립니다.' WHERE choice_id = 1;
UPDATE safety_choices SET seed_code = 'SAFE-SEX-001-S01-C02', choice_order = 2, result_text = '사수가 당황하며 손을 뗍니다. "미안해요, 내가 실수했네."', effect_text = '사수가 물러서고 다음 상황으로 이동합니다.' WHERE choice_id = 2;
UPDATE safety_choices SET seed_code = 'SAFE-SEX-001-S01-C03', choice_order = 3, result_text = 'X! 원하지 않는 접촉은 장난으로 넘기지 말고 분명히 말해야 해요.', effect_text = '상대방이 장난을 계속하는 연출이 나옵니다.' WHERE choice_id = 3;
UPDATE safety_choices SET seed_code = 'SAFE-SEX-001-S02-C01', choice_order = 1, result_text = 'X! 사생활 질문에 답할 의무는 없어요!', effect_text = '동료가 더 무례한 질문을 이어갑니다.' WHERE choice_id = 4;
UPDATE safety_choices SET seed_code = 'SAFE-SEX-001-S02-C02', choice_order = 2, result_text = '동료가 머쓱해하며 자리를 뜹니다.', effect_text = '동료가 물러서고 다음 상황으로 이동합니다.' WHERE choice_id = 5;
UPDATE safety_choices SET seed_code = 'SAFE-SEX-001-S02-C03', choice_order = 3, result_text = 'X! 사생활 질문을 통할 의무는 없어요.', effect_text = '동료가 무례한 질문을 이어갑니다.' WHERE choice_id = 6;
UPDATE safety_choices SET seed_code = 'SAFE-SEX-001-S03-C01', choice_order = 1, result_text = 'X! 내 사진은 소중한 개인정보예요. 함부로 보내면 나쁜 일에 쓰일 수 있어요!', effect_text = '내 사진이 인터넷에 떠도는 아찔한 상상 연출이 나옵니다.' WHERE choice_id = 7;
UPDATE safety_choices SET seed_code = 'SAFE-SEX-001-S03-C02', choice_order = 2, result_text = '상대방이 당황하며 "농담이었어."라고 답장을 보냅니다.', effect_text = '상대방이 물러서고 다음 상황으로 이동합니다.' WHERE choice_id = 8;
UPDATE safety_choices SET seed_code = 'SAFE-SEX-001-S03-C03', choice_order = 3, result_text = 'X! 내 사진은 소중한 개인정보예요.', effect_text = '사진 유출 위험을 보여주는 연출이 나옵니다.' WHERE choice_id = 9;
UPDATE safety_choices SET seed_code = 'SAFE-SEX-001-S04-C01', choice_order = 1, result_text = 'X! 주변 사람이 믿어주지 않거나 참으라고 하는 것은 2차 피해예요.', effect_text = '위축되고 어두운 분위기의 연출이 나옵니다.' WHERE choice_id = 10;
UPDATE safety_choices SET seed_code = 'SAFE-SEX-001-S04-C02', choice_order = 2, result_text = '나의 경험을 당당하게 말하는 것은 안전을 지키는 일입니다. 정말 잘했어요!', effect_text = '성공 엔딩 연출이 나옵니다.' WHERE choice_id = 11;
UPDATE safety_choices SET seed_code = 'SAFE-SEX-001-S04-C03', choice_order = 3, result_text = 'X! 화를 내기보다 내 경험을 차분히 말하고 도움을 요청하세요.', effect_text = '갈등이 커지는 연출이 나옵니다.' WHERE choice_id = 12;
UPDATE safety_choices SET seed_code = 'SAFE-INF-001-S01-C01', choice_order = 1, result_text = 'X! 비누가 없으면 세균이 죽지 않아요!', effect_text = '손바닥에 세균이 가득한 연출이 나옵니다.' WHERE choice_id = 13;
UPDATE safety_choices SET seed_code = 'SAFE-INF-001-S01-C02', choice_order = 2, result_text = '손이 깨끗해졌습니다.', effect_text = '손이 반짝이며 다음 상황으로 이동합니다.' WHERE choice_id = 14;
UPDATE safety_choices SET seed_code = 'SAFE-INF-001-S01-C03', choice_order = 3, result_text = 'X! 비누로 충분히 씻어야 세균을 줄일 수 있어요.', effect_text = '세균이 남아 있는 연출이 나옵니다.' WHERE choice_id = 15;
UPDATE safety_choices SET seed_code = 'SAFE-INF-001-S02-C01', choice_order = 1, result_text = 'X! 손으로 가리면 세균이 손에 묻어 다른 사람에게 옮길 수 있어요.', effect_text = '회의 테이블 위로 세균이 튀는 연출이 나옵니다.' WHERE choice_id = 16;
UPDATE safety_choices SET seed_code = 'SAFE-INF-001-S02-C02', choice_order = 2, result_text = '세균이 퍼지는 것을 막았습니다.', effect_text = '올바른 기침 예절 후 다음 상황으로 이동합니다.' WHERE choice_id = 17;
UPDATE safety_choices SET seed_code = 'SAFE-INF-001-S02-C03', choice_order = 3, result_text = 'X! 공중 기침은 주변 사람에게 위험할 수 있어요.', effect_text = '세균이 퍼지는 연출이 나옵니다.' WHERE choice_id = 18;
UPDATE safety_choices SET seed_code = 'SAFE-INF-001-S03-C01', choice_order = 1, result_text = 'X! 아픈 것을 참고 일하면 내 몸과 동료 모두 위험할 수 있어요.', effect_text = '주변 동료들도 기침을 하기 시작하는 연출이 나옵니다.' WHERE choice_id = 19;
UPDATE safety_choices SET seed_code = 'SAFE-INF-001-S03-C02', choice_order = 2, result_text = '관리자가 쉬어도 된다고 안내합니다.', effect_text = '성공 엔딩 연출이 나옵니다.' WHERE choice_id = 20;
UPDATE safety_choices SET seed_code = 'SAFE-INF-001-S03-C03', choice_order = 3, result_text = 'X! 숨지 말고 몸 상태를 관리자에게 알려야 해요.', effect_text = '도움이 늦어지는 연출이 나옵니다.' WHERE choice_id = 21;
UPDATE safety_choices SET seed_code = 'SAFE-COM-001-S01-C01', choice_order = 1, result_text = 'X! 스마트폰에 눈이 팔리면 차나 킥보드를 못 봐서 크게 다칠 수 있어요!', effect_text = '전동 킥보드 경적 소리가 납니다.' WHERE choice_id = 22;
UPDATE safety_choices SET seed_code = 'SAFE-COM-001-S01-C02', choice_order = 2, result_text = '앞을 보며 안전하게 걸었습니다.', effect_text = '안전하게 걷고 다음 상황으로 이동합니다.' WHERE choice_id = 23;
UPDATE safety_choices SET seed_code = 'SAFE-COM-001-S01-C03', choice_order = 3, result_text = 'X! 주변 소리를 듣지 못하면 위험을 늦게 알아차릴 수 있어요.', effect_text = '위험 경고음이 작게 들리는 연출이 나옵니다.' WHERE choice_id = 24;
UPDATE safety_choices SET seed_code = 'SAFE-COM-001-S02-C01', choice_order = 1, result_text = 'X! 깜빡이는 신호에 뛰는 건 위험해요. 늦더라도 안전이 먼저예요!', effect_text = '차들이 위태롭게 멈추는 연출이 나옵니다.' WHERE choice_id = 25;
UPDATE safety_choices SET seed_code = 'SAFE-COM-001-S02-C02', choice_order = 2, result_text = '차분하게 다음 신호를 기다렸습니다.', effect_text = '안전하게 길을 건너 다음 상황으로 이동합니다.' WHERE choice_id = 26;
UPDATE safety_choices SET seed_code = 'SAFE-COM-001-S02-C03', choice_order = 3, result_text = 'X! 무단횡단은 매우 위험합니다.', effect_text = '차가 급히 멈추는 연출이 나옵니다.' WHERE choice_id = 27;
UPDATE safety_choices SET seed_code = 'SAFE-COM-001-S03-C01', choice_order = 1, result_text = 'X! 차도로 내려가는 건 아주 위험해요. 안전선은 생명선입니다!', effect_text = '버스 바퀴가 발 앞까지 오는 연출이 나옵니다.' WHERE choice_id = 28;
UPDATE safety_choices SET seed_code = 'SAFE-COM-001-S03-C02', choice_order = 2, result_text = '버스가 완전히 멈춘 뒤 질서 있게 탑승했습니다.', effect_text = '안전하게 버스에 탑승하고 다음 상황으로 이동합니다.' WHERE choice_id = 29;
UPDATE safety_choices SET seed_code = 'SAFE-COM-001-S03-C03', choice_order = 3, result_text = 'X! 버스 문이 열리기 전 차 쪽으로 뛰어가면 위험해요.', effect_text = '버스와 가까워지는 위험 연출이 나옵니다.' WHERE choice_id = 30;
UPDATE safety_choices SET seed_code = 'SAFE-COM-001-S04-C01', choice_order = 1, result_text = '무사히 회사 앞에 도착해 출근 카드를 찍었습니다.', effect_text = '성공 엔딩 연출이 나옵니다.' WHERE choice_id = 31;
UPDATE safety_choices SET seed_code = 'SAFE-COM-001-S04-C02', choice_order = 2, result_text = 'X! 버스 안에서는 손잡이를 잡는 것이 먼저예요.', effect_text = '몸이 앞으로 쏠려 넘어지는 연출이 나옵니다.' WHERE choice_id = 32;
UPDATE safety_choices SET seed_code = 'SAFE-COM-001-S04-C03', choice_order = 3, result_text = 'X! 문에 기대어 서 있으면 급정거할 때 크게 다칠 수 있어요.', effect_text = '몸이 흔들리는 연출이 나옵니다.' WHERE choice_id = 33;

INSERT INTO document_questions
    (question_id, seed_code, title, document_text, question_text, question_type, correct_answer, explanation, difficulty, correct_feedback, wrong_feedback, is_active)
VALUES
    (26, 'DOC-MC-L1-001', '신입사원 워크숍 안내', '이번 신입사원 워크숍은 5월 10일 강원도 속초에서 진행됩니다. 모든 참가자는 당일 오전 8시까지 회사 정문 앞 대형 버스에 탑승해야 합니다. 개인 세면도구와 운동화는 개별적으로 준비하시기 바랍니다. 숙소 내에서 취사는 금지되어 있으며, 저녁 식사는 인근 식당에서 단체로 진행될 예정입니다.', '위 글의 내용과 일치하지 않는 것은 무엇인가요?', 'MULTIPLE_CHOICE', '숙소 안에서 직접 음식을 만들어 먹을 수 있다.', '숙소 내에서 취사는 금지되어 있으므로, 직접 음식을 만들어 먹을 수 있다는 내용은 글과 일치하지 않습니다.', 'LEVEL_1', '정답입니다. 세부 사항을 꼼꼼하게 읽었습니다.', '글의 마지막 문장에서 취사 금지 내용을 다시 확인해야 합니다.', TRUE),
    (27, 'DOC-MC-L2-001', '사내 복사기 사용 및 고장 대처법', '복사기를 사용하기 전에는 먼저 종이가 충분한지 확인해야 합니다. 만약 종이가 걸렸을 경우에는 강제로 잡아당기지 마십시오. 먼저 앞 덮개를 열고 걸린 종이를 천천히 제거한 뒤, 다시 덮개를 닫고 ''재시작'' 버튼을 누르세요. 만약 이 방법으로도 해결되지 않는다면, 즉시 관리부(내선 102번)로 연락하여 수리를 요청해야 합니다. 직접 기계를 분해하는 행동은 위험하므로 절대 금합니다.', '종이가 걸렸을 때 가장 먼저 해야 할 행동은 무엇인가요?', 'MULTIPLE_CHOICE', '복사기 앞 덮개 열기', '종이가 걸렸을 경우 먼저 앞 덮개를 열고 걸린 종이를 천천히 제거해야 합니다.', 'LEVEL_2', '업무 매뉴얼의 처리 순서를 정확히 파악했습니다.', '문제 해결 순서를 찾기 위해 먼저 해야 하는 행동을 확인해야 합니다.', TRUE),
    (28, 'DOC-MC-L3-001', '비품 절약 및 관리 협조 요청', '임직원 여러분, 최근 사무용품 소모량이 예년에 비해 30% 이상 증가했습니다. 특히 종이컵과 A4 용지의 무분별한 사용이 예산 낭비의 주요 원인이 되고 있습니다. 따라서 오늘부터 회의 시 개인 컵 사용을 권장하며, 이면지 활용을 생활화해주시기 바랍니다. 작은 실천이 모여 우리 회사의 환경을 보호하고 예산을 절감할 수 있습니다. 여러분의 적극적인 협조 부탁드립니다.', '이 메일을 보낸 가장 큰 목적은 무엇인가요?', 'MULTIPLE_CHOICE', '사무용품을 아껴 쓰자고 제안하기 위해', '글쓴이는 사무용품 사용 증가로 인한 예산 낭비를 줄이기 위해 개인 컵 사용과 이면지 활용을 제안하고 있습니다.', 'LEVEL_3', '글의 핵심 목적을 정확히 파악했습니다.', '글쓴이가 왜 이 글을 보냈는지 결론 문장을 다시 확인해야 합니다.', TRUE)
ON DUPLICATE KEY UPDATE
    seed_code = VALUES(seed_code),
    title = VALUES(title),
    document_text = VALUES(document_text),
    question_text = VALUES(question_text),
    question_type = VALUES(question_type),
    correct_answer = VALUES(correct_answer),
    explanation = VALUES(explanation),
    difficulty = VALUES(difficulty),
    correct_feedback = VALUES(correct_feedback),
    wrong_feedback = VALUES(wrong_feedback),
    is_active = VALUES(is_active);

INSERT INTO document_question_choices
    (choice_id, seed_code, question_id, choice_order, choice_text, is_correct)
VALUES
    (1, 'DOC-MC-L1-001-C01', 26, 1, '워크숍 장소는 강원도 속초이다.', FALSE),
    (2, 'DOC-MC-L1-001-C02', 26, 2, '오전 8시까지 버스에 타야 한다.', FALSE),
    (3, 'DOC-MC-L1-001-C03', 26, 3, '숙소 안에서 직접 음식을 만들어 먹을 수 있다.', TRUE),
    (4, 'DOC-MC-L2-001-C01', 27, 1, '관리부에 전화하기', FALSE),
    (5, 'DOC-MC-L2-001-C02', 27, 2, '복사기 앞 덮개 열기', TRUE),
    (6, 'DOC-MC-L2-001-C03', 27, 3, '걸린 종이 강제로 잡아당기기', FALSE),
    (7, 'DOC-MC-L3-001-C01', 28, 1, '종이컵의 종류를 알려주기 위해', FALSE),
    (8, 'DOC-MC-L3-001-C02', 28, 2, '사무용품을 아껴 쓰자고 제안하기 위해', TRUE),
    (9, 'DOC-MC-L3-001-C03', 28, 3, '새로운 회의 장소를 안내하기 위해', FALSE)
ON DUPLICATE KEY UPDATE
    seed_code = VALUES(seed_code),
    question_id = VALUES(question_id),
    choice_order = VALUES(choice_order),
    choice_text = VALUES(choice_text),
    is_correct = VALUES(is_correct);
