package dat.daos;

import dat.dtos.CandidateDTO;

public interface ICandidateDAO extends IDAO<CandidateDTO, Integer>
{
    CandidateDTO linkSkill(Integer candidateId, Integer skillId);
    CandidateDTO unlinkSkill(Integer candidateId, Integer skillId);
}
