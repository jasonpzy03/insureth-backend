package com.insureth.insurance.service.flightinsurance;

import com.insureth.insurance.domain.entity.InsuranceGovernanceProposal;
import com.insureth.insurance.domain.entity.InsuranceGovernanceVote;
import com.insureth.insurance.domain.repository.InsuranceGovernanceProposalDAO;
import com.insureth.insurance.domain.repository.InsuranceGovernanceVoteDAO;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

@Service
@Slf4j
public class InsuranceProductGovernanceTransactionService {
    private static final int RECEIPT_LOOKUP_ATTEMPTS = 10;
    private static final long RECEIPT_LOOKUP_DELAY_MS = 1_500L;
    private static final String TYPE_PROPOSAL_CREATED = "PROPOSAL_CREATED";
    private static final String TYPE_VOTE_CAST = "VOTE_CAST";

    private static final Event PROPOSAL_CREATED_EVENT = new Event(
            "ProposalCreated",
            List.of(
                    new TypeReference<Uint256>(true) {},
                    new TypeReference<Address>(true) {},
                    new TypeReference<Utf8String>() {},
                    new TypeReference<Utf8String>() {},
                    new TypeReference<Utf8String>() {},
                    new TypeReference<Uint256>() {},
                    new TypeReference<Uint256>() {},
                    new TypeReference<Uint256>() {},
                    new TypeReference<Uint256>() {}
            )
    );

    private static final Event VOTE_CAST_EVENT = new Event(
            "VoteCast",
            List.of(
                    new TypeReference<Uint256>(true) {},
                    new TypeReference<Address>(true) {},
                    new TypeReference<Uint8>() {},
                    new TypeReference<Uint256>() {}
            )
    );

    private static final Event PROPOSAL_EXECUTED_EVENT = new Event(
            "ProposalExecuted",
            List.of(
                    new TypeReference<Uint256>(true) {},
                    new TypeReference<Address>(true) {}
            )
    );

    private static final Event PROPOSAL_CANCELLED_EVENT = new Event(
            "ProposalCancelled",
            List.of(
                    new TypeReference<Uint256>(true) {},
                    new TypeReference<Address>(true) {}
            )
    );

    private final Web3j governanceWeb3j;
    private final InsuranceGovernanceProposalDAO proposalDAO;
    private final InsuranceGovernanceVoteDAO voteDAO;

    @Value("${insurance-governance.contract-address:}")
    private String governanceContractAddress;

    public InsuranceProductGovernanceTransactionService(
            @Qualifier("governanceWeb3j") Web3j governanceWeb3j,
            InsuranceGovernanceProposalDAO proposalDAO,
            InsuranceGovernanceVoteDAO voteDAO
    ) {
        this.governanceWeb3j = governanceWeb3j;
        this.proposalDAO = proposalDAO;
        this.voteDAO = voteDAO;
    }

    @Transactional
    public void recordProposalCreated(String transactionHash) {
        TransactionReceipt receipt = requireReceipt(transactionHash);
        Log log = requireEventLog(receipt, PROPOSAL_CREATED_EVENT, "ProposalCreated");
        Instant eventTimestamp = resolveReceiptTimestamp(receipt);

        long proposalId = decodeUint256Topic(log.getTopics().get(1)).longValueExact();
        String proposer = decodeAddressTopic(log.getTopics().get(2));
        List<Type> values = FunctionReturnDecoder.decode(
                log.getData(),
                PROPOSAL_CREATED_EVENT.getNonIndexedParameters()
        );

        InsuranceGovernanceProposal proposal = proposalDAO.findById(proposalId)
                .orElseGet(() -> InsuranceGovernanceProposal.builder().proposalId(proposalId).build());

        proposal.setProposer(proposer);
        proposal.setTitle(((Utf8String) values.get(0)).getValue());
        proposal.setDescription(((Utf8String) values.get(1)).getValue());
        proposal.setInsuranceType(((Utf8String) values.get(2)).getValue());
        proposal.setSnapshotBlock(((Uint256) values.get(3)).getValue().longValueExact());
        proposal.setStartBlock(((Uint256) values.get(4)).getValue().longValueExact());
        proposal.setEndBlock(((Uint256) values.get(5)).getValue().longValueExact());
        proposal.setQuorumVotesWei(((Uint256) values.get(6)).getValue().toString());
        proposal.setForVotesWei(defaultWei(proposal.getForVotesWei()));
        proposal.setAgainstVotesWei(defaultWei(proposal.getAgainstVotesWei()));
        proposal.setAbstainVotesWei(defaultWei(proposal.getAbstainVotesWei()));
        proposal.setStatus("PENDING");
        proposal.setExecuted(false);
        proposal.setCancelled(false);
        proposal.setTransactionHash(receipt.getTransactionHash());
        proposal.setBlockNumber(receipt.getBlockNumber().longValueExact());
        proposal.setCreatedAt(proposal.getCreatedAt() == null ? eventTimestamp : proposal.getCreatedAt());
        proposal.setUpdatedAt(Instant.now());

        proposalDAO.save(proposal);
    }

