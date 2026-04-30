INSERT INTO social_scenarios
    (scenario_id, job_type, title, background_text, situation_text, character_info, difficulty, is_active)
VALUES
    (1, 'OFFICE', 'Ask a coworker for help', 'You are working in an office and need help finishing a task.', 'A coworker is nearby, but they also look busy.', 'Coworker: kind but focused on their own work.', '1', TRUE),
    (2, 'LABOR', 'Report a workplace mistake', 'You are working at a job site and notice a small mistake in your task.', 'You need to explain the mistake to your supervisor calmly.', 'Supervisor: direct, practical, and safety-focused.', '1', TRUE);

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
    (1, 'Read a workplace notice 1', 'The team meeting starts at 10:00 AM in Room 2. Bring your notebook.', 'Where does the meeting start?', 'SHORT_ANSWER', 'Room 2', 'The notice says the meeting starts in Room 2.', 'LEVEL_1', TRUE),
    (2, 'Read a workplace notice 2', 'Lunch break begins at 12:30 PM today.', 'When does lunch break begin?', 'SHORT_ANSWER', '12:30 PM', 'The notice says lunch break begins at 12:30 PM.', 'LEVEL_1', TRUE),
    (3, 'Read a workplace notice 3', 'Please put completed forms in the blue tray.', 'Where should completed forms go?', 'SHORT_ANSWER', 'Blue tray', 'The notice says completed forms go in the blue tray.', 'LEVEL_1', TRUE),
    (4, 'Read a workplace notice 4', 'Use the side door while the front door is repaired.', 'Which door should you use?', 'SHORT_ANSWER', 'Side door', 'The notice asks people to use the side door.', 'LEVEL_1', TRUE),
    (5, 'Read a workplace notice 5', 'Training starts on Monday at 9:00 AM.', 'What day does training start?', 'SHORT_ANSWER', 'Monday', 'The notice says training starts on Monday.', 'LEVEL_1', TRUE),
    (6, 'Understand a safety sign 1', 'Wet Floor: Please walk slowly and use the side path.', 'What should you do after reading the sign?', 'SHORT_ANSWER', 'Walk slowly and use the side path.', 'The sign asks people to walk slowly and use the side path.', 'LEVEL_2', TRUE),
    (7, 'Understand a safety sign 2', 'Visitors must sign in at the office before entering.', 'Where must visitors sign in?', 'SHORT_ANSWER', 'Office', 'The sign says visitors sign in at the office.', 'LEVEL_2', TRUE),
    (8, 'Understand a safety sign 3', 'Do not use the elevator during a fire alarm.', 'What should you not use during a fire alarm?', 'SHORT_ANSWER', 'Elevator', 'The sign says not to use the elevator.', 'LEVEL_2', TRUE),
    (9, 'Understand a safety sign 4', 'Wear gloves when handling cleaning chemicals.', 'What should you wear when handling cleaning chemicals?', 'SHORT_ANSWER', 'Gloves', 'The sign tells workers to wear gloves.', 'LEVEL_2', TRUE),
    (10, 'Understand a safety sign 5', 'Report damaged tools to the supervisor before use.', 'Who should receive reports about damaged tools?', 'SHORT_ANSWER', 'Supervisor', 'The sign says to report damaged tools to the supervisor.', 'LEVEL_2', TRUE),
    (11, 'Read a memo 1', 'If the copier is empty, refill paper from the supply cabinet.', 'Where is copier paper stored?', 'SHORT_ANSWER', 'Supply cabinet', 'The memo says copier paper is in the supply cabinet.', 'LEVEL_3', TRUE),
    (12, 'Read a memo 2', 'Submit time sheets by Friday afternoon to payroll.', 'When should time sheets be submitted?', 'SHORT_ANSWER', 'Friday afternoon', 'The memo says time sheets are due by Friday afternoon.', 'LEVEL_3', TRUE),
    (13, 'Read a memo 3', 'New ID cards are available at the reception desk.', 'Where can workers get new ID cards?', 'SHORT_ANSWER', 'Reception desk', 'The memo says ID cards are available at reception.', 'LEVEL_3', TRUE),
    (14, 'Read a memo 4', 'Call maintenance if the room temperature is too low.', 'Who should be called when the room is too cold?', 'SHORT_ANSWER', 'Maintenance', 'The memo says to call maintenance.', 'LEVEL_3', TRUE),
    (15, 'Read a memo 5', 'Package labels must be checked before shipping.', 'What must be checked before shipping?', 'SHORT_ANSWER', 'Package labels', 'The memo says package labels must be checked.', 'LEVEL_3', TRUE),
    (16, 'Follow work instructions 1', 'After scanning each item, place it in Box A unless it is marked fragile.', 'Where do normal scanned items go?', 'SHORT_ANSWER', 'Box A', 'The instruction says non-fragile scanned items go in Box A.', 'LEVEL_4', TRUE),
    (17, 'Follow work instructions 2', 'Email the daily checklist after both opening tasks are complete.', 'When should the checklist be emailed?', 'SHORT_ANSWER', 'After both opening tasks are complete', 'The instruction says to email it after both opening tasks.', 'LEVEL_4', TRUE),
    (18, 'Follow work instructions 3', 'If a customer receipt is missing, ask the manager before processing a refund.', 'Who should be asked before processing the refund?', 'SHORT_ANSWER', 'Manager', 'The instruction says to ask the manager first.', 'LEVEL_4', TRUE),
    (19, 'Follow work instructions 4', 'Store cold deliveries in the refrigerator within ten minutes.', 'Where should cold deliveries be stored?', 'SHORT_ANSWER', 'Refrigerator', 'The instruction says cold deliveries go in the refrigerator.', 'LEVEL_4', TRUE),
    (20, 'Follow work instructions 5', 'Use the backup printer only when the main printer shows an error.', 'When should the backup printer be used?', 'SHORT_ANSWER', 'When the main printer shows an error', 'The instruction limits backup printer use to main printer errors.', 'LEVEL_4', TRUE),
    (21, 'Compare document details 1', 'Morning shift starts at 8:30 AM. Afternoon shift starts at 1:00 PM.', 'What time does the afternoon shift start?', 'SHORT_ANSWER', '1:00 PM', 'The document lists afternoon shift at 1:00 PM.', 'LEVEL_5', TRUE),
    (22, 'Compare document details 2', 'Urgent mail goes to Desk 3. Regular mail goes to Desk 1.', 'Where does urgent mail go?', 'SHORT_ANSWER', 'Desk 3', 'The document says urgent mail goes to Desk 3.', 'LEVEL_5', TRUE),
    (23, 'Compare document details 3', 'If an item is damaged, mark it HOLD and notify inventory.', 'What label should damaged items receive?', 'SHORT_ANSWER', 'HOLD', 'The document says damaged items should be marked HOLD.', 'LEVEL_5', TRUE),
    (24, 'Compare document details 4', 'Staff may swap shifts only after written approval from the supervisor.', 'What is needed before swapping shifts?', 'SHORT_ANSWER', 'Written approval from the supervisor', 'The document requires written supervisor approval.', 'LEVEL_5', TRUE),
    (25, 'Compare document details 5', 'Orders received after 4:00 PM are packed the next business day.', 'When are orders after 4:00 PM packed?', 'SHORT_ANSWER', 'Next business day', 'The document says these orders are packed the next business day.', 'LEVEL_5', TRUE);
