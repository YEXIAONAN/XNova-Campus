package com.xnova.service.impl.teacher;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xnova.common.enums.QuestionType;
import com.xnova.dto.teacher.CreateQuestionDTO;
import com.xnova.entity.Question;
import com.xnova.exception.BizException;
import com.xnova.mapper.QuestionMapper;
import com.xnova.service.teacher.TeacherQuestionService;
import com.xnova.utils.SecurityUtil;
import com.xnova.vo.teacher.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherQuestionServiceImpl implements TeacherQuestionService {

    private final QuestionMapper questionMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createQuestion(CreateQuestionDTO dto) {
        validateCreate(dto);

        Question question = new Question();
        question.setCreatorId(SecurityUtil.currentUserId());
        question.setQuestionType(dto.getQuestionType().name());
        question.setStem(dto.getStem().trim());
        question.setOptionsJson(writeJson(dto.getOptions()));
        question.setAnswerJson(writeJson(resolveAnswer(dto)));
        question.setAnalysis(dto.getAnalysis());
        question.setDefaultScore(dto.getDefaultScore() == null ? BigDecimal.valueOf(5) : dto.getDefaultScore());
        question.setStatus(1);
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        questionMapper.insert(question);
        return question.getId();
    }

    @Override
    public QuestionVO getQuestion(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BizException(404, "题目不存在");
        }
        QuestionVO vo = new QuestionVO();
        vo.setId(question.getId());
        vo.setQuestionType(question.getQuestionType());
        vo.setStem(question.getStem());
        vo.setOptions(readOptions(question.getOptionsJson()));
        if (QuestionType.MULTI.name().equals(question.getQuestionType())) {
            vo.setAnswers(readJsonList(question.getAnswerJson()));
        } else {
            vo.setAnswer(readJsonString(question.getAnswerJson()));
        }
        vo.setAnalysis(question.getAnalysis());
        vo.setDefaultScore(question.getDefaultScore());
        return vo;
    }

    private void validateCreate(CreateQuestionDTO dto) {
        if ((dto.getQuestionType() == QuestionType.SINGLE || dto.getQuestionType() == QuestionType.MULTI)
                && (dto.getOptions() == null || dto.getOptions().size() < 2)) {
            throw new BizException(400, "客观题至少需要2个选项");
        }
        if (dto.getQuestionType() == QuestionType.SINGLE && !StringUtils.hasText(dto.getAnswer())) {
            throw new BizException(400, "单选题答案不能为空");
        }
        if (dto.getQuestionType() == QuestionType.MULTI && (dto.getAnswers() == null || dto.getAnswers().isEmpty())) {
            throw new BizException(400, "多选题答案不能为空");
        }
        if ((dto.getQuestionType() == QuestionType.JUDGE || dto.getQuestionType() == QuestionType.SHORT)
                && !StringUtils.hasText(dto.getAnswer())) {
            throw new BizException(400, "答案不能为空");
        }
    }

    private Object resolveAnswer(CreateQuestionDTO dto) {
        if (dto.getQuestionType() == QuestionType.MULTI) {
            return dto.getAnswers();
        }
        return dto.getAnswer();
    }

    private String writeJson(Object value) {
        try {
            return value == null ? null : objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new BizException(500, "JSON序列化失败");
        }
    }

    private List<String> readOptions(String json) {
        return readJsonList(json);
    }

    private List<String> readJsonList(String json) {
        try {
            if (!StringUtils.hasText(json)) {
                return List.of();
            }
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    private String readJsonString(String json) {
        try {
            if (!StringUtils.hasText(json)) {
                return null;
            }
            return objectMapper.readValue(json, String.class);
        } catch (Exception e) {
            return json;
        }
    }
}

