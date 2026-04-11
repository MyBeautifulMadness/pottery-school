DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'role_enum') THEN
        CREATE TYPE role_enum AS ENUM ('STUDENT', 'TEACHER');
    END IF;
END$$;

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role role_enum NOT NULL
);

CREATE TABLE IF NOT EXISTS profiles (
    user_id UUID PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    about TEXT,
    CONSTRAINT fk_profiles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

INSERT INTO users (id, email, password, role) VALUES
('11111111-1111-1111-1111-111111111111', 'string', 'string', 'STUDENT'),
('22222222-2222-2222-2222-222222222222', 'student1@a.com', 'password', 'STUDENT'),
('33333333-3333-3333-3333-333333333333', 'student2@a.com', 'password', 'STUDENT'),
('44444444-4444-4444-4444-444444444444', 'teacher1@a.com', 'password', 'TEACHER'),
('55555555-5555-5555-5555-555555555555', 'teacher2@a.com', 'password', 'TEACHER')
ON CONFLICT DO NOTHING;

INSERT INTO profiles (user_id, full_name, about) VALUES
('11111111-1111-1111-1111-111111111111', 'Student One', '1'),
('22222222-2222-2222-2222-222222222222', 'Student Two', '2'),
('33333333-3333-3333-3333-333333333333', 'Student Three', '3'),
('44444444-4444-4444-4444-444444444444', 'Teacher One', '4'),
('55555555-5555-5555-5555-555555555555', 'Teacher Two', '5')
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS solutions (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL,
    student_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    text TEXT,
    video_url VARCHAR(255),
    attachment_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    submitted_at TIMESTAMP,
    CONSTRAINT fk_solutions_post
        FOREIGN KEY (post_id)
        REFERENCES posts(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_solutions_student
        FOREIGN KEY (student_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT uk_solution_post_student UNIQUE (post_id, student_id)
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_mode_enum') THEN
        CREATE TYPE task_mode_enum AS ENUM ('SOLO', 'TEAM');
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'team_distribution_type_enum') THEN
        CREATE TYPE team_distribution_type_enum AS ENUM ('MANUAL', 'RANDOM', 'SELF_SELECTION');
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'priority_solution_enum') THEN
        CREATE TYPE priority_solution_enum AS ENUM ('CAPITAIN', 'LAST', 'FIRST', 'VOTING');
    END IF;
END$$;

CREATE TABLE IF NOT EXISTS tasks (
    id UUID PRIMARY KEY,
    description TEXT,
    deadline TIMESTAMP,
    mode task_mode_enum NOT NULL,
    team_distribution_type team_distribution_type_enum,
    formation_deadline TIMESTAMP,
    min_teams_count INTEGER,
    max_teams_count INTEGER,
    min_members_per_team INTEGER,
    max_members_per_team INTEGER,
    priority_solution priority_solution_enum,
    selected_solution_id UUID,
    post_id UUID NOT NULL UNIQUE,
    CONSTRAINT fk_tasks_post
        FOREIGN KEY (post_id)
        REFERENCES posts(id)
        ON DELETE CASCADE
);