package dat.config;

import dat.entities.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public class Populate {

    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        new Populate().seed(emf);
        emf.close();
    }

    public void seed(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // --- Skills (by categories) ---
            Skill java = skill(em, "Java", SkillCategory.PROG_LANG, "General-purpose JVM language");
            Skill python = skill(em, "Python", SkillCategory.PROG_LANG, "Scripting & data science");
            Skill csharp = skill(em, "C#", SkillCategory.PROG_LANG, ".NET ecosystem");

            Skill postgres = skill(em, "PostgreSQL", SkillCategory.DB, "Relational database");
            Skill mysql = skill(em, "MySQL", SkillCategory.DB, "Relational database");

            Skill docker = skill(em, "Docker", SkillCategory.DEVOPS, "Containers");
            Skill k8s = skill(em, "Kubernetes", SkillCategory.DEVOPS, "Orchestration");
            Skill gha = skill(em, "GitHub Actions", SkillCategory.DEVOPS, "CI/CD pipelines");

            Skill html = skill(em, "HTML", SkillCategory.FRONTEND, "Markup");
            Skill css = skill(em, "CSS", SkillCategory.FRONTEND, "Styling");
            Skill ts = skill(em, "TypeScript", SkillCategory.FRONTEND, "Typed JS");
            Skill vue = skill(em, "Vue.js", SkillCategory.FRONTEND, "Frontend framework");

            Skill junit = skill(em, "JUnit", SkillCategory.TESTING, "Java testing");
            Skill cypress = skill(em, "Cypress", SkillCategory.TESTING, "E2E testing");

            Skill pandas = skill(em, "Pandas", SkillCategory.DATA, "Data analysis");
            Skill tensor = skill(em, "TensorFlow", SkillCategory.DATA, "Deep learning");

            Skill spring = skill(em, "Spring Boot", SkillCategory.FRAMEWORK, "Java framework");
            Skill react = skill(em, "React", SkillCategory.FRAMEWORK, "UI library");
            Skill angular = skill(em, "Angular", SkillCategory.FRAMEWORK, "SPA framework");

            // --- Candidates ---
            Candidate alice = candidate(em, "Alice Andersen", "11111111", "BSc Computer Science");
            Candidate bob   = candidate(em, "Bob Boesen",      "22222222", "MSc Software Engineering");
            Candidate clara = candidate(em, "Clara Carlsen",   "33333333", "AP Graduate in IT Tech");

            // attach skills
            alice.addSkill(java); alice.addSkill(spring); alice.addSkill(postgres); alice.addSkill(junit);
            bob.addSkill(python); bob.addSkill(pandas); bob.addSkill(tensor); bob.addSkill(docker);
            clara.addSkill(html); clara.addSkill(css);  clara.addSkill(ts);     clara.addSkill(vue);

            // Persist aggregates (skills are already managed, but candidates hold join rows)
            em.persist(alice);
            em.persist(bob);
            em.persist(clara);

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    private Skill skill(EntityManager em, String name, SkillCategory cat, String desc) {
        Skill s = new Skill();
        s.setName(name);
        s.setCategory(cat);
        s.setDescription(desc);
        em.persist(s);
        return s;
    }

    private Candidate candidate(EntityManager em, String name, String phone, String education) {
        Candidate c = new Candidate();
        c.setName(name);
        c.setPhone(phone);
        c.setEducation(education);
        return c; // persisted later (so we can attach skills first if desired)
    }
}