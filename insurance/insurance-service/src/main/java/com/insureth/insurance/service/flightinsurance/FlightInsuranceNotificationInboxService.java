package com.insureth.insurance.service.flightinsurance;

import com.insureth.insurance.domain.repository.FlightInsuranceNotificationRecordDAO;
import com.insureth.insurance.model.dto.FlightInsuranceNotificationModel;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FlightInsuranceNotificationInboxService {

    private final FlightInsuranceNotificationRecordDAO flightInsuranceNotificationRecordDAO;

    @Transactional(readOnly = true)
    public List<FlightInsuranceNotificationModel> getNotifications(String walletAddress, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        return flightInsuranceNotificationRecordDAO
                .findByHolderIgnoreCaseOrderByEventTimestampDescCreatedAtDesc(
                        walletAddress,
                        PageRequest.of(0, safeLimit)
                )
                .stream()
                .map(record -> FlightInsuranceNotificationModel.builder()
                        .id(record.getEventKey())
                        .policyId(record.getPolicyId())
                        .type(record.getType())
                        .title(record.getTitle())
                        .message(record.getMessage())
                        .timestamp(record.getEventTimestamp())
                        .amountEth(formatWeiToEth(record.getAmountWei()))
                        .build())
                .toList();
    }

    private String formatWeiToEth(String amountWei) {
        if (amountWei == null || amountWei.isBlank()) {
            return null;
        }

        BigDecimal ethAmount = new BigDecimal(new BigInteger(amountWei))
                .divide(BigDecimal.TEN.pow(18), 18, RoundingMode.HALF_UP)
                .stripTrailingZeros();

        return ethAmount.scale() < 0 ? ethAmount.setScale(0).toPlainString() : ethAmount.toPlainString();
    }
}
