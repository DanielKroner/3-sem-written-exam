package dat.entities;

import lombok.*;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Candidate
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false)
    private String education;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CandidateSkill> candidateSkills = new ArrayList<>();

    //helpers
    public void addSkill(Skill skill)
    {
        CandidateSkill cs = new CandidateSkill(this, skill);
        candidateSkills.add(cs);
        skill.getCandidateSkills().add(cs);
    }

    public void removeSkill(Skill skill)
    {
        candidateSkills.removeIf(cs ->
        {
            boolean match = cs.getSkill().equals(skill);
            if (match) skill.getCandidateSkills().remove(cs);
            cs.setCandidate(null);
            cs.setSkill(null);
            return match;
        });
    }
}
