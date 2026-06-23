package ktb.fullstack.talktalk.domain.user.service;

import ktb.fullstack.talktalk.domain.auth.repository.SessionRepository;
import ktb.fullstack.talktalk.domain.image.entity.Image;
import ktb.fullstack.talktalk.domain.image.repository.ImageRepository;
import ktb.fullstack.talktalk.domain.user.entity.ProfileImage;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.domain.user.dto.request.UserInfoUpdateRequestDto;
import ktb.fullstack.talktalk.domain.user.dto.request.UserPasswordUpdateRequestDto;
import ktb.fullstack.talktalk.domain.user.dto.request.UserSignupRequestDto;
import ktb.fullstack.talktalk.domain.user.dto.response.AvailabilityResponseDto;
import ktb.fullstack.talktalk.domain.user.dto.response.UserInfoResponseDto;
import ktb.fullstack.talktalk.domain.user.repository.ProfileImageRepository;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import ktb.fullstack.talktalk.global.common.response.CreateResponseDto;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String IMAGE_URL_PREFIX = "/images/";

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final ImageRepository imageRepository;
    private final ProfileImageRepository profileImageRepository;

    /**
     * 1. 회원가입 (유저 생성)
     */
    @Transactional
    public CreateResponseDto createUser(UserSignupRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        User savedUser = userRepository.save(
                new User(request.getEmail(), request.getPassword(), request.getNickname()));

        if (request.getProfileImageId() != null) {
            Image image = findImage(request.getProfileImageId());
            profileImageRepository.save(new ProfileImage(savedUser, image, true));
        }

        return new CreateResponseDto(savedUser.getId());
    }

    /**
     * 2. 이메일 중복 검사
     */
    @Transactional(readOnly = true)
    public AvailabilityResponseDto checkEmailAvailability(String email) {

        return new AvailabilityResponseDto(!userRepository.existsByEmail(email));
    }

    /**
     * 3. 닉네임 중복 검사
     */
    @Transactional(readOnly = true)
    public AvailabilityResponseDto checkNicknameAvailability(String nickname) {

        return new AvailabilityResponseDto(!userRepository.existsByNickname(nickname));
    }

    /**
     * 4. 내 회원정보 조회
     */
    @Transactional(readOnly = true)
    public UserInfoResponseDto getMyInfo(Long userId) {

        User user = findUser(userId);
        return new UserInfoResponseDto(
                user.getId(), user.getEmail(), user.getNickname(), currentProfileImageUrl(userId));
    }

    /**
     * 5. 내 회원정보 변경
     */
    @Transactional
    public UserInfoResponseDto updateMyInfo(Long userId, UserInfoUpdateRequestDto request) {

        User user = findUser(userId);

        String newNickname = request.getNickname();
        if (newNickname != null && !newNickname.equals(user.getNickname())) {
            if (userRepository.existsByNickname(newNickname)) {
                throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
            }
            user.updateNickname(newNickname);
        }

        if (request.getProfileImageId() != null) {
            profileImageRepository.findByUserIdAndCurrentTrue(user.getId())
                    .ifPresent(ProfileImage::unmarkCurrent);

            ProfileImage target = profileImageRepository
                    .findByUserIdAndImageId(userId, request.getProfileImageId())
                    .orElseGet(() -> profileImageRepository.save(
                            new ProfileImage(user, findImage(request.getProfileImageId()), false)));

            target.markCurrent();
        }

        return new UserInfoResponseDto(
                user.getId(), user.getEmail(), user.getNickname(), currentProfileImageUrl(userId));
    }

    /**
     * 6. 비밀번호 변경
     */
    @Transactional
    public void updatePassword(Long userId, UserPasswordUpdateRequestDto request) {

        User user = findUser(userId);
        user.updatePassword(request.getPassword());
    }

    /**
     * 7. 회원 탈퇴
     */
    @Transactional
    public void deleteMyAccount(Long userId) {

        User user = findUser(userId);
        user.softDelete();
        sessionRepository.deleteByUserId(user.getId());
    }


    /* ===== 헬퍼 메소드 ===== */

    private String currentProfileImageUrl(Long userId) {
        return profileImageRepository.findByUserIdAndCurrentTrue(userId)
                .map(pi -> IMAGE_URL_PREFIX + pi.getImage().getFileName())
                .orElse(null);
    }

    private Image findImage(Long imageId) {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
    }
}
