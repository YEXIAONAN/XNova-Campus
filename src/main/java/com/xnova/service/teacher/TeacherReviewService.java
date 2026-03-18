package com.xnova.service.teacher;

import com.xnova.common.model.PageResult;
import com.xnova.dto.teacher.GradeSubmissionDTO;
import com.xnova.dto.teacher.PendingReviewQueryDTO;
import com.xnova.dto.teacher.TeacherScoreQueryDTO;
import com.xnova.vo.teacher.ExamOverviewStatsVO;
import com.xnova.vo.teacher.PendingReviewVO;
import com.xnova.vo.teacher.ReviewDetailVO;
import com.xnova.vo.teacher.TeacherScoreVO;

public interface TeacherReviewService {

    PageResult<PendingReviewVO> pagePending(PendingReviewQueryDTO dto);

    ReviewDetailVO getReviewDetail(Long submissionId);

    void gradeSubmission(Long submissionId, GradeSubmissionDTO dto);

    PageResult<TeacherScoreVO> pageScores(TeacherScoreQueryDTO dto);

    ExamOverviewStatsVO getExamOverview(Long publishId, String className);
}
