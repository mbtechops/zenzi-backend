package com.zenzi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipientResponse {

    private String status;

    private String message;

    private RecipientDTO data;
}