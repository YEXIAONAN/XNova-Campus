package com.xnova.controller.admin;

import com.xnova.common.model.PageResult;
import com.xnova.common.result.ApiResponse;
import com.xnova.dto.user.CreateStudentDTO;
import com.xnova.dto.user.CreateTeacherDTO;
import com.xnova.dto.user.ResetPasswordDTO;
import com.xnova.dto.user.UpdateStudentDTO;
import com.xnova.dto.user.UpdateTeacherDTO;
import com.xnova.dto.user.UserPageQueryDTO;
import com.xnova.dto.user.UserStatusDTO;
import com.xnova.service.user.AdminUserService;
import com.xnova.vo.user.UserDetailVO;
import com.xnova.vo.user.UserItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PostMapping("/students")
    public ApiResponse<Long> createStudent(@Valid @RequestBody CreateStudentDTO dto) {
        return ApiResponse.ok(adminUserService.createStudent(dto));
    }

    @PostMapping("/teachers")
    public ApiResponse<Long> createTeacher(@Valid @RequestBody CreateTeacherDTO dto) {
        return ApiResponse.ok(adminUserService.createTeacher(dto));
    }

    @PutMapping("/students")
    public ApiResponse<Void> updateStudent(@Valid @RequestBody UpdateStudentDTO dto) {
        adminUserService.updateStudent(dto);
        return ApiResponse.ok();
    }

    @PutMapping("/teachers")
    public ApiResponse<Void> updateTeacher(@Valid @RequestBody UpdateTeacherDTO dto) {
        adminUserService.updateTeacher(dto);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<Void> deleteUser(@PathVariable Long userId) {
        adminUserService.deleteUser(userId);
        return ApiResponse.ok();
    }

    @PutMapping("/status")
    public ApiResponse<Void> updateStatus(@Valid @RequestBody UserStatusDTO dto) {
        adminUserService.updateStatus(dto);
        return ApiResponse.ok();
    }

    @PutMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        adminUserService.resetPassword(dto);
        return ApiResponse.ok();
    }

    @GetMapping
    public ApiResponse<PageResult<UserItemVO>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) Integer status
    ) {
        UserPageQueryDTO dto = new UserPageQueryDTO();
        dto.setPageNum(pageNum);
        dto.setPageSize(pageSize);
        dto.setRoleCode(roleCode);
        dto.setRealName(realName);
        dto.setClassName(className);
        dto.setMajor(major);
        dto.setStatus(status);
        return ApiResponse.ok(adminUserService.pageUsers(dto));
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserDetailVO> detail(@PathVariable Long userId) {
        return ApiResponse.ok(adminUserService.getDetail(userId));
    }
}

