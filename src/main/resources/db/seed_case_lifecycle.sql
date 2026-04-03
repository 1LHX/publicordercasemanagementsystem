-- Demo seed for case lifecycle scenarios (10-20 cases)
-- Prerequisite: run `src/main/resources/db/schema.sql` first.
-- This script is idempotent for demo/dev use.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM case_executions;
DELETE FROM case_decisions;
DELETE FROM case_legal_reviews;
DELETE FROM case_evidences;
DELETE FROM case_processes;
DELETE FROM cases;

SET FOREIGN_KEY_CHECKS = 1;

-- Ensure demo users exist (password: Admin@123456)
INSERT INTO users (
    id, password, name, role, department_id, is_active,
    last_login, login_attempts, locked_until, created_at, updated_at
) VALUES
    (2, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjQf8M2MvmP0xSg6u40qCMgfHdCqkfS', 'officer_wang', 'police_officer', 2, 1, NULL, 0, NULL, NOW(), NOW()),
    (3, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjQf8M2MvmP0xSg6u40qCMgfHdCqkfS', 'officer_li', 'police_officer', 2, 1, NULL, 0, NULL, NOW(), NOW()),
    (4, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjQf8M2MvmP0xSg6u40qCMgfHdCqkfS', 'legal_zhang', 'legal_officer', 3, 1, NULL, 0, NULL, NOW(), NOW()),
    (5, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjQf8M2MvmP0xSg6u40qCMgfHdCqkfS', 'supervisor_chen', 'supervisor', 1, 1, NULL, 0, NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    role = VALUES(role),
    department_id = VALUES(department_id),
    is_active = VALUES(is_active),
    updated_at = NOW();

-- 12 cases across full lifecycle + deadline scenarios
INSERT INTO cases (
    id, case_number, title, type_code, status,
    reporter_name, reporter_contact, incident_time, incident_location,
    brief_description, handling_officer_id, department_id,
    acceptance_time, deadline_time, is_overdue, created_at, updated_at
) VALUES
    (101, 'PO-2026-0001', 'Night market loudspeaker disturbance', 'NOISE_COMPLAINT', 'REGISTERED',
        'Liu A', '13800000001', DATE_SUB(NOW(), INTERVAL 3 DAY), 'Donghu District',
        'Residents reported excessive noise at midnight.', NULL, 1,
        NULL, DATE_ADD(NOW(), INTERVAL 7 DAY), 0, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),

    (102, 'PO-2026-0002', 'Roadside blocking with temporary stalls', 'TRAFFIC_OBSTRUCTION', 'ACCEPTED',
        'Zhang B', '13800000002', DATE_SUB(NOW(), INTERVAL 5 DAY), 'Qingshanhu Avenue',
        'Multiple stalls blocked one lane.', NULL, 1,
        DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 0, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),

    (103, 'PO-2026-0003', 'Unlicensed late-night snack operation', 'UNLICENSED_BUSINESS', 'INVESTIGATING',
        'Chen C', '13800000003', DATE_SUB(NOW(), INTERVAL 8 DAY), 'Honggutan Street',
        'Repeated unlicensed operation after warning.', 2, 2,
        DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY), 0, DATE_SUB(NOW(), INTERVAL 8 DAY), NOW()),

    (104, 'PO-2026-0004', 'Square gathering without permit', 'ILLEGAL_ASSEMBLY', 'LEGAL_REVIEW',
        'Wu D', '13800000004', DATE_SUB(NOW(), INTERVAL 10 DAY), 'People Square',
        'Gathering scale exceeded permit threshold.', 3, 2,
        DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY), 0, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),

    (105, 'PO-2026-0005', 'Public disturbance in subway entrance', 'PUBLIC_DISTURBANCE', 'LEGAL_REVIEW',
        'Sun E', '13800000005', DATE_SUB(NOW(), INTERVAL 11 DAY), 'Metro Line 1 Exit A',
        'Argument escalated and blocked pedestrian flow.', 2, 2,
        DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY), 0, DATE_SUB(NOW(), INTERVAL 11 DAY), NOW()),

    (106, 'PO-2026-0006', 'Illegal flyer distribution at crossroads', 'PUBLIC_DISTURBANCE', 'DECIDED',
        'He F', '13800000006', DATE_SUB(NOW(), INTERVAL 14 DAY), 'Fuqiao Intersection',
        'Distribution caused crowding and traffic delay.', 3, 2,
        DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), 0, DATE_SUB(NOW(), INTERVAL 14 DAY), NOW()),

    (107, 'PO-2026-0007', 'Repeated noise complaint from karaoke bar', 'NOISE_COMPLAINT', 'EXECUTED',
        'Zhao G', '13800000007', DATE_SUB(NOW(), INTERVAL 16 DAY), 'Jiefang West Road',
        'Repeated violations after prior correction notice.', 2, 2,
        DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), 0, DATE_SUB(NOW(), INTERVAL 16 DAY), NOW()),

    (108, 'PO-2026-0008', 'Illegal roadside assembly affecting bus stop', 'ILLEGAL_ASSEMBLY', 'ARCHIVED',
        'Qian H', '13800000008', DATE_SUB(NOW(), INTERVAL 20 DAY), 'Bayi Bridge South',
        'Unauthorized assembly blocked bus queue area.', 3, 2,
        DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 0, DATE_SUB(NOW(), INTERVAL 20 DAY), NOW()),

    (109, 'PO-2026-0009', 'Night food cart relaunch after seizure', 'UNLICENSED_BUSINESS', 'INVESTIGATING',
        'Xu I', '13800000009', DATE_SUB(NOW(), INTERVAL 12 DAY), 'Xihu Night Market',
        'Reopened without permit after previous enforcement.', 2, 2,
        DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 0, DATE_SUB(NOW(), INTERVAL 12 DAY), NOW()),

    (110, 'PO-2026-0010', 'Construction material occupying lane', 'TRAFFIC_OBSTRUCTION', 'INVESTIGATING',
        'Guo J', '13800000010', DATE_SUB(NOW(), INTERVAL 9 DAY), 'High-tech Zone Main Rd',
        'Materials stored in non-construction section.', 3, 2,
        DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 1, DATE_SUB(NOW(), INTERVAL 9 DAY), NOW()),

    (111, 'PO-2026-0011', 'Weekend square speaker complaint', 'NOISE_COMPLAINT', 'ACCEPTED',
        'Ma K', '13800000011', DATE_SUB(NOW(), INTERVAL 2 DAY), 'West Lake Plaza',
        'Intermittent high-volume broadcast.', NULL, 1,
        DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), 0, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),

    (112, 'PO-2026-0012', 'Street promotion causing crowding', 'PUBLIC_DISTURBANCE', 'ARCHIVED',
        'Lin L', '13800000012', DATE_SUB(NOW(), INTERVAL 18 DAY), 'Hongdu Middle Ave',
        'Promotion team caused prolonged crowd congregation.', 2, 2,
        DATE_SUB(NOW(), INTERVAL 17 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 0, DATE_SUB(NOW(), INTERVAL 18 DAY), NOW())
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    type_code = VALUES(type_code),
    status = VALUES(status),
    handling_officer_id = VALUES(handling_officer_id),
    department_id = VALUES(department_id),
    acceptance_time = VALUES(acceptance_time),
    deadline_time = VALUES(deadline_time),
    is_overdue = VALUES(is_overdue),
    updated_at = NOW();

INSERT INTO case_processes (
    id, case_id, from_status, to_status, operator_id, operation_time, `comment`, ip_address
) VALUES
    (1001, 101, NULL, 'REGISTERED', 5, DATE_SUB(NOW(), INTERVAL 3 DAY), 'Case created', '127.0.0.1'),

    (1002, 102, NULL, 'REGISTERED', 5, DATE_SUB(NOW(), INTERVAL 5 DAY), 'Case created', '127.0.0.1'),
    (1003, 102, 'REGISTERED', 'ACCEPTED', 5, DATE_SUB(NOW(), INTERVAL 4 DAY), 'Case accepted', '127.0.0.1'),

    (1004, 103, NULL, 'REGISTERED', 5, DATE_SUB(NOW(), INTERVAL 8 DAY), 'Case created', '127.0.0.1'),
    (1005, 103, 'REGISTERED', 'ACCEPTED', 5, DATE_SUB(NOW(), INTERVAL 7 DAY), 'Case accepted', '127.0.0.1'),
    (1006, 103, 'ACCEPTED', 'INVESTIGATING', 2, DATE_SUB(NOW(), INTERVAL 6 DAY), 'Investigation started', '127.0.0.1'),

    (1007, 104, NULL, 'REGISTERED', 5, DATE_SUB(NOW(), INTERVAL 10 DAY), 'Case created', '127.0.0.1'),
    (1008, 104, 'REGISTERED', 'ACCEPTED', 5, DATE_SUB(NOW(), INTERVAL 9 DAY), 'Case accepted', '127.0.0.1'),
    (1009, 104, 'ACCEPTED', 'INVESTIGATING', 3, DATE_SUB(NOW(), INTERVAL 8 DAY), 'Investigation started', '127.0.0.1'),
    (1010, 104, 'INVESTIGATING', 'LEGAL_REVIEW', 3, DATE_SUB(NOW(), INTERVAL 7 DAY), 'Submitted to legal review', '127.0.0.1'),

    (1011, 105, NULL, 'REGISTERED', 5, DATE_SUB(NOW(), INTERVAL 11 DAY), 'Case created', '127.0.0.1'),
    (1012, 105, 'REGISTERED', 'ACCEPTED', 5, DATE_SUB(NOW(), INTERVAL 10 DAY), 'Case accepted', '127.0.0.1'),
    (1013, 105, 'ACCEPTED', 'INVESTIGATING', 2, DATE_SUB(NOW(), INTERVAL 9 DAY), 'Investigation started', '127.0.0.1'),
    (1014, 105, 'INVESTIGATING', 'LEGAL_REVIEW', 2, DATE_SUB(NOW(), INTERVAL 8 DAY), 'Submitted to legal review', '127.0.0.1'),
    (1015, 105, 'LEGAL_REVIEW', 'LEGAL_REVIEW', 4, DATE_SUB(NOW(), INTERVAL 7 DAY), 'Legal review approved', '127.0.0.1'),

    (1016, 106, NULL, 'REGISTERED', 5, DATE_SUB(NOW(), INTERVAL 14 DAY), 'Case created', '127.0.0.1'),
    (1017, 106, 'REGISTERED', 'ACCEPTED', 5, DATE_SUB(NOW(), INTERVAL 13 DAY), 'Case accepted', '127.0.0.1'),
    (1018, 106, 'ACCEPTED', 'INVESTIGATING', 3, DATE_SUB(NOW(), INTERVAL 12 DAY), 'Investigation started', '127.0.0.1'),
    (1019, 106, 'INVESTIGATING', 'LEGAL_REVIEW', 3, DATE_SUB(NOW(), INTERVAL 11 DAY), 'Submitted to legal review', '127.0.0.1'),
    (1020, 106, 'LEGAL_REVIEW', 'LEGAL_REVIEW', 4, DATE_SUB(NOW(), INTERVAL 10 DAY), 'Legal review approved', '127.0.0.1'),
    (1021, 106, 'LEGAL_REVIEW', 'DECIDED', 3, DATE_SUB(NOW(), INTERVAL 9 DAY), 'Decision saved', '127.0.0.1'),

    (1022, 107, NULL, 'REGISTERED', 5, DATE_SUB(NOW(), INTERVAL 16 DAY), 'Case created', '127.0.0.1'),
    (1023, 107, 'REGISTERED', 'ACCEPTED', 5, DATE_SUB(NOW(), INTERVAL 15 DAY), 'Case accepted', '127.0.0.1'),
    (1024, 107, 'ACCEPTED', 'INVESTIGATING', 2, DATE_SUB(NOW(), INTERVAL 14 DAY), 'Investigation started', '127.0.0.1'),
    (1025, 107, 'INVESTIGATING', 'LEGAL_REVIEW', 2, DATE_SUB(NOW(), INTERVAL 13 DAY), 'Submitted to legal review', '127.0.0.1'),
    (1026, 107, 'LEGAL_REVIEW', 'LEGAL_REVIEW', 4, DATE_SUB(NOW(), INTERVAL 12 DAY), 'Legal review approved', '127.0.0.1'),
    (1027, 107, 'LEGAL_REVIEW', 'DECIDED', 2, DATE_SUB(NOW(), INTERVAL 11 DAY), 'Decision saved', '127.0.0.1'),
    (1028, 107, 'DECIDED', 'EXECUTED', 2, DATE_SUB(NOW(), INTERVAL 10 DAY), 'Execution recorded', '127.0.0.1'),

    (1029, 108, NULL, 'REGISTERED', 5, DATE_SUB(NOW(), INTERVAL 20 DAY), 'Case created', '127.0.0.1'),
    (1030, 108, 'REGISTERED', 'ACCEPTED', 5, DATE_SUB(NOW(), INTERVAL 19 DAY), 'Case accepted', '127.0.0.1'),
    (1031, 108, 'ACCEPTED', 'INVESTIGATING', 3, DATE_SUB(NOW(), INTERVAL 18 DAY), 'Investigation started', '127.0.0.1'),
    (1032, 108, 'INVESTIGATING', 'LEGAL_REVIEW', 3, DATE_SUB(NOW(), INTERVAL 17 DAY), 'Submitted to legal review', '127.0.0.1'),
    (1033, 108, 'LEGAL_REVIEW', 'LEGAL_REVIEW', 4, DATE_SUB(NOW(), INTERVAL 16 DAY), 'Legal review approved', '127.0.0.1'),
    (1034, 108, 'LEGAL_REVIEW', 'DECIDED', 3, DATE_SUB(NOW(), INTERVAL 15 DAY), 'Decision saved', '127.0.0.1'),
    (1035, 108, 'DECIDED', 'EXECUTED', 3, DATE_SUB(NOW(), INTERVAL 14 DAY), 'Execution recorded', '127.0.0.1'),
    (1036, 108, 'EXECUTED', 'ARCHIVED', 5, DATE_SUB(NOW(), INTERVAL 13 DAY), 'Case archived', '127.0.0.1'),

    (1037, 109, NULL, 'REGISTERED', 5, DATE_SUB(NOW(), INTERVAL 12 DAY), 'Case created', '127.0.0.1'),
    (1038, 109, 'REGISTERED', 'ACCEPTED', 5, DATE_SUB(NOW(), INTERVAL 11 DAY), 'Case accepted', '127.0.0.1'),
    (1039, 109, 'ACCEPTED', 'INVESTIGATING', 2, DATE_SUB(NOW(), INTERVAL 10 DAY), 'Investigation started', '127.0.0.1'),
    (1040, 109, 'INVESTIGATING', 'LEGAL_REVIEW', 2, DATE_SUB(NOW(), INTERVAL 9 DAY), 'Submitted to legal review', '127.0.0.1'),
    (1041, 109, 'LEGAL_REVIEW', 'INVESTIGATING', 4, DATE_SUB(NOW(), INTERVAL 8 DAY), 'Legal review rejected, rework required', '127.0.0.1'),

    (1042, 110, NULL, 'REGISTERED', 5, DATE_SUB(NOW(), INTERVAL 9 DAY), 'Case created', '127.0.0.1'),
    (1043, 110, 'REGISTERED', 'ACCEPTED', 5, DATE_SUB(NOW(), INTERVAL 8 DAY), 'Case accepted', '127.0.0.1'),
    (1044, 110, 'ACCEPTED', 'INVESTIGATING', 3, DATE_SUB(NOW(), INTERVAL 7 DAY), 'Investigation started', '127.0.0.1'),

    (1045, 111, NULL, 'REGISTERED', 5, DATE_SUB(NOW(), INTERVAL 2 DAY), 'Case created', '127.0.0.1'),
    (1046, 111, 'REGISTERED', 'ACCEPTED', 5, DATE_SUB(NOW(), INTERVAL 1 DAY), 'Case accepted', '127.0.0.1'),

    (1047, 112, NULL, 'REGISTERED', 5, DATE_SUB(NOW(), INTERVAL 18 DAY), 'Case created', '127.0.0.1'),
    (1048, 112, 'REGISTERED', 'ACCEPTED', 5, DATE_SUB(NOW(), INTERVAL 17 DAY), 'Case accepted', '127.0.0.1'),
    (1049, 112, 'ACCEPTED', 'INVESTIGATING', 2, DATE_SUB(NOW(), INTERVAL 16 DAY), 'Investigation started', '127.0.0.1'),
    (1050, 112, 'INVESTIGATING', 'LEGAL_REVIEW', 2, DATE_SUB(NOW(), INTERVAL 15 DAY), 'Submitted to legal review', '127.0.0.1'),
    (1051, 112, 'LEGAL_REVIEW', 'LEGAL_REVIEW', 4, DATE_SUB(NOW(), INTERVAL 14 DAY), 'Legal review approved', '127.0.0.1'),
    (1052, 112, 'LEGAL_REVIEW', 'DECIDED', 2, DATE_SUB(NOW(), INTERVAL 13 DAY), 'Decision saved', '127.0.0.1'),
    (1053, 112, 'DECIDED', 'EXECUTED', 2, DATE_SUB(NOW(), INTERVAL 12 DAY), 'Execution recorded', '127.0.0.1'),
    (1054, 112, 'EXECUTED', 'ARCHIVED', 5, DATE_SUB(NOW(), INTERVAL 11 DAY), 'Case archived', '127.0.0.1')
ON DUPLICATE KEY UPDATE
    from_status = VALUES(from_status),
    to_status = VALUES(to_status),
    operator_id = VALUES(operator_id),
    operation_time = VALUES(operation_time),
    `comment` = VALUES(`comment`),
    ip_address = VALUES(ip_address);

INSERT INTO case_evidences (
    id, case_id, file_name, file_path, file_type, file_size, upload_user_id, description, uploaded_at
) VALUES
    (2001, 103, 'scene-photo-103-1.jpg', '/evidence/2026/103/scene-photo-1.jpg', 'image/jpeg', 245120, 2, 'Initial on-site photo', DATE_SUB(NOW(), INTERVAL 6 DAY)),
    (2002, 104, 'statement-104.pdf', '/evidence/2026/104/statement.pdf', 'application/pdf', 112640, 3, 'Witness statement', DATE_SUB(NOW(), INTERVAL 7 DAY)),
    (2003, 107, 'noise-meter-107.xlsx', '/evidence/2026/107/noise-meter.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 51200, 2, 'Noise level measurement records', DATE_SUB(NOW(), INTERVAL 12 DAY)),
    (2004, 108, 'bus-stop-video-108.mp4', '/evidence/2026/108/bus-stop-video.mp4', 'video/mp4', 10485760, 3, 'Video of blockage period', DATE_SUB(NOW(), INTERVAL 16 DAY)),
    (2005, 110, 'road-occupy-110.jpg', '/evidence/2026/110/road-occupy.jpg', 'image/jpeg', 192300, 3, 'Road occupation evidence', DATE_SUB(NOW(), INTERVAL 7 DAY))
ON DUPLICATE KEY UPDATE
    file_name = VALUES(file_name),
    file_path = VALUES(file_path),
    file_type = VALUES(file_type),
    file_size = VALUES(file_size),
    upload_user_id = VALUES(upload_user_id),
    description = VALUES(description),
    uploaded_at = VALUES(uploaded_at);

INSERT INTO case_legal_reviews (
    id, case_id, review_status, review_comment, reviewer_id, reviewed_at, created_at, updated_at
) VALUES
    (3001, 104, 'SUBMITTED', 'Materials submitted, pending legal decision.', 3, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY), NOW()),
    (3002, 105, 'APPROVED', 'Fact and evidence chain are complete.', 4, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY), NOW()),
    (3003, 106, 'APPROVED', 'Approved for administrative penalty decision.', 4, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY), NOW()),
    (3004, 107, 'APPROVED', 'Repeated violation confirmed, approve decision stage.', 4, DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY), NOW()),
    (3005, 108, 'APPROVED', 'Archived after execution; legal review passed.', 4, DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 17 DAY), NOW()),
    (3006, 109, 'REJECTED', 'Evidence insufficient, please supplement statements.', 4, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY), NOW()),
    (3007, 112, 'APPROVED', 'Promotion activity impact evidence is complete.', 4, DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY), NOW())
