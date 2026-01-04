-- Create tags table
CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    color VARCHAR(7), -- Optional hex color code like #FF5733
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_tag_name CHECK (LENGTH(TRIM(name)) > 0)
);

-- Create task_tags junction table for many-to-many relationship
CREATE TABLE task_tags (
    task_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (task_id, tag_id),
    CONSTRAINT fk_task_tags_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_tags_name ON tags(name);
CREATE INDEX idx_task_tags_task_id ON task_tags(task_id);
CREATE INDEX idx_task_tags_tag_id ON task_tags(tag_id);

-- Insert some default tags
INSERT INTO tags (name, color) VALUES
    ('Bug', '#FF0000'),
    ('Feature', '#00FF00'),
    ('Enhancement', '#0000FF'),
    ('Documentation', '#FFA500'),
    ('Testing', '#800080'),
    ('Urgent', '#FF1493');
