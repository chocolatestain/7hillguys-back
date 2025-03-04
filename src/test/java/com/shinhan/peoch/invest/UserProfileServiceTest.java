package com.shinhan.peoch.invest;

import com.shinhan.peoch.invest.dto.UserProfileDTO;
import com.shinhan.peoch.invest.entity.UserProfileEntity;
import com.shinhan.peoch.invest.repository.UserProfileRepository;
import com.shinhan.peoch.invest.service.UserProfileService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
//@Transactional
public class UserProfileServiceTest {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    public void 유저_프로필_저장_테스트() {
        // Given (테스트 데이터 준비)
        UserProfileDTO dto = UserProfileDTO.builder()
                .userId(6L)
                .universityInfo("{\"name\":\"연세대학교\", \"degree\":\"경제학과\"}")
                .studentCard("{\"highschool\":\"봉민고등학교\", \"gpa\":1.6}")
                .certification("{\"certificate\":\"정보처리기사\"}")
                .familyStatus("{\"married\":true, \"children\":0}")
                .assets(20000000L) // 2천만원
                .criminalRecord(false)
                .healthStatus(90)
                .gender(false)   //남:true 여:false
                .address("서울특별시 마포구")
                .mentalStatus(85)
                .build();

        // When (서비스 메서드 실행)
        UserProfileEntity savedEntity = userProfileService.saveUserProfile(dto);

        // Then (결과 검증)
        UserProfileEntity foundEntity = userProfileRepository.findById(savedEntity.getUserProfileId()).orElse(null);

        assertThat(foundEntity).isNotNull();
        assertThat(foundEntity.getUserId()).isEqualTo(dto.getUserId());
        assertThat(foundEntity.getUniversityInfo()).isEqualTo(dto.getUniversityInfo());
        assertThat(foundEntity.getStudentCard()).isEqualTo(dto.getStudentCard());
        assertThat(foundEntity.getCertification()).isEqualTo(dto.getCertification());
        assertThat(foundEntity.getFamilyStatus()).isEqualTo(dto.getFamilyStatus());
        assertThat(foundEntity.getAssets()).isEqualTo(dto.getAssets());
        assertThat(foundEntity.getCriminalRecord()).isEqualTo(dto.getCriminalRecord());
        assertThat(foundEntity.getHealthStatus()).isEqualTo(dto.getHealthStatus());
        assertThat(foundEntity.getGender()).isEqualTo(dto.getGender());
        assertThat(foundEntity.getAddress()).isEqualTo(dto.getAddress());
        assertThat(foundEntity.getMentalStatus()).isEqualTo(dto.getMentalStatus());
    }
}
