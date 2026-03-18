package com.xnova.service.impl.teacher;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xnova.dto.teacher.AddPaperQuestionsDTO;
import com.xnova.dto.teacher.CreatePaperDTO;
import com.xnova.entity.Paper;
import com.xnova.entity.PaperQuestion;
import com.xnova.entity.Question;
import com.xnova.exception.BizException;
import com.xnova.mapper.PaperMapper;
import com.xnova.mapper.PaperQuestionMapper;
import com.xnova.mapper.QuestionMapper;
import com.xnova.service.teacher.TeacherPaperService;
import com.xnova.utils.SecurityUtil;
import com.xnova.vo.teacher.PaperDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TeacherPaperServiceImpl implements TeacherPaperService {

    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPaper(CreatePaperDTO dto) {
        Paper paper = new Paper();
        paper.setCreatorId(SecurityUtil.currentUserId());
        paper.setPaperName(dto.getPaperName().trim());
        paper.setDescription(dto.getDescription());
        paper.setDurationMinutes(dto.getDurationMinutes() == null ? 60 : dto.getDurationMinutes());
        paper.setTotalScore(BigDecimal.ZERO);
        paper.setStatus("DRAFT");
        paper.setCreatedAt(LocalDateTime.now());
        paper.setUpdatedAt(LocalDateTime.now());
        paperMapper.insert(paper);
        return paper.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addQuestions(Long paperId, AddPaperQuestionsDTO dto) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BizException(404, "试卷不存在");
        }

        int startOrder = getMaxOrder(paperId) + 1;
        BigDecimal appendScore = BigDecimal.ZERO;

        int i = 0;
        for (AddPaperQuestionsDTO.Item item : dto.getItems()) {
            Question question = questionMapper.selectById(item.getQuestionId());
            if (question == null) {
                throw new BizException(404, "题目不存在: " + item.getQuestionId());
            }
            PaperQuestion pq = new PaperQuestion();
            pq.setPaperId(paperId);
            pq.setQuestionId(item.getQuestionId());
            pq.setQuestionOrder(item.getQuestionOrder() == null ? startOrder + i : item.getQuestionOrder());
            BigDecimal score = item.getScore() == null ? question.getDefaultScore() : item.getScore();
            pq.setScore(score);
            pq.setRequired(item.getRequired() == null ? 1 : item.getRequired());
            paperQuestionMapper.insert(pq);
            appendScore = appendScore.add(score == null ? BigDecimal.ZERO : score);
            i++;
        }

        paper.setTotalScore((paper.getTotalScore() == null ? BigDecimal.ZERO : paper.getTotalScore()).add(appendScore));
        paper.setUpdatedAt(LocalDateTime.now());
        paperMapper.updateById(paper);
    }

    @Override
    public PaperDetailVO getDetail(Long paperId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BizException(404, "试卷不存在");
        }
        List<PaperQuestion> relations = paperQuestionMapper.selectList(new LambdaQueryWrapper<PaperQuestion>()
                .eq(PaperQuestion::getPaperId, paperId));

        List<Long> qids = relations.stream().map(PaperQuestion::getQuestionId).toList();
        final Map<Long, Question> questionMap = qids.isEmpty()
                ? Map.of()
                : questionMapper.selectBatchIds(qids).stream()
                        .collect(java.util.stream.Collectors.toMap(Question::getId, q -> q));

        PaperDetailVO vo = new PaperDetailVO();
        vo.setId(paper.getId());
        vo.setPaperName(paper.getPaperName());
        vo.setDescription(paper.getDescription());
        vo.setDurationMinutes(paper.getDurationMinutes());
        vo.setTotalScore(paper.getTotalScore());

        List<PaperDetailVO.PaperQuestionItemVO> items = relations.stream()
                .sorted(Comparator.comparing(PaperQuestion::getQuestionOrder))
                .map(r -> {
                    Question q = questionMap.get(r.getQuestionId());
                    PaperDetailVO.PaperQuestionItemVO item = new PaperDetailVO.PaperQuestionItemVO();
                    item.setQuestionId(r.getQuestionId());
                    item.setQuestionOrder(r.getQuestionOrder());
                    item.setScore(r.getScore());
                    if (q != null) {
                        item.setQuestionType(q.getQuestionType());
                        item.setStem(q.getStem());
                        item.setOptions(readList(q.getOptionsJson()));
                    }
                    return item;
                })
                .toList();
        vo.setQuestions(items);
        return vo;
    }

    private int getMaxOrder(Long paperId) {
        List<PaperQuestion> list = paperQuestionMapper.selectList(new LambdaQueryWrapper<PaperQuestion>()
                .eq(PaperQuestion::getPaperId, paperId));
        return list.stream().map(PaperQuestion::getQuestionOrder).max(Integer::compareTo).orElse(0);
    }

    private List<String> readList(String json) {
        try {
            if (json == null || json.isBlank()) {
                return List.of();
            }
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}

