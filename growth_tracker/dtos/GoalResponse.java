package growth_tracker.dtos;

import java.time.LocalDateTime;

public class GoalResponse {    
        
    private int id;
    private String title;
    private String dailyTarget;
    private String createdAt;

    public GoalResponse(int id, String title, String dailyTarget, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.dailyTarget = dailyTarget;
        this.createdAt = createdAt.toString();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDailyTarget() {
        return dailyTarget;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "GoalResponse{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", dailyTarget='" + dailyTarget + '\'' +
            ", createdAt=" + createdAt +
            '}';
}

}
