package com.xnova.controller.teacher;

import com.xnova.common.result.ApiResponse;
import com.xnova.dto.teacher.AddPaperQuestionsDTO;
import com.xnova.dto.teacher.CreatePaperDTO;
import com.xnova.service.teacher.TeacherPaperService;
import com.xnova.vo.teacher.PaperDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher/papers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherPaperController {

    private final TeacherPaperService teacherPaperService;

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody CreatePaperDTO dto) {
        return ApiResponse.ok(teacherPaperService.createPaper(dto));
    }

    @PostMapping("/{paperId}/questions")
    public ApiResponse<Void> addQuestions(@PathVariable Long paperId, @Valid @RequestBody AddPaperQuestionsDTO dto) {
        teacherPaperService.addQuestions(paperId, dto);
        return ApiResponse.ok();
    }

    @GetMapping("/{paperId}")
    public ApiResponse<PaperDetailVO> detail(@PathVariable Long paperId) {
        return ApiResponse.ok(teacherPaperService.getDetail(paperId));
    }
}

