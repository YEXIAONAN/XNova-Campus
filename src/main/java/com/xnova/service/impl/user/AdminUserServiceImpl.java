package com.xnova.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xnova.common.enums.UserType;
import com.xnova.common.model.PageResult;
import com.xnova.dto.user.CreateStudentDTO;
import com.xnova.dto.user.CreateTeacherDTO;
import com.xnova.dto.user.ResetPasswordDTO;
import com.xnova.dto.user.UpdateStudentDTO;
import com.xnova.dto.user.UpdateTeacherDTO;
import com.xnova.dto.user.UserPageQueryDTO;
import com.xnova.dto.user.UserStatusDTO;
import com.xnova.entity.StudentProfile;
import com.xnova.entity.SysRole;
import com.xnova.entity.SysUser;
import com.xnova.entity.SysUserRole;
import com.xnova.entity.TeacherProfile;
import com.xnova.exception.BizException;
import com.xnova.mapper.StudentProfileMapper;
import com.xnova.mapper.SysRoleMapper;
import com.xnova.mapper.SysUserMapper;
import com.xnova.mapper.SysUserRoleMapper;
import com.xnova.mapper.TeacherProfileMapper;
import com.xnova.service.user.AdminUserService;
import com.xnova.vo.user.UserDetailVO;
import com.xnova.vo.user.UserItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private static final String DEFAULT_PASSWORD = "123456";

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final StudentProfileMapper studentProfileMapper;
    private final TeacherProfileMapper teacherProfileMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStudent(CreateStudentDTO dto) {
        ensureUsernameUnique(dto.getUsername(), null);

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setUserType(UserType.STUDENT.name());
        user.setStatus(1);
        user.setDeleted(0);
        user.setPassword(passwordEncoder.encode(resolvePassword(dto.getPassword())));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.insert(user);

        bindRole(user.getId(), "STUDENT");

        StudentProfile profile = new StudentProfile();
        profile.setUserId(user.getId());
        profile.setStudentNo(dto.getStudentNo());
        profile.setEnrollmentYear(dto.getEnrollmentYear());
        profile.setMajor(dto.getMajor());
        profile.setGrade(dto.getGrade());
        profile.setClassName(dto.getClassName());
        profile.setContactAddress(dto.getContactAddress());
        studentProfileMapper.insert(profile);

        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTeacher(CreateTeacherDTO dto) {
        ensureUsernameUnique(dto.getUsername(), null);

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setUserType(UserType.TEACHER.name());
        user.setStatus(1);
        user.setDeleted(0);
        user.setPassword(passwordEncoder.encode(resolvePassword(dto.getPassword())));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.insert(user);

        bindRole(user.getId(), "TEACHER");

        TeacherProfile profile = new TeacherProfile();
        profile.setUserId(user.getId());
        profile.setTeacherNo(dto.getTeacherNo());
        profile.setTitle(dto.getTitle());
        profile.setDepartment(dto.getDepartment());
        profile.setMajor(dto.getMajor());
        profile.setContactOffice(dto.getContactOffice());
        teacherProfileMapper.insert(profile);

        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStudent(UpdateStudentDTO dto) {
        SysUser user = getExistingUser(dto.getUserId());
        if (!UserType.STUDENT.name().equals(user.getUserType())) {
            throw new BizException(400, "该用户不是学生");
        }

        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        StudentProfile profile = studentProfileMapper.selectOne(new LambdaQueryWrapper<StudentProfile>()
                .eq(StudentProfile::getUserId, dto.getUserId()));
        if (profile == null) {
            throw new BizException(404, "学生档案不存在");
        }
        profile.setEnrollmentYear(dto.getEnrollmentYear());
        profile.setMajor(dto.getMajor());
        profile.setGrade(dto.getGrade());
        profile.setClassName(dto.getClassName());
        profile.setContactAddress(dto.getContactAddress());
        studentProfileMapper.updateById(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTeacher(UpdateTeacherDTO dto) {
        SysUser user = getExistingUser(dto.getUserId());
        if (!UserType.TEACHER.name().equals(user.getUserType())) {
            throw new BizException(400, "该用户不是教师");
        }

        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        TeacherProfile profile = teacherProfileMapper.selectOne(new LambdaQueryWrapper<TeacherProfile>()
                .eq(TeacherProfile::getUserId, dto.getUserId()));
        if (profile == null) {
            throw new BizException(404, "教师档案不存在");
        }
        profile.setTitle(dto.getTitle());
        profile.setDepartment(dto.getDepartment());
        profile.setMajor(dto.getMajor());
        profile.setContactOffice(dto.getContactOffice());
        teacherProfileMapper.updateById(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        SysUser user = getExistingUser(userId);
        user.setStatus(0);
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
        sysUserMapper.deleteById(userId);

        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));

        studentProfileMapper.delete(new LambdaQueryWrapper<StudentProfile>()
                .eq(StudentProfile::getUserId, userId));
        teacherProfileMapper.delete(new LambdaQueryWrapper<TeacherProfile>()
                .eq(TeacherProfile::getUserId, userId));
    }

    @Override
    public void updateStatus(UserStatusDTO dto) {
        if (!Objects.equals(dto.getStatus(), 0) && !Objects.equals(dto.getStatus(), 1)) {
            throw new BizException(400, "状态仅支持0或1");
        }
        SysUser user = getExistingUser(dto.getUserId());
        user.setStatus(dto.getStatus());
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    @Override
    public void resetPassword(ResetPasswordDTO dto) {
        SysUser user = getExistingUser(dto.getUserId());
        String plainPassword = resolvePassword(dto.getNewPassword());
        user.setPassword(passwordEncoder.encode(plainPassword));
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    @Override
    public PageResult<UserItemVO> pageUsers(UserPageQueryDTO dto) {
        Set<Long> scopedUserIds = new HashSet<>();
        boolean scoped = false;

        if (StringUtils.hasText(dto.getRoleCode())) {
            Long roleId = getRoleId(dto.getRoleCode());
            List<Long> roleUserIds = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                            .eq(SysUserRole::getRoleId, roleId))
                    .stream()
                    .map(SysUserRole::getUserId)
                    .toList();
            scopedUserIds.addAll(roleUserIds);
            scoped = true;
        }

        if (StringUtils.hasText(dto.getClassName())) {
            List<Long> classUserIds = studentProfileMapper.selectList(new LambdaQueryWrapper<StudentProfile>()
                            .like(StudentProfile::getClassName, dto.getClassName()))
                    .stream()
                    .map(StudentProfile::getUserId)
                    .toList();
            scopedUserIds = scoped ? intersect(scopedUserIds, classUserIds) : new HashSet<>(classUserIds);
            scoped = true;
        }

        if (StringUtils.hasText(dto.getMajor())) {
            List<Long> majorUserIds = loadMajorUserIds(dto.getMajor(), dto.getRoleCode());
            scopedUserIds = scoped ? intersect(scopedUserIds, majorUserIds) : new HashSet<>(majorUserIds);
            scoped = true;
        }

        if (scoped && scopedUserIds.isEmpty()) {
            PageResult<UserItemVO> empty = new PageResult<>();
            empty.setPageNum(dto.getPageNum());
            empty.setPageSize(dto.getPageSize());
            empty.setTotal(0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<SysUser>()
                .like(StringUtils.hasText(dto.getRealName()), SysUser::getRealName, dto.getRealName())
                .eq(dto.getStatus() != null, SysUser::getStatus, dto.getStatus())
                .in(scoped, SysUser::getId, scopedUserIds)
                .orderByDesc(SysUser::getId);

        Page<SysUser> page = sysUserMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), userWrapper);

        List<Long> userIds = page.getRecords().stream().map(SysUser::getId).toList();
        if (userIds.isEmpty()) {
            PageResult<UserItemVO> result = new PageResult<>();
            result.setPageNum(dto.getPageNum());
            result.setPageSize(dto.getPageSize());
            result.setTotal(page.getTotal());
            result.setRecords(Collections.emptyList());
            return result;
        }

        Map<Long, String> roleCodeMap = loadRoleCodeMap(userIds);
        Map<Long, StudentProfile> studentMap = studentProfileMapper.selectList(new LambdaQueryWrapper<StudentProfile>()
                        .in(StudentProfile::getUserId, userIds))
                .stream().collect(Collectors.toMap(StudentProfile::getUserId, s -> s));
        Map<Long, TeacherProfile> teacherMap = teacherProfileMapper.selectList(new LambdaQueryWrapper<TeacherProfile>()
                        .in(TeacherProfile::getUserId, userIds))
                .stream().collect(Collectors.toMap(TeacherProfile::getUserId, t -> t));

        List<UserItemVO> records = page.getRecords().stream().map(user -> {
            UserItemVO vo = new UserItemVO();
            vo.setUserId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setPhone(user.getPhone());
            vo.setUserType(user.getUserType());
            vo.setStatus(user.getStatus());
            vo.setRoleCode(roleCodeMap.get(user.getId()));

            StudentProfile student = studentMap.get(user.getId());
            if (student != null) {
                vo.setStudentNo(student.getStudentNo());
                vo.setEnrollmentYear(student.getEnrollmentYear());
                vo.setGrade(student.getGrade());
                vo.setClassName(student.getClassName());
                vo.setMajor(student.getMajor());
            }

            TeacherProfile teacher = teacherMap.get(user.getId());
            if (teacher != null) {
                vo.setTeacherNo(teacher.getTeacherNo());
                vo.setTitle(teacher.getTitle());
                vo.setDepartment(teacher.getDepartment());
                vo.setMajor(teacher.getMajor());
            }
            return vo;
        }).toList();

        PageResult<UserItemVO> result = new PageResult<>();
        result.setPageNum(dto.getPageNum());
        result.setPageSize(dto.getPageSize());
        result.setTotal(page.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public UserDetailVO getDetail(Long userId) {
        SysUser user = getExistingUser(userId);

        UserDetailVO vo = new UserDetailVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setUserType(user.getUserType());
        vo.setStatus(user.getStatus());

        Map<Long, String> roleMap = loadRoleCodeMap(List.of(userId));
        vo.setRoleCode(roleMap.get(userId));

        StudentProfile student = studentProfileMapper.selectOne(new LambdaQueryWrapper<StudentProfile>()
                .eq(StudentProfile::getUserId, userId));
        if (student != null) {
            vo.setStudentNo(student.getStudentNo());
            vo.setEnrollmentYear(student.getEnrollmentYear());
            vo.setGrade(student.getGrade());
            vo.setClassName(student.getClassName());
            vo.setContactAddress(student.getContactAddress());
            vo.setMajor(student.getMajor());
        }

        TeacherProfile teacher = teacherProfileMapper.selectOne(new LambdaQueryWrapper<TeacherProfile>()
                .eq(TeacherProfile::getUserId, userId));
        if (teacher != null) {
            vo.setTeacherNo(teacher.getTeacherNo());
            vo.setTitle(teacher.getTitle());
            vo.setDepartment(teacher.getDepartment());
            vo.setContactOffice(teacher.getContactOffice());
            vo.setMajor(teacher.getMajor());
        }

        return vo;
    }

    private void ensureUsernameUnique(String username, Long excludeUserId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username);
        if (excludeUserId != null) {
            wrapper.ne(SysUser::getId, excludeUserId);
        }
        Long count = sysUserMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BizException(400, "用户名已存在");
        }
    }

    private String resolvePassword(String value) {
        if (StringUtils.hasText(value)) {
            return value;
        }
        return DEFAULT_PASSWORD;
    }

    private void bindRole(Long userId, String roleCode) {
        Long roleId = getRoleId(roleCode);
        SysUserRole relation = new SysUserRole();
        relation.setUserId(userId);
        relation.setRoleId(roleId);
        sysUserRoleMapper.insert(relation);
    }

    private Long getRoleId(String roleCode) {
        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode)
                .eq(SysRole::getStatus, 1));
        if (role == null) {
            throw new BizException(500, "角色未初始化: " + roleCode);
        }
        return role.getId();
    }

    private SysUser getExistingUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    private Set<Long> intersect(Set<Long> left, Collection<Long> right) {
        Set<Long> rightSet = new HashSet<>(right);
        return left.stream().filter(rightSet::contains).collect(Collectors.toSet());
    }

    private List<Long> loadMajorUserIds(String major, String roleCode) {
        List<Long> ids = new ArrayList<>();
        if (!StringUtils.hasText(roleCode) || "STUDENT".equals(roleCode)) {
            List<Long> studentIds = studentProfileMapper.selectList(new LambdaQueryWrapper<StudentProfile>()
                            .like(StudentProfile::getMajor, major))
                    .stream().map(StudentProfile::getUserId).toList();
            ids.addAll(studentIds);
        }

        if (!StringUtils.hasText(roleCode) || "TEACHER".equals(roleCode)) {
            List<Long> teacherIds = teacherProfileMapper.selectList(new LambdaQueryWrapper<TeacherProfile>()
                            .like(TeacherProfile::getMajor, major))
                    .stream().map(TeacherProfile::getUserId).toList();
            ids.addAll(teacherIds);
        }
        return ids;
    }

    private Map<Long, String> loadRoleCodeMap(List<Long> userIds) {
        List<SysUserRole> relations = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId, userIds));
        if (relations.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> roleIds = relations.stream().map(SysUserRole::getRoleId).distinct().toList();
        Map<Long, String> roleMap = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getId, roleIds))
                .stream().collect(Collectors.toMap(SysRole::getId, SysRole::getRoleCode));

        Map<Long, String> userRoleMap = new HashMap<>();
        for (SysUserRole relation : relations) {
            if (!userRoleMap.containsKey(relation.getUserId())) {
                userRoleMap.put(relation.getUserId(), roleMap.get(relation.getRoleId()));
            }
        }
        return userRoleMap;
    }
}

