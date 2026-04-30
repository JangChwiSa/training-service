ALTER TABLE user_document_progress
    ADD COLUMN current_level INT NOT NULL DEFAULT 1;

ALTER TABLE user_document_progress
    ADD COLUMN highest_unlocked_level INT NOT NULL DEFAULT 1;

ALTER TABLE user_document_progress
    ADD COLUMN last_played_level INT;

ALTER TABLE user_document_progress
    ADD COLUMN last_accuracy_rate DECIMAL(5, 2);

ALTER TABLE user_document_progress
    ADD CONSTRAINT ck_user_document_progress_current_level CHECK (current_level >= 1);

ALTER TABLE user_document_progress
    ADD CONSTRAINT ck_user_document_progress_highest_level CHECK (highest_unlocked_level >= 1);

ALTER TABLE user_document_progress
    ADD CONSTRAINT ck_user_document_progress_last_level CHECK (last_played_level IS NULL OR last_played_level >= 1);

ALTER TABLE user_document_progress
    ADD CONSTRAINT ck_user_document_progress_last_accuracy CHECK (last_accuracy_rate IS NULL OR last_accuracy_rate BETWEEN 0 AND 100);
