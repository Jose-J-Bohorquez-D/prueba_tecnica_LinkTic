package com.linktic.test.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SingleResponse<T> {
    private T data;
}
