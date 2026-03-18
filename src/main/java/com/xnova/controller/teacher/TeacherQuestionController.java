package com.xnova.controller.teacher;

import com.xnova.common.result.ApiResponse;
import com.xnova.dto.teacher.CreateQuestionDTO;
import com.xnova.service.teacher.TeacherQuestionService;
import com.xnova.vo.teacher.QuestionVO;
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
@RequestMapping("/api/teacher/questions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherQuestionController {

    private final TeacherQuestionService teacherQuestionService;

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody CreateQuestionDTO dto) {
        return ApiResponse.ok(teacherQuestionService.createQuestion(dto));
    }

    @GetMapping("/{questionId}")
    public ApiResponse<QuestionVO> detail(@PathVariable Long questionId) {
        return ApiResponse.ok(teacherQuestionService.getQuestion(questionId));
    }
}

