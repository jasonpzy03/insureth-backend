package com.insureth.auth.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackofficeRoleModel {
    private Long roleId;
    private String name;
    private String description;
    private List<String> rights;
}
