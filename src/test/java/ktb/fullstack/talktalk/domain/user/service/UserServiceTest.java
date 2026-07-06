package ktb.fullstack.talktalk.domain.user.service;

import ktb.fullstack.talktalk.domain.auth.repository.SessionRepository;
import ktb.fullstack.talktalk.domain.image.entity.Image;
import ktb.fullstack.talktalk.domain.image.repository.ImageRepository;
import ktb.fullstack.talktalk.domain.user.dto.request.UserInfoUpdateRequestDto;
import ktb.fullstack.talktalk.domain.user.dto.request.UserPasswordUpdateRequestDto;
import ktb.fullstack.talktalk.domain.user.dto.request.UserSignupRequestDto;
import ktb.fullstack.talktalk.domain.user.dto.response.AvailabilityResponseDto;
import ktb.fullstack.talktalk.domain.user.dto.response.UserInfoResponseDto;
import ktb.fullstack.talktalk.domain.user.entity.ProfileImage;
import ktb.fullstack.talktalk.domain.user.entity.User;
import ktb.fullstack.talktalk.domain.user.repository.ProfileImageRepository;
import ktb.fullstack.talktalk.domain.user.repository.UserRepository;
import ktb.fullstack.talktalk.global.common.response.CreateResponseDto;
import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    SessionRepository sessionRepository;

    @Mock
    ImageRepository imageRepository;

    @Mock
    ProfileImageRepository profileImageRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    private User userFixture(Long id, String email, String nickname) {
        User user = new User(email, "encodedPw", nickname);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Image imageFixture(Long id, String fileName) {
        Image image = new Image(fileName);
        ReflectionTestUtils.setField(image, "id", id);
        return image;
    }

    @Test
    void 프로필이미지_없이_정상_회원가입() {

        UserSignupRequestDto request = new UserSignupRequestDto(
                "aaa@aaa.aaa", "rawPw1!", "jerry", null);
        when(userRepository.existsByEmail("aaa@aaa.aaa")).thenReturn(false);
        when(userRepository.existsByNickname("jerry")).thenReturn(false);
        when(passwordEncoder.encode("rawPw1!")).thenReturn("encodedPw");
        when(userRepository.save(any(User.class))).
                thenReturn(userFixture(1L, "aaa@aaa.aaa", "jerry"));

        CreateResponseDto result = userService.createUser(request);

        assertEquals(1L, result.getId());
        verify(profileImageRepository, never()).save(any());
    }

    @Test
    void 프로필이미지_함께_정상_회원가입() {

        UserSignupRequestDto request = new UserSignupRequestDto(
                "aaa@aaa.aaa", "rawPw1!", "jerry", 10L);
        when(userRepository.existsByEmail("aaa@aaa.aaa")).thenReturn(false);
        when(userRepository.existsByNickname("jerry")).thenReturn(false);
        when(passwordEncoder.encode("rawPw1!")).thenReturn("encodedPw");
        when(userRepository.save(any(User.class))).
                thenReturn(userFixture(1L, "aaa@aaa.aaa", "jerry"));
        when(imageRepository.findById(10L)).thenReturn(Optional.of(imageFixture(10L, "jerry.png")));

        CreateResponseDto result = userService.createUser(request);

        assertEquals(1L, result.getId());
        verify(profileImageRepository, times(1)).save(any(ProfileImage.class));
    }

    @Test
    void 예외_회원가입_이메일_중복() {

        UserSignupRequestDto request = new UserSignupRequestDto(
                "aaa@aaa.aaa", "rawPw1!", "jerry", null);
        when(userRepository.existsByEmail("aaa@aaa.aaa")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.createUser(request));

        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, ex.getErrorCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void 예외_회원가입_닉네임_중복() {

        UserSignupRequestDto request = new UserSignupRequestDto(
                "aaa@aaa.aaa", "rawPw1!", "jerry", null);
        when(userRepository.existsByEmail("aaa@aaa.aaa")).thenReturn(false);
        when(userRepository.existsByNickname("jerry")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.createUser(request));

        assertEquals(ErrorCode.NICKNAME_ALREADY_EXISTS, ex.getErrorCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void 비밀번호_인코딩_저장_검사() {

        UserSignupRequestDto request = new UserSignupRequestDto(
                "aaa@aaa.aaa", "rawPw1!", "jerry", null);
        when(userRepository.existsByEmail("aaa@aaa.aaa")).thenReturn(false);
        when(userRepository.existsByNickname("jerry")).thenReturn(false);
        when(passwordEncoder.encode("rawPw1!")).thenReturn("encodedPw");
        when(userRepository.save(any(User.class)))
                .thenReturn(userFixture(1L, "aaa@aaa.aaa", "jerry"));

        userService.createUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("encodedPw", saved.getPassword());
        assertNotEquals("rawPw1!", saved.getPassword());
    }

    @Test
    void 예외_회원가입_요청_이미지_없음() {

        UserSignupRequestDto request = new UserSignupRequestDto(
                "aaa@aaa.aaa", "rawPw1!", "jerry", 99L);
        when(userRepository.existsByEmail("aaa@aaa.aaa")).thenReturn(false);
        when(userRepository.existsByNickname("jerry")).thenReturn(false);
        when(passwordEncoder.encode("rawPw1!")).thenReturn("encodedPw");
        when(userRepository.save(any(User.class)))
                .thenReturn(userFixture(1L, "aaa@aaa.aaa", "jerry"));
        when(imageRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.createUser(request));

        assertEquals(ErrorCode.IMAGE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void 이메일_사용가능() {

        when(userRepository.existsByEmail("aaa@aaa.aaa")).thenReturn(false);

        AvailabilityResponseDto result = userService.checkEmailAvailability("aaa@aaa.aaa");

        assertTrue(result.isAvailable());
    }

    @Test
    void 이메일_사용불가() {

        when(userRepository.existsByEmail("aaa@aaa.aaa")).thenReturn(true);

        AvailabilityResponseDto result = userService.checkEmailAvailability("aaa@aaa.aaa");

        assertFalse(result.isAvailable());
    }

    @Test
    void 닉네임_사용가능() {

        when(userRepository.existsByNickname("jerry")).thenReturn(false);

        AvailabilityResponseDto result = userService.checkNicknameAvailability("jerry");

        assertTrue(result.isAvailable());
    }

    @Test
    void 닉네임_사용불가() {

        when(userRepository.existsByNickname("jerry")).thenReturn(true);

        AvailabilityResponseDto result = userService.checkNicknameAvailability("jerry");

        assertFalse(result.isAvailable());
    }

    @Test
    void 개인정보_조회_프로필이미지_있으면_url_조립() {

        User user = userFixture(1L, "aaa@aaa.aaa", "jerry");
        Image image = imageFixture(5L, "jerry.png");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileImageRepository.findByUserIdAndCurrentTrue(1L))
                .thenReturn(Optional.of(new ProfileImage(user, image, true)));

        UserInfoResponseDto result = userService.getMyInfo(1L);

        assertEquals(1L, result.getId());
        assertEquals("aaa@aaa.aaa", result.getEmail());
        assertEquals("jerry", result.getNickname());
        assertEquals("/images/jerry.png", result.getProfileImageUrl());
    }

    @Test
    void 개인정보_조회_프로필이미지_없으면_url은_null() {

        User user = userFixture(1L, "aaa@aaa.aaa", "jerry");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileImageRepository.findByUserIdAndCurrentTrue(1L)).thenReturn(Optional.empty());

        UserInfoResponseDto result = userService.getMyInfo(1L);

        assertEquals("jerry", result.getNickname());
        assertNull(result.getProfileImageUrl());
    }

    @Test
    void 예외_개인정보_조회_유저_없음() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.getMyInfo(1L));

        assertEquals(ErrorCode.INVALID_TOKEN, ex.getErrorCode());
    }

    @Test
    void 닉네임_정상_변경() {

        User user = userFixture(1L, "aaa@aaa.aaa", "oldNick");
        UserInfoUpdateRequestDto request = new UserInfoUpdateRequestDto("newNick", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname("newNick")).thenReturn(false);
        when(profileImageRepository.findByUserIdAndCurrentTrue(1L)).thenReturn(Optional.empty());

        UserInfoResponseDto result = userService.updateMyInfo(1L, request);

        assertEquals("newNick", result.getNickname());
        assertEquals("newNick", user.getNickname()); // 변경 감지 회고에 쓰기
    }

    @Test
    void 닉네임_현재와_같으면_중복검사_안함() {

        User user = userFixture(1L, "aaa@aaa.aaa", "jerry");
        UserInfoUpdateRequestDto request = new UserInfoUpdateRequestDto("jerry", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileImageRepository.findByUserIdAndCurrentTrue(1L)).thenReturn(Optional.empty());

        userService.updateMyInfo(1L, request);

        verify(userRepository, never()).existsByNickname(any());
        assertEquals("jerry", user.getNickname());
    }

    @Test
    void 닉네임_null이면_변경_안함() {

        User user = userFixture(1L, "aaa@aaa.aaa", "jerry");
        UserInfoUpdateRequestDto request = new UserInfoUpdateRequestDto(null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileImageRepository.findByUserIdAndCurrentTrue(1L)).thenReturn(Optional.empty());

        userService.updateMyInfo(1L, request);

        verify(userRepository, never()).existsByNickname(any());
        assertEquals("jerry", user.getNickname());
    }

    @Test
    void 예외_닉네임_변경_중복() {

        User user = userFixture(1L, "aaa@aaa.aaa", "oldNick");
        UserInfoUpdateRequestDto request = new UserInfoUpdateRequestDto("dupNick", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname("dupNick")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.updateMyInfo(1L, request));

        assertEquals(ErrorCode.NICKNAME_ALREADY_EXISTS, ex.getErrorCode());
        assertEquals("oldNick", user.getNickname());
    }

    @Test
    void 프로필이미지_교체_기존_이미지가_있으면_재사용() {

        User user = userFixture(1L, "aaa@aaa.aaa", "jerry");
        Image oldImage = imageFixture(5L, "old.png");
        Image newImage = imageFixture(6L, "new.png");
        ProfileImage prev = new ProfileImage(user, oldImage, true);
        ProfileImage existing = new ProfileImage(user, newImage, false);

        UserInfoUpdateRequestDto request = new UserInfoUpdateRequestDto(null, 6L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileImageRepository.findByUserIdAndCurrentTrue(1L)).thenReturn(Optional.of(prev));
        when(profileImageRepository.findByUserIdAndImageId(1L, 6L)).thenReturn(Optional.of(existing));

        userService.updateMyInfo(1L, request);

        assertFalse(prev.isCurrent());
        assertTrue(existing.isCurrent());
        verify(profileImageRepository, never()).save(any());
    }

    @Test
    void 프로필이미지_교체_기존_이미지가_없으면_새로_생성() {

        User user = userFixture(1L, "aaa@aaa.aaa", "jerry");
        Image newImage = imageFixture(6L, "new.png");

        UserInfoUpdateRequestDto request = new UserInfoUpdateRequestDto(null, 6L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileImageRepository.findByUserIdAndCurrentTrue(1L)).thenReturn(Optional.empty());
        when(profileImageRepository.findByUserIdAndImageId(1L, 6L)).thenReturn(Optional.empty());
        when(imageRepository.findById(6L)).thenReturn(Optional.of(newImage));
        when(profileImageRepository.save(any(ProfileImage.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        userService.updateMyInfo(1L, request);

        verify(profileImageRepository, times(1)).save(any(ProfileImage.class));
    }

    @Test
    void 예외_프로필이미지_교체_이미지가_없음() {

        User user = userFixture(1L, "aaa@aaa.aaa", "jerry");
        UserInfoUpdateRequestDto request = new UserInfoUpdateRequestDto(null, 99L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileImageRepository.findByUserIdAndCurrentTrue(1L)).thenReturn(Optional.empty());
        when(profileImageRepository.findByUserIdAndImageId(1L, 99L)).thenReturn(Optional.empty());
        when(imageRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.updateMyInfo(1L, request));

        assertEquals(ErrorCode.IMAGE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void 예외_정보_수정_유저_없음() {

        UserInfoUpdateRequestDto request = new UserInfoUpdateRequestDto("newNick", null);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.updateMyInfo(1L, request));

        assertEquals(ErrorCode.INVALID_TOKEN, ex.getErrorCode());
    }

    @Test
    void 비밀번호_정상_변경() {

        User user = userFixture(1L, "aaa@aaa.aaa", "jerry");
        UserPasswordUpdateRequestDto request = new UserPasswordUpdateRequestDto("newRawPw1!");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newRawPw1!")).thenReturn("newEncodedPw");

        userService.updatePassword(1L, request);

        assertEquals("newEncodedPw", user.getPassword());
    }

    @Test
    void 예외_비밀번호_변경_유저_없음() {

        UserPasswordUpdateRequestDto request = new UserPasswordUpdateRequestDto("newRawPw1!");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.updatePassword(1L, request));

        assertEquals(ErrorCode.INVALID_TOKEN, ex.getErrorCode());
    }

    @Test
    void 회원_정상_탈퇴() {

        User user = userFixture(1L, "aaa@aaa.aaa", "jerry");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteMyAccount(1L);

        assertNotNull(user.getDeletedAt());
        verify(sessionRepository, times(1)).deleteByUserId(1L);
    }

    @Test
    void 예외_회원탈퇴_유저_없음() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.deleteMyAccount(1L));

        assertEquals(ErrorCode.INVALID_TOKEN, ex.getErrorCode());
        verify(sessionRepository, never()).deleteByUserId(any());
    }
}
