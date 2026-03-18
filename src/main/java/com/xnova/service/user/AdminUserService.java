package com.xnova.service.user;

import com.xnova.common.model.PageResult;
import com.xnova.dto.user.CreateStudentDTO;
import com.xnova.dto.user.CreateTeacherDTO;
import com.xnova.dto.user.ResetPasswordDTO;
import com.xnova.dto.user.UpdateStudentDTO;
import com.xnova.dto.user.UpdateTeacherDTO;
import com.xnova.dto.user.UserPageQueryDTO;
import com.xnova.dto.user.UserStatusDTO;
import com.xnova.vo.user.UserDetailVO;
import com.xnova.vo.user.UserItemVO;

public interface AdminUserService {

    Long createStudent(CreateStudentDTO dto);

    Long createTeacher(CreateTeacherDTO dto);

    void updateStudent(UpdateStudentDTO dto);

    void updateTeacher(UpdateTeacherDTO dto);

    void deleteUser(Long userId);

    void updateStatus(UserStatusDTO dto);

    void resetPassword(ResetPasswordDTO dto);

    PageResult<UserItemVO> pageUsers(UserPageQueryDTO dto);

    UserDetailVO getDetail(Long userId);
}