    @Transactional
    public void recordVoteCast(String transactionHash) {
        TransactionReceipt receipt = requireReceipt(transactionHash);
        Log log = requireEventLog(receipt, VOTE_CAST_EVENT, "VoteCast");
        Instant eventTimestamp = resolveReceiptTimestamp(receipt);

        long proposalId = decodeUint256Topic(log.getTopics().get(1)).longValueExact();
        String eventKey = buildEventKey(TYPE_VOTE_CAST, log);
        if (voteDAO.findByEventKey(eventKey).isPresent()) {
            return;
        }

        InsuranceGovernanceProposal proposal = ensureProposalExists(proposalId);
        String voter = decodeAddressTopic(log.getTopics().get(2));
        List<Type> values = FunctionReturnDecoder.decode(
                log.getData(),
                VOTE_CAST_EVENT.getNonIndexedParameters()
        );

        int supportCode = ((Uint8) values.get(0)).getValue().intValue();
        String support = switch (supportCode) {
            case 1 -> "FOR";
            case 2 -> "ABSTAIN";
            default -> "AGAINST";
        };
        String weightWei = ((Uint256) values.get(1)).getValue().toString();

        voteDAO.save(InsuranceGovernanceVote.builder()
                .eventKey(eventKey)
                .proposalId(proposalId)
                .voter(voter)
                .support(support)
                .weightWei(weightWei)
                .transactionHash(receipt.getTransactionHash())
                .blockNumber(receipt.getBlockNumber().longValueExact())
                .logIndex(log.getLogIndex().intValueExact())
                .eventTimestamp(eventTimestamp)
                .createdAt(Instant.now())
                .build());

        if ("FOR".equals(support)) {
            proposal.setForVotesWei(addWei(proposal.getForVotesWei(), weightWei));
        } else if ("ABSTAIN".equals(support)) {
            proposal.setAbstainVotesWei(addWei(proposal.getAbstainVotesWei(), weightWei));
        } else {
            proposal.setAgainstVotesWei(addWei(proposal.getAgainstVotesWei(), weightWei));
        }
        proposal.setUpdatedAt(Instant.now());
        proposalDAO.save(proposal);
    }

    @Transactional
    public void recordProposalExecuted(String transactionHash) {
        TransactionReceipt receipt = requireReceipt(transactionHash);
        Log log = requireEventLog(receipt, PROPOSAL_EXECUTED_EVENT, "ProposalExecuted");
        Instant eventTimestamp = resolveReceiptTimestamp(receipt);
        long proposalId = decodeUint256Topic(log.getTopics().get(1)).longValueExact();

        InsuranceGovernanceProposal proposal = ensureProposalExists(proposalId);
        proposal.setExecuted(true);
        proposal.setStatus("EXECUTED");
        proposal.setExecutedAt(eventTimestamp);
        proposal.setUpdatedAt(Instant.now());
        proposalDAO.save(proposal);
    }

    @Transactional
    public void recordProposalCancelled(String transactionHash) {
        TransactionReceipt receipt = requireReceipt(transactionHash);
        Log log = requireEventLog(receipt, PROPOSAL_CANCELLED_EVENT, "ProposalCancelled");
        Instant eventTimestamp = resolveReceiptTimestamp(receipt);
        long proposalId = decodeUint256Topic(log.getTopics().get(1)).longValueExact();

        InsuranceGovernanceProposal proposal = ensureProposalExists(proposalId);
        proposal.setCancelled(true);
        proposal.setStatus("CANCELLED");
        proposal.setCancelledAt(eventTimestamp);
        proposal.setUpdatedAt(Instant.now());
        proposalDAO.save(proposal);
    }

    private InsuranceGovernanceProposal ensureProposalExists(long proposalId) {
        return proposalDAO.findById(proposalId)
                .orElseGet(() -> backfillProposalFromChain(proposalId));
    }

