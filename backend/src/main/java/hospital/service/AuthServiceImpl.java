package hospital.service;

import hospital.dto.AuthRequest;
import hospital.dto.AuthResponse;
import hospital.dto.RegisterRequest;
import hospital.entity.Patient;
import hospital.entity.Role;
import hospital.entity.User;
import hospital.repository.PatientRepository;
import hospital.repository.UserRepository;
import hospital.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PatientRepository patientRepository;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        User user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.PATIENT)
                .build();
        userRepository.save(user);

        // Auto-create Patient profile when registering as PATIENT
        if (user.getRole() == Role.PATIENT) {
            Patient.PatientBuilder pb = Patient.builder().user(user);
            if (registerRequest.getSex() != null) pb.sex(registerRequest.getSex());
            if (registerRequest.getAge() != null) pb.age(registerRequest.getAge());
            Patient patient = pb.build();
            patientRepository.save(patient);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerRequest.getEmail(), registerRequest.getPassword())
        );

        String jwt = jwtUtils.generateJwtToken(authentication);

        return new AuthResponse(jwt, "Bearer", user.getEmail(), user.getName(), user.getRole().name());
    }

    @Override
    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );
        String jwt = jwtUtils.generateJwtToken(authentication);

        User user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        return new AuthResponse(jwt, "Bearer", user.getEmail(), user.getName(), user.getRole().name());
    }
}
