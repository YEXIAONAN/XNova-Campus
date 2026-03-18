package com.xnova.controller.student;

import com.xnova.common.model.PageResult;
import com.xnova.common.result.ApiResponse;
import com.xnova.dto.student.StudentExamQueryDTO;
import com.xnova.dto.student.SubmitExamDTO;
import com.xnova.service.student.StudentExamService;
import com.xnova.vo.student.RankingVO;
import com.xnova.vo.student.StudentExamDetailVO;
import com.xnova.vo.student.StudentExamListItemVO;
import com.xnova.vo.student.StudentExamResultVO;
import com.xnova.vo.student.SubmitExamVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/exams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentExamController {

    private final StudentExamService studentExamService;

    @GetMapping
    public ApiResponse<PageResult<StudentExamListItemVO>> page(StudentExamQueryDTO dto) {
        return ApiResponse.ok(studentExamService.pageExams(dto));
    }

    @GetMapping("/{publishId}")
    public ApiResponse<StudentExamDetailVO> detail(@PathVariable Long publishId) {
        return ApiResponse.ok(studentExamService.getExamDetail(publishId));
    }

    @PostMapping("/{publishId}/submit")
    public ApiResponse<SubmitExamVO> submit(@PathVariable Long publishId, @Valid @RequestBody SubmitExamDTO dto) {
        return ApiResponse.ok(studentExamService.submitExam(publishId, dto));
    }

    @GetMapping("/{publishId}/result")
    public ApiResponse<StudentExamResultVO> result(@PathVariable Long publishId) {
        return ApiResponse.ok(studentExamService.getResult(publishId));
    }

    @GetMapping("/{publishId}/ranking")
    public ApiResponse<RankingVO> ranking(@PathVariable Long publishId,
                                          @RequestParam(defaultValue = "CLASS") String dimension) {
        return ApiResponse.ok(studentExamService.getRanking(publishId, dimension));
    }
}

