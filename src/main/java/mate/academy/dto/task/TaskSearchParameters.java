package mate.academy.dto.task;

public record TaskSearchParameters(Long[] taskIds, String[] names, Long[] projectIds,
                                   String[] projectNames, Long[] assigneeIds,
                                   String[] assigneeNames) {
}
