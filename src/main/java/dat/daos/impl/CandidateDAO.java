package dat.daos.impl;

import dat.daos.ICandidateDAO;
import dat.dtos.CandidateDTO;
import dat.dtos.SkillDTO;
import dat.entities.Candidate;
import dat.entities.CandidateSkill;
import dat.entities.Skill;
import dat.entities.SkillCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.stream.Collectors;

public class CandidateDAO implements ICandidateDAO {

    private static CandidateDAO instance;
    private final EntityManagerFactory emf;

    private CandidateDAO(EntityManagerFactory emf) { this.emf = emf; }

    public static CandidateDAO getInstance(EntityManagerFactory emf) {
        if (instance == null) instance = new CandidateDAO(emf);
        return instance;
    }

    @Override
    public CandidateDTO create(CandidateDTO dto) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Candidate c = new Candidate();
            c.setName(dto.getName());
            c.setPhone(dto.getPhone());
            c.setEducation(dto.getEducation());
            em.persist(c);

            // Optional: attach incoming skills by id (if provided)
            if (dto.getSkills() != null && !dto.getSkills().isEmpty()) {
                for (SkillDTO sd : dto.getSkills()) {
                    if (sd.getId() == null) continue;
                    Skill s = em.find(Skill.class, sd.getId());
                    if (s != null) {
                        CandidateSkill cs = new CandidateSkill(c, s);
                        c.getCandidateSkills().add(cs);
                        s.getCandidateSkills().add(cs);
                        em.persist(cs);
                    }
                }
            }

            em.getTransaction().commit();
            return CandidateDTO.fromEntity(c);
        } finally {
            em.close();
        }
    }

    @Override
    public CandidateDTO read(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            Candidate c = em.find(Candidate.class, id);
            if (c == null) return null;
            // force load skills if LAZY: c.getCandidateSkills().size();
            return CandidateDTO.fromEntity(c);
        } finally {
            em.close();
        }
    }

    @Override
    public List<CandidateDTO> readAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Candidate> q = em.createQuery("SELECT c FROM Candidate c ORDER BY c.name", Candidate.class);
            return q.getResultList().stream().map(CandidateDTO::fromEntity).collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public CandidateDTO update(Integer id, CandidateDTO dto) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Candidate c = em.find(Candidate.class, id);
            if (c == null) { em.getTransaction().rollback(); return null; }
            if (dto.getName() != null) c.setName(dto.getName());
            if (dto.getPhone() != null) c.setPhone(dto.getPhone());
            if (dto.getEducation() != null) c.setEducation(dto.getEducation());
            em.getTransaction().commit();
            return CandidateDTO.fromEntity(c);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Candidate c = em.find(Candidate.class, id);
            if (c != null) em.remove(c); // orphanRemoval=true removes CandidateSkill rows
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
            return em.find(Candidate.class, id) != null;
        } finally {
            em.close();
        }
    }

    @Override
    public CandidateDTO linkSkill(Integer candidateId, Integer skillId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Candidate c = em.find(Candidate.class, candidateId);
            Skill s = em.find(Skill.class, skillId);
            if (c == null || s == null) { em.getTransaction().rollback(); return null; }

            boolean already = c.getCandidateSkills().stream()
                    .anyMatch(cs -> cs.getSkill().getId().equals(skillId));
            if (!already) {
                CandidateSkill cs = new CandidateSkill(c, s);
                c.getCandidateSkills().add(cs);
                s.getCandidateSkills().add(cs);
                em.persist(cs);
            }

            em.getTransaction().commit();
            return CandidateDTO.fromEntity(c);
        } finally {
            em.close();
        }
    }

    @Override
    public CandidateDTO unlinkSkill(Integer candidateId, Integer skillId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Candidate c = em.find(Candidate.class, candidateId);
            if (c == null) { em.getTransaction().rollback(); return null; }

            // remove join rows
            c.getCandidateSkills().removeIf(cs -> {
                if (cs.getSkill().getId().equals(skillId)) {
                    cs.getSkill().getCandidateSkills().remove(cs);
                    em.remove(em.contains(cs) ? cs : em.merge(cs));
                    return true;
                }
                return false;
            });

            em.getTransaction().commit();
            return CandidateDTO.fromEntity(c);
        } finally {
            em.close();
        }
    }

    public List<CandidateDTO> readAllByCategory(SkillCategory category){
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Candidate> q = em.createQuery(
                    "SELECT DISTINCT c " +
                            "FROM Candidate c " +
                            "JOIN c.candidateSkills cs " +
                            "JOIN cs.skill s " +
                            "WHERE s.category = :cat " +
                            "ORDER BY c.name", Candidate.class);
            q.setParameter("cat", category);
            return q.getResultList()
                    .stream()
                    .map(CandidateDTO::fromEntity)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }
}