-- Add assignee_id column to tasks table
ALTER TABLE tasks
ADD COLUMN assignee_id BIGINT;

-- Add foreign key constraint
ALTER TABLE tasks
ADD CONSTRAINT fk_tasks_assignee
FOREIGN KEY (assignee_id) REFERENCES users(id);

-- Create index for better query performance
CREATE INDEX idx_tasks_assignee_id ON tasks(assignee_id);
