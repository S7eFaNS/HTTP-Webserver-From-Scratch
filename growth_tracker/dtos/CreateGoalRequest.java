package growth_tracker.dtos;

public class CreateGoalRequest {
    
    private String title;
    private String dailyTarget;

    public CreateGoalRequest(){
        
    }

    public CreateGoalRequest(String title, String dailyTarget) {
        this.title = title;
        this.dailyTarget = dailyTarget;
    }

    public String getTitle() {
        return title;
    }

    public String getDailyTarget() {
        return dailyTarget;
    }

}
