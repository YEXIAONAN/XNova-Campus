package com.xnova.service.impl.teacher;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xnova.dto.teacher.ImportConfirmDTO;
import com.xnova.entity.Paper;
import com.xnova.entity.PaperQuestion;
import com.xnova.entity.Question;
import com.xnova.exception.BizException;
import com.xnova.mapper.PaperMapper;
import com.xnova.mapper.PaperQuestionMapper;
import com.xnova.mapper.QuestionMapper;
import com.xnova.parser.model.ParseResult;
import com.xnova.parser.model.ParsedQuestion;
import com.xnova.parser.txt.TxtQuestionParser;
import com.xnova.service.teacher.TeacherQuestionImportService;
import com.xnova.utils.SecurityUtil;
import com.xnova.vo.teacher.ImportConfirmVO;
import com.xnova.vo.teacher.ImportPreviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TeacherQuestionImportServiceImpl implements TeacherQuestionImportService {

    private static final Map<String, ImportPreviewContext> PREVIEW_CACHE = new ConcurrentHashMap<>();

    private final TxtQuestionParser txtQuestionParser;
    private final QuestionMapper questionMapper;
    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ImportPreviewVO preview(MultipartFile file, Long targetPaperId) {
        try {
            if (file == null || file.isEmpty()) {
                throw new BizException(400, "请上传txt文件");
            }

            ParseResult parseResult = txtQuestionParser.parse(file.getBytes());
            Long teacherId = SecurityUtil.currentUserId();

            List<ImportPreviewVO.ImportQuestionPreviewVO> previews = new ArrayList<>();
            int duplicateCount = 0;
            for (ParsedQuestion parsed : parseResult.getQuestions()) {
                boolean duplicate = existsHash(teacherId, parsed.getContentHash());
                if (duplicate) {
                    duplicateCount++;
                }
                ImportPreviewVO.ImportQuestionPreviewVO vo = new ImportPreviewVO.ImportQuestionPreviewVO();
                vo.setIndex(parsed.getIndex());
                vo.setQuestionType(parsed.getQuestionType());
                vo.setStem(parsed.getStem());
                vo.setOptions(parsed.getOptions());
                vo.setAnswer(parsed.getAnswer());
                vo.setAnswers(parsed.getAnswers());
                vo.setScore(parsed.getScore());
                vo.setContentHash(parsed.getContentHash());
                vo.setDuplicate(duplicate);
                previews.add(vo);
            }

            List<ImportPreviewVO.ImportErrorVO> errors = parseResult.getErrors().stream().map(e -> {
                ImportPreviewVO.ImportErrorVO vo = new ImportPreviewVO.ImportErrorVO();
                vo.setIndex(e.getIndex());
                vo.setLine(e.getLine());
                vo.setField(e.getField());
                vo.setReason(e.getReason());
                return vo;
            }).toList();

            String token = UUID.randomUUID().toString().replace("-", "");
            ImportPreviewContext context = new ImportPreviewContext();
            context.setTeacherId(teacherId);
            context.setPaperName(parseResult.getPaperName());
            context.setParsedQuestions(parseResult.getQuestions());
            context.setCreatedAt(System.currentTimeMillis());
            context.setTargetPaperId(targetPaperId);
            PREVIEW_CACHE.put(token, context);

            ImportPreviewVO result = new ImportPreviewVO();
            result.setImportToken(token);
            result.setPaperName(parseResult.getPaperName());
            result.setTotalCount(parseResult.getQuestions().size() + errors.size());
            result.setSuccessCount(parseResult.getQuestions().size());
            result.setFailCount(errors.size());
            result.setQuestions(previews);
            result.setErrors(errors);
            return result;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(500, "文件解析失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportConfirmVO confirm(ImportConfirmDTO dto) {
        ImportPreviewContext context = PREVIEW_CACHE.get(dto.getImportToken());
        if (context == null) {
            throw new BizException(400, "预览令牌无效或已过期，请重新预览");
        }

        Long teacherId = SecurityUtil.currentUserId();
        if (!teacherId.equals(context.getTeacherId())) {
            throw new BizException(403, "无权确认该导入任务");
        }

        cleanExpired();

        List<Long> questionIds = new ArrayList<>();
        int duplicateCount = 0;

        boolean saveToBank = Boolean.TRUE.equals(dto.getSaveToBank());
        for (ParsedQuestion parsed : context.getParsedQuestions()) {
            Question existing = findByHash(teacherId, parsed.getContentHash());
            if (existing != null) {
                duplicateCount++;
                questionIds.add(existing.getId());
                continue;
            }
            if (!saveToBank) {
                continue;
            }

            Question question = new Question();
            question.setCreatorId(teacherId);
            question.setQuestionType(parsed.getQuestionType());
            question.setStem(parsed.getStem());
            question.setOptionsJson(writeJson(parsed.getOptions()));
            question.setAnswerJson(writeJson(parsed.getAnswers() != null ? parsed.getAnswers() : parsed.getAnswer()));
            question.setAnalysis(parsed.getAnalysis());
            question.setDefaultScore(parsed.getScore());
            question.setStatus(1);
            question.setContentHash(parsed.getContentHash());
            question.setCreatedAt(LocalDateTime.now());
            question.setUpdatedAt(LocalDateTime.now());
            questionMapper.insert(question);
            questionIds.add(question.getId());
        }

        Long paperId = null;
        int appendedCount = 0;
        if (Boolean.TRUE.equals(dto.getAppendToPaper())) {
            paperId = resolvePaperId(dto, context, teacherId);
            int startOrder = getMaxOrder(paperId) + 1;
            BigDecimal totalAppendScore = BigDecimal.ZERO;

            for (int i = 0; i < questionIds.size(); i++) {
                Long qid = questionIds.get(i);
                Question question = questionMapper.selectById(qid);
                if (question == null) {
                    continue;
                }
                PaperQuestion rel = new PaperQuestion();
                rel.setPaperId(paperId);
                rel.setQuestionId(qid);
                rel.setQuestionOrder(startOrder + i);
                rel.setScore(question.getDefaultScore());
                rel.setRequired(1);
                paperQuestionMapper.insert(rel);
                totalAppendScore = totalAppendScore.add(question.getDefaultScore() == null ? BigDecimal.ZERO : question.getDefaultScore());
                appendedCount++;
            }

            Paper paper = paperMapper.selectById(paperId);
            if (paper != null) {
                paper.setTotalScore((paper.getTotalScore() == null ? BigDecimal.ZERO : paper.getTotalScore()).add(totalAppendScore));
                paper.setUpdatedAt(LocalDateTime.now());
                paperMapper.updateById(paper);
            }
        }

        PREVIEW_CACHE.remove(dto.getImportToken());

        ImportConfirmVO result = new ImportConfirmVO();
        result.setPaperId(paperId);
        result.setAppendedCount(appendedCount);
        result.setDuplicateCount(duplicateCount);
        result.setImportedQuestionIds(questionIds);
        return result;
    }

    private void cleanExpired() {
        long now = System.currentTimeMillis();
        PREVIEW_CACHE.entrySet().removeIf(entry -> now - entry.getValue().getCreatedAt() > 30 * 60 * 1000);
    }

    private Long resolvePaperId(ImportConfirmDTO dto, ImportPreviewContext context, Long teacherId) {
        if (dto.getTargetPaperId() != null) {
            Paper existing = paperMapper.selectById(dto.getTargetPaperId());
            if (existing == null) {
                throw new BizException(404, "目标试卷不存在");
            }
            return existing.getId();
        }

        if (context.getTargetPaperId() != null) {
            Paper existing = paperMapper.selectById(context.getTargetPaperId());
            if (existing != null) {
                return existing.getId();
            }
        }

        Paper paper = new Paper();
        paper.setCreatorId(teacherId);
        paper.setPaperName(StringUtils.hasText(context.getPaperName()) ? context.getPaperName() : "导入试卷-" + System.currentTimeMillis());
        paper.setDescription("TXT导入自动创建");
        paper.setDurationMinutes(60);
        paper.setTotalScore(BigDecimal.ZERO);
        paper.setStatus("DRAFT");
        paper.setCreatedAt(LocalDateTime.now());
        paper.setUpdatedAt(LocalDateTime.now());
        paperMapper.insert(paper);
        return paper.getId();
    }

    private int getMaxOrder(Long paperId) {
        return paperQuestionMapper.selectList(new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, paperId))
                .stream().map(PaperQuestion::getQuestionOrder)
                .max(Integer::compareTo).orElse(0);
    }

    private Question findByHash(Long teacherId, String contentHash) {
        return questionMapper.selectOne(new LambdaQueryWrapper<Question>()
                .eq(Question::getCreatorId, teacherId)
                .eq(Question::getContentHash, contentHash)
                .last("limit 1"));
    }

    private boolean existsHash(Long teacherId, String contentHash) {
        Long count = questionMapper.selectCount(new LambdaQueryWrapper<Question>()
                .eq(Question::getCreatorId, teacherId)
                .eq(Question::getContentHash, contentHash));
        return count != null && count > 0;
    }

    private String writeJson(Object value) {
        try {
            return value == null ? null : objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new BizException(500, "JSON序列化失败");
        }
    }

    private static class ImportPreviewContext {
        private Long teacherId;
        private String paperName;
        private List<ParsedQuestion> parsedQuestions;
        private long createdAt;
        private Long targetPaperId;

        public Long getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(Long teacherId) {
            this.teacherId = teacherId;
        }

        public String getPaperName() {
            return paperName;
        }

        public void setPaperName(String paperName) {
            this.paperName = paperName;
        }

        public List<ParsedQuestion> getParsedQuestions() {
            return parsedQuestions;
        }

        public void setParsedQuestions(List<ParsedQuestion> parsedQuestions) {
            this.parsedQuestions = parsedQuestions;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(long createdAt) {
            this.createdAt = createdAt;
        }

        public Long getTargetPaperId() {
            return targetPaperId;
        }

        public void setTargetPaperId(Long targetPaperId) {
            this.targetPaperId = targetPaperId;
        }
    }
}

