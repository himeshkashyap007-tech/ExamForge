CREATE DATABASE IF NOT EXISTS examforge;
USE examforge;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('TEACHER', 'STUDENT', 'SUPER_ADMIN') NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE questions (
    question_id INT AUTO_INCREMENT PRIMARY KEY,
    question_text TEXT NOT NULL,
    subject VARCHAR(100) NOT NULL,
    unit VARCHAR(100) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    marks INT NOT NULL,
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL,
    question_type ENUM('MCQ', 'SHORT_ANSWER', 'LONG_ANSWER') NOT NULL,
    active_status BOOLEAN DEFAULT TRUE,
    option_a TEXT,
    option_b TEXT,
    option_c TEXT,
    option_d TEXT,
    correct_option VARCHAR(1),
    explanation TEXT,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE TABLE blueprints (
    blueprint_id INT AUTO_INCREMENT PRIMARY KEY,
    exam_name VARCHAR(200) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    total_marks INT NOT NULL,
    duration_minutes INT NOT NULL,
    number_of_questions INT NOT NULL,
    status ENUM('DRAFT', 'VALID', 'INVALID', 'INACTIVE') DEFAULT 'DRAFT',
    previous_status ENUM('DRAFT', 'VALID', 'INVALID') DEFAULT NULL,
    exposure_policy_type ENUM('SOFT_AVOID', 'STRICT_BLOCK') DEFAULT 'SOFT_AVOID',
    exposure_window INT DEFAULT 3,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE TABLE blueprint_sections (
    section_id INT AUTO_INCREMENT PRIMARY KEY,
    blueprint_id INT NOT NULL,
    section_name VARCHAR(100) NOT NULL,
    question_count INT NOT NULL,
    total_marks INT NOT NULL,
    question_type_filter VARCHAR(50),
    difficulty_filter VARCHAR(50),
    sort_order INT DEFAULT 0,
    FOREIGN KEY (blueprint_id) REFERENCES blueprints(blueprint_id) ON DELETE CASCADE
);

CREATE TABLE blueprint_constraints (
    constraint_id INT AUTO_INCREMENT PRIMARY KEY,
    blueprint_id INT NOT NULL,
    category ENUM('UNIT', 'DIFFICULTY', 'QUESTION_TYPE', 'SECTION', 'EXPOSURE') NOT NULL,
    basis ENUM('MARKS', 'COUNT', 'PERCENTAGE') NOT NULL,
    target VARCHAR(100) NOT NULL,
    required_value DOUBLE NOT NULL,
    tolerance DOUBLE DEFAULT 0,
    priority ENUM('HARD', 'SOFT') DEFAULT 'HARD',
    FOREIGN KEY (blueprint_id) REFERENCES blueprints(blueprint_id) ON DELETE CASCADE
);

CREATE TABLE papers (
    paper_id INT AUTO_INCREMENT PRIMARY KEY,
    blueprint_id INT NOT NULL,
    exam_name VARCHAR(200) NOT NULL,
    status ENUM('DRAFT', 'VALIDATED', 'FINALIZED') DEFAULT 'DRAFT',
    total_marks INT NOT NULL,
    question_count INT NOT NULL,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (blueprint_id) REFERENCES blueprints(blueprint_id),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE TABLE paper_questions (
    paper_question_id INT AUTO_INCREMENT PRIMARY KEY,
    paper_id INT NOT NULL,
    question_id INT NOT NULL,
    section_index INT NOT NULL,
    sort_order INT DEFAULT 0,
    FOREIGN KEY (paper_id) REFERENCES papers(paper_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
);

CREATE TABLE question_exposure (
    exposure_id INT AUTO_INCREMENT PRIMARY KEY,
    question_id INT UNIQUE NOT NULL,
    total_official_usage INT DEFAULT 0,
    last_official_use TIMESTAMP NULL,
    recent_usage_count INT DEFAULT 0,
    exposure_score DOUBLE DEFAULT 0,
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
);

CREATE TABLE examination_records (
    exam_id INT AUTO_INCREMENT PRIMARY KEY,
    paper_id INT NOT NULL,
    exam_name VARCHAR(200) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    total_marks INT NOT NULL,
    finalized_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    finalized_by INT,
    FOREIGN KEY (paper_id) REFERENCES papers(paper_id),
    FOREIGN KEY (finalized_by) REFERENCES users(user_id)
);

CREATE TABLE student_attempts (
    attempt_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    practice_type VARCHAR(50) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    total_questions INT NOT NULL,
    correct_answers INT DEFAULT 0,
    total_marks INT NOT NULL,
    marks_obtained INT DEFAULT 0,
    time_spent_seconds INT DEFAULT 0,
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(user_id)
);

CREATE TABLE student_responses (
    response_id INT AUTO_INCREMENT PRIMARY KEY,
    attempt_id INT NOT NULL,
    question_id INT NOT NULL,
    selected_option VARCHAR(1),
    is_correct BOOLEAN DEFAULT FALSE,
    marks_obtained INT DEFAULT 0,
    FOREIGN KEY (attempt_id) REFERENCES student_attempts(attempt_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
);

-- Insert default Super Admin account (password: admin123)
INSERT INTO users (username, password_hash, full_name, role)
VALUES ('admin', 'admin123', 'System Administrator', 'SUPER_ADMIN');

-- Insert sample student account (password: student123)
INSERT INTO users (username, password_hash, full_name, role)
VALUES ('student1', 'student123', 'Sample Student', 'STUDENT');
