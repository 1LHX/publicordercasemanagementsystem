create table if not exists roles (
    code varchar(50) primary key,
    name varchar(50) not null
);

create table if not exists departments (
    id bigint primary key auto_increment,
    name varchar(100) not null unique
);

create table if not exists users (
    id bigint primary key auto_increment,
    name varchar(50) not null unique,
    password varchar(100) not null,
    role varchar(50) not null,
    department_id bigint,
    is_active tinyint(1) not null default 1,
    last_login datetime null,
    login_attempts int not null default 0,
    locked_until datetime null,
    created_at datetime not null,
    updated_at datetime not null,
    constraint fk_users_role foreign key (role) references roles(code),
    constraint fk_users_department foreign key (department_id) references departments(id)
);

create table if not exists refresh_tokens (
    id bigint primary key auto_increment,
    user_id bigint not null,
    token_hash char(64) not null,
    expires_at datetime not null,
    revoked tinyint(1) not null default 0,
    created_at datetime not null,
    last_used_at datetime null,
    index idx_refresh_token_hash (token_hash),
    constraint fk_refresh_tokens_user foreign key (user_id) references users(id)
);

create table if not exists login_logs (
    id bigint primary key auto_increment,
    user_id bigint null,
    name varchar(50) null,
    ip varchar(64) null,
    login_time datetime not null,
    login_result tinyint not null,
    device_type varchar(50) null,
    browser varchar(255) null,
    os varchar(255) null,
    constraint fk_login_logs_user foreign key (user_id) references users(id)
);

insert into roles (code, name) values
    ('Police Station Chief', '所长'),
    ('Legal Reviewer', '法制审核员'),
    ('Case Administrator', '案件管理员'),
    ('police_officer', '办案民警'),
    ('System Administrator', '系统管理员')
on duplicate key update name = values(name);

insert into departments (name) values
    ('刑侦支队'),
    ('治安支队'),
    ('指挥中心'),
    ('法制科')
on duplicate key update name = values(name);

-- 辅助字典表：案件类型
create table if not exists dict_case_types (
    code varchar(50) primary key,
    name varchar(100) not null unique,
    sort_order int not null default 0,
    is_active tinyint(1) not null default 1
);

-- 辅助字典表：强制措施（可选）
create table if not exists dict_coercive_measures (
    code varchar(50) primary key,
    name varchar(100) not null unique,
    sort_order int not null default 0,
    is_active tinyint(1) not null default 1
);

-- 核心业务表：案件主表
create table if not exists cases (
    id bigint primary key auto_increment,
    case_number varchar(50) not null unique,
    title varchar(255) not null,
    type_code varchar(50) not null,
    status varchar(30) not null,
    reporter_name varchar(50) null,
    reporter_contact varchar(100) null,
    incident_time datetime null,
    incident_location varchar(255) null,
    brief_description text null,
    handling_officer_id bigint null,
    department_id bigint null,
    acceptance_time datetime null,
    deadline_time datetime null,
    is_overdue tinyint(1) not null default 0,
    created_at datetime not null,
    updated_at datetime not null,
    index idx_cases_status (status),
    index idx_cases_type_code (type_code),
    index idx_cases_officer (handling_officer_id),
    index idx_cases_department (department_id),
    index idx_cases_deadline (deadline_time),
    constraint fk_cases_officer foreign key (handling_officer_id) references users(id),
    constraint fk_cases_department foreign key (department_id) references departments(id),
    constraint fk_cases_type foreign key (type_code) references dict_case_types(code)
);

-- 流程流转表：案件历史记录
create table if not exists case_processes (
    id bigint primary key auto_increment,
    case_id bigint not null,
    from_status varchar(30) null,
    to_status varchar(30) not null,
    operator_id bigint not null,
    operation_time datetime not null,
    comment varchar(500) null,
    ip_address varchar(64) null,
    index idx_case_processes_case_id (case_id),
    index idx_case_processes_operator (operator_id),
    index idx_case_processes_time (operation_time),
    constraint fk_case_processes_case foreign key (case_id) references cases(id),
    constraint fk_case_processes_operator foreign key (operator_id) references users(id)
);

-- 证据材料表
create table if not exists case_evidences (
    id bigint primary key auto_increment,
    case_id bigint not null,
    file_name varchar(255) not null,
    file_path varchar(500) not null,
    file_type varchar(100) null,
    file_size bigint null,
    upload_user_id bigint not null,
    description varchar(500) null,
    uploaded_at datetime not null,
    index idx_case_evidences_case_id (case_id),
    index idx_case_evidences_uploader (upload_user_id),
    index idx_case_evidences_uploaded_at (uploaded_at),
    constraint fk_case_evidences_case foreign key (case_id) references cases(id),
    constraint fk_case_evidences_uploader foreign key (upload_user_id) references users(id)
);

-- 初始化案件类型
insert into dict_case_types (code, name, sort_order, is_active) values
    ('THEFT', '盗窃', 1, 1),
    ('FIGHT', '打架斗殴', 2, 1),
    ('FRAUD', '诈骗', 3, 1),
    ('PUBLIC_ORDER', '治安案件', 4, 1),
    ('CRIMINAL', '刑事案件', 5, 1),
    ('DISPUTE', '纠纷', 6, 1)
on duplicate key update
    name = values(name),
    sort_order = values(sort_order),
    is_active = values(is_active);

-- 初始化强制措施
insert into dict_coercive_measures (code, name, sort_order, is_active) values
    ('SUMMONS', '传唤', 1, 1),
    ('DETENTION', '拘留', 2, 1),
    ('BAIL', '取保候审', 3, 1),
    ('MONITORING', '监视居住', 4, 1)
on duplicate key update
    name = values(name),
    sort_order = values(sort_order),
    is_active = values(is_active);
