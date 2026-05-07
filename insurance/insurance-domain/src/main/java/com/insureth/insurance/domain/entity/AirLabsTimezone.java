package com.insureth.insurance.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "country_timezones")
public class AirLabsTimezone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "gmt_offset")
    private Integer gmtOffset;

    @Column(name = "dst_offset")
    private Integer dstOffset;
}
