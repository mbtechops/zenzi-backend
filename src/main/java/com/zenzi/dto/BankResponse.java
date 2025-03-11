package com.zenzi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankResponse {

    private String status;

    private String message;

    private List<BankDTO> data;
}