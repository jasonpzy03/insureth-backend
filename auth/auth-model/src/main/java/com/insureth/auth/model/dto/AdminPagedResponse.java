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
public class AdminPagedResponse<T> {
    private List<T> items;
    private long totalElements;
    private int page;
    private int size;
}
