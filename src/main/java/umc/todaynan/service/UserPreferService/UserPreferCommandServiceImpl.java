package umc.todaynan.service.UserPreferService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.todaynan.repository.QueryDsl.UserPreferQueryDslRepository;

import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserPreferCommandServiceImpl implements UserPreferCommandService {
    private final UserPreferQueryDslRepository userPreferQueryDslRepository;
    @Override
    public void changeMyInterset(long userId, List<Integer> Interests) {
        userPreferQueryDslRepository.changePreferList(userId, Interests);
        log.info("[Service - changeMyInterset] 관심사 변경이 완료되었습니다.");
    }
}