    private InsuranceGovernanceProposal backfillProposalFromChain(long proposalId) {
        GovernanceProposalStruct proposalStruct = fetchProposalFromChain(proposalId);
        InsuranceGovernanceProposal proposal = InsuranceGovernanceProposal.builder()
                .proposalId(proposalStruct.proposalId.longValueExact())
                .proposer(proposalStruct.proposer)
                .title(proposalStruct.title)
                .description(proposalStruct.description)
                .insuranceType(proposalStruct.insuranceType)
                .snapshotBlock(proposalStruct.snapshotBlock.longValueExact())
                .startBlock(proposalStruct.startBlock.longValueExact())
                .endBlock(proposalStruct.endBlock.longValueExact())
                .quorumVotesWei(proposalStruct.quorumVotes.toString())
                .forVotesWei(proposalStruct.forVotes.toString())
                .againstVotesWei(proposalStruct.againstVotes.toString())
                .abstainVotesWei(proposalStruct.abstainVotes.toString())
                .status("PENDING")
                .executed(proposalStruct.executed)
                .cancelled(proposalStruct.cancelled)
                .transactionHash("BACKFILLED")
                .blockNumber(proposalStruct.startBlock.longValueExact())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .executedAt(proposalStruct.executed ? Instant.now() : null)
                .cancelledAt(proposalStruct.cancelled ? Instant.now() : null)
                .build();
        return proposalDAO.save(proposal);
    }

    private GovernanceProposalStruct fetchProposalFromChain(long proposalId) {
        try {
            Function function = new Function(
                    "getProposal",
                    List.of(new Uint256(BigInteger.valueOf(proposalId))),
                    List.of(new TypeReference<GovernanceProposalStruct>() {})
            );

            EthCall response = governanceWeb3j.ethCall(
                    Transaction.createEthCallTransaction(
                            null,
                            normalizeContractAddress(governanceContractAddress),
                            FunctionEncoder.encode(function)
                    ),
                    DefaultBlockParameterName.LATEST
            ).send();

            List<Type> decoded = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
            if (decoded.isEmpty()) {
                throw new IllegalStateException("Governance proposal does not exist on-chain");
            }

            return (GovernanceProposalStruct) decoded.get(0);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to backfill governance proposal from chain", exception);
        }
    }

    private TransactionReceipt requireReceipt(String transactionHash) {
        try {
            if (transactionHash == null || transactionHash.isBlank()) {
                throw new IllegalArgumentException("Governance transaction hash is required");
            }

            String normalizedHash = transactionHash.trim();
            Optional<TransactionReceipt> optionalReceipt = Optional.empty();
            for (int attempt = 1; attempt <= RECEIPT_LOOKUP_ATTEMPTS; attempt++) {
                optionalReceipt = governanceWeb3j
                        .ethGetTransactionReceipt(normalizedHash)
                        .send()
                        .getTransactionReceipt();

                if (optionalReceipt.isPresent()) {
                    break;
                }

                if (attempt < RECEIPT_LOOKUP_ATTEMPTS) {
                    Thread.sleep(RECEIPT_LOOKUP_DELAY_MS);
                }
            }

            TransactionReceipt receipt = optionalReceipt.orElseThrow(
                    () -> new IllegalStateException("Governance transaction receipt was not found")
            );

            if (!"0x1".equals(receipt.getStatus())) {
                throw new IllegalStateException("Governance transaction was not successful on-chain");
            }

            return receipt;
        } catch (IllegalStateException | IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to load governance transaction receipt", exception);
        }
    }

