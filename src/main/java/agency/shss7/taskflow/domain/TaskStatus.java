package agency.shss7.taskflow.domain;

public enum TaskStatus {
    BACKLOG,
    IN_PROGRESS,
    IN_REVIEW,
    DONE,
    ARCHIVED;

    public boolean canTransitionTo(TaskStatus target) {
        if (this == target) {
            return true;
        }
        return switch (this) {
            case BACKLOG -> target == IN_PROGRESS || target == ARCHIVED;
            case IN_PROGRESS -> target == BACKLOG || target == IN_REVIEW || target == DONE || target == ARCHIVED;
            case IN_REVIEW -> target == IN_PROGRESS || target == DONE || target == ARCHIVED;
            case DONE -> target == IN_PROGRESS || target == ARCHIVED;
            case ARCHIVED -> target == BACKLOG;
        };
    }
}
