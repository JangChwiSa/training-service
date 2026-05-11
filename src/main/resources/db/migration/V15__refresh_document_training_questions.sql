UPDATE document_questions
SET is_active = false
WHERE seed_code IS NULL
   OR seed_code LIKE 'DOC-MC-L%';

INSERT INTO document_questions (
    seed_code, title, document_text, question_text, question_type, correct_answer, explanation,
    difficulty, is_active, correct_feedback, wrong_feedback
)
VALUES
    ('DOC-HQ-L1-001', '회의실 예약 안내 확인', '오늘 오후 2시부터 4시까지 회의실 B는 고객사 미팅으로 사용합니다. 같은 시간에 회의가 필요한 팀은 회의실 A 또는 C를 예약해 주세요. 회의실 B 앞에는 예약 안내문을 붙여 두었습니다.', '오후 2시부터 4시까지 사용할 수 없는 회의실은 어디인가요?', 'MULTIPLE_CHOICE', '회의실 B', '첫 문장에 회의실 B가 고객사 미팅으로 사용된다고 적혀 있습니다.', 'LEVEL_1', true, '시간과 장소 정보를 정확히 찾았습니다.', '첫 문장에서 시간과 회의실 이름을 다시 확인해 보세요.'),
    ('DOC-HQ-L1-002', '방문자 명단 확인 메모', '지우님, 출근하면 먼저 안내 데스크에 놓인 방문자 명단을 확인해 주세요. 명단에 빠진 이름이 있으면 바로 김대리에게 알려 주세요. 방문증은 명단 확인이 끝난 뒤 전달하면 됩니다.', '명단에 빠진 이름이 있으면 누구에게 알려야 하나요?', 'MULTIPLE_CHOICE', '김대리', '두 번째 문장에 빠진 이름이 있으면 김대리에게 알리라고 되어 있습니다.', 'LEVEL_1', true, '필요한 사람 정보를 잘 찾았습니다.', '빠진 이름이 있을 때의 행동을 안내한 문장을 다시 읽어 보세요.'),
    ('DOC-HQ-L1-003', '택배 보관 위치 안내', '오늘 도착한 일반 택배는 총무팀 보관함에 넣어 주세요. 냉장 표시가 있는 택배만 탕비실 냉장고에 넣습니다. 수령 확인 스티커는 택배 상자 오른쪽 위에 붙이면 됩니다.', '냉장 표시가 있는 택배는 어디에 넣어야 하나요?', 'MULTIPLE_CHOICE', '탕비실 냉장고', '냉장 표시가 있는 택배만 탕비실 냉장고에 넣으라고 안내합니다.', 'LEVEL_1', true, '조건에 맞는 보관 장소를 정확히 골랐습니다.', '일반 택배와 냉장 표시 택배의 보관 장소가 다릅니다.'),
    ('DOC-HQ-L1-004', '개인 컵 정리 안내', '금요일 오후 5시 전까지 개인 컵은 이름표가 붙은 선반에 올려 주세요. 이름표가 없는 컵은 공용 컵 바구니에 넣습니다. 세척이 필요한 컵은 싱크대 오른쪽에 모아 주세요.', '이름표가 붙은 개인 컵은 어디에 올려야 하나요?', 'MULTIPLE_CHOICE', '이름표가 붙은 선반', '첫 문장에 개인 컵을 이름표가 붙은 선반에 올리라고 적혀 있습니다.', 'LEVEL_1', true, '물건과 위치 정보를 정확히 연결했습니다.', '이름표가 있는 컵과 없는 컵의 위치를 구분해 보세요.'),
    ('DOC-HQ-L1-005', '교육장 위치 안내', '신규 안전 교육은 월요일 오전 9시에 시작합니다. 교육 장소는 3층 세미나실입니다. 교육 시작 10분 전까지 도착해 출석표에 서명해 주세요.', '신규 안전 교육 장소는 어디인가요?', 'MULTIPLE_CHOICE', '3층 세미나실', '두 번째 문장에 교육 장소가 3층 세미나실이라고 안내되어 있습니다.', 'LEVEL_1', true, '장소 정보를 정확히 확인했습니다.', '교육 시간 다음에 나오는 장소 안내를 다시 확인해 보세요.'),

    ('DOC-HQ-L2-001', '복사기 용지 보충 순서', '복사기 용지가 부족하면 먼저 아래 칸에서 A4 용지를 꺼냅니다. 용지를 넣은 뒤에는 덮개를 닫고 확인 버튼을 누릅니다. 화면에 오류가 계속 표시되면 관리부에 연락해 주세요.', 'A4 용지를 넣은 뒤 바로 해야 할 일은 무엇인가요?', 'MULTIPLE_CHOICE', '덮개를 닫고 확인 버튼을 누른다', '문서에는 용지를 넣은 뒤 덮개를 닫고 확인 버튼을 누르라고 되어 있습니다.', 'LEVEL_2', true, '업무 순서를 정확히 이해했습니다.', '용지를 넣은 뒤 다음 행동을 순서대로 찾아보세요.'),
    ('DOC-HQ-L2-002', '회의 자료 준비 지시', '회의 자료는 흑백으로 12부 인쇄합니다. 인쇄가 끝나면 왼쪽 위를 스테이플러로 묶고 회의실 A 앞 테이블에 둡니다. 컬러 인쇄는 팀장 요청이 있을 때만 사용합니다.', '회의 자료를 인쇄한 뒤 어디에 두어야 하나요?', 'MULTIPLE_CHOICE', '회의실 A 앞 테이블', '인쇄 후 스테이플러로 묶고 회의실 A 앞 테이블에 두라고 안내합니다.', 'LEVEL_2', true, '처리 후 배치 장소를 잘 파악했습니다.', '인쇄 후 해야 할 일을 순서대로 따라가 보세요.'),
    ('DOC-HQ-L2-003', '사무용품 신청 안내', '이번 달 사무용품 신청은 목요일 오후 3시에 마감됩니다. 필요한 물품은 신청서에 수량을 적어 총무팀에 제출해 주세요. 마감 이후 신청한 물품은 다음 달에 지급됩니다.', '마감 이후 신청한 물품은 언제 지급되나요?', 'MULTIPLE_CHOICE', '다음 달', '마감 이후 신청분은 다음 달에 지급된다고 마지막 문장에 적혀 있습니다.', 'LEVEL_2', true, '마감 조건에 따른 결과를 정확히 골랐습니다.', '마감 이후에는 처리 시점이 달라진다는 점을 확인해 보세요.'),
    ('DOC-HQ-L2-004', '파일명 변경 요청', '공유 폴더에 올린 견적서 파일명을 "거래처명_견적서_날짜" 형식으로 바꿔 주세요. 파일 내용은 수정하지 않아도 됩니다. 변경 후에는 담당자에게 완료 메시지를 보내 주세요.', '견적서 파일에서 수정하지 않아도 되는 것은 무엇인가요?', 'MULTIPLE_CHOICE', '파일 내용', '문서에는 파일 내용은 수정하지 않아도 된다고 되어 있습니다.', 'LEVEL_2', true, '해야 할 일과 하지 않아도 되는 일을 구분했습니다.', '파일명 변경과 파일 내용 수정은 서로 다른 작업입니다.'),
    ('DOC-HQ-L2-005', '교육 후 정리 안내', '오후 교육이 끝나면 회의실 의자는 뒤쪽 벽으로 붙여 주세요. 사용한 네임펜은 강사 책상 위 상자에 모읍니다. 빔프로젝터 전원은 강사가 직접 끌 예정입니다.', '사용한 네임펜은 어디에 모아야 하나요?', 'MULTIPLE_CHOICE', '강사 책상 위 상자', '사용한 네임펜은 강사 책상 위 상자에 모으라고 안내되어 있습니다.', 'LEVEL_2', true, '물품 정리 위치를 정확히 확인했습니다.', '의자 정리와 네임펜 정리 지시를 구분해 보세요.'),

    ('DOC-HQ-L3-001', '비품 절약 협조 메일', '최근 사무용품 소모량이 예년에 비해 30% 이상 증가했습니다. 특히 종이컵과 A4 용지 사용이 예산 낭비의 주요 원인입니다. 오늘부터 회의 시 개인 컵 사용을 권장하고, 출력 전에는 꼭 필요한 문서인지 확인해 주세요.', '이 글의 가장 큰 목적은 무엇인가요?', 'MULTIPLE_CHOICE', '사무용품을 아껴 쓰자고 요청하기 위해', '글은 소모량 증가 문제를 설명하고 개인 컵 사용과 출력 절약을 요청하고 있습니다.', 'LEVEL_3', true, '글의 목적을 정확히 파악했습니다.', '문서가 어떤 행동 변화를 요청하는지 마지막 문장을 중심으로 보세요.'),
    ('DOC-HQ-L3-002', '개인정보 문서 처리 안내', '주민등록번호나 연락처가 적힌 문서는 일반 쓰레기통에 버리지 않습니다. 필요한 내용 확인이 끝나면 문서 파쇄함에 넣어 주세요. 파쇄함이 가득 찼다면 총무팀에 비움 요청을 해야 합니다.', '이 안내에서 가장 중요하게 지키려는 것은 무엇인가요?', 'MULTIPLE_CHOICE', '개인정보가 밖으로 새어 나가지 않게 하는 것', '개인정보가 적힌 문서를 일반 쓰레기통에 버리지 말고 파쇄함에 넣으라는 내용입니다.', 'LEVEL_3', true, '문서의 보호 목적을 잘 추론했습니다.', '주민등록번호와 연락처가 왜 특별히 언급됐는지 생각해 보세요.'),
    ('DOC-HQ-L3-003', '엘리베이터 점검 안내', '오늘 오후 1시부터 3시까지 2호기 엘리베이터를 점검합니다. 점검 중에는 1호기 엘리베이터나 계단을 이용해 주세요. 휠체어 이용자는 안내 데스크에 도움을 요청할 수 있습니다.', '점검 시간에 2호기 엘리베이터를 이용하려는 사람에게 가장 알맞은 안내는 무엇인가요?', 'MULTIPLE_CHOICE', '1호기 엘리베이터나 계단을 이용하라고 안내한다', '점검 중 대체 이동 방법으로 1호기 엘리베이터와 계단이 제시되어 있습니다.', 'LEVEL_3', true, '상황에 맞는 대체 방법을 잘 찾았습니다.', '점검 중 이용 가능한 이동 방법을 안내한 문장을 확인해 보세요.'),
    ('DOC-HQ-L3-004', '반품 상자 분류 안내', '반품 상자는 송장 사진을 찍은 뒤 물류팀 앞 파란 카트에 올려 주세요. 새 상품 상자와 섞이지 않게 해야 합니다. 송장 사진이 흐리게 찍혔다면 다시 촬영한 뒤 올려 주세요.', '반품 상자를 새 상품 상자와 섞지 말아야 하는 이유로 가장 알맞은 것은 무엇인가요?', 'MULTIPLE_CHOICE', '반품과 새 상품을 구분해 처리하기 위해', '문서는 반품 상자를 별도 카트에 두어 새 상품과 섞이지 않게 하라고 합니다.', 'LEVEL_3', true, '분류 지시의 이유를 잘 이해했습니다.', '파란 카트가 어떤 물건을 따로 모으기 위한 장소인지 생각해 보세요.'),
    ('DOC-HQ-L3-005', '고객 전화 기록 지침', '고객 전화가 오면 이름, 연락처, 요청 내용을 기록합니다. 해결이 어려운 요청은 혼자 답하지 말고 팀장에게 전달합니다. 통화가 끝나면 기록지를 고객 응대함에 넣어 주세요.', '해결이 어려운 요청을 받았을 때 알맞은 행동은 무엇인가요?', 'MULTIPLE_CHOICE', '팀장에게 전달한다', '문서에는 해결이 어려운 요청은 혼자 답하지 말고 팀장에게 전달하라고 되어 있습니다.', 'LEVEL_3', true, '예외 상황에서의 대응 방법을 정확히 골랐습니다.', '혼자 처리하면 안 되는 상황을 안내한 문장을 다시 확인해 보세요.'),

    ('DOC-HQ-L4-001', '상품 상태별 분류 기준', '상품 포장지가 찢어졌거나 라벨이 잘못 붙은 상품은 불량품 바구니에 넣습니다. 단, 박스 모서리만 살짝 눌린 상품은 정상 상품으로 분류합니다. 판단이 어려우면 사진을 찍어 품질 담당자에게 확인받습니다.', '정상 상품으로 분류해야 하는 경우는 무엇인가요?', 'MULTIPLE_CHOICE', '박스 모서리만 살짝 눌린 상품', '단서 문장에 박스 모서리만 살짝 눌린 상품은 정상 상품이라고 되어 있습니다.', 'LEVEL_4', true, '예외 조건을 정확히 구분했습니다.', '불량 조건 뒤에 나오는 “단”이라는 예외 문장을 확인해 보세요.'),
    ('DOC-HQ-L4-002', '세탁물 제출 일정', '이번 주부터 근무복 세탁물은 수요일 오전까지 2층 세탁함에 넣어 주세요. 목요일 이후 제출한 세탁물은 다음 주에 처리됩니다. 이름표가 없는 세탁물은 접수되지 않습니다.', '목요일에 제출한 근무복은 언제 처리되나요?', 'MULTIPLE_CHOICE', '다음 주', '목요일 이후 제출한 세탁물은 다음 주에 처리된다고 안내되어 있습니다.', 'LEVEL_4', true, '기한을 넘긴 경우의 처리를 정확히 파악했습니다.', '수요일 오전까지와 목요일 이후의 처리 차이를 비교해 보세요.'),
    ('DOC-HQ-L4-003', '고객 방문 준비 메모', '오후 3시에 방문하는 고객에게는 회의실 C를 안내해 주세요. 담당자가 도착하기 전까지 생수 한 병과 회사 소개 자료 2부를 준비합니다. 고객이 먼저 도착하면 대기석이 아니라 회의실 C로 안내합니다.', '고객이 담당자보다 먼저 도착했을 때 어디로 안내해야 하나요?', 'MULTIPLE_CHOICE', '회의실 C', '마지막 문장에 고객이 먼저 도착하면 회의실 C로 안내하라고 되어 있습니다.', 'LEVEL_4', true, '조건이 붙은 안내를 정확히 읽었습니다.', '고객이 먼저 도착한 경우를 따로 설명한 마지막 문장을 확인해 보세요.'),
    ('DOC-HQ-L4-004', '전자세금계산서 확인 절차', '전자세금계산서를 받으면 거래처명과 금액을 먼저 확인합니다. 금액이 주문서와 같으면 승인 요청을 올립니다. 금액이 다르면 승인하지 말고 회계팀에 확인 요청을 보냅니다.', '금액이 주문서와 다를 때 해야 할 일은 무엇인가요?', 'MULTIPLE_CHOICE', '회계팀에 확인 요청을 보낸다', '금액이 다르면 승인하지 말고 회계팀에 확인 요청을 보내야 합니다.', 'LEVEL_4', true, '조건에 따른 처리 절차를 정확히 골랐습니다.', '금액이 같은 경우와 다른 경우의 행동을 비교해 보세요.'),
    ('DOC-HQ-L4-005', '보안 카드 분실 안내', '보안 카드를 분실한 직원은 즉시 총무팀에 신고해야 합니다. 신고 후 임시 출입증을 받을 수 있지만 당일만 사용할 수 있습니다. 다음 날부터는 재발급 카드가 나오기 전까지 안내 데스크에서 신분 확인을 받아야 합니다.', '임시 출입증은 언제까지 사용할 수 있나요?', 'MULTIPLE_CHOICE', '신고한 당일만', '문서에는 임시 출입증을 당일만 사용할 수 있다고 되어 있습니다.', 'LEVEL_4', true, '기간 제한 정보를 정확히 확인했습니다.', '임시 출입증 사용 가능 기간을 설명한 문장을 찾아보세요.'),

    ('DOC-HQ-L5-001', '납품 일정 변경 메일', '오늘 납품 예정이던 A상품은 거래처 요청으로 내일 오전 11시에 출고합니다. 대신 B상품 20개를 오늘 오후 4시까지 먼저 준비해 주세요. A상품 송장은 오늘 출력하지 말고, B상품 송장만 출력합니다.', '오늘 해야 할 일로 알맞은 것은 무엇인가요?', 'MULTIPLE_CHOICE', 'B상품 20개를 준비하고 B상품 송장만 출력한다', 'A상품은 내일 출고하므로 오늘은 B상품 준비와 B상품 송장 출력만 해야 합니다.', 'LEVEL_5', true, '변경된 일정과 오늘 해야 할 일을 함께 판단했습니다.', 'A상품과 B상품의 처리 날짜가 서로 다르다는 점을 비교해 보세요.'),
    ('DOC-HQ-L5-002', '근태 정정 신청 안내', '출근 기록이 누락된 직원은 당일 오후 6시 전까지 근태 정정 신청서를 작성해야 합니다. 신청서에는 누락 사유와 팀장 확인 서명이 필요합니다. 오후 6시 이후 제출하면 인사팀 검토가 다음 영업일로 넘어갑니다.', '오늘 안에 바로 검토받으려면 어떤 조건을 모두 지켜야 하나요?', 'MULTIPLE_CHOICE', '오후 6시 전까지 사유와 팀장 서명이 있는 신청서를 제출한다', '당일 검토를 위해서는 제출 시각, 누락 사유, 팀장 확인 서명이 모두 필요합니다.', 'LEVEL_5', true, '여러 조건을 빠짐없이 묶어 판단했습니다.', '시간 조건과 신청서에 들어갈 내용을 함께 확인해야 합니다.'),
    ('DOC-HQ-L5-003', '고객 정보 파일 전달 규칙', '고객 연락처가 포함된 파일은 외부 메일로 보내지 않습니다. 꼭 전달해야 할 때는 비밀번호를 설정한 뒤 승인받은 공유 링크로 전달합니다. 비밀번호는 파일과 같은 메시지에 적지 말고 전화로 따로 알려 주세요.', '고객 연락처 파일을 외부에 전달해야 할 때 올바른 방법은 무엇인가요?', 'MULTIPLE_CHOICE', '비밀번호를 설정하고 승인받은 공유 링크로 전달한 뒤 비밀번호는 전화로 알린다', '문서는 공유 링크 사용, 비밀번호 설정, 비밀번호 별도 전달을 모두 요구합니다.', 'LEVEL_5', true, '보안 절차의 여러 조건을 정확히 결합했습니다.', '파일 전달 방법과 비밀번호 전달 방법을 모두 확인해 보세요.'),
    ('DOC-HQ-L5-004', '월간 보고서 확인 요청', '월간 보고서에서 3페이지 매출 표만 다시 확인해 주세요. 표의 합계가 맞으면 파일명 뒤에 "_확인완료"를 붙여 저장하면 됩니다. 합계가 맞지 않으면 파일명을 바꾸지 말고 회계팀에 오류 내용을 전달해 주세요.', '매출 표 합계가 맞지 않을 때 해야 할 일은 무엇인가요?', 'MULTIPLE_CHOICE', '파일명을 바꾸지 말고 회계팀에 오류 내용을 전달한다', '합계가 맞지 않으면 파일명을 변경하지 않고 회계팀에 오류 내용을 전달해야 합니다.', 'LEVEL_5', true, '결과에 따른 분기 처리를 정확히 이해했습니다.', '합계가 맞을 때와 맞지 않을 때의 행동이 다릅니다.'),
    ('DOC-HQ-L5-005', '행사 준비 체크리스트', '행사장 입구에는 안내 배너를 세우고, 접수대에는 명찰과 참석자 명단을 둡니다. 점심 도시락은 오전 11시 이후 도착하면 냉장 보관하지 않고 바로 배식 준비 테이블로 옮깁니다. 오전 11시 전에 도착한 도시락만 냉장고에 넣어 주세요.', '오전 11시 20분에 도착한 도시락은 어떻게 처리해야 하나요?', 'MULTIPLE_CHOICE', '바로 배식 준비 테이블로 옮긴다', '오전 11시 이후 도착한 도시락은 냉장 보관하지 않고 배식 준비 테이블로 옮겨야 합니다.', 'LEVEL_5', true, '시간 조건에 따른 처리 방법을 정확히 판단했습니다.', '오전 11시 전과 이후의 처리 방법을 비교해 보세요.')
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    document_text = VALUES(document_text),
    question_text = VALUES(question_text),
    question_type = VALUES(question_type),
    correct_answer = VALUES(correct_answer),
    explanation = VALUES(explanation),
    difficulty = VALUES(difficulty),
    is_active = VALUES(is_active),
    correct_feedback = VALUES(correct_feedback),
    wrong_feedback = VALUES(wrong_feedback);

