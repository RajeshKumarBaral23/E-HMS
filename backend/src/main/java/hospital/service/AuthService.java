package hospital.service;

import hospital.dto.AuthRequest;
import hospital.dto.AuthResponse;
import hospital.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest registerRequest);
    AuthResponse login(AuthRequest authRequest);
}
