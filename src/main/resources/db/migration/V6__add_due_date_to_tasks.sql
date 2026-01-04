-- Add due_date column to tasks table
ALTER TABLE tasks
ADD COLUMN due_date TIMESTAMP;

-- Create index for better query performance on due date filtering/sorting
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
