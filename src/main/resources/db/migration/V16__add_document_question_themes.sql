ALTER TABLE document_questions
    ADD COLUMN theme VARCHAR(30) NOT NULL DEFAULT 'ANNOUNCEMENT';

UPDATE document_questions
SET theme = 'ANNOUNCEMENT'
WHERE seed_code IN (
    'DOC-HQ-L1-001',
    'DOC-HQ-L1-004',
    'DOC-HQ-L1-005',
    'DOC-HQ-L2-003',
    'DOC-HQ-L3-001',
    'DOC-HQ-L3-003',
    'DOC-HQ-L4-002',
    'DOC-HQ-L4-005',
    'DOC-HQ-L5-002',
    'DOC-HQ-L5-005',
    'DOC-MC-L1-001',
    'DOC-MC-L3-001'
);

UPDATE document_questions
SET theme = 'MANUAL'
WHERE seed_code IN (
    'DOC-HQ-L1-003',
    'DOC-HQ-L2-001',
    'DOC-HQ-L2-002',
    'DOC-HQ-L2-005',
    'DOC-HQ-L3-002',
    'DOC-HQ-L3-004',
    'DOC-HQ-L4-001',
    'DOC-HQ-L4-004',
    'DOC-HQ-L5-003',
    'DOC-HQ-L5-004',
    'DOC-MC-L2-001'
);

UPDATE document_questions
SET theme = 'MESSENGER'
WHERE seed_code IN (
    'DOC-HQ-L1-002',
    'DOC-HQ-L2-004',
    'DOC-HQ-L3-005',
    'DOC-HQ-L4-003',
    'DOC-HQ-L5-001'
);

ALTER TABLE document_questions
    ADD CONSTRAINT ck_document_questions_theme
        CHECK (theme IN ('ANNOUNCEMENT', 'MANUAL', 'MESSENGER'));
