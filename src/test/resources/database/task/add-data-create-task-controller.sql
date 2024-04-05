insert into users (id, user_name, password, email, first_name, last_name) values (2, "username", "password", "email@example.com", "firstName", "lastName");
insert into users_roles (user_id, role_id) values (2,3);
insert into users_roles (user_id, role_id) values (2,2);
insert into projects (id, name, description, start_date, end_date, status) values (1, "projectName", "projectDescription", "2100-01-01", "2200-01-01", "IN_PROGRESS");
