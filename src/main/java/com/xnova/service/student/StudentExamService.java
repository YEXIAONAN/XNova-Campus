package com.xnova.service.student;

import com.xnova.common.model.PageResult;
import com.xnova.dto.student.StudentExamQueryDTO;
import com.xnova.dto.student.SubmitExamDTO;
import com.xnova.vo.student.RankingVO;
import com.xnova.vo.student.StudentExamDetailVO;
import com.xnova.vo.student.StudentExamListItemVO;
import com.xnova.vo.student.StudentExamResultVO;
import com.xnova.vo.student.SubmitExamVO;

public interface StudentExamService {

    PageResult<StudentExamListItemVO> pageExams(StudentExamQueryDTO dto);

    StudentExamDetailVO getExamDetail(Long publishId);

    SubmitExamVO submitExam(Long publishId, SubmitExamDTO dto);

    StudentExamResultVO getResult(Long publishId);

    RankingVO getRanking(Long publishId, String dimension);
}

