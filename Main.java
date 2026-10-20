import java.io.IOException;
import framework.handlers.Router;
import framework.services.HttpServer;
import growth_tracker.controllers.GoalController;
import growth_tracker.controllers.interfaces.IGoalController;
import growth_tracker.repositories.GoalRepository;
import growth_tracker.repositories.interfaces.IGoalRepository;
import growth_tracker.services.GoalService;
import growth_tracker.services.interfaces.IGoalService;

public class Main {

    public static void main(String[] args)  {

        Router router = new Router();

        IGoalRepository repo = new GoalRepository();
        IGoalService service = new GoalService(repo);
        IGoalController controller = new GoalController(service);

        controller.registerRoutes(router);

        HttpServer server = new HttpServer(8080, router);
        
        System.out.println("Server is running...");
        try{
            server.start();
        } catch(IOException e){
            System.out.println("Error: " + e.getMessage());
        }
        
        Runtime.getRuntime().addShutdownHook(
            new Thread(()-> {
                System.out.println("Server is shutting down");
                server.stop();
            })
        );
    }

}
