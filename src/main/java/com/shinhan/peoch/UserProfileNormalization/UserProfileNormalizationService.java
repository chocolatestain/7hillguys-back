package com.shinhan.peoch.UserProfileNormalization;

import com.shinhan.entity.UserProfileEntity;
import com.shinhan.entity.NormUserProfilesEntity;
import com.shinhan.repository.UserProfileRepository;
import com.shinhan.repository.NormUserProfilesRepository;
import com.shinhan.peoch.lifecycleincome.service.NgrokApiRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserProfileNormalizationService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private NormUserProfilesRepository normUserProfilesRepository;

    @Autowired
    private NgrokApiRequest ngrokApiRequest;

    private static final int DEFAULT_SCORE = 50; // 기본값

    @Transactional
    public NormUserProfilesEntity normalizeAndSaveUserProfile(Integer userProfileId) throws JSONException {
        // 1. 사용자 프로필 조회
        Optional<UserProfileEntity> userProfileOpt = userProfileRepository.findById(userProfileId);
        if (!userProfileOpt.isPresent()) {
            throw new RuntimeException("User profile not found with ID: " + userProfileId);
        }

        UserProfileEntity userProfile = userProfileOpt.get();
        System.out.println(userProfile.toString());

        // 2. AI 모델을 사용하여 각 필드 정규화
        NormUserProfilesEntity normalizedProfile = NormUserProfilesEntity.builder()
                .university(normalizeUniversity(userProfile.getUniversityInfo()))
                .educationMajor(normalizeEducationMajor(userProfile.getUniversityInfo()))
                .certification(normalizeCertification(userProfile.getCertification()))
                .familyStatus(normalizeFamilyStatus(userProfile.getFamilyStatus()))
                .assets(normalizeAssets(userProfile.getAssets()))
                .criminalRecord(normalizeCriminalRecord(userProfile.getCriminalRecord()))
                .healthStatus(normalizeHealthStatus(userProfile.getHealthStatus()))
                .gender(userProfile.getGender())
                .address(normalizeAddress(userProfile.getAddress()))
                .mentalStatus(normalizeMentalStatus(userProfile.getMentalStatus()))
                .build();

        return normUserProfilesRepository.save(normalizedProfile);
    }

    // 공통 숫자 추출 함수
    private Integer extractNumberFromResponse(String response) {
        if (response == null || response.isEmpty()) {
            return DEFAULT_SCORE; // 기본값 반환
        }

        Pattern pattern = Pattern.compile("\\d+"); // 숫자만 추출하는 정규식
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group()); // 첫 번째로 발견된 숫자 반환
            } catch (NumberFormatException e) {
                return DEFAULT_SCORE; // 숫자로 변환 실패 시 기본값 반환
            }
        }

        return DEFAULT_SCORE; // 숫자가 없을 경우 기본값 반환
    }

    private Integer normalizeUniversity(String universityInfo) throws JSONException {
        if (universityInfo == null || universityInfo.isEmpty()) {
            return DEFAULT_SCORE; // 기본값
        }

        JSONObject universityData = new JSONObject(universityInfo);
        String universityName = universityData.optString("name", "");

        if (universityName.isEmpty()) {
            return DEFAULT_SCORE; // 기본값
        }

        String prompt = "코드를 달라는게 아니야 숫자를 답해줘" +
                "다음 대학교의 생애 소득 잠재력을 0에서 100 사이로 평가하고, 미래 소득 예측 점수를 매겨줘.\n" +
                "                평가는 다음 기준으로 해줘:\n" +
                "                졸업생의 평균 초봉\n" +
                "                중간 경력 연봉\n" +
                "                졸업 후 30년간 누적 소득\n" +
                "\n" +"반드시 숫자 하나만 출력해줘: " + universityName;
        String response = ngrokApiRequest.sendRequestToApi("llama3.1", prompt);
        System.out.println(response);

        return extractNumberFromResponse(response);
    }

    private Integer normalizeEducationMajor(String universityInfo) throws JSONException {
        if (universityInfo == null || universityInfo.isEmpty()) {
            return DEFAULT_SCORE; // 기본값
        }

        JSONObject universityData = new JSONObject(universityInfo);
        String major = universityData.optString("degree", "");

        if (major.isEmpty()) {
            return DEFAULT_SCORE; // 기본값
        }

        String prompt = "다음 대학 전공의 취업 경쟁력과 미래 가치를 0에서 100 사이 숫자로 평가해줘. 반드시 숫자 하나만 출력해줘: " + major;
        String response = ngrokApiRequest.sendRequestToApi("llama3.1", prompt);

        return extractNumberFromResponse(response);
    }

    private Integer normalizeCertification(String certification) throws JSONException {
        if (certification == null || certification.isEmpty()) {
            return DEFAULT_SCORE; // 기본값
        }

        JSONObject certData = new JSONObject(certification);
        String certName = certData.optString("certificate", "");

        if (certName.isEmpty()) {
            return DEFAULT_SCORE; // 기본값
        }

        String prompt = "다음 자격증의 취업 시장 가치와 경쟁력을 0에서 100 사이 숫자로 평가해줘. 반드시 숫자 하나만 출력해줘: " + certName;
        String response = ngrokApiRequest.sendRequestToApi("llama3.1", prompt);

        return extractNumberFromResponse(response);
    }

    private Integer normalizeFamilyStatus(String familyStatus) throws JSONException {
        if (familyStatus == null || familyStatus.isEmpty()) {
            return DEFAULT_SCORE; // 기본값
        }

        JSONObject familyData = new JSONObject(familyStatus);
        boolean isMarried = familyData.optBoolean("married", false);
        int children = familyData.optInt("children", 0);

        String prompt = "다음 가족 상태의 사회적 안정성과 경제적 책임도를 0에서 100 사이 숫자로 평가해줘. 반드시 숫자 하나만 출력해줘: 결혼 여부: "
                + (isMarried ? "기혼" : "미혼") + ", 자녀 수: " + children;
        String response = ngrokApiRequest.sendRequestToApi("llama3.1", prompt);

        return extractNumberFromResponse(response);
    }

    private Integer normalizeAssets(Long assets) throws JSONException {
        if (assets == null) {
            return DEFAULT_SCORE; // 기본값
        }

        String prompt = "자산 " + assets + "원의 경제적 안정성과 부의 수준을 0에서 100 사이 숫자로 평가해줘. 반드시 숫자 하나만 출력해줘";
        String response = ngrokApiRequest.sendRequestToApi("llama3.1", prompt);

        return extractNumberFromResponse(response);
    }

    private Integer normalizeCriminalRecord(Boolean criminalRecord) throws JSONException {
        if (criminalRecord == null) {
            return DEFAULT_SCORE; // 기본값
        }

        // 범죄 기록이 있다면 낮은 점수, 없다면 높은 점수 반환
        return criminalRecord ? 20 : 80;
    }

    private Integer normalizeHealthStatus(Integer healthStatus) {
        // 건강 상태는 이미 정규화되어 있음
        return healthStatus != null ? healthStatus : DEFAULT_SCORE;
    }

    private Integer normalizeGender(Boolean gender) throws JSONException {
        // 성별은 중립적인 값 반환
        return DEFAULT_SCORE;
    }

    private Integer normalizeAddress(String address) throws JSONException {
        if (address == null || address.isEmpty()) {
            return DEFAULT_SCORE; // 기본값
        }

        String prompt = "다음 주소의 지역 경제력과 주거 환경을 0에서 100 사이 숫자로 평가해줘. 반드시 숫자 하나만 출력해줘: " + address;
        String response = ngrokApiRequest.sendRequestToApi("llama3.1", prompt);

        return extractNumberFromResponse(response);
    }

    private Integer normalizeMentalStatus(Integer mentalStatus) {
        // 정신 상태는 이미 정규화되어 있음
        return mentalStatus != null ? mentalStatus : DEFAULT_SCORE;
    }
}
