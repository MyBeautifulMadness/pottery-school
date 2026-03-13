package com.example.PotteryPotSchool.service.UserActivation;

import com.example.PotteryPotSchool.dto.UserActivation.UserActivationCreateRequest;
import com.example.PotteryPotSchool.dto.UserActivation.UserActivationDetails;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserActivation {
    UserActivationDetails activation(UserActivationCreateRequest request);
}
