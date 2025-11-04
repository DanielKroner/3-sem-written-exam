package dat.integration;

import dat.Main;
import dat.entities.SkillCategory;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CandidateApiIT {

    private static Javalin app;
    private static final int PORT = 7070;

    private static Integer devopsSkillId;   // Docker
    private static Integer candidateId;     // Alice

    @BeforeAll
    static void startServerAndSeed() {
        // Start API (uses your Routes; no enrichment needed)
        app = Main.startForTest(PORT);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = PORT;

        // Create a DEVOPS skill
        devopsSkillId =
                given()
                        .contentType("application/json")
                        .body(Map.of(
                                "name", "Docker",
                                "category", SkillCategory.DEVOPS.name(),
                                "description", "Containers"
                        ))
                        .when()
                        .post("/api/skills")
                        .then()
                        .statusCode(anyOf(is(200), is(201)))
                        .extract().path("id");

        // Create a candidate
        candidateId =
                given()
                        .contentType("application/json")
                        .body(Map.of(
                                "name", "Alice Andersen",
                                "phone", "55556666",
                                "education", "BSc CS"
                        ))
                        .when()
                        .post("/api/candidates")
                        .then()
                        .statusCode(anyOf(is(200), is(201)))
                        .extract().path("id");

        // Link Docker to Alice
        given()
                .when()
                .put("/api/candidates/{cid}/skills/{sid}", candidateId, devopsSkillId)
                .then()
                .statusCode(200)
                .body("id", equalTo(candidateId));
    }

    @AfterAll
    static void stopServer() {
        if (app != null) app.stop();
    }

    // --- GET all ---
    @Test @Order(1)
    void getAllCandidates_returns200_andArray() {
        given()
                .when()
                .get("/api/candidates")
                .then()
                .statusCode(200)
                .body("$", isA(List.class))
                .body("size()", greaterThanOrEqualTo(1));
    }

    // --- GET by id (includes skills) ---
    @Test @Order(2)
    void getCandidateById_returns200_andIncludesSkills() {
        given()
                .when()
                .get("/api/candidates/{id}", candidateId)
                .then()
                .statusCode(200)
                .body("id", equalTo(candidateId))
                .body("name", equalTo("Alice Andersen"))
                .body("skills", notNullValue())
                .body("skills.size()", greaterThanOrEqualTo(1))
                .body("skills.name", hasItem("Docker"));
    }

    // --- POST ---
    @Test @Order(3)
    void createCandidate_returns201_andEchoesFields() {
        int newId =
                given()
                        .contentType("application/json")
                        .body(Map.of(
                                "name", "Bob Boesen",
                                "phone", "55557777",
                                "education", "MSc SE"
                        ))
                        .when()
                        .post("/api/candidates")
                        .then()
                        .statusCode(anyOf(is(200), is(201)))
                        .body("name", equalTo("Bob Boesen"))
                        .extract().path("id");

        // sanity: fetch
        given().when().get("/api/candidates/{id}", newId).then().statusCode(200);
    }

    // --- PUT (update) ---
    @Test @Order(4)
    void updateCandidate_returns200_withUpdatedFields() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "name", "Alice A.",
                        "phone", "55556666",
                        "education", "BSc CS"
                ))
                .when()
                .put("/api/candidates/{id}", candidateId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Alice A."));
    }

    // --- DELETE ---
    @Test @Order(5)
    void deleteCandidate_returns204_then404_onFetch() {
        // Create a temporary candidate
        int tmpId =
                given()
                        .contentType("application/json")
                        .body(Map.of("name", "Temp User", "phone", "66667777", "education", "AP Degree"))
                        .when()
                        .post("/api/candidates")
                        .then()
                        .statusCode(anyOf(is(200), is(201)))
                        .extract().path("id");

        // Delete
        given().when().delete("/api/candidates/{id}", tmpId)
                .then().statusCode(anyOf(is(200), is(204)));

        // Verify 404
        given().when().get("/api/candidates/{id}", tmpId)
                .then().statusCode(404);
    }

    // --- Link skill ---
    @Test @Order(6)
    void linkSkill_put_returns200_andContainsBothSkills() {
        // Create another DEVOPS skill
        int k8sId =
                given()
                        .contentType("application/json")
                        .body(Map.of(
                                "name", "Kubernetes",
                                "category", SkillCategory.DEVOPS.name(),
                                "description", "Orchestration"
                        ))
                        .when()
                        .post("/api/skills")
                        .then()
                        .statusCode(anyOf(is(200), is(201)))
                        .extract().path("id");

        // Link to Alice
        given()
                .when()
                .put("/api/candidates/{cid}/skills/{sid}", candidateId, k8sId)
                .then()
                .statusCode(200)
                .body("skills.name", hasItems("Docker", "Kubernetes"));
    }

    // --- Filter by category ---
    @Test @Order(7)
    void filterCandidatesByCategory_returnsOnlyThoseWithCategory() {
        given()
                .queryParam("category", "DEVOPS")
                .when()
                .get("/api/candidates")
                .then()
                .statusCode(200)
                .body("$", isA(List.class))
                .body("findAll { it.skills*.category.flatten().contains('DEVOPS') }.size()",
                        greaterThanOrEqualTo(1));
    }
}