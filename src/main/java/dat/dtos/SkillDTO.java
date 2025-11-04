package dat.dtos;

import lombok.*;
import dat.entities.Skill;
import dat.entities.SkillCategory;

@Getter
@Setter
public class SkillDTO
{
    private Integer id;
    private String name;
    private SkillCategory category;
    private String description;

    public SkillDTO(){}

    public SkillDTO(Integer id, String name, SkillCategory category, String description){
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
    }

    public static SkillDTO fromEntity(Skill s){
        if (s == null) return null;
        return new SkillDTO(s.getId(), s.getName(), s.getCategory(), s.getDescription());
    }

}
