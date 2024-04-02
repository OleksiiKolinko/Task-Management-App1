package mate.academy.service.impl;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mate.academy.model.Attachment;
import mate.academy.model.Comment;
import mate.academy.model.Task;
import mate.academy.model.User;
import mate.academy.service.EmailMessageUtil;
import mate.academy.service.EmailService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailMessageUtilImpl implements EmailMessageUtil {
    private static final String SPACES = "";
    private static final String NEW_PERSON = "New person";
    private static final String NEW_PERSON_BODY_TEXT = "The registered new person, please, "
            + "put him roles. Information about him:%nUser identification%11s%d%n"
            + "Username%24s%s%n"
            + "Email%31s%s%nFirst name%23s%s%nLast name%24s%s%nRoles%31s%s";
    private static final String UPDATE_ROLES_BODY_TEXT = "The admin updated your roles. "
            + "Your new roles: %s.";
    private static final String UPDATE_ROLES = "Your roles updated";
    private static final String PUT_ASSIGNEE = "Put new assignee for tasks";
    private static final String PUT_ASSIGNEE_BODY_TEXT_1 = "The admin had taken ROLE_USER off from"
            + " user with id %d and with username  %s. So need change assignee for tasks:%s";
    private static final String PUT_ASSIGNEE_BODY_TEXT_2 = "%n-- task id %d, with task name %s,"
            + " from project with id %d and project name %s";
    private static final String BODY_TEXT_UPLOAD_OR_DELETE_FILE = "This file was %s:%nAttachment "
            + "id %d and filename %s, refers to the task %s with task id %d. This file uploaded %s"
            + " and received dropbox file id %s.";
    private static final String BODY_TEXT_ADD_OR_REMOVE_COMMENT = "This comment %s:%nThe comment "
            + "with id %d %s by user with username %s and with user id %d this comment refers task"
            + " with task name %s with task id %d and was added %s.";
    private static final String NEW_TASK = "New task";
    private static final String NEW_TASK_BODY_TEXT =
            "You received new task. The number of task is %d.";
    private static final String UPDATE_TASK = "Update task";
    private static final String TEXT_UPDATE_TASK = "Your task updated. The number of task is %d.";
    private static final String TASK_TRANSFERRED =
            "Your task has been transferred to another person. The number of task is %d.";
    private static final String RECEIVED_TASK = "You received new task. The number of task is %d."
            + " The task was before assigned for user with username %s, with email %s.";
    private static final String DELETED_TASK = "Deleted task";
    private static final String BODY_TEXT_REMOVED_TASK =
            "Your task removed. The name of task was %s and task id was %d.";
    private final EmailService emailService;

    @Override
    public void sendNewPerson(String adminEmail, Long idNewUser, String usernameNewUser,
                              String emailNewUser, String firstNameNewUser,
                              String lastNameNewUser, Set<String> roleDtosNewUser) {
        emailService.sendEmail(adminEmail, NEW_PERSON, String.format(NEW_PERSON_BODY_TEXT, SPACES,
                idNewUser, SPACES, usernameNewUser, SPACES, emailNewUser, SPACES, firstNameNewUser,
                SPACES, lastNameNewUser, SPACES, roleDtosNewUser));
    }

    @Override
    public void sendRoleUpdate(String userEmail, Set<String> roleDtos) {
        emailService.sendEmail(userEmail, UPDATE_ROLES,
                String.format(UPDATE_ROLES_BODY_TEXT, roleDtos));
    }

    @Override
    public void sendChangeAssignee(Set<String> managerEmails, Set<Task> tasksNotCompleted,
                                   User replaceUser) {
        final String bodyText = String.format(PUT_ASSIGNEE_BODY_TEXT_1, replaceUser.getId(),
                replaceUser.getUsername(), tasksNotCompleted.stream()
                        .map(task -> String.format(PUT_ASSIGNEE_BODY_TEXT_2, task.getId(),
                                task.getName(), task.getProject().getId(),
                                task.getProject().getName())).collect(Collectors.joining()));
        managerEmails.forEach(managerEmail -> emailService
                .sendEmail(managerEmail, PUT_ASSIGNEE, bodyText));
    }

    @Override
    public void sendAddOrRemoveFile(String subject, String action, Set<String> emails,
                                    Attachment attachment, Task task) {
        final String bodyText = String.format(BODY_TEXT_UPLOAD_OR_DELETE_FILE, action,
                attachment.getId(), attachment.getFilename(), task.getName(), task.getId(),
                attachment.getUploadDate(), attachment.getDropboxFileId());
        emails.forEach(email -> emailService.sendEmail(email, subject, bodyText));
    }

    @Override
    public void sendAddOrRemoveComment(Set<String> emails, String subject, String action,
                                       Comment comment, User user) {
        final String bodyText = String.format(BODY_TEXT_ADD_OR_REMOVE_COMMENT, action,
                comment.getId(), action, user.getUsername(), user.getId(),
                comment.getTask().getName(), comment.getTask().getId(), comment.getTimestamp());
        emails.forEach(email -> emailService.sendEmail(email, subject, bodyText));
    }

    @Override
    public void sendNewTask(String emailAssignee, Long taskId) {
        emailService.sendEmail(emailAssignee, NEW_TASK, String.format(NEW_TASK_BODY_TEXT, taskId));
    }

    @Override
    public void sendUpdateTask(User userBefore, User userUpdate, Long taskId) {
        if (userBefore.equals(userUpdate)) {
            emailService.sendEmail(userUpdate.getEmail(), UPDATE_TASK,
                    String.format(TEXT_UPDATE_TASK, taskId));
        } else {
            final String userEmailBefore = userBefore.getEmail();
            emailService.sendEmail(userEmailBefore, UPDATE_TASK,
                    String.format(TASK_TRANSFERRED, taskId));
            emailService.sendEmail(userUpdate.getEmail(), UPDATE_TASK, String.format(
                    RECEIVED_TASK, taskId, userBefore.getUsername(), userEmailBefore));
        }
    }

    @Override
    public void sendDeleteTask(Task task) {
        emailService.sendEmail(task.getAssignee().getEmail(), DELETED_TASK, String.format(
                BODY_TEXT_REMOVED_TASK, task.getName(), task.getId()));
    }
}
