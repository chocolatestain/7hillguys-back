package com.shinhan.peoch.UserProfileNormalization.perplexity;

import com.shinhan.entity.ExpectedIncomeEntity;
import com.shinhan.entity.NormUserProfilesEntity;
import com.shinhan.entity.UserProfileEntity;
import com.shinhan.peoch.lifecycleincome.DTO.ApiResponseDTO;
import com.shinhan.repository.ExpectedIncomeRepository;
import com.shinhan.repository.NormUserProfilesRepository;
import com.shinhan.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserProfileNormalizationPerplexityService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private NormUserProfilesRepository normUserProfilesRepository;

    @Autowired
    ExpectedIncomeRepository expectedIncomeRepository;

    @Autowired
    private PerplexityApiRequest perplexityApiRequest;

    private static final int DEFAULT_SCORE = 50; // 기본값


    public ResponseEntity<ApiResponseDTO<String>> normalizeAndSaveUserProfile(Integer userProfileId) throws JSONException {
        try {
            // 1. 사용자 프로필 조회

            UserProfileEntity userProfile = userProfileRepository.findByUserProfileId(userProfileId);

            // 2. 정규화된 프로필 엔티티 조회 (있으면 업데이트, 없으면 새로 생성)
            NormUserProfilesEntity normalizedProfile = normUserProfilesRepository
                    .findById(userProfile.getUserProfileId())
                    .orElse(new NormUserProfilesEntity());

            // 3. 프로필 데이터를 JSON 형태로 변환
            JSONObject profileData = convertProfileToJson(userProfile);
            // 4. AI 모델에 한 번에 요청하여 모든 필드 정규화
            JSONObject normalizedScores = normalizeAllFields(profileData);
            // 5. 정규화된 점수로 엔티티 업데이트
            normalizedProfile.setUserProfile(userProfile);
            normalizedProfile.setUniversity(getScoreFromJson(normalizedScores, "university"));
            normalizedProfile.setEducationMajor(getScoreFromJson(normalizedScores, "education_major"));
            normalizedProfile.setCertification(getScoreFromJson(normalizedScores, "certification"));
            normalizedProfile.setFamilyStatus(getScoreFromJson(normalizedScores, "family_status"));
            normalizedProfile.setAssets(getScoreFromJson(normalizedScores, "assets"));
            normalizedProfile.setCriminalRecord(getScoreFromJson(normalizedScores, "criminal_record"));
            normalizedProfile.setHealthStatus(userProfile.getHealthStatus());
            normalizedProfile.setGender(userProfile.getGender());
            normalizedProfile.setAddress(getScoreFromJson(normalizedScores, "address"));
            normalizedProfile.setMentalStatus(userProfile.getMentalStatus());
            System.out.println(normalizedProfile);
            // 저장
            normUserProfilesRepository.save(normalizedProfile);

            // 성공 응답 반환
            return ResponseEntity.ok(ApiResponseDTO.success("프로필 정규화가 성공적으로 완료되었습니다."));
        } catch (ObjectOptimisticLockingFailureException e) {
            // 낙관적 잠금 예외 처리
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponseDTO.error("프로필 정규화 중 충돌이 발생했습니다. 잠시 후 다시 시도해주세요.", "CONFLICT_ERROR"));
        } catch (Exception e) {
            // 기타 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("프로필 정규화 중 오류가 발생했습니다: " + e.getMessage(), "SERVER_ERROR"));
        }
    }
    public ResponseEntity<ApiResponseDTO<String>> normalizeProfileToExpectedIncome(Integer userProfileId) throws JSONException {
        try {
            // 1. 사용자 프로필 조회
            UserProfileEntity userProfile = userProfileRepository.findByUserProfileId(userProfileId);
            if (userProfile == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDTO.error("사용자 프로필을 찾을 수 없습니다.", "NOT_FOUND"));
            }

            // 2. 정규화된 프로필 엔티티 조회
            NormUserProfilesEntity normalizedProfile = normUserProfilesRepository
                    .findById(userProfile.getUserProfileId())
                    .orElseThrow(() -> new RuntimeException("정규화된 프로필이 없습니다."));

            // 3. 정규화된 프로필 데이터를 JSON 형태로 변환
            JSONObject normProfileData = new JSONObject();
            normProfileData.put("university", normalizedProfile.getUniversity());
            normProfileData.put("educationMajor", normalizedProfile.getEducationMajor());
            normProfileData.put("certification", normalizedProfile.getCertification());
            normProfileData.put("familyStatus", normalizedProfile.getFamilyStatus());
            normProfileData.put("assets", normalizedProfile.getAssets());
            normProfileData.put("criminalRecord", normalizedProfile.getCriminalRecord());
            normProfileData.put("healthStatus", normalizedProfile.getHealthStatus());
            normProfileData.put("gender", normalizedProfile.getGender());
            normProfileData.put("address", normalizedProfile.getAddress());
            normProfileData.put("mentalStatus", normalizedProfile.getMentalStatus());

            // 4. AI 모델에 정규화된 프로필 데이터를 전송하여 예상 수입 계산
            String prompt = "당신은 정규화된 사용자 프로필 데이터를 분석하여 20세부터 55세까지의 연령별 예상 연간 수입을 계산하는 전문가입니다.\n\n" +
                    "다음 정규화된 사용자 프로필 데이터가 주어졌습니다:\n" +
                    "- 대학교 점수(university): " + normalizedProfile.getUniversity() + " (0-100 사이 값, 높을수록 명문대)\n" +
                    "- 전공 점수(educationMajor): " + normalizedProfile.getEducationMajor() + " (0-100 사이 값, 높을수록 고수익 전공)\n" +
                    "- 자격증 점수(certification): " + normalizedProfile.getCertification() + " (0-100 사이 값, 높을수록 가치 있는 자격증)\n" +
                    "- 가족 상태 점수(familyStatus): " + normalizedProfile.getFamilyStatus() + " (0-100 사이 값)\n" +
                    "- 자산 점수(assets): " + normalizedProfile.getAssets() + " (0-100 사이 값, 높을수록 많은 자산)\n" +
                    "- 범죄 기록 점수(criminalRecord): " + normalizedProfile.getCriminalRecord() + " (0-100 사이 값, 높을수록 기록 없음)\n" +
                    "- 건강 상태 점수(healthStatus): " + normalizedProfile.getHealthStatus() + " (0-100 사이 값, 높을수록 건강)\n" +
                    "- 성별(gender): " + (normalizedProfile.getGender() ? "남성" : "여성") + "\n" +
                    "- 주소 점수(address): " + normalizedProfile.getAddress() + " (0-100 사이 값, 높을수록 좋은 지역)\n" +
                    "- 정신 상태 점수(mentalStatus): " + normalizedProfile.getMentalStatus() + " (0-100 사이 값, 높을수록 건강)\n\n" +

                    "위 데이터를 기반으로 20세부터 55세까지의 연령별 예상 연간 수입을 계산해주세요. 다음 사항을 반드시 고려하세요:\n\n" +

                    "1. 한국의 최신 직업별 소득 통계 데이터를 기반으로 현실적이고 구체적인 소득 예측 모델을 적용하세요.\n\n" +

                    "2. 다음과 같은 현실적인 요소를 포함하여 실제 소득 데이터와 유사한 변동성을 반영하세요:\n" +
                    "- 경기 침체나 경제 위기로 인한 일시적 소득 감소 구간 포함\n" +
                    "- 승진이나 이직 등으로 인한 급격한 소득 증가 구간 포함\n" +
                    "- 경력 단절 또는 휴직 후 복귀 시의 소득 변화 포함\n\n" +

                    "3. 연령대별 특성을 다음과 같이 반영하세요:\n" +
                    "- 20-27세: 소득을 0원으로 산정\n" +
                    "- 28-35세: 경력 초기로 승진 및 이직에 따른 급격하고 불규칙한 소득 증가 패턴\n" +
                    "- 36-45세: 경력 중기로 수입이 최대치에 도달하되 건강 및 정신 상태에 따라 일부 변동 포함\n" +
                    "- 46-55세: 경력 후기로 산업 특성에 따라 수입이 정체되거나 완만히 증가하며 일부 변동성 포함\n\n" +

                    "4. 매년 연속된 수입 변화율은 -5%에서 최대 +15% 범위 내에서 무작위로 변동시키고, 5~7년 주기로 더 큰 폭의 변화(-10%~+25%)를 추가하여 현실적인 통계적 변동성을 구현하세요.\n\n" +

                    "결과는 다음과 같은 JSON 형식으로만 반환해주세요. 다른 설명은 포함하지 마세요:\n" +
                    "{\n" +
                    "   \"20\": 값, \"21\": 값, ... \"55\": 값 (각 연령별 예상 연간 수입, 원 단위)\n" +
                    "}";



            // Perplexity API 호출
            String aiResponse = perplexityApiRequest.sendRequestToApi("sonar", prompt);

            // 5. AI 응답에서 JSON 데이터 추출
            JSONObject expectedIncomeJson = extractJsonFromResponse(aiResponse);
            // 6. Expected Income 엔티티 조회 또는 생성
            ExpectedIncomeEntity expectedIncome = expectedIncomeRepository.findByUserProfileIdOptional(userProfileId)
                    .orElse(ExpectedIncomeEntity.builder()
                            .userProfile(userProfile)
                            .expectedIncome(expectedIncomeJson.toString())
                            .build());

//            // 7. Expected Income 엔티티 업데이트
//            expectedIncome.setUserProfile(userProfile);
//            expectedIncome.setExpectedIncome(expectedIncomeJson.toString());

            // 8. 저장
            expectedIncomeRepository.save(expectedIncome);

            return ResponseEntity.ok(ApiResponseDTO.success("예상 수입 계산이 성공적으로 완료되었습니다."));
        } catch (ObjectOptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponseDTO.error("예상 수입 계산 중 충돌이 발생했습니다. 잠시 후 다시 시도해주세요.", "CONFLICT_ERROR"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("예상 수입 계산 중 오류가 발생했습니다: " + e.getMessage(), "SERVER_ERROR"));
        }
    }

    // AI 응답에서 JSON 데이터 추출하는 메서드
    private JSONObject extractJsonFromResponse(String response) throws JSONException {
        // AI 응답이 이미 JSON 형식인 경우
        if (response.trim().startsWith("{") && response.trim().endsWith("}")) {
            return new JSONObject(response);
        }

        // 기본 예상 수입 템플릿 생성 (AI 응답 파싱 실패 시 사용)
        JSONObject defaultExpectedIncome = new JSONObject();
        for (int age = 20; age <= 55; age++) {
            defaultExpectedIncome.put(String.valueOf(age), 0);
        }

        try {
            // AI 응답에서 JSON 부분 추출 시도
            int startIndex = response.indexOf('{');
            int endIndex = response.lastIndexOf('}');

            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                String jsonStr = response.substring(startIndex, endIndex + 1);
                return new JSONObject(jsonStr);
            }
        } catch (Exception e) {
            System.err.println("JSON 추출 실패: " + e.getMessage());
        }

        return defaultExpectedIncome;
    }

    // 프로필 데이터를 JSON 형태로 변환
    private JSONObject convertProfileToJson(UserProfileEntity profile) throws JSONException {
        JSONObject profileData = new JSONObject();

        // 주소 추가
        if (profile.getAddress() != null && !profile.getAddress().isEmpty()) {
            profileData.put("address", profile.getAddress());
        }

        // 자산 추가
        if (profile.getAssets() != null) {
            profileData.put("assets", profile.getAssets());
        }

        // 자격증 추가
        if (profile.getCertification() != null && !profile.getCertification().isEmpty()) {
            profileData.put("certification", new JSONObject(profile.getCertification()));
        }

        // 범죄 기록 추가
        profileData.put("criminal_record", profile.getCriminalRecord() != null && profile.getCriminalRecord() ? 1 : 0);

        // 대학 및 전공 정보 추가 (변경된 구조 적용)
        if (profile.getUniversityInfo() != null && !profile.getUniversityInfo().isEmpty()) {
            JSONObject uniInfo = new JSONObject(profile.getUniversityInfo());

            JSONObject universityObj = new JSONObject();
            if (uniInfo.has("universityName")) {  // 키 이름 변경
                universityObj.put("name", uniInfo.getString("universityName"));
            }
            if (uniInfo.has("major")) {  // 키 이름 변경
                universityObj.put("major", uniInfo.getString("major"));
            }
            profileData.put("university", universityObj);

            // 교육 전공 정보도 동일하게 수정
            JSONObject educationMajor = new JSONObject();
            if (uniInfo.has("major")) {  // 'degree' → 'major'로 변경
                educationMajor.put("major", uniInfo.getString("major"));
            }
            profileData.put("education_major", educationMajor);
        }

        // 가족 상태 추가
        if (profile.getFamilyStatus() != null && !profile.getFamilyStatus().isEmpty()) {
            profileData.put("family_status", new JSONObject(profile.getFamilyStatus()));
        }

        // 건강 상태 추가
        if (profile.getHealthStatus() != null) {
            profileData.put("health_status", profile.getHealthStatus());
        }

        // 정신 상태 추가
        if (profile.getMentalStatus() != null) {
            profileData.put("mental_status", profile.getMentalStatus());
        }

        return profileData;
    }


    // 모든 필드를 한 번에 정규화하는 메서드
    private JSONObject normalizeAllFields(JSONObject profileData) throws JSONException {
        String prompt = "다음 사용자 프로필 데이터의 각 항목을 0에서 100 사이의 점수로 평가해주세요. " +
                "각 항목은 사회적, 경제적 가치와 경쟁력 측면에서 평가합니다. " +
                "결과는 JSON 형식으로 각 필드별 점수만 반환해주세요. 다음 형식으로 응답해주세요:\n" +
                "{\n" +
                "  \"address\": 숫자,\n" +
                "  \"assets\": 숫자,\n" +
                "  \"certification\": 숫자,\n" +
                "  \"criminal_record\": 숫자,\n" +
                "  \"education_major\": 숫자,\n" +
                "  \"family_status\": 숫자,\n" +
                "  \"health_status\": 숫자,\n" +
                "  \"mental_status\": 숫자,\n" +
                "  \"university\": 숫자\n" +
                "}\n\n" +
                "프로필 데이터:\n" + profileData.toString();

        JSONObject payload = new JSONObject();
        payload.put("model", "sonar");

        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "당신은 사용자 프로필 데이터를 통해서 소득수준을 평가하는 AI입니다. 각 항목을 0-100 사이의 미래 소득을 고려한 점수로 평가하고, " +
                "JSON 형식으로 결과를 반환해야 합니다. 각 필드는 원본 필드명을 유지하고, 값은 정수여야 합니다. 그외의 값은 필요없습니다.");

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        JSONArray messages = new JSONArray();
        messages.put(systemMessage);
        messages.put(userMessage);

        payload.put("messages", messages);

        // response_format 부분 제거 또는 수정
        // Perplexity API가 현재 지원하는 형식으로 변경
        JSONObject responseFormat = new JSONObject();
        responseFormat.put("type", "text"); // "json_schema" 대신 "text" 사용
        payload.put("response_format", responseFormat);

        String response = perplexityApiRequest.sendRequestToApi(payload);

        try {
            // 응답에서 JSON 부분 추출 시도
            String jsonStr = extractJsonFromText(response);
            return new JSONObject(jsonStr);
        } catch (JSONException e) {
            System.err.println("Failed to parse API response: " + response);
            return new JSONObject(); // 빈 객체 반환
        }
    }

    // 텍스트에서 JSON 부분 추출
    private String extractJsonFromText(String text) {
        // JSON 시작과 끝 부분 찾기
        int startIdx = text.indexOf('{');
        int endIdx = text.lastIndexOf('}') + 1;

        if (startIdx >= 0 && endIdx > startIdx) {
            return text.substring(startIdx, endIdx);
        }

        // JSON을 찾지 못한 경우 빈 JSON 객체 반환
        return "{}";
    }

    // JSON에서 점수 추출
    private Integer getScoreFromJson(JSONObject scores, String fieldName) {
        if (scores.has(fieldName)) {
            try {
                return scores.getInt(fieldName);
            } catch (JSONException e) {
                return DEFAULT_SCORE;
            }
        }
        return DEFAULT_SCORE;
    }
}
