package growth_tracker.services;

import java.util.List;

import growth_tracker.entities.Goal;
import growth_tracker.repositories.interfaces.IGoalRepository;
import growth_tracker.services.interfaces.IGoalService;

public class GoalService implements IGoalService{

    private final IGoalRepository repo;

    public GoalService(IGoalRepository repo){
        this.repo = repo;
    }

    @Override 
    public void saveGoal(String title, String dailyTarget){
        if(title == null || title.isBlank()){
            throw new IllegalArgumentException(
                "Title cannot be empty"
            );
        }

        if(dailyTarget == null || dailyTarget.isBlank()){
            throw new IllegalArgumentException(
                "Target description cannot be empty"
            );
        }

        Goal goal = new Goal(title, dailyTarget);

        repo.save(goal);

    }

    @Override 
    public List<Goal> getAllGoals(){
        return repo.findAll();
    }

    @Override
    public Goal getGoalById(int id){

        if(id <= 0){
            throw new IllegalArgumentException(
                "Invalid goal id"
            );
        }

        Goal goal = repo.getGoalById(id);

        if(goal == null){
            throw new IllegalArgumentException(
                "Goal not found"
            );
        }

        return goal;
    }

    @Override
    public void deleteGoal(int id){

        Goal goal = getGoalById(id);

        repo.delete(goal.getId());
    }
    
}
