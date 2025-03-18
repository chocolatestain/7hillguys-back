package com.shinhan.peoch.user.controller;
 

import com.shinhan.peoch.user.dto.UserInfoDTO;
import com.shinhan.peoch.user.service.UserSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
 
import org.springframework.web.bind.annotation.*;
 
import java.util.List; 
 
import org.springframework.http.ResponseEntity; 
 
import com.shinhan.peoch.auth.entity.UserEntity;
 

@RestController
@RequestMapping("/api/user")
public class UserSearchController {


    @Autowired
    private UserSearchService UserSearchService;
  
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam("query") String query) {
        // 검색어가 비어있는 경우 400 에러 반환
        if(query == null || query.trim().isEmpty()){
            return new ResponseEntity<>("검색어(query)가 필요합니다.", HttpStatus.BAD_REQUEST);
        }
        try {
            List<UserEntity> users = UserSearchService.searchUsersByName(query);
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("서버 내부 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfos(@RequestParam("userid") Long userid) {
        if (userid == null) {
            return new ResponseEntity<>("사용자 아이디(userid)가 필요합니다.", HttpStatus.BAD_REQUEST);
        }
        try {
            UserInfoDTO dto = UserSearchService.getAdminUserFlatDetail(userid);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("서버 내부 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
