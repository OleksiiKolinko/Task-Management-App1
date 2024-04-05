# task-management-app
## Introduction
This application helps organise work on projects, make tasks on projects, moniculates tasks, comments tasks, upload to application and download from application files which need for tasks.  
## The technologies and tools used
* Java 17
* Maven
* Mockito
* Mapstruct
* MySql 8
* Liquibase
* Hibernate
* Spring Boot
* Spring Security
* Spring Data JPA
* Lombok
* Docker
* Swagger
* Dropbox API
* JavaMailSender
## Endpoints
### Auth controller
* POST: /api/auth/register - allows to register new users. Email and username is unique. An example:
```json
{
  "username": "user",
  "password": "12345678",
  "repeatPassword": "12345678",
  "email": "email@example.com",
  "firstName": "firstName",
  "lastName": "lastName"
}
```
* POST: /api/auth/login - Authenticates a user and returns JWT token. An example:
```json
{
  "username": "user",
  "password": "12345678"
}
```
### User controller
* PATCH: /api/users//{userId}/role - After register the user has role WITHOUT_ROLE, and the admin receive message about new registered user, and only admin can put roles which needs for particular user. After admin updated roles for user, the user receive message about it. There are four roles: ROLE_ADMIN, ROLE_MANAGER, ROLE_USER, WITHOUT_ROLE. An example:
```json
{
  "roles": [
    "ROLE_MANAGER", "ROLE_USER"
  ]
}
```
* GET: /api/users/me - Showing profile info authenticated user.
* PATCH: /api/users/me - Update profile info authenticated user. An example:
```json
{
  "username": "user",
  "password": "12345678",
  "repeatPassword": "12345678",
  "email": "example@gmail.com",
  "firstName": "firstName",
  "lastName": "lastName"
}
```
### Project controller
* POST: /api/projects - Create new project. Name - is uniqure. Default status is INITIATED. It allowed only for users with ROLE_MANAGER. An example:
```json
{
  "name": "projectName",
  "description": "projectDescription",
  "startDate": "2024-03-20",
  "endDate": "2024-05-20"
}
```
* GET: /api/projects - Get a list of all available projects. It allowed for users with ROLE_USER and ROLE_MANAGER.
* GET: /api/projects/{projectId} - Get a project by id. It allowed for users with ROLE_USER and ROLE_MANAGER.
* PATCH: /api/projects/{projectId} - Update project by id of that project. There are three statuses INITIATED, IN_PROGRESS, COMPLETED. It allowed only for users with ROLE_MANAGER. An example:
```json
{
  "name": "projectName",
  "description": "projectDescription",
  "startDate": "2024-03-21",
  "endDate": "2024-08-22",
  "status": "IN_PROGRESS"
}
```
* DELETE: /api/projects/{projectId} - Delete project by id of that project. By using soft delete concept. And delete all tasks for this project. Comments and attachments for these tasks. If these tasks added to particular labels, they remove from these labels. Assignees of these tasks receive message about it. It allowed only for users with ROLE_MANAGER.
### Task controller
* POST: /api/tasks - Create new task. Name - is uniqure. There are three priorities LOW, MEDIUM, HIGH and three statuses NOT_STARTED, IN_PROGRESS, COMPLETED. After created the assignee receives message about it. It allowed only for users with ROLE_MANAGER. An example:
```json
{
  "name": "nameTask",
  "description": "descriptionTask",
  "priority": "LOW",
  "status": "NOT_STARTED",
  "dueDate": "2024-03-24",
  "project": 3,
  "assignee": 2
}
```
* GET: /api/tasks?page={0}&size={10}&projectIds={1}&projectNames={projectName}&taskIds={1}&names={taskName}&assigneeIds={2}&assigneeNames={user} - Get a list of all available tasks. There are filters. It allowed for users with ROLE_USER and ROLE_MANAGER.
* GET: /api/tasks/{taskId} - Get a task by id. It allowed for users with ROLE_USER and ROLE_MANAGER.
* PATCH: /api/tasks/{taskId} - Update task by id of that task. By using the same body as in case create new task. After updated the assignee receives message about it. It allowed only for users with ROLE_MANAGER.
* DELETE: /api/tasks/{taskId} - Delete task by id of that task. By using soft delete concept. And delete all comments and attachments for this task. If this task added to particular label, they remove from this label. After deleted the assignee receives message about it. It allowed only for users with ROLE_MANAGER.
### Attachment controller
* POST: /api/attachments - Upload an attachment to a task. The file gets uploaded to Dropbox and application store the Dropbox File ID in database. The filename must be unique. The assignee or managers receive messages about it, it depend of who uploaded file. The user with ROLE_USER can upload files only for tasks where he assignee but the user with ROLE_MANAGER can upload files for all tasks. An example:
![alt text](<Photo-1.png>)
* GET: /api/attachments?page={0}&size={10}&projectIds={1}&projectNames={projectName}&taskIds={1}&names={taskName}&assigneeIds={2}&assigneeNames={user} - Get all attachments for tasks. That is all Dropbox File IDs from the database and links of donwload of the actual files from Dropbox. There are filters. The user with ROLE_USER can receive only attachments for tasks where he assignee but the user with ROLE_MANAGER can receive all attachments.
* DELETE: /api/attachments/{attachmentId} - Delete attachment by id of that attachment. The user with ROLE_USER can delete attachment only for tasks where he assignee but the user with ROLE_MANAGER can delete attachment for all tasks. The assignee or managers receive messages about it, it depend of who deleted file.
### Comment controller
* POST: /api/comments - Add new comment. The assignee or managers receive messages about it, it depend of who make comment. The user with ROLE_USER can make comments only for tasks where he assignee but the user with ROLE_MANAGER can make comments for all tasks. An example:
```json
  {
  "taskId": 6,
  "text": "text"
}
```
* GET: /api/comments?page={0}&size={10}&projectIds={1}&projectNames={projectName}&taskIds={1}&names={taskName}&assigneeIds={2}&assigneeNames={username} - Get all comments for tasks. There are filters. It allowed for users with ROLE_USER and ROLE_MANAGER.
* DELETE: /api/comments/{commentId} - Delete comment by id of that comment. By using soft delete concept. The assignee or managers receive messages about it, it depend of who remove comment. You can remove only yours comments. It allowed for users with ROLE_USER and ROLE_MANAGER.
### Label controller
* POST: /api/labels - Create new label. Name - is uniqure. It allowed only for users with ROLE_MANAGER. An example:
```json
  {
  "name": "name",
  "color": "color",
  "tasks": [
    4, 2
  ]
}
```
* GET: /api/labels - Get a list of all available labels. It allowed for users with ROLE_USER and ROLE_MANAGER.
* PATCH: /api/labels//{labelId} - Update label by id of that label. By using the same body as in case create new label. It allowed only for users with ROLE_MANAGER.
* DELETE: /api/labels//{labelId} - Delete label by id of that label. It allowed only for users with ROLE_MANAGER.
## How to use the application
1. Make sure you have installed next tools:
* JDK 17+
* Docker
2. Clone the repository from GitHub
3. Rename .env.sample to .env or create new file .env and copy fields from .env.sample to new file.
4. Then need to fill in these fields.
* In most situations ports put:
MYSQLDB_DOCKER_PORT=3306
MYSQLDB_LOCAL_PORT=3307
SPRING_DOCKER_PORT=8080
SPRING_LOCAL_PORT=8081
DEBUG_PORT=5005
* In fields MYSQLDB_USER= and MYSQLDB_ROOT_PASSWORD= put username and password of MYSQL.
* SPRING_MAIL_USERNAME= - Put gmail from which will be sending notifications. Like Example@gmail.com.
* SPRING_MAIL_PASSWORD= - Sign in account of gmail from which will be sending notifications. Click on you profile icon -> Manage your google account -> Security (on the left menu) -> Two-step verification -> Application passwords. Create password and put it in the field.
* JWT_EXPIRATION= - Expressed in seconds or a string describing a time span zeit/ms. An example: 60, "2 days", "10h", "7d".
* JWT_SECRET= - Must be more than 32 symbols length.
* DROPBOX_CLIENT_ID_APP_KEY= and DROPBOX_CLIENT_APP_SECRET= - Sign in Dropbox account which you want to use and go to links https://www.dropbox.com/developers/apps. Click create up. It is important at the second step take: App folder - Access to a single folder created specifically for your app. After created app sign in setting of this app and there will be field App key which need put at DROPBOX_CLIENT_ID_APP_KEY= and field App secret which need put at DROPBOX_CLIENT_APP_SECRET=. It is also important in tab the permissions put files.content.write and files.content.read.
3. Run the following commands:
```json
mvn clean package
docker-compose build
docker-compose run --service-ports app shell
```
Admin login:
```json
{
  "username": "admin",
  "password": "12345678"
}
```
Swagger is available for testing at http://localhost:SPRING_LOCAL_PORT/api/swagger-ui/index.html#/. To sign in can to use admin.
