INSERT INTO social_scenarios
    (scenario_id, job_type, title, background_text, situation_text, character_info, difficulty, is_active)
VALUES
    (1, 'OFFICE', 'Ask a coworker for help', 'You are working in an office and need help finishing a task.', 'A coworker is nearby, but they also look busy.', 'Coworker: kind but focused on their own work.', 'EASY', TRUE),
    (2, 'LABOR', 'Report a workplace mistake', 'You are working at a job site and notice a small mistake in your task.', 'You need to explain the mistake to your supervisor calmly.', 'Supervisor: direct, practical, and safety-focused.', 'EASY', TRUE);

INSERT INTO safety_scenarios
    (scenario_id, title, category, description, is_active, created_at)
VALUES
    (1, 'Respond to an uncomfortable comment', 'SEXUAL_EDUCATION', 'Practice choosing a safe response when someone makes an uncomfortable personal comment.', TRUE, CURRENT_TIMESTAMP(6)),
    (2, 'Handle coughing symptoms at work', 'INFECTIOUS_DISEASE', 'Practice safe behavior when you or a coworker has respiratory symptoms.', TRUE, CURRENT_TIMESTAMP(6)),
    (3, 'Cross the street on the way to work', 'COMMUTE_SAFETY', 'Practice choosing a safe commute action near a crosswalk.', TRUE, CURRENT_TIMESTAMP(6));

INSERT INTO safety_scenes
    (scene_id, scenario_id, scene_order, screen_info, situation_text, question_text, is_end_scene)
VALUES
    (1, 1, 1, 'workplace conversation', 'A coworker makes a personal comment that makes you uncomfortable.', 'What should you do first?', TRUE),
    (2, 2, 1, 'office health situation', 'A coworker keeps coughing near your desk.', 'What is the safest response?', TRUE),
    (3, 3, 1, 'crosswalk commute', 'The pedestrian signal is red, but there are no cars nearby.', 'What should you do?', TRUE);

INSERT INTO safety_choices
    (choice_id, scene_id, choice_text, next_scene_id, is_correct)
VALUES
    (1, 1, 'Say that the comment makes you uncomfortable and ask them to stop.', NULL, TRUE),
    (2, 1, 'Ignore every uncomfortable comment without telling anyone.', NULL, FALSE),
    (3, 2, 'Keep distance and suggest following the workplace health rule.', NULL, TRUE),
    (4, 2, 'Share the same cup because you know the coworker well.', NULL, FALSE),
    (5, 3, 'Wait until the pedestrian signal turns green.', NULL, TRUE),
    (6, 3, 'Cross quickly while the signal is still red.', NULL, FALSE);

INSERT INTO focus_level_rules
    (level, duration_seconds, command_interval_ms, command_complexity, required_accuracy_rate, is_active, created_at, updated_at)
VALUES
    (1, 180, 3000, 'SIMPLE', 80.00, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (2, 180, 2500, 'MEDIUM', 85.00, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
    (3, 180, 2000, 'COMPLEX', 90.00, TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));

INSERT INTO document_questions
    (question_id, title, document_text, question_text, question_type, correct_answer, explanation, difficulty, is_active)
VALUES
    (1, 'Read a workplace notice', 'The team meeting starts at 10:00 AM in Room 2. Bring your notebook.', 'Where does the meeting start?', 'SHORT_ANSWER', 'Room 2', 'The notice says the meeting starts in Room 2.', 'EASY', TRUE),
    (2, 'Understand a safety sign', 'Wet Floor: Please walk slowly and use the side path.', 'What should you do after reading the sign?', 'SHORT_ANSWER', 'Walk slowly and use the side path.', 'The sign asks people to walk slowly and use the side path.', 'EASY', TRUE);
