package com.example.PotteryPotSchool.dto.Students;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class StudentDetailsDto {

    private UUID id;
    private String fullName;
    private String email;
    private String about;

}