    private Log requireEventLog(TransactionReceipt receipt, Event event, String eventName) {
        String expectedTopic = hexTopic(EventEncoder.encode(event));

        return receipt.getLogs().stream()
                .filter(log -> normalizeContractAddress(log.getAddress())
                        .equalsIgnoreCase(normalizeContractAddress(governanceContractAddress)))
                .filter(log -> log.getTopics() != null && !log.getTopics().isEmpty())
                .filter(log -> hexTopic(log.getTopics().get(0)).equalsIgnoreCase(expectedTopic))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(eventName + " event was not found in the transaction receipt"));
    }

    private Instant resolveReceiptTimestamp(TransactionReceipt receipt) {
        try {
            EthBlock blockResponse = governanceWeb3j.ethGetBlockByHash(receipt.getBlockHash(), false).send();
            if (blockResponse.getBlock() != null && blockResponse.getBlock().getTimestamp() != null) {
                return Instant.ofEpochSecond(blockResponse.getBlock().getTimestamp().longValueExact());
            }
        } catch (Exception exception) {
            log.warn("Failed to resolve governance receipt timestamp for tx={}", receipt.getTransactionHash(), exception);
        }

        return Instant.now();
    }

    private BigInteger decodeUint256Topic(String topic) {
        return Numeric.toBigInt(topic);
    }

    private String decodeAddressTopic(String topic) {
        return "0x" + stripHexPrefix(topic).substring(stripHexPrefix(topic).length() - 40);
    }

    private String stripHexPrefix(String value) {
        return value != null && value.startsWith("0x") ? value.substring(2) : value;
    }

    private String hexTopic(String topic) {
        return Numeric.prependHexPrefix(stripHexPrefix(topic));
    }

    private String normalizeContractAddress(String address) {
        return Numeric.prependHexPrefix(stripHexPrefix(address == null ? "" : address.trim()));
    }

    private String addWei(String left, String right) {
        BigInteger leftValue = new BigInteger(defaultWei(left));
        BigInteger rightValue = new BigInteger(defaultWei(right));
        return leftValue.add(rightValue).toString();
    }

    private String buildEventKey(String eventType, Log logEntry) {
        return eventType + ":" + logEntry.getTransactionHash() + ":" + logEntry.getLogIndex();
    }

    private String defaultWei(String amountWei) {
        return amountWei == null || amountWei.isBlank() ? "0" : amountWei;
    }

    public static class GovernanceProposalStruct extends DynamicStruct {
        public BigInteger proposalId;
        public String title;
        public String description;
        public String insuranceType;
        public String proposer;
        public BigInteger snapshotBlock;
        public BigInteger startBlock;
        public BigInteger endBlock;
        public BigInteger quorumVotes;
        public BigInteger forVotes;
        public BigInteger againstVotes;
        public BigInteger abstainVotes;
        public boolean executed;
        public boolean cancelled;

        public GovernanceProposalStruct(
                BigInteger proposalId,
                String title,
                String description,
                String insuranceType,
                String proposer,
                BigInteger snapshotBlock,
                BigInteger startBlock,
                BigInteger endBlock,
                BigInteger quorumVotes,
                BigInteger forVotes,
                BigInteger againstVotes,
                BigInteger abstainVotes,
                boolean executed,
                boolean cancelled
        ) {
            super(
                    new Uint256(proposalId),
                    new Utf8String(title),
                    new Utf8String(description),
                    new Utf8String(insuranceType),
                    new Address(proposer),
                    new Uint256(snapshotBlock),
                    new Uint256(startBlock),
                    new Uint256(endBlock),
                    new Uint256(quorumVotes),
                    new Uint256(forVotes),
                    new Uint256(againstVotes),
                    new Uint256(abstainVotes),
                    new Bool(executed),
                    new Bool(cancelled)
            );
            this.proposalId = proposalId;
            this.title = title;
            this.description = description;
            this.insuranceType = insuranceType;
            this.proposer = proposer;
            this.snapshotBlock = snapshotBlock;
            this.startBlock = startBlock;
            this.endBlock = endBlock;
            this.quorumVotes = quorumVotes;
            this.forVotes = forVotes;
            this.againstVotes = againstVotes;
            this.abstainVotes = abstainVotes;
            this.executed = executed;
            this.cancelled = cancelled;
        }

        public GovernanceProposalStruct(
                Uint256 proposalId,
                Utf8String title,
                Utf8String description,
                Utf8String insuranceType,
                Address proposer,
                Uint256 snapshotBlock,
                Uint256 startBlock,
                Uint256 endBlock,
                Uint256 quorumVotes,
                Uint256 forVotes,
                Uint256 againstVotes,
                Uint256 abstainVotes,
                Bool executed,
                Bool cancelled
        ) {
            super(
                    proposalId,
                    title,
                    description,
                    insuranceType,
                    proposer,
                    snapshotBlock,
                    startBlock,
                    endBlock,
                    quorumVotes,
                    forVotes,
                    againstVotes,
                    abstainVotes,
                    executed,
                    cancelled
            );
            this.proposalId = proposalId.getValue();
            this.title = title.getValue();
            this.description = description.getValue();
            this.insuranceType = insuranceType.getValue();
            this.proposer = proposer.getValue();
            this.snapshotBlock = snapshotBlock.getValue();
            this.startBlock = startBlock.getValue();
            this.endBlock = endBlock.getValue();
            this.quorumVotes = quorumVotes.getValue();
            this.forVotes = forVotes.getValue();
            this.againstVotes = againstVotes.getValue();
            this.abstainVotes = abstainVotes.getValue();
            this.executed = executed.getValue();
            this.cancelled = cancelled.getValue();
        }
    }
}
