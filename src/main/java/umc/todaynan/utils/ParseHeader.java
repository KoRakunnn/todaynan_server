package umc.todaynan.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import umc.todaynan.apiPayload.code.status.ErrorStatus;
import umc.todaynan.apiPayload.exception.AuthenticationException;
import umc.todaynan.apiPayload.exception.GeneralException;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.oauth2.TokenService;
import umc.todaynan.repository.UserRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ParseHeader {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    public User parseHeaderToUser(
            HttpServletRequest request
    ){
        String givenToken = tokenService.getJwtFromHeader(request);
        String email = tokenService.getUid(givenToken);
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) return user.get();
        else throw new AuthenticationException(ErrorStatus.USER_ACCESS_TOKEN_NOT_VERITY);
    }

    public Long parseHeaderToUserId(
            HttpServletRequest request
    ){
        String givenToken = tokenService.getJwtFromHeader(request);
        String email = tokenService.getUid(givenToken);
        Optional<Long> userId = userRepository.findUserIdByEmail(email);
        if (userId.isPresent()) return userId.get();
        else throw new AuthenticationException(ErrorStatus.USER_ACCESS_TOKEN_NOT_VERITY);
    }
}
