-- Add audit fields to tasks table for tracking who created/modified/deleted tasks
-- Note: Using BIGINT columns without foreign key constraints for audit logging
-- This allows audit data to be retained even if users are deleted

ALTER TABLE tasks
    ADD COLUMN created_by_id BIGINT,
    ADD COLUMN modified_by_id BIGINT,
    ADD COLUMN deleted_by_id BIGINT;

-- Create indexes for better query performance on audit fields
CREATE INDEX idx_tasks_created_by ON tasks(created_by_id);
CREATE INDEX idx_tasks_modified_by ON tasks(modified_by_id);
CREATE INDEX idx_tasks_deleted_by ON tasks(deleted_by_id);
