package com.xnova.controller.teacher;

import com.xnova.common.model.PageResult;
import com.xnova.common.result.ApiResponse;
import com.xnova.dto.teacher.GradeSubmissionDTO;
import com.xnova.dto.teacher.PendingReviewQueryDTO;
import com.xnova.dto.teacher.TeacherScoreQueryDTO;
import com.xnova.service.teacher.TeacherReviewService;
import com.xnova.vo.teacher.ExamOverviewStatsVO;
import com.xnova.vo.teacher.PendingReviewVO;
import com.xnova.vo.teacher.ReviewDetailVO;
import com.xnova.vo.teacher.TeacherScoreVO;
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
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherReviewController {

    private final TeacherReviewService teacherReviewService;

    @GetMapping("/reviews/pending")
    public ApiResponse<PageResult<PendingReviewVO>> pending(PendingReviewQueryDTO dto) {
        return ApiResponse.ok(teacherReviewService.pagePending(dto));
    }

    @GetMapping("/reviews/{submissionId}")
    public ApiResponse<ReviewDetailVO> detail(@PathVariable Long submissionId) {
        return ApiResponse.ok(teacherReviewService.getReviewDetail(submissionId));
    }

    @PostMapping("/reviews/{submissionId}/grade")
    public ApiResponse<Void> grade(@PathVariable Long submissionId, @Valid @RequestBody GradeSubmissionDTO dto) {
        teacherReviewService.gradeSubmission(submissionId, dto);
        return ApiResponse.ok();
    }

    @GetMapping("/scores")
    public ApiResponse<PageResult<TeacherScoreVO>> scores(TeacherScoreQueryDTO dto) {
        return ApiResponse.ok(teacherReviewService.pageScores(dto));
    }

    @GetMapping("/stats/exam-overview")
    public ApiResponse<ExamOverviewStatsVO> overview(@RequestParam Long publishId,
                                                     @RequestParam(required = false) String className) {
        return ApiResponse.ok(teacherReviewService.getExamOverview(publishId, className));
    }
}
