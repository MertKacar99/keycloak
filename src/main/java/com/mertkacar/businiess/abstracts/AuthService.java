package com.mertkacar.businiess.abstracts;

import com.mertkacar.dto.requests.RegisterRequest;

public interface AuthService {
    String register(RegisterRequest req);
}
