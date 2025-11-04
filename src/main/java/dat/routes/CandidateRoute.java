package dat.routes;

import dat.controllers.impl.CandidateController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class CandidateRoute {

    private final CandidateController candidateController = new CandidateController();

    protected EndpointGroup getRoutes() {
        return () -> {
            get("/", candidateController::readAll);
            get("/{id}", candidateController::read);
            post("/", candidateController::create);
            put("/{id}", candidateController::update);
            delete("/{id}", candidateController::delete);

            put("/{candidateId}/skills/{skillId}", candidateController::linkSkill);
        };
    }
}