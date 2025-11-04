package dat.entities;

import lombok.*;
import jakarta.persistence.*;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_candidate_skill", columnNames = {"candidate_id", "skill_id"}))
public class CandidateSkill
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    public CandidateSkill() {}

    public CandidateSkill(Candidate candidate, Skill skill)
    {
        this.candidate = candidate;
        this.skill = skill;
    }

    @Override public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof CandidateSkill cs)) return false;
        return Objects.equals(candidate, cs.candidate) && Objects.equals(skill, cs.skill);
    }

    @Override public int hashCode(){
        return Objects.hash(candidate, skill);
    }
}
