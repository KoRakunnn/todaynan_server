package umc.todaynan.service.TokenService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import umc.todaynan.apiPayload.code.status.ErrorStatus;
import umc.todaynan.apiPayload.exception.AuthenticationException;
import umc.todaynan.web.dto.TokenDTO.TokenInfoDTO;

import java.util.Optional;

@Service
public class GoogleTokenService {

    private static final String GOOGLE_TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo";
    private final RestTemplate restTemplate;
    public GoogleTokenService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public TokenInfoDTO.GoogleTokenInfoDTO verifyAccessToken(String accessToken) {
        String url = UriComponentsBuilder.fromHttpUrl(GOOGLE_TOKENINFO_URL)
                .queryParam("access_token", accessToken)
                .toUriString();
        try {
            return restTemplate.getForObject(url, TokenInfoDTO.GoogleTokenInfoDTO.class);
        } catch (HttpClientErrorException e) {
            throw new AuthenticationException(ErrorStatus.USER_ACCESS_TOKEN_NOT_VERITY);
        }
    }
}






