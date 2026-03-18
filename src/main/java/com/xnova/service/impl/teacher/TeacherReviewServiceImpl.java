package com.xnova.service.impl.teacher;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xnova.common.enums.QuestionType;
import com.xnova.common.enums.ScoreStatus;
import com.xnova.common.model.PageResult;
import com.xnova.dto.teacher.GradeSubmissionDTO;
import com.xnova.dto.teacher.PendingReviewQueryDTO;
import com.xnova.dto.teacher.TeacherScoreQueryDTO;
import com.xnova.entity.ExamAnswerDetail;
import com.xnova.entity.ExamPublish;
import com.xnova.entity.ExamScore;
import com.xnova.entity.ExamSubmission;
import com.xnova.entity.Paper;
import com.xnova.entity.PaperQuestion;
import com.xnova.entity.Question;
import com.xnova.entity.StudentProfile;
import com.xnova.entity.SysUser;
import com.xnova.entity.TeacherReviewLog;
import com.xnova.exception.BizException;
import com.xnova.mapper.ExamAnswerDetailMapper;
import com.xnova.mapper.ExamPublishMapper;
import com.xnova.mapper.ExamScoreMapper;
import com.xnova.mapper.ExamSubmissionMapper;
import com.xnova.mapper.PaperMapper;
import com.xnova.mapper.PaperQuestionMapper;
import com.xnova.mapper.QuestionMapper;
import com.xnova.mapper.StudentProfileMapper;
import com.xnova.mapper.SysUserMapper;
import com.xnova.mapper.TeacherReviewLogMapper;
import com.xnova.service.teacher.TeacherReviewService;
import com.xnova.utils.SecurityUtil;
import com.xnova.vo.teacher.ExamOverviewStatsVO;
import com.xnova.vo.teacher.PendingReviewVO;
import com.xnova.vo.teacher.ReviewDetailVO;
import com.xnova.vo.teacher.TeacherScoreVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherReviewServiceImpl implements TeacherReviewService {

    private static final BigDecimal PASS_LINE = BigDecimal.valueOf(60);

    private final ExamSubmissionMapper examSubmissionMapper;
    private final ExamAnswerDetailMapper examAnswerDetailMapper;
    private final ExamPublishMapper examPublishMapper;
    private final ExamScoreMapper examScoreMapper;
    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;
    private final SysUserMapper sysUserMapper;
    private final StudentProfileMapper studentProfileMapper;
    private final TeacherReviewLogMapper teacherReviewLogMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<PendingReviewVO> pagePending(PendingReviewQueryDTO dto) {
        List<ExamSubmission> scoped = listTeacherScopedSubmissions(SecurityUtil.currentUserId(), dto.getPublishId());

        List<ExamSubmission> pending = scoped.stream()
                .filter(s -> "PENDING_REVIEW".equalsIgnoreCase(s.getReviewStatus()))
                .toList();

        List<PendingReviewVO> all = buildPendingVOs(pending).stream()
                .filter(v -> filterByClassAndName(v.getClassName(), v.getStudentName(), dto.getClassName(), dto.getStudentName()))
                .toList();

        return slicePage(all, dto.getPageNum(), dto.getPageSize());
    }

    @Override
    public ReviewDetailVO getReviewDetail(Long submissionId) {
        Long teacherId = SecurityUtil.currentUserId();
        ExamSubmission submission = mustSubmission(submissionId);
        ExamPublish publish = mustPublish(submission.getPublishId());
        ensureTeacherAccess(teacherId, publish);

        SysUser student = sysUserMapper.selectById(submission.getStudentId());
        StudentProfile profile = studentProfileMapper.selectOne(new LambdaQueryWrapper<StudentProfile>()
                .eq(StudentProfile::getUserId, submission.getStudentId())
                .last("limit 1"));

        List<ExamAnswerDetail> details = examAnswerDetailMapper.selectList(new LambdaQueryWrapper<ExamAnswerDetail>()
                .eq(ExamAnswerDetail::getSubmissionId, submissionId));

        List<Long> qids = details.stream().map(ExamAnswerDetail::getQuestionId).distinct().toList();
        Map<Long, Question> qMap = qids.isEmpty() ? Map.of() : questionMapper.selectBatchIds(qids).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        Map<Long, BigDecimal> scoreMap = paperQuestionMapper.selectList(new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, publish.getPaperId()))
                .stream().collect(Collectors.toMap(PaperQuestion::getQuestionId, PaperQuestion::getScore, (a, b) -> a));

        ReviewDetailVO vo = new ReviewDetailVO();
        vo.setSubmissionId(submission.getId());
        vo.setPublishId(publish.getId());
        vo.setExamName(publish.getExamName());
        vo.setStudentId(submission.getStudentId());
        vo.setStudentName(student == null ? "" : student.getRealName());
        vo.setClassName(profile == null ? null : profile.getClassName());
        vo.setGrade(profile == null ? null : profile.getGrade());
        vo.setSubmitTime(submission.getSubmitTime());
        vo.setObjectiveScore(submission.getObjectiveScore());
        vo.setSubjectiveScore(submission.getSubjectiveScore());
        vo.setTotalScore(submission.getTotalScore());
        vo.setReviewStatus(submission.getReviewStatus());
        vo.setScoreStatus(submission.getScoreStatus());

        List<ReviewDetailVO.QuestionReviewItemVO> items = details.stream().map(d -> {
            ReviewDetailVO.QuestionReviewItemVO item = new ReviewDetailVO.QuestionReviewItemVO();
            item.setAnswerDetailId(d.getId());
            item.setQuestionId(d.getQuestionId());
            item.setQuestionType(d.getQuestionType());
            Question q = qMap.get(d.getQuestionId());
            item.setStem(q == null ? null : q.getStem());
            item.setStandardAnswer(formatAnswer(d.getStandardAnswerJson()));
            item.setStudentAnswer(formatAnswer(d.getStudentAnswerJson()));
            item.setMaxScore(scoreMap.get(d.getQuestionId()));
            item.setAutoScore(d.getAutoScore());
            item.setManualScore(d.getManualScore());
            item.setFinalScore(d.getFinalScore());
            item.setJudgeStatus(d.getJudgeStatus());
            return item;
        }).toList();
        vo.setQuestions(items);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void gradeSubmission(Long submissionId, GradeSubmissionDTO dto) {
        Long teacherId = SecurityUtil.currentUserId();
        ExamSubmission submission = mustSubmission(submissionId);
        ExamPublish publish = mustPublish(submission.getPublishId());
        ensureTeacherAccess(teacherId, publish);

        if (!"PENDING_REVIEW".equalsIgnoreCase(submission.getReviewStatus())
                && !"REVIEWING".equalsIgnoreCase(submission.getReviewStatus())) {
            throw new BizException(400, "该答卷不在可批改状态");
        }

        List<ExamAnswerDetail> subjective = examAnswerDetailMapper.selectList(new LambdaQueryWrapper<ExamAnswerDetail>()
                .eq(ExamAnswerDetail::getSubmissionId, submissionId)
                .eq(ExamAnswerDetail::getQuestionType, QuestionType.SHORT.name()));
        if (subjective.isEmpty()) {
            throw new BizException(400, "该答卷无主观题，无需批改");
        }

        Map<Long, BigDecimal> maxScoreMap = paperQuestionMapper.selectList(new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, publish.getPaperId()))
                .stream().collect(Collectors.toMap(PaperQuestion::getQuestionId, PaperQuestion::getScore, (a, b) -> a));

        Map<Long, GradeSubmissionDTO.Item> gradeMap = dto.getItems().stream()
                .collect(Collectors.toMap(GradeSubmissionDTO.Item::getAnswerDetailId, i -> i, (a, b) -> b));

        submission.setReviewStatus("REVIEWING");
        examSubmissionMapper.updateById(submission);

        for (ExamAnswerDetail detail : subjective) {
            GradeSubmissionDTO.Item item = gradeMap.get(detail.getId());
            if (item == null) {
                throw new BizException(400, "主观题未全部批改，缺少answerDetailId=" + detail.getId());
            }
            BigDecimal maxScore = maxScoreMap.getOrDefault(detail.getQuestionId(), BigDecimal.ZERO);
            if (item.getManualScore().compareTo(maxScore) > 0) {
                throw new BizException(400, "分数超出题目上限，answerDetailId=" + detail.getId());
            }
            detail.setManualScore(item.getManualScore());
            detail.setFinalScore(item.getManualScore());
            detail.setJudgeStatus("MANUAL_DONE");
            examAnswerDetailMapper.updateById(detail);
        }

        BigDecimal subjectiveTotal = subjective.stream()
                .map(ExamAnswerDetail::getFinalScore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal objectiveTotal = submission.getObjectiveScore() == null ? BigDecimal.ZERO : submission.getObjectiveScore();
        BigDecimal totalScore = objectiveTotal.add(subjectiveTotal);

        submission.setSubjectiveScore(subjectiveTotal);
        submission.setTotalScore(totalScore);
        submission.setReviewStatus("REVIEW_FINISHED");
        submission.setScoreStatus(ScoreStatus.FINAL.name());
        examSubmissionMapper.updateById(submission);

        ExamScore score = examScoreMapper.selectOne(new LambdaQueryWrapper<ExamScore>()
                .eq(ExamScore::getSubmissionId, submissionId)
                .last("limit 1"));
        if (score != null) {
            score.setTotalScore(totalScore);
            score.setScoreStatus(ScoreStatus.FINAL.name());
            examScoreMapper.updateById(score);
        }

        TeacherReviewLog log = new TeacherReviewLog();
        log.setTeacherId(teacherId);
        log.setSubmissionId(submissionId);
        log.setPublishId(publish.getId());
        log.setActionType("FINISH_REVIEW");
        log.setCreatedAt(LocalDateTime.now());
        teacherReviewLogMapper.insert(log);

        refreshRanking(publish.getId());
    }

    @Override
    public PageResult<TeacherScoreVO> pageScores(TeacherScoreQueryDTO dto) {
        List<ExamSubmission> scoped = listTeacherScopedSubmissions(SecurityUtil.currentUserId(), dto.getPublishId());

        List<TeacherScoreVO> all = buildScoreVOs(scoped).stream()
                .filter(v -> filterByClassAndName(v.getClassName(), v.getStudentName(), dto.getClassName(), dto.getStudentName()))
                .filter(v -> !StringUtils.hasText(dto.getReviewStatus()) || dto.getReviewStatus().equalsIgnoreCase(v.getReviewStatus()))
                .toList();

        return slicePage(all, dto.getPageNum(), dto.getPageSize());
    }

    @Override
    public ExamOverviewStatsVO getExamOverview(Long publishId, String className) {
        Long teacherId = SecurityUtil.currentUserId();
        ExamPublish publish = mustPublish(publishId);
        ensureTeacherAccess(teacherId, publish);

        List<ExamSubmission> submissions = examSubmissionMapper.selectList(new LambdaQueryWrapper<ExamSubmission>()
                .eq(ExamSubmission::getPublishId, publishId));

        if (StringUtils.hasText(className)) {
            Set<Long> classStudents = studentProfileMapper.selectList(new LambdaQueryWrapper<StudentProfile>()
                            .eq(StudentProfile::getClassName, className))
                    .stream().map(StudentProfile::getUserId).collect(Collectors.toSet());
            submissions = submissions.stream().filter(s -> classStudents.contains(s.getStudentId())).toList();
        }

        List<Long> submissionIds = submissions.stream().map(ExamSubmission::getId).toList();
        List<ExamScore> scores = submissionIds.isEmpty() ? List.of() : examScoreMapper.selectList(new LambdaQueryWrapper<ExamScore>()
                .in(ExamScore::getSubmissionId, submissionIds)
                .eq(ExamScore::getScoreStatus, ScoreStatus.FINAL.name()));

        BigDecimal avg = BigDecimal.ZERO;
        BigDecimal max = BigDecimal.ZERO;
        BigDecimal min = BigDecimal.ZERO;
        BigDecimal passRate = BigDecimal.ZERO;

        if (!scores.isEmpty()) {
            List<BigDecimal> values = scores.stream().map(ExamScore::getTotalScore).filter(Objects::nonNull).toList();
            if (!values.isEmpty()) {
                BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                avg = sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
                max = values.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                min = values.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                long pass = values.stream().filter(v -> v.compareTo(PASS_LINE) >= 0).count();
                passRate = BigDecimal.valueOf(pass)
                        .divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
            }
        }

        Long reviewTimes = teacherReviewLogMapper.selectCount(new LambdaQueryWrapper<TeacherReviewLog>()
                .eq(TeacherReviewLog::getTeacherId, teacherId)
                .eq(TeacherReviewLog::getPublishId, publishId)
                .eq(TeacherReviewLog::getActionType, "FINISH_REVIEW"));

        int reviewFinished = (int) submissions.stream().filter(s -> "REVIEW_FINISHED".equalsIgnoreCase(s.getReviewStatus())).count();
        int pendingReview = (int) submissions.stream().filter(s -> "PENDING_REVIEW".equalsIgnoreCase(s.getReviewStatus())).count();

        ExamOverviewStatsVO vo = new ExamOverviewStatsVO();
        vo.setPublishId(publishId);
        vo.setClassName(className);
        vo.setSubmittedCount(submissions.size());
        vo.setReviewFinishedCount(reviewFinished);
        vo.setPendingReviewCount(pendingReview);
        vo.setAvgScore(avg);
        vo.setMaxScore(max);
        vo.setMinScore(min);
        vo.setPassRate(passRate);
        vo.setReviewTimes(reviewTimes == null ? 0 : reviewTimes);
        return vo;
    }

    private List<ExamSubmission> listTeacherScopedSubmissions(Long teacherId, Long publishId) {
        List<Long> publishIds;
        if (publishId != null) {
            ExamPublish publish = mustPublish(publishId);
            ensureTeacherAccess(teacherId, publish);
            publishIds = List.of(publishId);
        } else {
            List<Long> paperIds = paperMapper.selectList(new LambdaQueryWrapper<Paper>()
                            .eq(Paper::getCreatorId, teacherId))
                    .stream().map(Paper::getId).toList();
            if (paperIds.isEmpty()) {
                return List.of();
            }
            publishIds = examPublishMapper.selectList(new LambdaQueryWrapper<ExamPublish>()
                            .in(ExamPublish::getPaperId, paperIds))
                    .stream().map(ExamPublish::getId).toList();
            if (publishIds.isEmpty()) {
                return List.of();
            }
        }

        return examSubmissionMapper.selectList(new LambdaQueryWrapper<ExamSubmission>()
                .in(ExamSubmission::getPublishId, publishIds)
                .orderByDesc(ExamSubmission::getSubmitTime));
    }

    private List<PendingReviewVO> buildPendingVOs(List<ExamSubmission> submissions) {
        if (submissions.isEmpty()) {
            return List.of();
        }
        List<Long> publishIds = submissions.stream().map(ExamSubmission::getPublishId).distinct().toList();
        Map<Long, ExamPublish> publishMap = examPublishMapper.selectBatchIds(publishIds).stream()
                .collect(Collectors.toMap(ExamPublish::getId, p -> p));

        List<Long> studentIds = submissions.stream().map(ExamSubmission::getStudentId).distinct().toList();
        Map<Long, SysUser> userMap = sysUserMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));
        Map<Long, StudentProfile> profileMap = studentProfileMapper.selectList(new LambdaQueryWrapper<StudentProfile>()
                        .in(StudentProfile::getUserId, studentIds))
                .stream().collect(Collectors.toMap(StudentProfile::getUserId, p -> p));

        List<PendingReviewVO> list = new ArrayList<>();
        for (ExamSubmission submission : submissions) {
            PendingReviewVO vo = new PendingReviewVO();
            vo.setSubmissionId(submission.getId());
            vo.setPublishId(submission.getPublishId());
            ExamPublish publish = publishMap.get(submission.getPublishId());
            vo.setExamName(publish == null ? "" : publish.getExamName());
            vo.setStudentId(submission.getStudentId());
            SysUser user = userMap.get(submission.getStudentId());
            vo.setStudentName(user == null ? "" : user.getRealName());
            StudentProfile profile = profileMap.get(submission.getStudentId());
            vo.setClassName(profile == null ? null : profile.getClassName());
            vo.setGrade(profile == null ? null : profile.getGrade());
            vo.setSubmitTime(submission.getSubmitTime());
            vo.setObjectiveScore(submission.getObjectiveScore());
            vo.setReviewStatus(submission.getReviewStatus());
            vo.setScoreStatus(submission.getScoreStatus());
            list.add(vo);
        }
        return list;
    }

    private List<TeacherScoreVO> buildScoreVOs(List<ExamSubmission> submissions) {
        if (submissions.isEmpty()) {
            return List.of();
        }
        List<Long> publishIds = submissions.stream().map(ExamSubmission::getPublishId).distinct().toList();
        Map<Long, ExamPublish> publishMap = examPublishMapper.selectBatchIds(publishIds).stream()
                .collect(Collectors.toMap(ExamPublish::getId, p -> p));

        List<Long> studentIds = submissions.stream().map(ExamSubmission::getStudentId).distinct().toList();
        Map<Long, SysUser> userMap = sysUserMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));
        Map<Long, StudentProfile> profileMap = studentProfileMapper.selectList(new LambdaQueryWrapper<StudentProfile>()
                        .in(StudentProfile::getUserId, studentIds))
                .stream().collect(Collectors.toMap(StudentProfile::getUserId, p -> p));

        List<TeacherScoreVO> list = new ArrayList<>();
        for (ExamSubmission submission : submissions) {
            TeacherScoreVO vo = new TeacherScoreVO();
            vo.setSubmissionId(submission.getId());
            vo.setPublishId(submission.getPublishId());
            ExamPublish publish = publishMap.get(submission.getPublishId());
            vo.setExamName(publish == null ? "" : publish.getExamName());
            vo.setStudentId(submission.getStudentId());
            SysUser user = userMap.get(submission.getStudentId());
            vo.setStudentName(user == null ? "" : user.getRealName());
            StudentProfile profile = profileMap.get(submission.getStudentId());
            vo.setClassName(profile == null ? null : profile.getClassName());
            vo.setGrade(profile == null ? null : profile.getGrade());
            vo.setSubmitTime(submission.getSubmitTime());
            vo.setObjectiveScore(submission.getObjectiveScore());
            vo.setSubjectiveScore(submission.getSubjectiveScore());
            vo.setTotalScore(submission.getTotalScore());
            vo.setReviewStatus(submission.getReviewStatus());
            vo.setScoreStatus(submission.getScoreStatus());
            list.add(vo);
        }
        return list;
    }

    private boolean filterByClassAndName(String className, String studentName, String classFilter, String nameFilter) {
        boolean classOk = !StringUtils.hasText(classFilter)
                || (StringUtils.hasText(className) && className.contains(classFilter));
        boolean nameOk = !StringUtils.hasText(nameFilter)
                || (StringUtils.hasText(studentName) && studentName.contains(nameFilter));
        return classOk && nameOk;
    }

    private <T> PageResult<T> slicePage(List<T> all, long pageNum, long pageSize) {
        int start = (int) ((pageNum - 1) * pageSize);
        int end = Math.min(start + (int) pageSize, all.size());
        List<T> records = start >= all.size() ? List.of() : all.subList(start, end);

        PageResult<T> result = new PageResult<>();
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotal(all.size());
        result.setRecords(records);
        return result;
    }

    private ExamSubmission mustSubmission(Long submissionId) {
        ExamSubmission submission = examSubmissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BizException(404, "答卷不存在");
        }
        return submission;
    }

    private ExamPublish mustPublish(Long publishId) {
        ExamPublish publish = examPublishMapper.selectById(publishId);
        if (publish == null || publish.getStatus() == null || publish.getStatus() != 1) {
            throw new BizException(404, "考试不存在或已下线");
        }
        return publish;
    }

    private void ensureTeacherAccess(Long teacherId, ExamPublish publish) {
        Paper paper = paperMapper.selectById(publish.getPaperId());
        if (paper == null || !teacherId.equals(paper.getCreatorId())) {
            throw new BizException(403, "无权操作该考试");
        }
    }

    private String formatAnswer(String json) {
        if (!StringUtils.hasText(json)) {
            return "";
        }
        try {
            if (json.trim().startsWith("[")) {
                List<String> list = objectMapper.readValue(json, new TypeReference<>() {
                });
                return String.join(",", list);
            }
            return objectMapper.readValue(json, String.class);
        } catch (Exception e) {
            return json;
        }
    }

    private void refreshRanking(Long publishId) {
        List<ExamScore> list = examScoreMapper.selectList(new LambdaQueryWrapper<ExamScore>()
                .eq(ExamScore::getPublishId, publishId)
                .eq(ExamScore::getScoreStatus, ScoreStatus.FINAL.name()));
        if (list.isEmpty()) {
            return;
        }

        Map<String, List<ExamScore>> classGroup = list.stream().collect(Collectors.groupingBy(s -> {
            String k = s.getClassName();
            return StringUtils.hasText(k) ? k : "_";
        }));
        classGroup.values().forEach(group -> rank(group, true));

        Map<String, List<ExamScore>> gradeGroup = list.stream().collect(Collectors.groupingBy(s -> {
            String k = s.getGrade();
            return StringUtils.hasText(k) ? k : "_";
        }));
        gradeGroup.values().forEach(group -> rank(group, false));

        list.forEach(examScoreMapper::updateById);
    }

    private void rank(List<ExamScore> group, boolean classRank) {
        group.sort(Comparator.comparing(ExamScore::getTotalScore, Comparator.nullsLast(BigDecimal::compareTo)).reversed()
                .thenComparing(ExamScore::getStudentId));
        int rank = 0;
        BigDecimal last = null;
        for (ExamScore score : group) {
            if (last == null || score.getTotalScore() == null || score.getTotalScore().compareTo(last) != 0) {
                rank++;
                last = score.getTotalScore();
            }
            if (classRank) {
                score.setRankClass(rank);
            } else {
                score.setRankGrade(rank);
            }
        }
    }
}
