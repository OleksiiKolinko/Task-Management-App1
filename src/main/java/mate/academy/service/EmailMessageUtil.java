package mate.academy.service;

import java.util.Set;
import mate.academy.model.Attachment;
import mate.academy.model.Comment;
import mate.academy.model.Task;
import mate.academy.model.User;

public interface EmailMessageUtil {
    void sendNewPerson(String adminEmail, Long idNewUser, String usernameNewUser,
                       String emailNewUser, String firstNameNewUser,
                       String lastNameNewUser, Set<String> roleDtosNewUser);

    void sendRoleUpdate(String userEmail, Set<String> roleDtos);

    void sendChangeAssignee(Set<String> managerEmails, Set<Task> tasksNotCompleted,
                            User replaceUser);

    void sendAddOrRemoveFile(String subject, String action, Set<String> emails,
                             Attachment attachment, Task task);

    void sendAddOrRemoveComment(Set<String> emails, String subject, String action,
                                Comment comment, User user);

    void sendNewTask(String emailAssignee, Long taskId);

    void sendUpdateTask(User userBefore, User userUpdate, Long taskId);

    void sendDeleteTask(Task task);
}
