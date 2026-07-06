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
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CreateResponseDto createUser(UserSignupRequestDto request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        User savedUser = userRepository.save(
                new User(request.email(), passwordEncoder.encode(request.password()), request.nickname()));

        if (request.profileImageId() != null) {
            Image image = findImage(request.profileImageId());
            profileImageRepository.save(new ProfileImage(savedUser, image, true));
        }

        return new CreateResponseDto(savedUser.getId());
    }

    @Transactional(readOnly = true)
    public AvailabilityResponseDto checkEmailAvailability(String email) {

        return new AvailabilityResponseDto(!userRepository.existsByEmail(email));
    }

    @Transactional(readOnly = true)
    public AvailabilityResponseDto checkNicknameAvailability(String nickname) {

        return new AvailabilityResponseDto(!userRepository.existsByNickname(nickname));
    }

    @Transactional(readOnly = true)
    public UserInfoResponseDto getMyInfo(Long userId) {

        User user = findUser(userId);
        return new UserInfoResponseDto(
                user.getId(), user.getEmail(), user.getNickname(), currentProfileImageUrl(userId));
    }

    @Transactional
    public UserInfoResponseDto updateMyInfo(Long userId, UserInfoUpdateRequestDto request) {

        User user = findUser(userId);

        String newNickname = request.nickname();
        if (newNickname != null && !newNickname.equals(user.getNickname())) {
            if (userRepository.existsByNickname(newNickname)) {
                throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
            }
            user.updateNickname(newNickname);
        }

        if (request.profileImageId() != null) {
            profileImageRepository.findByUserIdAndCurrentTrue(user.getId())
                    .ifPresent(ProfileImage::unmarkCurrent);

            ProfileImage target = profileImageRepository
                    .findByUserIdAndImageId(userId, request.profileImageId())
                    .orElseGet(() -> profileImageRepository.save(
                            new ProfileImage(user, findImage(request.profileImageId()), false)));

            target.markCurrent();
        }

        return new UserInfoResponseDto(
                user.getId(), user.getEmail(), user.getNickname(), currentProfileImageUrl(userId));
    }

    @Transactional
    public void updatePassword(Long userId, UserPasswordUpdateRequestDto request) {

        User user = findUser(userId);
        user.updatePassword(passwordEncoder.encode(request.password()));
    }

    @Transactional
    public void deleteMyAccount(Long userId) {

        User user = findUser(userId);
        user.softDelete();
        sessionRepository.deleteByUserId(user.getId());
    }


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
