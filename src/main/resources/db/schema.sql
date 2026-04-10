-- Core schema for Public Order Case Management System
-- MySQL 8+, InnoDB, utf8mb4

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS case_executions;
DROP TABLE IF EXISTS case_decisions;
DROP TABLE IF EXISTS case_legal_reviews;
DROP TABLE IF EXISTS case_evidences;
DROP TABLE IF EXISTS case_processes;
DROP TABLE IF EXISTS case_workflow_action_logs;
DROP TABLE IF EXISTS case_workflow_tasks;
DROP TABLE IF EXISTS case_workflow_instances;
DROP TABLE IF EXISTS workflow_nodes;
DROP TABLE IF EXISTS workflow_definitions;
DROP TABLE IF EXISTS cases;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS login_logs;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS dict_case_types;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS roles;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE roles (
    code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (code),
    UNIQUE KEY uk_roles_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE departments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    parent_id BIGINT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_departments_name (name),
    KEY idx_departments_parent_id (parent_id),
    CONSTRAINT fk_departments_parent
        FOREIGN KEY (parent_id) REFERENCES departments (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE dict_case_types (
    code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (code),
    KEY idx_dict_case_types_active_order (is_active, sort_order, code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(64) NOT NULL,
    role VARCHAR(64) NOT NULL,
    department_id BIGINT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    last_login DATETIME NULL,
    login_attempts INT NOT NULL DEFAULT 0,
    locked_until DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_name (name),
    KEY idx_users_role (role),
    KEY idx_users_department_id (department_id),
    KEY idx_users_active (is_active),
    CONSTRAINT fk_users_role
        FOREIGN KEY (role) REFERENCES roles (code)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_users_department
        FOREIGN KEY (department_id) REFERENCES departments (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_refresh_tokens_hash_revoked (token_hash, revoked),
    KEY idx_refresh_tokens_user_revoked (user_id, revoked),
    KEY idx_refresh_tokens_expires_at (expires_at),
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE login_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NULL,
    name VARCHAR(64) NULL,
    ip VARCHAR(45) NULL,
    login_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    login_result TINYINT NOT NULL,
    device_type VARCHAR(64) NULL,
    browser VARCHAR(128) NULL,
    os VARCHAR(128) NULL,
    PRIMARY KEY (id),
    KEY idx_login_logs_user_time (user_id, login_time),
    KEY idx_login_logs_name_time (name, login_time),
    CONSTRAINT fk_login_logs_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE cases (
    id BIGINT NOT NULL AUTO_INCREMENT,
    case_number VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    type_code VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    reporter_name VARCHAR(64) NOT NULL,
    reporter_contact VARCHAR(64) NULL,
    incident_time DATETIME NULL,
    incident_location VARCHAR(255) NULL,
    brief_description TEXT NULL,
    handling_officer_id BIGINT NULL,
    department_id BIGINT NULL,
    acceptance_time DATETIME NULL,
    deadline_time DATETIME NULL,
    is_overdue TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cases_case_number (case_number),
    KEY idx_cases_status (status),
    KEY idx_cases_type_code (type_code),
    KEY idx_cases_department_id (department_id),
    KEY idx_cases_handling_officer_id (handling_officer_id),
    KEY idx_cases_deadline_status (deadline_time, status),
    KEY idx_cases_overdue_status (is_overdue, status),
    KEY idx_cases_created_at (created_at),
    CONSTRAINT fk_cases_type_code
        FOREIGN KEY (type_code) REFERENCES dict_case_types (code)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_cases_officer
        FOREIGN KEY (handling_officer_id) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_cases_department
        FOREIGN KEY (department_id) REFERENCES departments (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE case_processes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    case_id BIGINT NOT NULL,
    from_status VARCHAR(32) NULL,
    to_status VARCHAR(32) NOT NULL,
    operator_id BIGINT NULL,
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `comment` VARCHAR(500) NULL,
    ip_address VARCHAR(45) NULL,
    PRIMARY KEY (id),
    KEY idx_case_processes_case_id_id (case_id, id),
    KEY idx_case_processes_case_to_status_id (case_id, to_status, id),
    KEY idx_case_processes_operator_id (operator_id),
    CONSTRAINT fk_case_processes_case
        FOREIGN KEY (case_id) REFERENCES cases (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_case_processes_operator
        FOREIGN KEY (operator_id) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE case_evidences (
    id BIGINT NOT NULL AUTO_INCREMENT,
    case_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    file_type VARCHAR(128) NULL,
    file_size BIGINT NULL,
    upload_user_id BIGINT NULL,
    description VARCHAR(500) NULL,
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_case_evidences_case_id_id (case_id, id),
    KEY idx_case_evidences_upload_user_id (upload_user_id),
    CONSTRAINT fk_case_evidences_case
        FOREIGN KEY (case_id) REFERENCES cases (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_case_evidences_upload_user
        FOREIGN KEY (upload_user_id) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE case_legal_reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    case_id BIGINT NOT NULL,
    review_status VARCHAR(32) NOT NULL,
    review_comment VARCHAR(1000) NULL,
    reviewer_id BIGINT NULL,
    reviewed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_case_legal_reviews_case_id (case_id),
    KEY idx_case_legal_reviews_status (review_status),
    KEY idx_case_legal_reviews_reviewed_at (reviewed_at),
    CONSTRAINT fk_case_legal_reviews_case
        FOREIGN KEY (case_id) REFERENCES cases (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_case_legal_reviews_reviewer
        FOREIGN KEY (reviewer_id) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE case_decisions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    case_id BIGINT NOT NULL,
    decision_result VARCHAR(64) NOT NULL,
    decision_content VARCHAR(2000) NULL,
    coercive_measure_code VARCHAR(64) NULL,
    decided_by BIGINT NULL,
    decided_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_case_decisions_case_id (case_id),
    KEY idx_case_decisions_decided_at (decided_at),
    KEY idx_case_decisions_decided_by (decided_by),
    CONSTRAINT fk_case_decisions_case
        FOREIGN KEY (case_id) REFERENCES cases (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_case_decisions_decided_by
        FOREIGN KEY (decided_by) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE case_executions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    case_id BIGINT NOT NULL,
    execution_result VARCHAR(64) NOT NULL,
    execution_note VARCHAR(1000) NULL,
    executed_by BIGINT NULL,
    executed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_case_executions_case_id (case_id),
    KEY idx_case_executions_executed_at (executed_at),
    KEY idx_case_executions_executed_by (executed_by),
    CONSTRAINT fk_case_executions_case
        FOREIGN KEY (case_id) REFERENCES cases (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_case_executions_executed_by
        FOREIGN KEY (executed_by) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE workflow_definitions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    flow_type VARCHAR(64) NOT NULL,
    version INT NOT NULL,
    name VARCHAR(128) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_workflow_definitions_flow_version (flow_type, version),
    KEY idx_workflow_definitions_flow_active (flow_type, is_active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE workflow_nodes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    workflow_definition_id BIGINT NOT NULL,
    node_key VARCHAR(64) NOT NULL,
    node_name VARCHAR(128) NOT NULL,
    node_order INT NOT NULL,
    role_code VARCHAR(64) NOT NULL,
    decision_mode VARCHAR(32) NOT NULL DEFAULT 'ANY_ONE',
    PRIMARY KEY (id),
    UNIQUE KEY uk_workflow_nodes_def_node (workflow_definition_id, node_key),
    UNIQUE KEY uk_workflow_nodes_def_order (workflow_definition_id, node_order),
    KEY idx_workflow_nodes_role (role_code),
    CONSTRAINT fk_workflow_nodes_definition
        FOREIGN KEY (workflow_definition_id) REFERENCES workflow_definitions (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_workflow_nodes_role
        FOREIGN KEY (role_code) REFERENCES roles (code)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE case_workflow_instances (
    id BIGINT NOT NULL AUTO_INCREMENT,
    case_id BIGINT NOT NULL,
    flow_type VARCHAR(64) NOT NULL,
    flow_version INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    current_node_key VARCHAR(64) NULL,
    started_by BIGINT NULL,
    started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_by BIGINT NULL,
    finished_at DATETIME NULL,
    snapshot_json JSON NULL,
    PRIMARY KEY (id),
    KEY idx_case_workflow_instances_case (case_id),
    KEY idx_case_workflow_instances_case_flow_status (case_id, flow_type, status),
    KEY idx_case_workflow_instances_started_by (started_by),
    KEY idx_case_workflow_instances_finished_by (finished_by),
    CONSTRAINT fk_case_workflow_instances_case
        FOREIGN KEY (case_id) REFERENCES cases (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_case_workflow_instances_started_by
        FOREIGN KEY (started_by) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_case_workflow_instances_finished_by
        FOREIGN KEY (finished_by) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE case_workflow_tasks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    instance_id BIGINT NOT NULL,
    node_key VARCHAR(64) NOT NULL,
    round_no INT NOT NULL DEFAULT 1,
    assignee_role VARCHAR(64) NOT NULL,
    assignee_user_id BIGINT NULL,
    status VARCHAR(32) NOT NULL,
    comment VARCHAR(1000) NULL,
    acted_by BIGINT NULL,
    acted_at DATETIME NULL,
    due_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_case_workflow_tasks_instance_node_round (instance_id, node_key, round_no),
    KEY idx_case_workflow_tasks_status_role (status, assignee_role),
    KEY idx_case_workflow_tasks_assignee_user (assignee_user_id),
    KEY idx_case_workflow_tasks_acted_by (acted_by),
    CONSTRAINT fk_case_workflow_tasks_instance
        FOREIGN KEY (instance_id) REFERENCES case_workflow_instances (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_case_workflow_tasks_assignee_role
        FOREIGN KEY (assignee_role) REFERENCES roles (code)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_case_workflow_tasks_assignee_user
        FOREIGN KEY (assignee_user_id) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_case_workflow_tasks_acted_by
        FOREIGN KEY (acted_by) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE case_workflow_action_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    instance_id BIGINT NOT NULL,
    task_id BIGINT NULL,
    action_type VARCHAR(32) NOT NULL,
    actor_id BIGINT NULL,
    actor_name VARCHAR(64) NULL,
    actor_role VARCHAR(64) NULL,
    comment VARCHAR(1000) NULL,
    payload_json JSON NULL,
    request_id VARCHAR(128) NULL,
    ip VARCHAR(45) NULL,
    user_agent VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_case_workflow_action_logs_request_id (request_id),
    KEY idx_case_workflow_action_logs_instance (instance_id),
    KEY idx_case_workflow_action_logs_task (task_id),
    KEY idx_case_workflow_action_logs_actor (actor_id),
    CONSTRAINT fk_case_workflow_action_logs_instance
        FOREIGN KEY (instance_id) REFERENCES case_workflow_instances (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_case_workflow_action_logs_task
        FOREIGN KEY (task_id) REFERENCES case_workflow_tasks (id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_case_workflow_action_logs_actor
        FOREIGN KEY (actor_id) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Minimal seed data
INSERT INTO roles (code, name, sort_order, is_active) VALUES
    ('admin', 'Administrator', 1, 1),
    ('supervisor', 'Supervisor', 2, 1),
    ('legal_officer', 'Legal Officer', 3, 1),
    ('police_officer', 'Police Officer', 4, 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    sort_order = VALUES(sort_order),
    is_active = VALUES(is_active);

INSERT INTO departments (id, name, parent_id, is_active) VALUES
    (1, 'Public Order Brigade', NULL, 1),
    (2, 'Urban Patrol Unit', 1, 1),
    (3, 'Legal Affairs Unit', 1, 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    parent_id = VALUES(parent_id),
    is_active = VALUES(is_active);

INSERT INTO dict_case_types (code, name, sort_order, is_active) VALUES
    ('PUBLIC_DISTURBANCE', 'Public Disturbance', 1, 1),
    ('TRAFFIC_OBSTRUCTION', 'Traffic Obstruction', 2, 1),
    ('UNLICENSED_BUSINESS', 'Unlicensed Business', 3, 1),
    ('NOISE_COMPLAINT', 'Noise Complaint', 4, 1),
    ('ILLEGAL_ASSEMBLY', 'Illegal Assembly', 5, 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    sort_order = VALUES(sort_order),
    is_active = VALUES(is_active);

-- Default admin password (change after first login): Admin@123456
-- BCrypt hash generated once and suitable for Spring Security PasswordEncoder
INSERT INTO users (
    id, password, name, role, department_id, is_active,
    last_login, login_attempts, locked_until, created_at, updated_at
) VALUES (
    1,
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjQf8M2MvmP0xSg6u40qCMgfHdCqkfS',
    'admin',
    'admin',
    1,
    1,
    NULL,
    0,
    NULL,
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    role = VALUES(role),
    department_id = VALUES(department_id),
    is_active = VALUES(is_active),
    updated_at = NOW();

INSERT INTO workflow_definitions (id, flow_type, version, name, is_active) VALUES
    (1001, 'ACCEPTANCE_REVIEW', 1, 'Acceptance Review Workflow', 1),
    (1002, 'FILING_REVIEW', 1, 'Filing Review Workflow', 1),
    (1003, 'LEGAL_AUDIT_REVIEW', 1, 'Legal Audit Workflow', 1),
    (1004, 'DECISION_REVIEW', 1, 'Decision Review Workflow', 1),
    (1005, 'EXECUTION_REVIEW', 1, 'Execution Review Workflow', 1),
    (1006, 'ARCHIVE_REVIEW', 1, 'Archive Review Workflow', 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    is_active = VALUES(is_active);

INSERT INTO workflow_nodes (
    id, workflow_definition_id, node_key, node_name, node_order, role_code, decision_mode
) VALUES
    (1101, 1001, 'SUPERVISOR_APPROVAL', 'Supervisor Acceptance Approval', 1, 'supervisor', 'ANY_ONE'),

    (1201, 1002, 'LEGAL_REVIEW', 'Legal Filing Review', 1, 'legal_officer', 'ANY_ONE'),
    (1202, 1002, 'SUPERVISOR_APPROVAL', 'Supervisor Filing Approval', 2, 'supervisor', 'ANY_ONE'),

    (1301, 1003, 'LEGAL_REVIEW', 'Legal Audit Review', 1, 'legal_officer', 'ANY_ONE'),
    (1302, 1003, 'SUPERVISOR_APPROVAL', 'Supervisor Legal Audit Approval', 2, 'supervisor', 'ANY_ONE'),

    (1401, 1004, 'LEGAL_REVIEW', 'Legal Decision Review', 1, 'legal_officer', 'ANY_ONE'),
    (1402, 1004, 'SUPERVISOR_APPROVAL', 'Supervisor Decision Approval', 2, 'supervisor', 'ANY_ONE'),

    (1501, 1005, 'LEGAL_REVIEW', 'Legal Execution Review', 1, 'legal_officer', 'ANY_ONE'),
    (1502, 1005, 'SUPERVISOR_APPROVAL', 'Supervisor Execution Approval', 2, 'supervisor', 'ANY_ONE'),

    (1601, 1006, 'LEGAL_REVIEW', 'Legal Archive Review', 1, 'legal_officer', 'ANY_ONE'),
    (1602, 1006, 'SUPERVISOR_APPROVAL', 'Supervisor Archive Approval', 2, 'supervisor', 'ANY_ONE')
ON DUPLICATE KEY UPDATE
    node_name = VALUES(node_name),
    node_order = VALUES(node_order),
    role_code = VALUES(role_code),
    decision_mode = VALUES(decision_mode);

