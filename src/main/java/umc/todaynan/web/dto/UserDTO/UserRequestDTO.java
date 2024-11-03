package umc.todaynan.web.dto.UserDTO;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import umc.todaynan.domain.enums.LoginType;
import umc.todaynan.domain.enums.MyPet;
import umc.todaynan.domain.enums.PlaceCategory;

import java.util.List;

public class UserRequestDTO {
    @Builder
    @Getter
    public static class JoinUserRequestDTO {
        String nickName;
        List<Long> preferCategory;
        String address;
        MyPet mypet;
    }

    @Getter
    public static class LoginUserRequestDTO {
        String email;
    }

    @Getter
    public static class UserLikeRequestDTO {
        String title;
        String description;
        String placeAddress;
        String image;
        PlaceCategory category;
    }


    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserGeneralRequestDTO {
        String request;
    }

    @Getter
    public static class UserInterestRequestDTO {
        List<Integer> interestList;
    }
}
