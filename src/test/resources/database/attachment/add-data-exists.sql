insert into projects (id, name, description, start_date, end_date, status) values (1, "projectName", "projectDescription", "2100-01-01", "2200-01-01", "INITIATED");
insert into users (id, user_name, password, email, first_name, last_name) values (2, "user", "password", "email@example.com", "first_name", "last_name");
insert into users_roles (user_id, role_id) values (2,3);
insert into tasks (id, name, description, priority, status, due_date, project_id, assignee_id) values (1, "taskName", "taskDescription", "MEDIUM", "IN_PROGRESS", "2150-01-01", 1, 2);
insert into attachments (id, task_id, dropbox_file_id, filename, upload_date) values (1, 1, "dropbox_file_id", "filename", "2145-01-01-00-00-00.000");
insert into attachments (id, task_id, dropbox_file_id, filename, upload_date) values (2, 1, "dropbox_file_id2", "filename2", "2145-01-01-00-00-00.000");
