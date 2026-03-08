package com.example.PotteryPotSchool.security;

import java.util.UUID;

public interface CurrentUserProvider {
    UUID getCurrentUserId();
}
