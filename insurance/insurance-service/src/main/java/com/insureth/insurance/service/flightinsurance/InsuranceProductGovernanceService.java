package com.insureth.insurance.service.flightinsurance;

import com.insureth.insurance.domain.entity.InsuranceGovernanceProposal;
import com.insureth.insurance.domain.entity.InsuranceGovernanceVote;
import com.insureth.insurance.domain.repository.InsuranceGovernanceProposalDAO;
import com.insureth.insurance.domain.repository.InsuranceGovernanceVoteDAO;
import com.insureth.insurance.model.dto.InsuranceGovernanceProposalDetailModel;
import com.insureth.insurance.model.dto.InsuranceGovernanceProposalModel;
import com.insureth.insurance.model.dto.InsuranceGovernanceVoteModel;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.Web3j;

@Service
public class InsuranceProductGovernanceService {

    private final InsuranceGovernanceProposalDAO proposalDAO;
    private final InsuranceGovernanceVoteDAO voteDAO;
    private final Web3j web3j;

    public InsuranceProductGovernanceService(
            InsuranceGovernanceProposalDAO proposalDAO,
            InsuranceGovernanceVoteDAO voteDAO,
            @Qualifier("governanceWeb3j") Web3j web3j
    ) {
        this.proposalDAO = proposalDAO;
        this.voteDAO = voteDAO;
        this.web3j = web3j;
    }

    @Transactional(readOnly = true)
    public List<InsuranceGovernanceProposalModel> listProposals() {
        long currentBlock = getCurrentBlock();
        return proposalDAO.findAllByOrderByProposalIdDesc().stream()
                .map(proposal -> toProposalModel(proposal, currentBlock))
                .toList();
    }

    @Transactional(readOnly = true)
    public InsuranceGovernanceProposalDetailModel getProposal(Long proposalId) {
        InsuranceGovernanceProposal proposal = proposalDAO.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Governance proposal not found"));

        long currentBlock = getCurrentBlock();
        List<InsuranceGovernanceVoteModel> votes = voteDAO.findAllByProposalIdOrderByBlockNumberAscLogIndexAsc(proposalId)
                .stream()
                .map(this::toVoteModel)
                .toList();

        return InsuranceGovernanceProposalDetailModel.builder()
                .proposal(toProposalModel(proposal, currentBlock))
                .votes(votes)
                .build();
    }

    private InsuranceGovernanceProposalModel toProposalModel(
            InsuranceGovernanceProposal proposal,
            long currentBlock
    ) {
        return InsuranceGovernanceProposalModel.builder()
                .proposalId(proposal.getProposalId())
                .title(proposal.getTitle())
                .description(proposal.getDescription())
                .insuranceType(proposal.getInsuranceType())
                .proposer(proposal.getProposer())
                .snapshotBlock(proposal.getSnapshotBlock())
                .startBlock(proposal.getStartBlock())
                .endBlock(proposal.getEndBlock())
                .status(resolveStatus(proposal, currentBlock))
                .forVotesEth(formatWeiToEth(proposal.getForVotesWei()))
                .againstVotesEth(formatWeiToEth(proposal.getAgainstVotesWei()))
                .abstainVotesEth(formatWeiToEth(proposal.getAbstainVotesWei()))
                .quorumVotesEth(formatWeiToEth(proposal.getQuorumVotesWei()))
                .executed(proposal.isExecuted())
                .cancelled(proposal.isCancelled())
                .createdAt(proposal.getCreatedAt())
                .executedAt(proposal.getExecutedAt())
                .cancelledAt(proposal.getCancelledAt())
                .build();
    }

    private InsuranceGovernanceVoteModel toVoteModel(InsuranceGovernanceVote vote) {
        return InsuranceGovernanceVoteModel.builder()
                .voter(vote.getVoter())
                .support(vote.getSupport())
                .weightEth(formatWeiToEth(vote.getWeightWei()))
                .timestamp(vote.getEventTimestamp())
                .transactionHash(vote.getTransactionHash())
                .build();
    }

    private String resolveStatus(InsuranceGovernanceProposal proposal, long currentBlock) {
        if (proposal.isCancelled()) {
            return "CANCELLED";
        }
        if (proposal.isExecuted()) {
            return "EXECUTED";
        }
        if (currentBlock < proposal.getStartBlock()) {
            return "PENDING";
        }
        if (currentBlock <= proposal.getEndBlock()) {
            return "ACTIVE";
        }

        BigInteger forVotes = new BigInteger(defaultWei(proposal.getForVotesWei()));
        BigInteger againstVotes = new BigInteger(defaultWei(proposal.getAgainstVotesWei()));
        BigInteger abstainVotes = new BigInteger(defaultWei(proposal.getAbstainVotesWei()));
        BigInteger quorumVotes = new BigInteger(defaultWei(proposal.getQuorumVotesWei()));
        BigInteger participation = forVotes.add(againstVotes).add(abstainVotes);

        if (participation.compareTo(quorumVotes) < 0 || forVotes.compareTo(againstVotes) <= 0) {
            return "DEFEATED";
        }

        return "SUCCEEDED";
    }

    private long getCurrentBlock() {
        try {
            return web3j.ethBlockNumber().send().getBlockNumber().longValue();
        } catch (Exception exception) {
            return Long.MAX_VALUE;
        }
    }

    private String formatWeiToEth(String amountWei) {
        BigDecimal ethAmount = new BigDecimal(new BigInteger(defaultWei(amountWei)))
                .divide(BigDecimal.TEN.pow(18), 18, RoundingMode.HALF_UP)
                .stripTrailingZeros();

        return ethAmount.scale() < 0 ? ethAmount.setScale(0).toPlainString() : ethAmount.toPlainString();
    }

    private String defaultWei(String amountWei) {
        return amountWei == null || amountWei.isBlank() ? "0" : amountWei;
    }
}