ON DUPLICATE KEY UPDATE
    review_status = VALUES(review_status),
    review_comment = VALUES(review_comment),
    reviewer_id = VALUES(reviewer_id),
    reviewed_at = VALUES(reviewed_at),
    updated_at = VALUES(updated_at);

INSERT INTO case_decisions (
    id, case_id, decision_result, decision_content, coercive_measure_code,
    decided_by, decided_at, created_at, updated_at
) VALUES
    (4001, 106, 'FINE', 'Impose administrative fine and written warning.', 'CM_WARN_FINE', 3, DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY), NOW()),
    (4002, 107, 'ORDER_RECTIFICATION', 'Order immediate business-hour adjustment and rectification.', 'CM_ORDER_RECT', 2, DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY), NOW()),
    (4003, 108, 'FINE', 'Fine for repeated assembly disturbance near bus stop.', 'CM_WARN_FINE', 3, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY), NOW()),
    (4004, 112, 'ORDER_RECTIFICATION', 'Rectify promotion setup and restore road order.', 'CM_ORDER_RECT', 2, DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY), NOW())
ON DUPLICATE KEY UPDATE
    decision_result = VALUES(decision_result),
    decision_content = VALUES(decision_content),
    coercive_measure_code = VALUES(coercive_measure_code),
    decided_by = VALUES(decided_by),
    decided_at = VALUES(decided_at),
    updated_at = VALUES(updated_at);

INSERT INTO case_executions (
    id, case_id, execution_result, execution_note, executed_by, executed_at, created_at, updated_at
) VALUES
    (5001, 107, 'COMPLETED', 'Rectification verified on-site.', 2, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),
    (5002, 108, 'COMPLETED', 'Fine collected and crowd dispersed.', 3, DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY), NOW()),
    (5003, 112, 'COMPLETED', 'Road order restored and recheck passed.', 2, DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY), NOW())
ON DUPLICATE KEY UPDATE
    execution_result = VALUES(execution_result),
    execution_note = VALUES(execution_note),
    executed_by = VALUES(executed_by),
    executed_at = VALUES(executed_at),
    updated_at = VALUES(updated_at);
