package com.xnova.service.teacher;

import com.xnova.dto.teacher.CreateQuestionDTO;
import com.xnova.vo.teacher.QuestionVO;

public interface TeacherQuestionService {
    Long createQuestion(CreateQuestionDTO dto);

    QuestionVO getQuestion(Long questionId);
}

