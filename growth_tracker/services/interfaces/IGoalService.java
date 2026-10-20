package growth_tracker.services.interfaces;
import java.util.List;
import growth_tracker.entities.Goal;

public interface IGoalService {
    void saveGoal(String title, String dailyTarget);

    List<Goal> getAllGoals();
        
    Goal getGoalById(int id);

    void deleteGoal(int id);
}
