package growth_tracker.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import growth_tracker.config.DatabaseConfig;
import growth_tracker.entities.Goal;
import growth_tracker.repositories.interfaces.IGoalRepository;

public class GoalRepository implements IGoalRepository{

    public void save(Goal goal) {

        String sql = """
                INSERT INTO goals (
                    title,
                    daily_target,
                    created_at
                )
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, goal.getTitle());

            statement.setString(2, goal.getDailyTarget());

            statement.setTimestamp(3, Timestamp.valueOf(goal.getCreatedAt()));

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }    
    }



    public List<Goal> findAll() {

        String sql = "SELECT * FROM goals";

        List<Goal> goals = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {

                goals.add(
                    new Goal(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("daily_target"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                    )
                );
            }

            return goals;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    public Goal getGoalById(int id){
        String sql = "SELECT * FROM goals WHERE id = ?";

        try(Connection connection = DatabaseConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
                
                statement.setInt(1, id);

                try(ResultSet rs = statement.executeQuery()) {
                    if(rs.next()){
                        return new Goal(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("daily_target"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    }
                    return null;
                }
            } catch (SQLException e){
                throw new RuntimeException(e);
            }
    }



    public void delete(int id) {

        String sql = "DELETE FROM goals WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
