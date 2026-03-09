package com.example.PotteryPotSchool.dto.Students;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class StudentSummaryDto {

    private UUID id;
    private String fullName;
    private String email;

}
