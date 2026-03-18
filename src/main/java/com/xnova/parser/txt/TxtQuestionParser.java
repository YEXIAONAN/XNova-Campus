package com.xnova.parser.txt;

import com.xnova.common.enums.QuestionType;
import com.xnova.parser.model.ParseError;
import com.xnova.parser.model.ParseResult;
import com.xnova.parser.model.ParsedQuestion;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class TxtQuestionParser {

    private static final Set<String> FIELDS = Set.of("TYPE", "STEM", "OPTIONS", "ANSWER", "ANALYSIS", "SCORE");

    public ParseResult parse(byte[] fileContent) {
        String content = new String(fileContent, StandardCharsets.UTF_8);
        String[] lines = content.replace("\r\n", "\n").replace("\r", "\n").split("\n", -1);

        ParseResult result = new ParseResult();
        Integer questionIndex = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.startsWith("#PAPER:")) {
                result.setPaperName(line.substring(7).trim());
                continue;
            }

            if ("#QUESTION".equals(line)) {
                questionIndex++;
                int startLine = i + 1;
                int end = -1;
                for (int j = i + 1; j < lines.length; j++) {
                    if ("#END".equals(lines[j].trim())) {
                        end = j;
                        break;
                    }
                }

                if (end < 0) {
                    ParseError error = new ParseError();
                    error.setIndex(questionIndex);
                    error.setLine(startLine);
                    error.setField("#END");
                    error.setReason("题目块缺少#END");
                    result.getErrors().add(error);
                    break;
                }

                try {
                    ParsedQuestion parsedQuestion = parseQuestionBlock(lines, i + 1, end - 1, questionIndex, startLine);
                    result.getQuestions().add(parsedQuestion);
                } catch (IllegalArgumentException ex) {
                    ParseError error = new ParseError();
                    error.setIndex(questionIndex);
                    error.setLine(startLine);
                    error.setField("BLOCK");
                    error.setReason(ex.getMessage());
                    result.getErrors().add(error);
                }
                i = end;
            }
        }
        return result;
    }

    private ParsedQuestion parseQuestionBlock(String[] lines, int from, int to, int index, int startLine) {
        Map<String, StringBuilder> fieldMap = new HashMap<>();
        String currentField = null;

        for (int i = from; i <= to; i++) {
            String raw = lines[i];
            String trimmed = raw.trim();
            if (!StringUtils.hasText(trimmed)) {
                continue;
            }

            int colon = trimmed.indexOf(':');
            if (colon > 0) {
                String key = trimmed.substring(0, colon).trim().toUpperCase(Locale.ROOT);
                if (FIELDS.contains(key)) {
                    currentField = key;
                    String value = trimmed.substring(colon + 1).trim();
                    fieldMap.computeIfAbsent(key, k -> new StringBuilder());
                    if (StringUtils.hasText(value)) {
                        fieldMap.get(key).append(value);
                    }
                    continue;
                }
            }

            if (currentField == null) {
                throw new IllegalArgumentException("无法识别的行: " + trimmed);
            }
            if (fieldMap.get(currentField).length() > 0) {
                fieldMap.get(currentField).append("\n");
            }
            fieldMap.get(currentField).append(trimmed);
        }

        String typeText = getRequired(fieldMap, "TYPE");
        String stem = getRequired(fieldMap, "STEM");
        String answerRaw = getRequired(fieldMap, "ANSWER");

        QuestionType type;
        try {
            type = QuestionType.valueOf(typeText.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new IllegalArgumentException("TYPE不支持: " + typeText);
        }

        ParsedQuestion question = new ParsedQuestion();
        question.setIndex(index);
        question.setStartLine(startLine);
        question.setQuestionType(type.name());
        question.setStem(stem.trim());
        question.setAnalysis(getOptional(fieldMap, "ANALYSIS"));
        question.setScore(parseScore(getOptional(fieldMap, "SCORE")));

        if (type == QuestionType.SINGLE || type == QuestionType.MULTI) {
            String optionsRaw = getRequired(fieldMap, "OPTIONS");
            List<String> options = parseOptions(optionsRaw);
            if (options.size() < 2) {
                throw new IllegalArgumentException("客观题选项至少2个");
            }
            question.setOptions(options);
        }

        switch (type) {
            case SINGLE -> {
                String answer = answerRaw.trim().toUpperCase(Locale.ROOT);
                if (!answer.matches("[A-Z]")) {
                    throw new IllegalArgumentException("单选题ANSWER格式错误，示例: A");
                }
                question.setAnswer(answer);
            }
            case MULTI -> {
                String normalized = answerRaw.trim().toUpperCase(Locale.ROOT).replace(" ", "");
                if (!normalized.matches("[A-Z](,[A-Z])+")) {
                    throw new IllegalArgumentException("多选题ANSWER格式错误，示例: A,B");
                }
                Set<String> set = new LinkedHashSet<>(List.of(normalized.split(",")));
                question.setAnswers(new ArrayList<>(set));
            }
            case JUDGE -> {
                String normalized = answerRaw.trim().toUpperCase(Locale.ROOT);
                if ("TRUE".equals(normalized)) {
                    normalized = "T";
                }
                if ("FALSE".equals(normalized)) {
                    normalized = "F";
                }
                if (!"T".equals(normalized) && !"F".equals(normalized)) {
                    throw new IllegalArgumentException("判断题ANSWER仅支持 T/F/TRUE/FALSE");
                }
                question.setAnswer(normalized);
            }
            case SHORT -> question.setAnswer(answerRaw.trim());
        }

        question.setContentHash(buildHash(question));
        return question;
    }

    private BigDecimal parseScore(String raw) {
        if (!StringUtils.hasText(raw)) {
            return BigDecimal.valueOf(5);
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("SCORE必须是数字");
        }
    }

    private List<String> parseOptions(String raw) {
        List<String> options = new ArrayList<>();
        String[] lines = raw.split("\\n");
        for (String line : lines) {
            String t = line.trim();
            if (!StringUtils.hasText(t)) {
                continue;
            }
            if (t.matches("^[A-Z][\\.|、].*")) {
                options.add(t.substring(2).trim());
            } else {
                options.add(t);
            }
        }
        return options;
    }

    private String getRequired(Map<String, StringBuilder> map, String key) {
        String value = getOptional(map, key);
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(key + "不能为空");
        }
        return value;
    }

    private String getOptional(Map<String, StringBuilder> map, String key) {
        StringBuilder builder = map.get(key);
        return builder == null ? null : builder.toString();
    }

    private String buildHash(ParsedQuestion question) {
        String payload = String.join("|",
                question.getQuestionType(),
                normalize(question.getStem()),
                normalize(question.getOptions() == null ? "" : String.join("#", question.getOptions())),
                normalize(question.getAnswer() == null ? "" : question.getAnswer()),
                normalize(question.getAnswers() == null ? "" : String.join(",", question.getAnswers()))
        );
        return md5(payload);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private String md5(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("hash失败", e);
        }
    }
}

