package com.ssafy.yumyum.service;

import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ssafy.yumyum.model.User;
import com.ssafy.yumyum.repository.UserRepository;
import com.ssafy.yumyum.util.IdGenerator;
import com.ssafy.yumyum.util.ServiceResult;

@Service
public class AuthService {

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]?\\$\\d{2}\\$.*$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ServiceResult<User> login(String email, String password) {
        String trimmedEmail = email == null ? "" : email.trim();
        User user = userRepository.findByEmail(trimmedEmail);

        if (user == null) {
            return ServiceResult.failure("등록되지 않은 이메일입니다.");
        }

        if (!user.isActive()) {
            return ServiceResult.failure("비활성화된 계정입니다.");
        }

        if (!isPasswordMatched(user, password)) {
            return ServiceResult.failure("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        return ServiceResult.success("로그인되었습니다.", user);
    }

    public ServiceResult<User> register(String email, String password, String nickname, String gender,
                                        int birthYear, double height, double weight, String goal, String healthNote) {
        if (email == null || !email.contains("@")) {
            return ServiceResult.failure("올바른 이메일을 입력해 주세요.");
        }

        if (password == null || password.length() < 8) {
            return ServiceResult.failure("비밀번호는 8자 이상이어야 합니다.");
        }

        if (nickname == null || nickname.trim().isEmpty()) {
            return ServiceResult.failure("닉네임을 입력해 주세요.");
        }

        String trimmedEmail = email.trim();
        if (userRepository.findByEmail(trimmedEmail) != null) {
            return ServiceResult.failure("이미 사용 중인 이메일입니다.");
        }

        User user = new User();
        user.setId(IdGenerator.next("USER"));
        user.setEmail(trimmedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname.trim());
        user.setGender(gender);
        user.setBirthYear(birthYear);
        user.setHeight(height);
        user.setWeight(weight);
        user.setGoal(goal);
        user.setHealthNote(healthNote == null ? "" : healthNote.trim());
        user.setActive(true);

        userRepository.save(user);
        return ServiceResult.success("회원가입이 완료되었습니다.", user);
    }

    private boolean isPasswordMatched(User user, String rawPassword) {
        String storedPassword = user.getPassword();
        if (storedPassword == null || rawPassword == null) {
            return false;
        }

        if (isEncodedPassword(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        if (!storedPassword.equals(rawPassword)) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        return true;
    }

    private boolean isEncodedPassword(String password) {
        return BCRYPT_PATTERN.matcher(password).matches();
    }
}
