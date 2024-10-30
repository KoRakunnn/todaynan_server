package umc.todaynan.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import umc.todaynan.apiPayload.ApiResponse;
import umc.todaynan.apiPayload.code.status.ErrorStatus;
import umc.todaynan.apiPayload.code.status.SuccessStatus;
import umc.todaynan.converter.SearchConverter;
import umc.todaynan.converter.UserConverter;
import umc.todaynan.domain.entity.User.User.User;
import umc.todaynan.domain.entity.User.UserLike.UserLike;
import umc.todaynan.oauth2.TokenService;
import umc.todaynan.repository.UserRepository;
import umc.todaynan.service.GoogleService.GoogleGeminiService;
import umc.todaynan.service.GoogleService.GoogleSearchService;
import umc.todaynan.web.dto.SearchDTO.SearchPlaceDTO;
import umc.todaynan.service.SearchService.SearchImageService;
import umc.todaynan.service.UserService.UserService;
import umc.todaynan.web.dto.SearchDTO.SearchGeminiDTO;
import umc.todaynan.web.dto.SearchDTO.SearchImageDTO;
import umc.todaynan.web.dto.UserDTO.UserRequestDTO;
import umc.todaynan.web.dto.UserDTO.UserResponseDTO;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/place")
public class SearchRestController {
    private final SearchImageService searchImageService;
    private final GoogleGeminiService googleGeminiService;
    private final SearchConverter naverConverter;
    private final UserService userService;
    private final GoogleSearchService googleSearchService;
    private final TokenService tokenService;

    private final UserConverter userConverter;
    private final SearchConverter searchConverter;

    private final UserRepository userRepository;

