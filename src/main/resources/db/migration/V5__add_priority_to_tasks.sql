-- Add priority column to tasks table
ALTER TABLE tasks
ADD COLUMN priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM';

-- Create index for better query performance on priority filtering/sorting
CREATE INDEX idx_tasks_priority ON tasks(priority);
