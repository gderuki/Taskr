-- Create comments table
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    task_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,

    CONSTRAINT fk_comments_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_comments_task_id ON comments(task_id);
CREATE INDEX idx_comments_author_id ON comments(author_id);
CREATE INDEX idx_comments_deleted_at ON comments(deleted_at);
CREATE INDEX idx_comments_created_at ON comments(created_at DESC);
