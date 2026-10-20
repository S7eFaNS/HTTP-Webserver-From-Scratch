package growth_tracker.controllers;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import framework.entities.HttpRequest;
import framework.entities.HttpResponse;
import framework.entities.StatusCodeEnum;
import framework.exceptions.HttpParseException;
import framework.handlers.Router;
import growth_tracker.controllers.interfaces.IGoalController;
import growth_tracker.dtos.CreateGoalRequest;
import growth_tracker.dtos.GoalResponse;
import growth_tracker.entities.Goal;
import growth_tracker.services.interfaces.IGoalService;

public class GoalController implements IGoalController{
    
    private final IGoalService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GoalController(IGoalService service){
        this.service = service;
    }

    @Override 
    public void registerRoutes(Router router){
        router.register("GET", "/goals", this::getAllGoals);
        router.register("GET", "/goal", this::getGoalById);
        router.register("DELETE", "/goal", this::deleteGoalById);
        router.register("POST", "/goal", this::createGoal);
    }

    private HttpResponse getAllGoals(HttpRequest request){
        List<GoalResponse> responses = service.getAllGoals()
                        .stream()
                        .map(goal -> new GoalResponse(goal.getId(), goal.getTitle(), goal.getDailyTarget(), goal.getCreatedAt()))
                        .toList();

        return HttpResponse.text(StatusCodeEnum.OK, responses.toString());
    }

    private HttpResponse getGoalById(HttpRequest request){
        try {
            int id = idFromQuery(request);

            Goal goal = service.getGoalById(id);

            GoalResponse response = new GoalResponse(goal.getId(), goal.getTitle(), goal.getDailyTarget(), goal.getCreatedAt());

            return HttpResponse.text(StatusCodeEnum.OK, response.toString());
        } catch (IllegalArgumentException e) {
            return HttpResponse.text(StatusCodeEnum.NOT_FOUND, e.getMessage());
        }
        
    }

    private HttpResponse deleteGoalById(HttpRequest request){
        try {
            int id = idFromQuery(request);

            service.deleteGoal(id);
            
            return HttpResponse.text(StatusCodeEnum.OK,"Goal deleted successfully");
        } catch (IllegalArgumentException e) {
            return HttpResponse.text(StatusCodeEnum.NOT_FOUND,e.getMessage());
        }
    }

    private HttpResponse createGoal(HttpRequest request){
        
        try {
            String body = new String(request.getBody());

            CreateGoalRequest goalCreation = objectMapper.readValue(body, CreateGoalRequest.class);

            service.saveGoal(goalCreation.getTitle(), goalCreation.getDailyTarget());
            return HttpResponse.text(StatusCodeEnum.OK, "Goal was created");
        } catch (Exception e) {
            return HttpResponse.text(StatusCodeEnum.BAD_REQUEST, e.getMessage());
        }
    }






    private int idFromQuery(HttpRequest request){
        String query = request.getQuery();

        if(query == null){
            throw new HttpParseException(StatusCodeEnum.BAD_REQUEST, "Missing id parameter");
        }

        String[] parts = query.split("=");

        if(parts.length != 2){
            throw new HttpParseException(StatusCodeEnum.BAD_REQUEST, "Invalid id parameter");
        }
        try {
            return Integer.parseInt(parts[1]);        
        } catch (NumberFormatException e) {
            throw new HttpParseException(StatusCodeEnum.BAD_REQUEST, "Invalid id value");
        }
    }
}
