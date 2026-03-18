package com.xnova.controller.teacher;

import com.xnova.common.result.ApiResponse;
import com.xnova.dto.teacher.ImportConfirmDTO;
import com.xnova.service.teacher.TeacherQuestionImportService;
import com.xnova.vo.teacher.ImportConfirmVO;
import com.xnova.vo.teacher.ImportPreviewVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/teacher/question-import")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherQuestionImportController {

    private final TeacherQuestionImportService teacherQuestionImportService;

    @PostMapping("/preview")
    public ApiResponse<ImportPreviewVO> preview(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) Long targetPaperId
    ) {
        return ApiResponse.ok(teacherQuestionImportService.preview(file, targetPaperId));
    }

    @PostMapping("/confirm")
    public ApiResponse<ImportConfirmVO> confirm(@Valid @RequestBody ImportConfirmDTO dto) {
        return ApiResponse.ok(teacherQuestionImportService.confirm(dto));
    }
}

