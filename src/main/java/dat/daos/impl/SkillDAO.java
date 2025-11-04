package dat.daos.impl;

import dat.config.HibernateConfig;
import dat.daos.ISkillDAO;
import dat.dtos.SkillDTO;
import dat.entities.Skill;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.stream.Collectors;

public class SkillDAO implements ISkillDAO {

    private static SkillDAO instance;
    private final EntityManagerFactory emf;

    private SkillDAO(EntityManagerFactory emf) { this.emf = emf; }

    public static SkillDAO getInstance(EntityManagerFactory emf) {
        if (instance == null) instance = new SkillDAO(emf);
        return instance;
    }

    @Override
    public SkillDTO create(SkillDTO dto) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Skill s = new Skill();
            s.setName(dto.getName());
            s.setCategory(dto.getCategory());
            s.setDescription(dto.getDescription());
            em.persist(s);
            em.getTransaction().commit();
            return SkillDTO.fromEntity(s);
        } finally {
            em.close();
        }
    }

    @Override
    public SkillDTO read(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            Skill s = em.find(Skill.class, id);
            if (s == null) return null;
            return SkillDTO.fromEntity(s);
        } finally {
            em.close();
        }
    }

    @Override
    public List<SkillDTO> readAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Skill> q = em.createQuery("SELECT s FROM Skill s ORDER BY s.name", Skill.class);
            return q.getResultList().stream().map(SkillDTO::fromEntity).collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public SkillDTO update(Integer id, SkillDTO dto) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Skill s = em.find(Skill.class, id);
            if (s == null) { em.getTransaction().rollback(); return null; }
            if (dto.getName() != null) s.setName(dto.getName());
            if (dto.getCategory() != null) s.setCategory(dto.getCategory());
            if (dto.getDescription() != null) s.setDescription(dto.getDescription());
            em.getTransaction().commit();
            return SkillDTO.fromEntity(s);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Skill s = em.find(Skill.class, id);
            if (s != null) em.remove(s); // cascades remove CandidateSkill via orphanRemoval=true
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Override
    public boolean validatePrimaryKey(Integer id) {
        if (id == null || id <= 0) return false;
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Skill.class, id) != null;
        } finally {
            em.close();
        }
    }
}