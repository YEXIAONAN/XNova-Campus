package com.xnova.service.impl.student;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xnova.common.enums.PublishScopeType;
import com.xnova.common.enums.QuestionType;
import com.xnova.common.enums.ScoreStatus;
import com.xnova.common.enums.SubmissionStatus;
import com.xnova.common.model.PageResult;
import com.xnova.dto.student.StudentExamQueryDTO;
import com.xnova.dto.student.SubmitExamDTO;
import com.xnova.entity.ExamAnswerDetail;
import com.xnova.entity.ExamPublish;
import com.xnova.entity.ExamScore;
import com.xnova.entity.ExamSubmission;
import com.xnova.entity.Paper;
import com.xnova.entity.PaperQuestion;
import com.xnova.entity.Question;
import com.xnova.entity.StudentProfile;
import com.xnova.exception.BizException;
import com.xnova.mapper.ExamAnswerDetailMapper;
import com.xnova.mapper.ExamPublishMapper;
import com.xnova.mapper.ExamScoreMapper;
import com.xnova.mapper.ExamSubmissionMapper;
import com.xnova.mapper.PaperMapper;
import com.xnova.mapper.PaperQuestionMapper;
import com.xnova.mapper.QuestionMapper;
import com.xnova.mapper.StudentProfileMapper;
import com.xnova.service.student.StudentExamService;
import com.xnova.utils.SecurityUtil;
import com.xnova.vo.student.RankingVO;
import com.xnova.vo.student.StudentExamDetailVO;
import com.xnova.vo.student.StudentExamListItemVO;
import com.xnova.vo.student.StudentExamResultVO;
import com.xnova.vo.student.SubmitExamVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentExamServiceImpl implements StudentExamService {

    private final ExamPublishMapper examPublishMapper;
    private final ExamSubmissionMapper examSubmissionMapper;
    private final ExamAnswerDetailMapper examAnswerDetailMapper;
    private final ExamScoreMapper examScoreMapper;
    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;
    private final StudentProfileMapper studentProfileMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<StudentExamListItemVO> pageExams(StudentExamQueryDTO dto) {
        Long studentId = SecurityUtil.currentUserId();
        StudentProfile profile = getStudentProfile(studentId);

        List<ExamPublish> publishes = examPublishMapper.selectList(new LambdaQueryWrapper<ExamPublish>()
                .eq(ExamPublish::getStatus, 1)
                .orderByDesc(ExamPublish::getStartTime));

        List<StudentExamListItemVO> all = publishes.stream()
                .filter(p -> matchTarget(p, profile))
                .map(p -> toListItemVO(studentId, p, dto.getStatus()))
                .filter(Objects::nonNull)
                .toList();

        int start = (int) ((dto.getPageNum() - 1) * dto.getPageSize());
        int end = Math.min(start + (int) dto.getPageSize(), all.size());
        List<StudentExamListItemVO> records = start >= all.size() ? List.of() : all.subList(start, end);

        PageResult<StudentExamListItemVO> result = new PageResult<>();
        result.setPageNum(dto.getPageNum());
        result.setPageSize(dto.getPageSize());
        result.setTotal(all.size());
        result.setRecords(records);
        return result;
    }

    @Override
    public StudentExamDetailVO getExamDetail(Long publishId) {
        Long studentId = SecurityUtil.currentUserId();
        StudentProfile profile = getStudentProfile(studentId);

        ExamPublish publish = getPublish(publishId);
        checkAccessible(publish, profile);

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(publish.getStartTime()) || now.isAfter(publish.getEndTime())) {
            throw new BizException(400, "当前不在考试作答时间范围内");
        }

        if (isSubmitted(publishId, studentId)) {
            throw new BizException(400, "该考试已提交，不能再次作答");
        }

        List<PaperQuestion> rels = paperQuestionMapper.selectList(new LambdaQueryWrapper<PaperQuestion>()
                .eq(PaperQuestion::getPaperId, publish.getPaperId()));
        List<Long> qids = rels.stream().map(PaperQuestion::getQuestionId).toList();
        Map<Long, Question> questionMap = qids.isEmpty() ? Map.of() : questionMapper.selectBatchIds(qids).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        Paper paper = paperMapper.selectById(publish.getPaperId());

        StudentExamDetailVO vo = new StudentExamDetailVO();
        vo.setPublishId(publish.getId());
        vo.setPaperId(publish.getPaperId());
        vo.setExamName(publish.getExamName());
        vo.setStartTime(publish.getStartTime());
        vo.setEndTime(publish.getEndTime());
        vo.setDurationMinutes(publish.getDurationMinutes());
        vo.setRemainSeconds(Math.max(0, Duration.between(now, publish.getEndTime()).getSeconds()));
        vo.setTotalScore(paper == null ? BigDecimal.ZERO : paper.getTotalScore());

        List<StudentExamDetailVO.QuestionItemVO> questions = rels.stream()
                .sorted(Comparator.comparing(PaperQuestion::getQuestionOrder))
                .map(rel -> {
                    Question q = questionMap.get(rel.getQuestionId());
                    if (q == null) {
                        return null;
                    }
                    StudentExamDetailVO.QuestionItemVO item = new StudentExamDetailVO.QuestionItemVO();
                    item.setQuestionId(q.getId());
                    item.setQuestionType(q.getQuestionType());
                    item.setStem(q.getStem());
                    item.setOptions(readJsonList(q.getOptionsJson()));
                    item.setQuestionOrder(rel.getQuestionOrder());
                    item.setScore(rel.getScore());
                    return item;
                })
                .filter(Objects::nonNull)
                .toList();
        vo.setQuestions(questions);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubmitExamVO submitExam(Long publishId, SubmitExamDTO dto) {
        Long studentId = SecurityUtil.currentUserId();
        StudentProfile profile = getStudentProfile(studentId);

        ExamPublish publish = getPublish(publishId);
        checkAccessible(publish, profile);

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(publish.getEndTime())) {
            throw new BizException(400, "考试已结束，不能提交");
        }
        if (now.isBefore(publish.getStartTime())) {
            throw new BizException(400, "考试尚未开始");
        }
        if (isSubmitted(publishId, studentId)) {
            throw new BizException(400, "请勿重复提交");
        }

        List<PaperQuestion> rels = paperQuestionMapper.selectList(new LambdaQueryWrapper<PaperQuestion>()
                .eq(PaperQuestion::getPaperId, publish.getPaperId()));
        if (rels.isEmpty()) {
            throw new BizException(400, "试卷未配置题目");
        }

        List<Long> qids = rels.stream().map(PaperQuestion::getQuestionId).toList();
        Map<Long, Question> questionMap = questionMapper.selectBatchIds(qids).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        Map<Long, SubmitExamDTO.AnswerItemDTO> answerMap = dto.getAnswers().stream()
                .collect(Collectors.toMap(SubmitExamDTO.AnswerItemDTO::getQuestionId, a -> a, (a, b) -> b));

        ExamSubmission submission = new ExamSubmission();
        submission.setPublishId(publishId);
        submission.setStudentId(studentId);
        submission.setSubmitTime(now);
        submission.setObjectiveScore(BigDecimal.ZERO);
        submission.setSubjectiveScore(null);
        submission.setTotalScore(BigDecimal.ZERO);
        submission.setStatus(SubmissionStatus.SUBMITTED.name());
        submission.setReviewStatus("AUTO_FINISHED");
        submission.setScoreStatus(ScoreStatus.FINAL.name());
        submission.setDurationSeconds(dto.getDurationSeconds());
        submission.setIsTimeout(0);
        examSubmissionMapper.insert(submission);

        BigDecimal objectiveTotal = BigDecimal.ZERO;
        boolean hasSubjective = false;

        for (PaperQuestion rel : rels) {
            Question question = questionMap.get(rel.getQuestionId());
            if (question == null) {
                continue;
            }
            SubmitExamDTO.AnswerItemDTO ans = answerMap.get(rel.getQuestionId());

            ExamAnswerDetail detail = new ExamAnswerDetail();
            detail.setSubmissionId(submission.getId());
            detail.setQuestionId(question.getId());
            detail.setQuestionType(question.getQuestionType());
            detail.setStandardAnswerJson(question.getAnswerJson());

            BigDecimal score = rel.getScore() == null ? BigDecimal.ZERO : rel.getScore();
            String qType = question.getQuestionType();

            if (QuestionType.SHORT.name().equals(qType)) {
                hasSubjective = true;
                detail.setStudentAnswerJson(writeJson(ans == null ? null : ans.getAnswer()));
                detail.setAutoScore(null);
                detail.setManualScore(null);
                detail.setFinalScore(null);
                detail.setIsCorrect(null);
                detail.setJudgeStatus("PENDING_REVIEW");
            } else if (QuestionType.SINGLE.name().equals(qType) || QuestionType.JUDGE.name().equals(qType)) {
                String studentAns = normalizeSingle(ans == null ? null : ans.getAnswer(), qType);
                String standard = normalizeSingle(readJsonString(question.getAnswerJson()), qType);
                boolean correct = StringUtils.hasText(studentAns) && studentAns.equals(standard);
                BigDecimal finalScore = correct ? score : BigDecimal.ZERO;
                detail.setStudentAnswerJson(writeJson(studentAns));
                detail.setAutoScore(finalScore);
                detail.setFinalScore(finalScore);
                detail.setManualScore(null);
                detail.setIsCorrect(correct ? 1 : 0);
                detail.setJudgeStatus("AUTO");
                objectiveTotal = objectiveTotal.add(finalScore);
            } else if (QuestionType.MULTI.name().equals(qType)) {
                List<String> studentSet = normalizeMulti(ans);
                List<String> standardSet = normalizeMulti(readJsonList(question.getAnswerJson()));
                boolean correct = studentSet.equals(standardSet);
                BigDecimal finalScore = correct ? score : BigDecimal.ZERO;
                detail.setStudentAnswerJson(writeJson(studentSet));
                detail.setAutoScore(finalScore);
                detail.setFinalScore(finalScore);
                detail.setManualScore(null);
                detail.setIsCorrect(correct ? 1 : 0);
                detail.setJudgeStatus("AUTO");
                objectiveTotal = objectiveTotal.add(finalScore);
            } else {
                throw new BizException(500, "不支持的题型: " + qType);
            }
            examAnswerDetailMapper.insert(detail);
        }

        String scoreStatus = hasSubjective ? ScoreStatus.PENDING_REVIEW.name() : ScoreStatus.FINAL.name();
        String reviewStatus = hasSubjective ? "PENDING_REVIEW" : "AUTO_FINISHED";

        submission.setObjectiveScore(objectiveTotal);
        submission.setTotalScore(objectiveTotal);
        submission.setReviewStatus(reviewStatus);
        submission.setScoreStatus(scoreStatus);
        examSubmissionMapper.updateById(submission);

        ExamScore examScore = new ExamScore();
        examScore.setPublishId(publishId);
        examScore.setStudentId(studentId);
        examScore.setSubmissionId(submission.getId());
        examScore.setClassName(profile.getClassName());
        examScore.setGrade(profile.getGrade());
        examScore.setTotalScore(objectiveTotal);
        examScore.setScoreStatus(scoreStatus);
        examScore.setCreatedAt(LocalDateTime.now());
        examScoreMapper.insert(examScore);

        refreshRanking(publishId);

        SubmitExamVO vo = new SubmitExamVO();
        vo.setSubmissionId(submission.getId());
        vo.setObjectiveScore(objectiveTotal);
        vo.setSubjectiveScore(null);
        vo.setTotalScore(objectiveTotal);
        vo.setScoreStatus(scoreStatus);
        return vo;
    }

    @Override
    public StudentExamResultVO getResult(Long publishId) {
        Long studentId = SecurityUtil.currentUserId();
        ExamSubmission submission = examSubmissionMapper.selectOne(new LambdaQueryWrapper<ExamSubmission>()
                .eq(ExamSubmission::getPublishId, publishId)
                .eq(ExamSubmission::getStudentId, studentId)
                .last("limit 1"));
        if (submission == null) {
            throw new BizException(404, "尚未提交该考试");
        }

        ExamScore score = examScoreMapper.selectOne(new LambdaQueryWrapper<ExamScore>()
                .eq(ExamScore::getSubmissionId, submission.getId())
                .last("limit 1"));

        StudentExamResultVO vo = new StudentExamResultVO();
        vo.setPublishId(publishId);
        vo.setSubmissionId(submission.getId());
        vo.setObjectiveScore(submission.getObjectiveScore());
        vo.setSubjectiveScore(submission.getSubjectiveScore());
        vo.setTotalScore(submission.getTotalScore());
        vo.setSubmissionStatus(submission.getStatus());
        vo.setScoreStatus(StringUtils.hasText(submission.getScoreStatus())
                ? submission.getScoreStatus()
                : (score == null ? ScoreStatus.PENDING_REVIEW.name() : score.getScoreStatus()));
        return vo;
    }

    @Override
    public RankingVO getRanking(Long publishId, String dimension) {
        Long studentId = SecurityUtil.currentUserId();
        StudentProfile profile = getStudentProfile(studentId);

        String dim = StringUtils.hasText(dimension) ? dimension.toUpperCase(Locale.ROOT) : "CLASS";
        if (!"CLASS".equals(dim) && !"GRADE".equals(dim)) {
            throw new BizException(400, "dimension仅支持CLASS或GRADE");
        }

        refreshRanking(publishId);

        LambdaQueryWrapper<ExamScore> wrapper = new LambdaQueryWrapper<ExamScore>()
                .eq(ExamScore::getPublishId, publishId)
                .eq(ExamScore::getScoreStatus, ScoreStatus.FINAL.name());

        if ("CLASS".equals(dim)) {
            wrapper.eq(ExamScore::getClassName, profile.getClassName());
        } else {
            wrapper.eq(ExamScore::getGrade, profile.getGrade());
        }

        List<ExamScore> list = examScoreMapper.selectList(wrapper);
        if (list.isEmpty()) {
            RankingVO vo = new RankingVO();
            vo.setPublishId(publishId);
            vo.setDimension(dim);
            vo.setMyRank(null);
            vo.setTotalParticipants(0);
            return vo;
        }

        Integer myRank;
        ExamScore mine = list.stream().filter(s -> studentId.equals(s.getStudentId())).findFirst().orElse(null);
        if (mine == null) {
            myRank = null;
        } else {
            myRank = "CLASS".equals(dim) ? mine.getRankClass() : mine.getRankGrade();
        }

        RankingVO vo = new RankingVO();
        vo.setPublishId(publishId);
        vo.setDimension(dim);
        vo.setMyRank(myRank);
        vo.setTotalParticipants(list.size());
        return vo;
    }

    private StudentExamListItemVO toListItemVO(Long studentId, ExamPublish publish, String statusFilter) {
        LocalDateTime now = LocalDateTime.now();
        boolean submitted = isSubmitted(publish.getId(), studentId);

        String examStatus;
        if (now.isBefore(publish.getStartTime())) {
            examStatus = "NOT_STARTED";
        } else if (now.isAfter(publish.getEndTime())) {
            examStatus = submitted ? "FINISHED" : "MISSED";
        } else {
            examStatus = submitted ? "FINISHED" : "AVAILABLE";
        }

        if (StringUtils.hasText(statusFilter) && !statusFilter.equalsIgnoreCase(examStatus)) {
            return null;
        }

        StudentExamListItemVO vo = new StudentExamListItemVO();
        vo.setPublishId(publish.getId());
        vo.setExamName(publish.getExamName());
        vo.setStartTime(publish.getStartTime());
        vo.setEndTime(publish.getEndTime());
        vo.setDurationMinutes(publish.getDurationMinutes());
        vo.setPaperQuestionCount(countPaperQuestions(publish.getPaperId()));
        vo.setSubmitStatus(submitted ? "SUBMITTED" : "NOT_SUBMITTED");
        vo.setExamStatus(examStatus);
        return vo;
    }

    private int countPaperQuestions(Long paperId) {
        Long count = paperQuestionMapper.selectCount(new LambdaQueryWrapper<PaperQuestion>()
                .eq(PaperQuestion::getPaperId, paperId));
        return count == null ? 0 : count.intValue();
    }

    private boolean isSubmitted(Long publishId, Long studentId) {
        Long count = examSubmissionMapper.selectCount(new LambdaQueryWrapper<ExamSubmission>()
                .eq(ExamSubmission::getPublishId, publishId)
                .eq(ExamSubmission::getStudentId, studentId));
        return count != null && count > 0;
    }

    private ExamPublish getPublish(Long publishId) {
        ExamPublish publish = examPublishMapper.selectById(publishId);
        if (publish == null || publish.getStatus() == null || publish.getStatus() != 1) {
            throw new BizException(404, "考试不存在或已下线");
        }
        return publish;
    }

    private StudentProfile getStudentProfile(Long studentId) {
        StudentProfile profile = studentProfileMapper.selectOne(new LambdaQueryWrapper<StudentProfile>()
                .eq(StudentProfile::getUserId, studentId)
                .last("limit 1"));
        if (profile == null) {
            throw new BizException(400, "当前用户未绑定学生档案");
        }
        return profile;
    }

    private void checkAccessible(ExamPublish publish, StudentProfile profile) {
        if (!matchTarget(publish, profile)) {
            throw new BizException(403, "你不在该考试发布范围内");
        }
    }

    private boolean matchTarget(ExamPublish publish, StudentProfile profile) {
        String type = publish.getTargetScopeType();
        if (!StringUtils.hasText(type) || PublishScopeType.ALL.name().equalsIgnoreCase(type)) {
            return true;
        }
        if (PublishScopeType.CLASS.name().equalsIgnoreCase(type)) {
            return StringUtils.hasText(profile.getClassName()) && profile.getClassName().equals(publish.getTargetScopeValue());
        }
        if (PublishScopeType.GRADE.name().equalsIgnoreCase(type)) {
            return StringUtils.hasText(profile.getGrade()) && profile.getGrade().equals(publish.getTargetScopeValue());
        }
        return false;
    }

    private String normalizeSingle(String value, String qType) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String v = value.trim().toUpperCase(Locale.ROOT);
        if (QuestionType.JUDGE.name().equals(qType)) {
            if ("TRUE".equals(v)) {
                return "T";
            }
            if ("FALSE".equals(v)) {
                return "F";
            }
        }
        return v;
    }

    private List<String> normalizeMulti(SubmitExamDTO.AnswerItemDTO ans) {
        if (ans == null) {
            return List.of();
        }
        if (ans.getAnswers() != null && !ans.getAnswers().isEmpty()) {
            return normalizeMulti(ans.getAnswers());
        }
        if (StringUtils.hasText(ans.getAnswer())) {
            return normalizeMulti(List.of(ans.getAnswer().split(",")));
        }
        return List.of();
    }

    private List<String> normalizeMulti(List<String> values) {
        Set<String> set = new HashSet<>();
        for (String s : values) {
            if (!StringUtils.hasText(s)) {
                continue;
            }
            String normalized = s.trim().toUpperCase(Locale.ROOT);
            if (normalized.length() > 1 && normalized.contains(",")) {
                for (String part : normalized.split(",")) {
                    if (StringUtils.hasText(part)) {
                        set.add(part.trim());
                    }
                }
            } else {
                set.add(normalized);
            }
        }
        List<String> list = new ArrayList<>(set);
        list.sort(String::compareTo);
        return list;
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

    private String writeJson(Object value) {
        try {
            return value == null ? null : objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new BizException(500, "答案序列化失败");
        }
    }

    private void refreshRanking(Long publishId) {
        List<ExamScore> list = examScoreMapper.selectList(new LambdaQueryWrapper<ExamScore>()
                .eq(ExamScore::getPublishId, publishId)
                .eq(ExamScore::getScoreStatus, ScoreStatus.FINAL.name()));
        if (list.isEmpty()) {
            return;
        }

        Map<String, List<ExamScore>> classGroup = list.stream().collect(Collectors.groupingBy(ExamScore::getClassName));
        for (List<ExamScore> group : classGroup.values()) {
            rankGroup(group, true);
        }

        Map<String, List<ExamScore>> gradeGroup = list.stream().collect(Collectors.groupingBy(ExamScore::getGrade));
        for (List<ExamScore> group : gradeGroup.values()) {
            rankGroup(group, false);
        }

        for (ExamScore score : list) {
            examScoreMapper.updateById(score);
        }
    }

    private void rankGroup(List<ExamScore> group, boolean isClass) {
        group.sort(Comparator.comparing(ExamScore::getTotalScore, Comparator.nullsLast(BigDecimal::compareTo)).reversed()
                .thenComparing(ExamScore::getStudentId));

        int rank = 0;
        BigDecimal last = null;
        for (ExamScore score : group) {
            if (last == null || score.getTotalScore() == null || score.getTotalScore().compareTo(last) != 0) {
                rank++;
                last = score.getTotalScore();
            }
            if (isClass) {
                score.setRankClass(rank);
            } else {
                score.setRankGrade(rank);
            }
        }
    }
}

