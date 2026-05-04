UPDATE safety_scenarios
SET title = '출퇴근 안전',
    description = '횡단보도, 버스 정류장, 버스 탑승 상황에서 안전한 행동을 익히는 훈련'
WHERE scenario_id = 3;

UPDATE safety_scenes
SET screen_info = '횡단보도 앞',
    situation_text = '신호가 깜빡이고 있습니다.',
    question_text = '어떻게 행동해야 할까요?',
    image_url = '/static/trainings/safety/scenes/commute-crosswalk-01.png',
    image_alt = '횡단보도 앞에서 신호를 기다리는 장면'
WHERE scene_id = 8;

UPDATE safety_scenes
SET screen_info = '버스 정류장',
    situation_text = '버스가 도착했습니다.',
    question_text = '어디에 서야 할까요?',
    image_url = '/static/trainings/safety/scenes/commute-bus-stop-02.png',
    image_alt = '버스 정류장에서 줄을 서야 하는 장면'
WHERE scene_id = 10;

UPDATE safety_scenes
SET screen_info = '버스 안',
    situation_text = '버스가 갑자기 흔들립니다.',
    question_text = '어떻게 몸을 지켜야 할까요?',
    image_url = '/static/trainings/safety/scenes/commute-bus-inside-03.png',
    image_alt = '버스 안에서 손잡이를 잡아야 하는 장면'
WHERE scene_id = 11;

UPDATE safety_choices
SET result_text = '안전하게 기다렸습니다.',
    effect_text = '사고 위험을 줄였습니다.',
    feedback_image_url = '/static/trainings/safety/feedback/commute-crosswalk-correct.png',
    feedback_image_alt = '안전하게 기다린 뒤 횡단보도를 건너는 결과 화면'
WHERE choice_id = 26;

UPDATE safety_choices
SET result_text = '줄 뒤에서 차분히 기다렸습니다.',
    effect_text = '밀치거나 부딪힐 위험을 줄였습니다.',
    feedback_image_url = '/static/trainings/safety/feedback/commute-bus-stop-correct.png',
    feedback_image_alt = '줄 뒤에 서서 차례를 기다리는 결과 화면'
WHERE choice_id = 29;

UPDATE safety_choices
SET result_text = '손잡이를 잡고 자세를 지켰습니다.',
    effect_text = '급정거에도 넘어질 위험을 줄였습니다.',
    feedback_image_url = '/static/trainings/safety/feedback/commute-bus-inside-correct.png',
    feedback_image_alt = '버스 안에서 손잡이를 잡고 균형을 유지하는 결과 화면'
WHERE choice_id = 31;
