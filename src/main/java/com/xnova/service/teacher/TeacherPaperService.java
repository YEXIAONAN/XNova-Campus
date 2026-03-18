package com.xnova.service.teacher;

import com.xnova.dto.teacher.AddPaperQuestionsDTO;
import com.xnova.dto.teacher.CreatePaperDTO;
import com.xnova.vo.teacher.PaperDetailVO;

public interface TeacherPaperService {
    Long createPaper(CreatePaperDTO dto);

    void addQuestions(Long paperId, AddPaperQuestionsDTO dto);

    PaperDetailVO getDetail(Long paperId);
}