    /**
     * 장소 검색 API
     * No Authorization
     */
    @Operation(summary = "밖 놀거리 검색 API",description = "No Authorization")
    @GetMapping("/search/outside")
    public ApiResponse<SearchPlaceDTO.GooglePlaceResponseDTO> getSearchLocation(
            HttpServletRequest httpServletRequest,
            @RequestParam(name = "searchString") String searchString,
            @RequestParam(name = "pageToken", required = false) String pageToken) throws IOException {
        String givenToken = tokenService.getJwtFromHeader(httpServletRequest);
        String email = tokenService.getUid(givenToken);//1

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            return ApiResponse.onFailure(ErrorStatus.USER_NOT_EXIST.getCode(), ErrorStatus.USER_NOT_EXIST.getMessage(), null);
        }else {
            return ApiResponse.onSuccess(googleSearchService.searchPlaces(searchString, pageToken, user.get()));
        }
    }

    /**
     * 안에서 놀거리 검색 API
     */
    @Operation(summary = "안 놀거리 검색 API",description = "User Jwt Authorization")
    @GetMapping("/search/inside")
    public ApiResponse<SearchGeminiDTO.GeminiResponseDTO> getSearchInside(HttpServletRequest httpServletRequest) throws JsonProcessingException {
        String givenToken = tokenService.getJwtFromHeader(httpServletRequest);
        String email = tokenService.getUid(givenToken);

        Optional<User> user = userRepository.findByEmail(email);

        if(user.isEmpty())  return ApiResponse.onFailure(ErrorStatus.USER_NOT_EXIST.getCode(), ErrorStatus.USER_NOT_EXIST.getMessage(), null);
        List<String> userPreferTitleList = userService.getPreferCategoryItems(user.get());

        if (userPreferTitleList == null) {
            return ApiResponse.onFailure(ErrorStatus.USER_NOT_EXIST.getCode(), ErrorStatus.USER_NOT_EXIST.getMessage(), null);
        }else {
            Optional<SearchGeminiDTO.GeminiSearchResultDTO> geminiSearchResultDTO
                    = googleGeminiService.getGeminiSearch(userPreferTitleList);

            if(geminiSearchResultDTO.isPresent()) {
                String result = geminiSearchResultDTO.get().getCandidates().get(0).getContent().getParts().get(0).getText();

                SearchGeminiDTO.GeminiResponseDTO geminiResponseDTO = searchConverter.convertJsonToGeminiResponseDTO(result);

                List<SearchImageDTO.NaverImageItems> naverImageItemsList = geminiResponseDTO.getGeminiResponseItemDTOList().stream()
                        .map(item -> {
                            Optional<SearchImageDTO.NaverImageInfo> naverImageInfo = searchImageService.searchImage(item.getTitle());
                            if (naverImageInfo.isPresent()) {
                                return naverImageInfo.get().getItems().get(0);
                            } else {
                                return null;
                            }
                        }).collect(Collectors.toList());

                return ApiResponse.of(SuccessStatus.USER_SEARCH_SUCCESS, naverConverter.toGeminiSearchDTO(geminiResponseDTO, naverImageItemsList, user.get()));
            }else {
                return ApiResponse.onFailure(ErrorStatus.SEARCH_ERROR.getCode(), ErrorStatus.SEARCH_ERROR.getMessage(), null);
            }
        }
    }

    /**
     * 장소 좋아요 API
     * Authorization -> JWT
     */
    @Operation(summary = "장소 좋아요 API",description = "User Jwt Authorization")
    @PostMapping("/like")
    public ApiResponse<UserResponseDTO.UserLikeResponseDTO> postLikeItem(
            HttpServletRequest httpServletRequest,
            @RequestBody UserRequestDTO.UserLikeRequestDTO userLikeDTO) {

        UserLike userLike = userService.createLikeItem(httpServletRequest, userLikeDTO);

        if (userLike == null) {
            return ApiResponse.onFailure(ErrorStatus.USER_NOT_EXIST.getCode(), ErrorStatus.USER_NOT_EXIST.getMessage(), null);
        }else {
            return ApiResponse.of(SuccessStatus.USER_LIKE_SUCCESS, userConverter.toUserLikeResponseDTO(userLike));
        }
    }

    /**
     * 장소 좋아요 모아보기 API
     * Authorization -> JWT
     */
    @Operation(summary = "장소 좋아요 모아보기 API",description = "User Jwt Authorization")
    @GetMapping("/like")
    public ApiResponse<UserResponseDTO.GetUserLikeListResponseDTO> getLikeItems(
            HttpServletRequest httpServletRequest) {

        UserResponseDTO.GetUserLikeListResponseDTO userLikeListResultList = userService.getLikeItems(httpServletRequest);

        if (userLikeListResultList == null) {
            return ApiResponse.onFailure(ErrorStatus.USER_NOT_EXIST.getCode(), ErrorStatus.USER_NOT_EXIST.getMessage(), null);
        }else {
            Collections.sort(userLikeListResultList.getUserLikeItems(), new Comparator<UserResponseDTO.UserLikeItems>() {
                @Override
                public int compare(UserResponseDTO.UserLikeItems o1, UserResponseDTO.UserLikeItems o2) {
                    return o2.getCreated_at().compareTo(o1.getCreated_at());
                }
            });
            return ApiResponse.of(SuccessStatus.USER_LIKE_COLLECT_SUCCESS, userLikeListResultList);
        }
    }

    /**
     * 장소 좋아요 삭제 API
     * Authorization -> JWT
     */
    @Operation(summary = "장소 좋아요 삭제 API",description = "User Jwt Authorization")
    @DeleteMapping("/like/{like_id}")
    public ApiResponse<String> deleteLikeItem(
            HttpServletRequest httpServletRequest,
            @PathVariable (name = "like_id") Long likeId) {

        if (userService.deleteLikeItem(httpServletRequest, likeId)) {
            return ApiResponse.of(SuccessStatus.USER_LIKE_DELETE_SUCCESS, null);
        }else {
            return ApiResponse.onFailure(ErrorStatus.USER_NOT_EXIST.getCode(), ErrorStatus.USER_NOT_EXIST.getMessage(), null);
        }
    }
}

