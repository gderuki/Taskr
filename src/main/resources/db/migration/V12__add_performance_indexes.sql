-- Add performance indexes for common query patterns

-- Tasks: filter by status (listing, filtering)
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);

-- Tasks: filter by priority (listing, filtering)
CREATE INDEX IF NOT EXISTS idx_tasks_priority ON tasks(priority);

-- Tasks: filter by assignee (user task lists)
CREATE INDEX IF NOT EXISTS idx_tasks_assignee_id ON tasks(assignee_id);

-- Tasks: filter by due date (upcoming tasks, overdue queries)
CREATE INDEX IF NOT EXISTS idx_tasks_due_date ON tasks(due_date);

-- Tasks: filter out soft-deleted (most queries exclude deleted)
CREATE INDEX IF NOT EXISTS idx_tasks_deleted_at ON tasks(deleted_at);

-- Tasks: common composite query (assignee + non-deleted tasks)
CREATE INDEX IF NOT EXISTS idx_tasks_assignee_deleted ON tasks(assignee_id, deleted_at) WHERE deleted_at IS NULL;

-- Tasks: common composite query (status + priority for filtering)
CREATE INDEX IF NOT EXISTS idx_tasks_status_priority ON tasks(status, priority);

-- Comments: get comments for a task (very common)
CREATE INDEX IF NOT EXISTS idx_comments_task_id ON comments(task_id);

-- Comments: filter out soft-deleted comments
CREATE INDEX IF NOT EXISTS idx_comments_deleted_at ON comments(deleted_at);

-- Comments: composite for active comments on a task
CREATE INDEX IF NOT EXISTS idx_comments_task_deleted ON comments(task_id, deleted_at) WHERE deleted_at IS NULL;

-- Attachments: get attachments for a task
CREATE INDEX IF NOT EXISTS idx_attachments_task_id ON attachments(task_id);

-- Attachments: filter out soft-deleted attachments
CREATE INDEX IF NOT EXISTS idx_attachments_deleted_at ON attachments(deleted_at);

-- RefreshTokens: lookup by token (authentication)
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens(token);

-- RefreshTokens: cleanup expired tokens, find user tokens
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- RefreshTokens: cleanup expired tokens efficiently
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expiry_date ON refresh_tokens(expiry_date);

-- Tags: lookup by name (tag search, autocomplete)
CREATE INDEX IF NOT EXISTS idx_tags_name ON tags(name);

-- Task-Tags join table: improve join performance (already has FK constraints but explicit indexes help)
CREATE INDEX IF NOT EXISTS idx_task_tags_task_id ON task_tags(task_id);
CREATE INDEX IF NOT EXISTS idx_task_tags_tag_id ON task_tags(tag_id);
