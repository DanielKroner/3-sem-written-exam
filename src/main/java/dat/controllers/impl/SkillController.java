package dat.controllers.impl;

import dat.config.HibernateConfig;
import dat.controllers.IController;
import dat.daos.impl.SkillDAO;
import dat.dtos.SkillDTO;
import dat.entities.SkillCategory;
import dat.exceptions.Message;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class SkillController implements IController<SkillDTO, Integer> {

    private final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private final SkillDAO dao = SkillDAO.getInstance(emf);

    @Override
    public void read(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(this::validatePrimaryKey, "Not a valid id")
                .get();

        SkillDTO dto = dao.read(id);
        if (dto == null) {
            ctx.status(404).json(new Message(404, "Skill " + id + " not found"));
            return;
        }
        ctx.status(200).json(dto, SkillDTO.class);
    }

    @Override
    public void readAll(Context ctx) {
        List<SkillDTO> list = dao.readAll();
        ctx.status(200).json(list, SkillDTO.class);
    }

    @Override
    public void create(Context ctx) {
        SkillDTO incoming = validateEntity(ctx);
        SkillDTO created = dao.create(incoming);
        ctx.status(201).json(created, SkillDTO.class);
    }

    @Override
    public void update(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(this::validatePrimaryKey, "Not a valid id")
                .get();

        SkillDTO incoming = validateEntity(ctx);
        SkillDTO updated = dao.update(id, incoming);
        if (updated == null) {
            ctx.status(404).json(new Message(404, "Skill " + id + " not found"));
            return;
        }
        ctx.status(200).json(updated, SkillDTO.class);
    }

    @Override
    public void delete(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(this::validatePrimaryKey, "Not a valid id")
                .get();

        dao.delete(id);
        ctx.status(204).json(new Message(204, "Skill " + id + " deleted"));
    }

    @Override
    public boolean validatePrimaryKey(Integer id) {
        return id != null && id > 0 && dao.validatePrimaryKey(id);
    }

    @Override
    public SkillDTO validateEntity(Context ctx) {
        // Expect JSON like: {"name":"Docker","category":"DEVOPS","description":"..."}
        return ctx.bodyValidator(SkillDTO.class)
                .check(s -> s.getName() != null && !s.getName().isBlank(), "Not a valid name")
                .check(s -> s.getCategory() != null, "Category is required")
                .check(s -> isValidCategory(s.getCategory()), "Invalid category")
                .get();
    }

    private boolean isValidCategory(SkillCategory cat) {
        // Non-null already checked; this method is future-proof if you add custom validation
        for (SkillCategory c : SkillCategory.values()) {
            if (c == cat) return true;
        }
        return false;
    }
}