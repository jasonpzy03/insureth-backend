package com.insureth.insurance.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class AirLabsTimezoneResponse {
    @JsonProperty("response")
    private List<AirLabsTimezoneItem> response;

    @Data
    public static class AirLabsTimezoneItem {
        @JsonProperty("timezone")
        private String timezone;

        @JsonProperty("country_code")
        private String countryCode;

        @JsonProperty("gmt")
        private Integer gmt;

        @JsonProperty("dst")
        private Integer dst;
    }
}