INSERT INTO document_question_choices (seed_code, question_id, choice_order, choice_text, is_correct)
SELECT 'DOC-HQ-L1-001-C01', question_id, 1, '회의실 A', false FROM document_questions WHERE seed_code = 'DOC-HQ-L1-001'
UNION ALL SELECT 'DOC-HQ-L1-001-C02', question_id, 2, '회의실 B', true FROM document_questions WHERE seed_code = 'DOC-HQ-L1-001'
UNION ALL SELECT 'DOC-HQ-L1-001-C03', question_id, 3, '회의실 C', false FROM document_questions WHERE seed_code = 'DOC-HQ-L1-001'
UNION ALL SELECT 'DOC-HQ-L1-002-C01', question_id, 1, '안내 데스크', false FROM document_questions WHERE seed_code = 'DOC-HQ-L1-002'
UNION ALL SELECT 'DOC-HQ-L1-002-C02', question_id, 2, '김대리', true FROM document_questions WHERE seed_code = 'DOC-HQ-L1-002'
UNION ALL SELECT 'DOC-HQ-L1-002-C03', question_id, 3, '방문자', false FROM document_questions WHERE seed_code = 'DOC-HQ-L1-002'
UNION ALL SELECT 'DOC-HQ-L1-003-C01', question_id, 1, '총무팀 보관함', false FROM document_questions WHERE seed_code = 'DOC-HQ-L1-003'
UNION ALL SELECT 'DOC-HQ-L1-003-C02', question_id, 2, '탕비실 냉장고', true FROM document_questions WHERE seed_code = 'DOC-HQ-L1-003'
UNION ALL SELECT 'DOC-HQ-L1-003-C03', question_id, 3, '물류팀 파란 카트', false FROM document_questions WHERE seed_code = 'DOC-HQ-L1-003'
UNION ALL SELECT 'DOC-HQ-L1-004-C01', question_id, 1, '공용 컵 바구니', false FROM document_questions WHERE seed_code = 'DOC-HQ-L1-004'
UNION ALL SELECT 'DOC-HQ-L1-004-C02', question_id, 2, '싱크대 오른쪽', false FROM document_questions WHERE seed_code = 'DOC-HQ-L1-004'
UNION ALL SELECT 'DOC-HQ-L1-004-C03', question_id, 3, '이름표가 붙은 선반', true FROM document_questions WHERE seed_code = 'DOC-HQ-L1-004'
UNION ALL SELECT 'DOC-HQ-L1-005-C01', question_id, 1, '3층 세미나실', true FROM document_questions WHERE seed_code = 'DOC-HQ-L1-005'
UNION ALL SELECT 'DOC-HQ-L1-005-C02', question_id, 2, '2층 회의실', false FROM document_questions WHERE seed_code = 'DOC-HQ-L1-005'
UNION ALL SELECT 'DOC-HQ-L1-005-C03', question_id, 3, '회사 정문 앞', false FROM document_questions WHERE seed_code = 'DOC-HQ-L1-005'
UNION ALL SELECT 'DOC-HQ-L2-001-C01', question_id, 1, '관리부에 먼저 연락한다', false FROM document_questions WHERE seed_code = 'DOC-HQ-L2-001'
UNION ALL SELECT 'DOC-HQ-L2-001-C02', question_id, 2, '덮개를 닫고 확인 버튼을 누른다', true FROM document_questions WHERE seed_code = 'DOC-HQ-L2-001'
UNION ALL SELECT 'DOC-HQ-L2-001-C03', question_id, 3, '화면 오류를 무시한다', false FROM document_questions WHERE seed_code = 'DOC-HQ-L2-001'
UNION ALL SELECT 'DOC-HQ-L2-002-C01', question_id, 1, '회의실 A 앞 테이블', true FROM document_questions WHERE seed_code = 'DOC-HQ-L2-002'
UNION ALL SELECT 'DOC-HQ-L2-002-C02', question_id, 2, '팀장 책상 위', false FROM document_questions WHERE seed_code = 'DOC-HQ-L2-002'
UNION ALL SELECT 'DOC-HQ-L2-002-C03', question_id, 3, '관리부 보관함', false FROM document_questions WHERE seed_code = 'DOC-HQ-L2-002'
UNION ALL SELECT 'DOC-HQ-L2-003-C01', question_id, 1, '이번 달', false FROM document_questions WHERE seed_code = 'DOC-HQ-L2-003'
UNION ALL SELECT 'DOC-HQ-L2-003-C02', question_id, 2, '다음 달', true FROM document_questions WHERE seed_code = 'DOC-HQ-L2-003'
UNION ALL SELECT 'DOC-HQ-L2-003-C03', question_id, 3, '목요일 오후 3시 전', false FROM document_questions WHERE seed_code = 'DOC-HQ-L2-003'
UNION ALL SELECT 'DOC-HQ-L2-004-C01', question_id, 1, '파일명', false FROM document_questions WHERE seed_code = 'DOC-HQ-L2-004'
UNION ALL SELECT 'DOC-HQ-L2-004-C02', question_id, 2, '완료 메시지', false FROM document_questions WHERE seed_code = 'DOC-HQ-L2-004'
UNION ALL SELECT 'DOC-HQ-L2-004-C03', question_id, 3, '파일 내용', true FROM document_questions WHERE seed_code = 'DOC-HQ-L2-004'
UNION ALL SELECT 'DOC-HQ-L2-005-C01', question_id, 1, '강사 책상 위 상자', true FROM document_questions WHERE seed_code = 'DOC-HQ-L2-005'
UNION ALL SELECT 'DOC-HQ-L2-005-C02', question_id, 2, '뒤쪽 벽 옆', false FROM document_questions WHERE seed_code = 'DOC-HQ-L2-005'
UNION ALL SELECT 'DOC-HQ-L2-005-C03', question_id, 3, '빔프로젝터 아래', false FROM document_questions WHERE seed_code = 'DOC-HQ-L2-005'
UNION ALL SELECT 'DOC-HQ-L3-001-C01', question_id, 1, '사무용품을 아껴 쓰자고 요청하기 위해', true FROM document_questions WHERE seed_code = 'DOC-HQ-L3-001'
UNION ALL SELECT 'DOC-HQ-L3-001-C02', question_id, 2, '새 회의실 사용법을 안내하기 위해', false FROM document_questions WHERE seed_code = 'DOC-HQ-L3-001'
UNION ALL SELECT 'DOC-HQ-L3-001-C03', question_id, 3, '종이컵 구매처를 소개하기 위해', false FROM document_questions WHERE seed_code = 'DOC-HQ-L3-001'
UNION ALL SELECT 'DOC-HQ-L3-002-C01', question_id, 1, '청소 시간을 줄이는 것', false FROM document_questions WHERE seed_code = 'DOC-HQ-L3-002'
UNION ALL SELECT 'DOC-HQ-L3-002-C02', question_id, 2, '개인정보가 밖으로 새어 나가지 않게 하는 것', true FROM document_questions WHERE seed_code = 'DOC-HQ-L3-002'
UNION ALL SELECT 'DOC-HQ-L3-002-C03', question_id, 3, '문서 보관함을 비우는 것', false FROM document_questions WHERE seed_code = 'DOC-HQ-L3-002'
UNION ALL SELECT 'DOC-HQ-L3-003-C01', question_id, 1, '점검이 끝날 때까지 기다리라고 안내한다', false FROM document_questions WHERE seed_code = 'DOC-HQ-L3-003'
UNION ALL SELECT 'DOC-HQ-L3-003-C02', question_id, 2, '1호기 엘리베이터나 계단을 이용하라고 안내한다', true FROM document_questions WHERE seed_code = 'DOC-HQ-L3-003'
UNION ALL SELECT 'DOC-HQ-L3-003-C03', question_id, 3, '2호기 엘리베이터를 그냥 타라고 안내한다', false FROM document_questions WHERE seed_code = 'DOC-HQ-L3-003'
UNION ALL SELECT 'DOC-HQ-L3-004-C01', question_id, 1, '반품과 새 상품을 구분해 처리하기 위해', true FROM document_questions WHERE seed_code = 'DOC-HQ-L3-004'
UNION ALL SELECT 'DOC-HQ-L3-004-C02', question_id, 2, '사진 촬영 시간을 줄이기 위해', false FROM document_questions WHERE seed_code = 'DOC-HQ-L3-004'
UNION ALL SELECT 'DOC-HQ-L3-004-C03', question_id, 3, '파란 카트를 비워 두기 위해', false FROM document_questions WHERE seed_code = 'DOC-HQ-L3-004'
UNION ALL SELECT 'DOC-HQ-L3-005-C01', question_id, 1, '고객에게 바로 거절한다고 말한다', false FROM document_questions WHERE seed_code = 'DOC-HQ-L3-005'
UNION ALL SELECT 'DOC-HQ-L3-005-C02', question_id, 2, '팀장에게 전달한다', true FROM document_questions WHERE seed_code = 'DOC-HQ-L3-005'
UNION ALL SELECT 'DOC-HQ-L3-005-C03', question_id, 3, '기록지를 버린다', false FROM document_questions WHERE seed_code = 'DOC-HQ-L3-005'
UNION ALL SELECT 'DOC-HQ-L4-001-C01', question_id, 1, '포장지가 찢어진 상품', false FROM document_questions WHERE seed_code = 'DOC-HQ-L4-001'
UNION ALL SELECT 'DOC-HQ-L4-001-C02', question_id, 2, '라벨이 잘못 붙은 상품', false FROM document_questions WHERE seed_code = 'DOC-HQ-L4-001'
UNION ALL SELECT 'DOC-HQ-L4-001-C03', question_id, 3, '박스 모서리만 살짝 눌린 상품', true FROM document_questions WHERE seed_code = 'DOC-HQ-L4-001'
UNION ALL SELECT 'DOC-HQ-L4-002-C01', question_id, 1, '이번 주', false FROM document_questions WHERE seed_code = 'DOC-HQ-L4-002'
UNION ALL SELECT 'DOC-HQ-L4-002-C02', question_id, 2, '다음 주', true FROM document_questions WHERE seed_code = 'DOC-HQ-L4-002'
UNION ALL SELECT 'DOC-HQ-L4-002-C03', question_id, 3, '목요일 오전', false FROM document_questions WHERE seed_code = 'DOC-HQ-L4-002'
UNION ALL SELECT 'DOC-HQ-L4-003-C01', question_id, 1, '대기석', false FROM document_questions WHERE seed_code = 'DOC-HQ-L4-003'
UNION ALL SELECT 'DOC-HQ-L4-003-C02', question_id, 2, '회의실 C', true FROM document_questions WHERE seed_code = 'DOC-HQ-L4-003'
UNION ALL SELECT 'DOC-HQ-L4-003-C03', question_id, 3, '안내 데스크', false FROM document_questions WHERE seed_code = 'DOC-HQ-L4-003'
UNION ALL SELECT 'DOC-HQ-L4-004-C01', question_id, 1, '승인 요청을 올린다', false FROM document_questions WHERE seed_code = 'DOC-HQ-L4-004'
UNION ALL SELECT 'DOC-HQ-L4-004-C02', question_id, 2, '회계팀에 확인 요청을 보낸다', true FROM document_questions WHERE seed_code = 'DOC-HQ-L4-004'
UNION ALL SELECT 'DOC-HQ-L4-004-C03', question_id, 3, '거래처명을 지운다', false FROM document_questions WHERE seed_code = 'DOC-HQ-L4-004'
UNION ALL SELECT 'DOC-HQ-L4-005-C01', question_id, 1, '신고한 당일만', true FROM document_questions WHERE seed_code = 'DOC-HQ-L4-005'
UNION ALL SELECT 'DOC-HQ-L4-005-C02', question_id, 2, '재발급 카드가 나올 때까지 계속', false FROM document_questions WHERE seed_code = 'DOC-HQ-L4-005'
UNION ALL SELECT 'DOC-HQ-L4-005-C03', question_id, 3, '다음 날부터 일주일 동안', false FROM document_questions WHERE seed_code = 'DOC-HQ-L4-005'
UNION ALL SELECT 'DOC-HQ-L5-001-C01', question_id, 1, 'A상품 송장을 출력하고 A상품을 준비한다', false FROM document_questions WHERE seed_code = 'DOC-HQ-L5-001'
UNION ALL SELECT 'DOC-HQ-L5-001-C02', question_id, 2, 'B상품 20개를 준비하고 B상품 송장만 출력한다', true FROM document_questions WHERE seed_code = 'DOC-HQ-L5-001'
UNION ALL SELECT 'DOC-HQ-L5-001-C03', question_id, 3, 'A상품과 B상품을 모두 내일 준비한다', false FROM document_questions WHERE seed_code = 'DOC-HQ-L5-001'
UNION ALL SELECT 'DOC-HQ-L5-002-C01', question_id, 1, '오후 6시 전까지 사유와 팀장 서명이 있는 신청서를 제출한다', true FROM document_questions WHERE seed_code = 'DOC-HQ-L5-002'
UNION ALL SELECT 'DOC-HQ-L5-002-C02', question_id, 2, '오후 6시 이후 신청서만 제출한다', false FROM document_questions WHERE seed_code = 'DOC-HQ-L5-002'
UNION ALL SELECT 'DOC-HQ-L5-002-C03', question_id, 3, '팀장 서명 없이 사유만 적는다', false FROM document_questions WHERE seed_code = 'DOC-HQ-L5-002'
UNION ALL SELECT 'DOC-HQ-L5-003-C01', question_id, 1, '외부 메일에 파일과 비밀번호를 함께 보낸다', false FROM document_questions WHERE seed_code = 'DOC-HQ-L5-003'
UNION ALL SELECT 'DOC-HQ-L5-003-C02', question_id, 2, '비밀번호 없이 공유 링크만 보낸다', false FROM document_questions WHERE seed_code = 'DOC-HQ-L5-003'
UNION ALL SELECT 'DOC-HQ-L5-003-C03', question_id, 3, '비밀번호를 설정하고 승인받은 공유 링크로 전달한 뒤 비밀번호는 전화로 알린다', true FROM document_questions WHERE seed_code = 'DOC-HQ-L5-003'
UNION ALL SELECT 'DOC-HQ-L5-004-C01', question_id, 1, '파일명을 바꾸지 말고 회계팀에 오류 내용을 전달한다', true FROM document_questions WHERE seed_code = 'DOC-HQ-L5-004'
UNION ALL SELECT 'DOC-HQ-L5-004-C02', question_id, 2, '파일명 뒤에 "_확인완료"를 붙인다', false FROM document_questions WHERE seed_code = 'DOC-HQ-L5-004'
UNION ALL SELECT 'DOC-HQ-L5-004-C03', question_id, 3, '3페이지를 삭제한다', false FROM document_questions WHERE seed_code = 'DOC-HQ-L5-004'
UNION ALL SELECT 'DOC-HQ-L5-005-C01', question_id, 1, '냉장고에 넣는다', false FROM document_questions WHERE seed_code = 'DOC-HQ-L5-005'
UNION ALL SELECT 'DOC-HQ-L5-005-C02', question_id, 2, '바로 배식 준비 테이블로 옮긴다', true FROM document_questions WHERE seed_code = 'DOC-HQ-L5-005'
UNION ALL SELECT 'DOC-HQ-L5-005-C03', question_id, 3, '접수대에 둔다', false FROM document_questions WHERE seed_code = 'DOC-HQ-L5-005'
ON DUPLICATE KEY UPDATE
    question_id = VALUES(question_id),
    choice_order = VALUES(choice_order),
    choice_text = VALUES(choice_text),
    is_correct = VALUES(is_correct);
