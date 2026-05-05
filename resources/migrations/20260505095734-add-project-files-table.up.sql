CREATE TABLE project_files (
  id bigserial PRIMARY KEY,
  project_id bigint NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  original_filename text NOT NULL,
  stored_filename text NOT NULL,
  content_type text NOT NULL,
  storage_path text NOT NULL,
  file_size bigint,
  created_at timestamp NOT NULL DEFAULT now()
);
