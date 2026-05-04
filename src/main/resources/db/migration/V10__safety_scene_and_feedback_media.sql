ALTER TABLE safety_scenes
    ADD COLUMN image_url VARCHAR(500);

ALTER TABLE safety_scenes
    ADD COLUMN image_alt VARCHAR(500);

ALTER TABLE safety_choices
    ADD COLUMN feedback_image_url VARCHAR(500);

ALTER TABLE safety_choices
    ADD COLUMN feedback_image_alt VARCHAR(500);
