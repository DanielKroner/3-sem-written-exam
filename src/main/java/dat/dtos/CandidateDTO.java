package dat.dtos;

import dat.entities.Candidate;
import dat.entities.CandidateSkill;

import lombok.*;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class CandidateDTO
{
    private Integer id;
    private String name;
    private String phone;
    private String education;

    private List<SkillDTO> skills;

    public CandidateDTO(){}

    public CandidateDTO(Integer id, String name, String phone, String education, List<SkillDTO> skills)
    {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.education = education;
        this.skills = skills;
    }

    public static CandidateDTO fromEntity(Candidate c){
        if (c == null) return null;
        List<SkillDTO> skills = c.getCandidateSkills() == null ? List.of() :
                c.getCandidateSkills().stream()
                        .map(CandidateSkill::getSkill)
                        .map(SkillDTO::fromEntity)
                        .collect(Collectors.toList());
        return new CandidateDTO(c.getId(), c.getName(), c.getPhone(), c.getEducation(), skills);
    }
}
