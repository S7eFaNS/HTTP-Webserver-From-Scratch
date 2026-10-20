package growth_tracker.entities;

import java.time.LocalDateTime;

public class Goal {

    private int id;
    private String title;
    private String dailyTarget;
    private LocalDateTime createdAt;

    public Goal(String title, String dailyTarget){
        this.title = title;
        this.dailyTarget = dailyTarget;
        this.createdAt = LocalDateTime.now();
    }

    public Goal(int id, String title, String dailyTarget, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.dailyTarget = dailyTarget;
        this.createdAt = createdAt;
    }

    public String getTitle(){
        return title;
    }

    public String getDailyTarget(){
        return dailyTarget;
    }

    public int getId(){
        return id;
    }

    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
}
