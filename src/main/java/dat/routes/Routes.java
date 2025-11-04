package dat.routes;

import io.javalin.apibuilder.EndpointGroup;
import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes {

    private final CandidateRoute candidateRoute = new CandidateRoute();
    private final SkillRoute skillRoute = new SkillRoute();

    public EndpointGroup getRoutes() {
        return () -> {
            path("/candidates", candidateRoute.getRoutes());
            path("/skills", skillRoute.getRoutes());
        };
    }
}