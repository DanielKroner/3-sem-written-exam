package dat.controllers.impl;

import dat.entities.SkillCategory;
import dat.config.HibernateConfig;
import dat.controllers.IController;
import dat.daos.impl.CandidateDAO;
import dat.dtos.CandidateDTO;
import dat.exceptions.Message;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class CandidateController implements IController<CandidateDTO, Integer> {

    private final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private final CandidateDAO dao = CandidateDAO.getInstance(emf);

    @Override
    public void read(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(this::validatePrimaryKey, "Not a valid id")
                .get();

        CandidateDTO dto = dao.read(id);
        if (dto == null) {
            ctx.status(404).json(new Message(404, "Candidate " + id + " not found"));
            return;
        }
        ctx.status(200).json(dto, CandidateDTO.class);
    }


    // checks if it is null or blank, if it is null or blank it will return all candidates
    // else it will only return candidates that have the specified category
    @Override
    public void readAll(Context ctx) {
        String catStr = ctx.queryParam("category");
        if (catStr == null || catStr.isBlank()) {
            List<CandidateDTO> list = dao.readAll();
            ctx.status(200).json(list, CandidateDTO.class);
            return;
        }

        try {
            SkillCategory cat = SkillCategory.valueOf(catStr.trim().toUpperCase());
            List<CandidateDTO> list = dao.readAllByCategory(cat);
            ctx.status(200).json(list, CandidateDTO.class);
        } catch (IllegalArgumentException ex) {
            ctx.status(400).json(new Message(400,
                    "Invalid category. Allowed values: PROG_LANG, DB, DEVOPS, FRONTEND, TESTING, DATA, FRAMEWORK"));
        }
    }

    @Override
    public void create(Context ctx) {
        CandidateDTO incoming = validateEntity(ctx);
        CandidateDTO created = dao.create(incoming);
        ctx.status(201).json(created, CandidateDTO.class);
    }

    @Override
    public void update(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(this::validatePrimaryKey, "Not a valid id")
                .get();

        CandidateDTO incoming = validateEntity(ctx);
        CandidateDTO updated = dao.update(id, incoming);
        if (updated == null) {
            ctx.status(404).json(new Message(404, "Candidate " + id + " not found"));
            return;
        }
        ctx.status(200).json(updated, CandidateDTO.class);
    }

    @Override
    public void delete(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(this::validatePrimaryKey, "Not a valid id")
                .get();

        dao.delete(id);
        ctx.status(204).json(new Message(204, "Candidate " + id + " deleted"));
    }

    public void linkSkill(Context ctx) {
        int candidateId = ctx.pathParamAsClass("candidateId", Integer.class)
                .check(this::validatePrimaryKey, "Not a valid candidate id")
                .get();

        int skillId = ctx.pathParamAsClass("skillId", Integer.class)
                .check(id -> id != null && id > 0, "Not a valid skill id")
                .get();

        CandidateDTO updated = dao.linkSkill(candidateId, skillId);
        if (updated == null) {
            ctx.status(404).json(new Message(404,
                    "Candidate " + candidateId + " or Skill " + skillId + " not found"));
            return;
        }
        ctx.status(200).json(updated, CandidateDTO.class);
    }

    @Override
    public boolean validatePrimaryKey(Integer id) {
        return id != null && id > 0 && dao.validatePrimaryKey(id);
    }

    @Override
    public CandidateDTO validateEntity(Context ctx) {
        return ctx.bodyValidator(CandidateDTO.class)
                .check(dto -> dto.getName() != null && !dto.getName().isBlank(), "Not a valid name")
                .check(dto -> dto.getPhone() != null && dto.getPhone().matches("\\d{8}"),
                        "Phone must be 8 digits")
                .check(dto -> dto.getEducation() != null && !dto.getEducation().isBlank(),
                        "Not a valid education")
                .get();
    }
}