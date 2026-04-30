UPDATE social_scenarios
SET difficulty = CASE UPPER(difficulty)
    WHEN 'EASY' THEN '1'
    WHEN 'MEDIUM' THEN '2'
    WHEN 'HARD' THEN '3'
    ELSE difficulty
END
WHERE difficulty IS NOT NULL;
