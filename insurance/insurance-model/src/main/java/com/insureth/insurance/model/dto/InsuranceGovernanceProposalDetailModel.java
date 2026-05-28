package com.insureth.insurance.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceGovernanceProposalDetailModel {
    private InsuranceGovernanceProposalModel proposal;
    private List<InsuranceGovernanceVoteModel> votes;
}
