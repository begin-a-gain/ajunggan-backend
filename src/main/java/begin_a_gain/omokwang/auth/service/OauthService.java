package begin_a_gain.omokwang.auth.service;

import begin_a_gain.omokwang.auth.dto.OauthDto;
import begin_a_gain.omokwang.exception.CustomException;
import begin_a_gain.omokwang.exception.ErrorCode;
import begin_a_gain.omokwang.user.dto.User;
import begin_a_gain.omokwang.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OauthService {
    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final KakaoOauthService kakaoOauthService;

    //카카오 로그인
    public OauthDto loginWithKakao(String accessToken, HttpServletResponse response) {
        User user = kakaoOauthService.getUserProfileByToken(accessToken);
        return new OauthDto(getTokens(user.getSocialId(), response), user.getSocialId());
    }

    //액세스토큰, 리프레시토큰 생성
    public String getTokens(Long id, HttpServletResponse response) {
        final String accessToken = jwtTokenService.createAccessToken(id.toString());
        final String refreshToken = jwtTokenService.createRefreshToken();

        Optional<User> optionalUserInfo = userService.findBySocialId(id);
        optionalUserInfo.ifPresent(user -> {
            user.setRefreshToken(refreshToken);
            userService.updateRefreshToken(user);
        });

        jwtTokenService.addRefreshTokenToCookie(refreshToken, response);
        return accessToken;
    }

    // 리프레시 토큰으로 액세스토큰 새로 갱신
    public String refreshAccessToken(String refreshToken) {
        User user = userService.findByRefreshToken(refreshToken);
        if (user == null) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (!jwtTokenService.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        return jwtTokenService.createAccessToken(user.getSocialId().toString());
    }
}