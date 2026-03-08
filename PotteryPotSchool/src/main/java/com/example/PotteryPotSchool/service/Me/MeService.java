package com.example.PotteryPotSchool.service.Me;

import com.example.PotteryPotSchool.dto.Profiles.Profile;
import com.example.PotteryPotSchool.dto.Users.User;

public interface MeService {

    User getMe();
    Profile getMyProfile();

}
