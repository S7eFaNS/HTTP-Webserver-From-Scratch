package growth_tracker.repositories.interfaces;

import java.util.List;

import growth_tracker.entities.Goal;

public interface IGoalRepository {
    void save(Goal goal);

    List<Goal> findAll();

    Goal getGoalById(int id);

    void delete(int id);
}
