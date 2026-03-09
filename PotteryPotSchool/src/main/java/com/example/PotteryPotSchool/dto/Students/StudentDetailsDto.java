package com.example.PotteryPotSchool.dto.Students;

import com.example.PotteryPotSchool.dto.Profiles.Profile;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class StudentDetailsDto {

    private UUID id;
    private Profile profile;

}
