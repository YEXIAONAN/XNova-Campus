package com.xnova.service.teacher;

import com.xnova.dto.teacher.ImportConfirmDTO;
import com.xnova.vo.teacher.ImportConfirmVO;
import com.xnova.vo.teacher.ImportPreviewVO;
import org.springframework.web.multipart.MultipartFile;

public interface TeacherQuestionImportService {

    ImportPreviewVO preview(MultipartFile file, Long targetPaperId);

    ImportConfirmVO confirm(ImportConfirmDTO dto);
}

