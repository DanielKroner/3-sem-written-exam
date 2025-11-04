package dat.routes;

import dat.controllers.impl.SkillController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class SkillRoute {

    private final SkillController skillController = new SkillController();

    protected EndpointGroup getRoutes() {
        return () -> {
            get("/", skillController::readAll);
            get("/{id}", skillController::read);
            post("/", skillController::create);
            put("/{id}", skillController::update);
            delete("/{id}", skillController::delete);
        };
    }
}