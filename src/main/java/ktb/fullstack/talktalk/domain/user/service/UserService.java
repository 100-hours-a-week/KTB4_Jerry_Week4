package ktb.fullstack.talktalk.domain.user.service;

import jakarta.validation.Valid;
import ktb.fullstack.talktalk.domain.auth.repository.SessionRepository;
import ktb.fullstack.talktalk.domain.user.domain.User;
import ktb.fullstack.talktalk.domain.user.dto.request.UserInfoUpdateRequestDto;
import ktb.fullstack.talktalk.domain.user.dto.request.UserPasswordUpdateRequestDto;
import ktb.fullstack.talktalk.domain.user.dto.request.UserSignupRequestDto;
import ktb.fullstack.talktalk.domain.user.dto.response.AvailabilityResponseDto;
import ktb.fullstack.talktalk.domain.user.dto.response.UserInfoResponseDto;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import ktb.fullstack.talktalk.global.common.response.CreateResponseDto;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    /**
     *  1. 회원가입 (유저 생성)
     */
    public CreateResponseDto createUser(@Valid UserSignupRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        User user = new User(
                request.getEmail(),
                request.getPassword(),
                request.getNickname()
        );
        user.updateProfileImageUrl(request.getProfileImageUrl());

        User savedUser = userRepository.save(user);
        return new CreateResponseDto(savedUser.getId());
    }

    /**
     *  2. 이메일 중복 검사
     */
    public AvailabilityResponseDto checkEmailAvailability(String email) {

        boolean isAvailable = !userRepository.existsByEmail(email);
        return new AvailabilityResponseDto(isAvailable);
    }

    /**
     *  3. 닉네임 중복 검사
     */
    public AvailabilityResponseDto checkNicknameAvailability(String nickname) {

        boolean isAvailable = !userRepository.existsByNickname(nickname);
        return new AvailabilityResponseDto(isAvailable);
    }

    /**
     *  4. 내 회원정보 조회
     */
    public UserInfoResponseDto getMyInfo(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
        return new UserInfoResponseDto(user.getId(), user.getEmail(), user.getNickname(), user.getProfileImageUrl());
    }

    /**
     *  5. 내 회원정보 변경
     */
    public UserInfoResponseDto updateMyInfo(Long userId, UserInfoUpdateRequestDto request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        String newNickname = request.getNickname();
        if (newNickname != null && !newNickname.equals(user.getNickname())) {
            if (userRepository.existsByNickname(newNickname)) {
                throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
            }
            user.updateNickname(newNickname);
        }

        String newProfileImageUrl = request.getProfileImageUrl();
        if (newProfileImageUrl != null && !newProfileImageUrl.equals(user.getProfileImageUrl())) {
            user.updateProfileImageUrl(newProfileImageUrl);
        }

        userRepository.save(user);
        return new UserInfoResponseDto(user.getId(), user.getEmail(), user.getNickname(), user.getProfileImageUrl());
    }

    /**
     *  6. 비밀번호 변경
     */
    public void updatePassword(Long userId, UserPasswordUpdateRequestDto request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        user.updatePassword(request.getPassword());
        userRepository.save(user);
    }

    /**
     *  7. 회원 탈퇴
     */
    public void deleteMyAccount(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        userRepository.deleteById(user.getId());
        sessionRepository.deleteByUserId(user.getId());
    }

}
