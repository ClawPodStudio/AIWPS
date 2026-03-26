package com.aiwps.controller;

import com.aiwps.entity.*;
import com.aiwps.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 班级整体统计分析接口
 */
@RestController
@RequestMapping("/api")
public class AdminClassStatisticsController {

    @Autowired
    private ClassService classService;

    @Autowired
    private UserService userService;

    @Autowired
    private WrongQuestionService wrongQuestionService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private KnowledgeMasteryService knowledgeMasteryService;

    @Autowired
    private KnowledgePointService knowledgePointService;

    /**
     * 全班高频错题统计
     * 返回错题列表按错误次数降序，含题目内容、错误次数、错误率
     */
    @GetMapping("/admin/class/{classId}/wrong-topic-stats")
    public Map<String, Object> wrongTopicStats(@PathVariable Long classId) {
        // 1. 获取班级学生列表
        List<User> students = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getClassId, classId)
                .eq(User::getRole, "STUDENT"));
        
        if (students.isEmpty()) {
            return Map.of("code", 200, "msg", "success", "data", Map.of("records", List.of(), "total", 0));
        }
        
        List<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toList());
        int studentCount = studentIds.size();
        
        // 2. 获取班级所有学生的错题（汇总）
        List<WrongQuestion> wrongQuestions = wrongQuestionService.list(
                new LambdaQueryWrapper<WrongQuestion>()
                        .in(WrongQuestion::getStudentId, studentIds)
                        .orderByDesc(WrongQuestion::getWrongCount));
        
        // 3. 按questionId分组统计
        Map<Long, Integer> questionWrongCountMap = new HashMap<>();
        Map<Long, WrongQuestion> questionWrongMap = new HashMap<>();
        for (WrongQuestion wq : wrongQuestions) {
            Long qId = wq.getQuestionId();
            int totalWrong = questionWrongCountMap.getOrDefault(qId, 0) + wq.getWrongCount();
            questionWrongCountMap.put(qId, totalWrong);
            if (!questionWrongMap.containsKey(qId)) {
                questionWrongMap.put(qId, wq);
            }
        }
        
        // 4. 获取题目详情
        List<Long> questionIds = new ArrayList<>(questionWrongCountMap.keySet());
        Map<Long, Question> questionMap;
        if (!questionIds.isEmpty()) {
            List<Question> questions = questionService.listByIds(questionIds);
            questionMap = questions.stream().collect(Collectors.toMap(Question::getId, q -> q));
        } else {
            questionMap = new HashMap<>();
        }
        
        // 5. 构建结果（按错误次数降序）
        List<Map<String, Object>> records = questionWrongCountMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(entry -> {
                    Long qId = entry.getKey();
                    Integer totalWrong = entry.getValue();
                    Question question = questionMap.get(qId);
                    BigDecimal wrongRate = BigDecimal.valueOf(totalWrong)
                            .divide(BigDecimal.valueOf(studentCount), 2, RoundingMode.HALF_UP);
                    
                    Map<String, Object> item = new HashMap<>();
                    item.put("questionId", qId);
                    item.put("content", question != null ? question.getContent() : "");
                    item.put("type", question != null ? question.getType() : "");
                    item.put("answer", question != null ? question.getAnswer() : "");
                    item.put("analysis", question != null ? question.getAnalysis() : "");
                    item.put("difficulty", question != null ? question.getDifficulty() : 0);
                    item.put("wrongCount", totalWrong);
                    item.put("wrongRate", wrongRate);
                    item.put("studentCount", studentCount);
                    return item;
                })
                .collect(Collectors.toList());
        
        return Map.of("code", 200, "msg", "success", "data",
                Map.of("records", records, "total", records.size(), "studentCount", studentCount));
    }

    /**
     * 共性薄弱知识点 - 错误率>50%的知识点
     */
    @GetMapping("/admin/class/{classId}/weak-knowledge-stats")
    public Map<String, Object> weakKnowledgeStats(@PathVariable Long classId) {
        // 1. 获取班级学生列表
        List<User> students = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getClassId, classId)
                .eq(User::getRole, "STUDENT"));
        
        if (students.isEmpty()) {
            return Map.of("code", 200, "msg", "success", "data", List.of());
        }
        
        List<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toList());
        int studentCount = studentIds.size();
        
        // 2. 获取班级所有学生的错题
        List<WrongQuestion> wrongQuestions = wrongQuestionService.list(
                new LambdaQueryWrapper<WrongQuestion>()
                        .in(WrongQuestion::getStudentId, studentIds));
        
        // 3. 获取涉及的题目
        List<Long> questionIds = wrongQuestions.stream()
                .map(WrongQuestion::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
        
        if (questionIds.isEmpty()) {
            return Map.of("code", 200, "msg", "success", "data", List.of());
        }
        
        Map<Long, Question> questionMap = questionService.listByIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        
        // 4. 按知识点统计错误率
        // 错误率 = 有该知识点错题的学生数 / 总学生数
        Map<Long, Set<Long>> kpStudentWrongMap = new HashMap<>(); // knowledgePointId -> set of studentIds who got it wrong
        
        for (WrongQuestion wq : wrongQuestions) {
            Question q = questionMap.get(wq.getQuestionId());
            if (q == null || q.getKnowledgePointIds() == null || q.getKnowledgePointIds().isEmpty()) {
                continue;
            }
            
            // 题目可能有多个知识点，用逗号分隔
            String[] kpIds = q.getKnowledgePointIds().split(",");
            for (String kpIdStr : kpIds) {
                try {
                    Long kpId = Long.parseLong(kpIdStr.trim());
                    kpStudentWrongMap.computeIfAbsent(kpId, k -> new HashSet<>()).add(wq.getStudentId());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        
        // 5. 获取知识点详情
        List<Long> kpIds = new ArrayList<>(kpStudentWrongMap.keySet());
        Map<Long, KnowledgePoint> kpMap;
        if (!kpIds.isEmpty()) {
            kpMap = knowledgePointService.listByIds(kpIds).stream()
                    .collect(Collectors.toMap(KnowledgePoint::getId, kp -> kp));
        } else {
            kpMap = new HashMap<>();
        }
        
        // 6. 计算错误率>50%的知识点
        List<Map<String, Object>> weakKps = kpStudentWrongMap.entrySet().stream()
                .filter(entry -> {
                    BigDecimal rate = BigDecimal.valueOf(entry.getValue().size())
                            .divide(BigDecimal.valueOf(studentCount), 2, RoundingMode.HALF_UP);
                    return rate.compareTo(BigDecimal.valueOf(0.5)) > 0;
                })
                .map(entry -> {
                    Long kpId = entry.getKey();
                    int wrongStudentCount = entry.getValue().size();
                    BigDecimal wrongRate = BigDecimal.valueOf(wrongStudentCount)
                            .divide(BigDecimal.valueOf(studentCount), 2, RoundingMode.HALF_UP);
                    
                    KnowledgePoint kp = kpMap.get(kpId);
                    Map<String, Object> item = new HashMap<>();
                    item.put("knowledgePointId", kpId);
                    item.put("knowledgePointName", kp != null ? kp.getName() : "");
                    item.put("wrongStudentCount", wrongStudentCount);
                    item.put("wrongRate", wrongRate);
                    item.put("studentCount", studentCount);
                    return item;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("wrongRate")).compareTo((BigDecimal) a.get("wrongRate")))
                .collect(Collectors.toList());
        
        return Map.of("code", 200, "msg", "success", "data", weakKps);
    }

    /**
     * 题型错误率统计
     */
    @GetMapping("/admin/class/{classId}/question-type-stats")
    public Map<String, Object> questionTypeStats(@PathVariable Long classId) {
        // 1. 获取班级学生列表
        List<User> students = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getClassId, classId)
                .eq(User::getRole, "STUDENT"));
        
        if (students.isEmpty()) {
            return Map.of("code", 200, "msg", "success", "data", List.of());
        }
        
        List<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toList());
        int studentCount = studentIds.size();
        
        // 2. 获取班级所有学生的错题
        List<WrongQuestion> wrongQuestions = wrongQuestionService.list(
                new LambdaQueryWrapper<WrongQuestion>()
                        .in(WrongQuestion::getStudentId, studentIds));
        
        // 3. 获取涉及的题目
        List<Long> questionIds = wrongQuestions.stream()
                .map(WrongQuestion::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, Question> questionMap;
        if (!questionIds.isEmpty()) {
            questionMap = questionService.listByIds(questionIds).stream()
                    .collect(Collectors.toMap(Question::getId, q -> q));
        } else {
            questionMap = new HashMap<>();
        }
        
        // 4. 按题型统计 - 每个学生每个题型算一次"参与"
        // 错误率 = 有该题型错题的学生数 / 总学生数
        Map<String, Set<Long>> typeStudentWrongMap = new HashMap<>();
        
        for (WrongQuestion wq : wrongQuestions) {
            Question q = questionMap.get(wq.getQuestionId());
            if (q == null || q.getType() == null) {
                continue;
            }
            typeStudentWrongMap.computeIfAbsent(q.getType(), k -> new HashSet<>()).add(wq.getStudentId());
        }
        
        // 5. 计算每种题型的错误率
        List<Map<String, Object>> typeStats = typeStudentWrongMap.entrySet().stream()
                .map(entry -> {
                    String type = entry.getKey();
                    int wrongStudentCount = entry.getValue().size();
                    BigDecimal wrongRate = BigDecimal.valueOf(wrongStudentCount)
                            .divide(BigDecimal.valueOf(studentCount), 2, RoundingMode.HALF_UP);
                    
                    Map<String, Object> item = new HashMap<>();
                    item.put("questionType", type);
                    item.put("wrongStudentCount", wrongStudentCount);
                    item.put("wrongRate", wrongRate);
                    item.put("studentCount", studentCount);
                    return item;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("wrongRate")).compareTo((BigDecimal) a.get("wrongRate")))
                .collect(Collectors.toList());
        
        return Map.of("code", 200, "msg", "success", "data", typeStats);
    }

    /**
     * 知识点掌握度聚合
     */
    @GetMapping("/admin/class/{classId}/knowledge-mastery")
    public Map<String, Object> knowledgeMastery(@PathVariable Long classId) {
        // 1. 获取班级学生列表
        List<User> students = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getClassId, classId)
                .eq(User::getRole, "STUDENT"));
        
        if (students.isEmpty()) {
            return Map.of("code", 200, "msg", "success", "data", List.of());
        }
        
        List<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toList());
        
        // 2. 获取班级所有学生的知识点掌握数据
        List<KnowledgeMastery> masteryList = knowledgeMasteryService.list(
                new LambdaQueryWrapper<KnowledgeMastery>()
                        .in(KnowledgeMastery::getStudentId, studentIds));
        
        // 3. 按知识点聚合
        Map<Long, List<KnowledgeMastery>> kpMasteryMap = masteryList.stream()
                .collect(Collectors.groupingBy(KnowledgeMastery::getKnowledgePointId));
        
        // 4. 获取知识点详情
        List<Long> kpIds = new ArrayList<>(kpMasteryMap.keySet());
        Map<Long, KnowledgePoint> kpMap;
        if (!kpIds.isEmpty()) {
            kpMap = knowledgePointService.listByIds(kpIds).stream()
                    .collect(Collectors.toMap(KnowledgePoint::getId, kp -> kp));
        } else {
            kpMap = new HashMap<>();
        }
        
        // 5. 计算每个知识点的平均掌握度
        List<Map<String, Object>> masteryStats = kpMasteryMap.entrySet().stream()
                .map(entry -> {
                    Long kpId = entry.getKey();
                    List<KnowledgeMastery> list = entry.getValue();
                    
                    // 计算平均掌握度
                    BigDecimal avgMastery = list.stream()
                            .map(KnowledgeMastery::getMasteryLevel)
                            .filter(ml -> ml != null)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(list.size()), 2, RoundingMode.HALF_UP);
                    
                    // 统计各状态数量
                    long masteredCount = list.stream()
                            .filter(km -> km.getMasteryStatus() != null && km.getMasteryStatus() == 2)
                            .count();
                    long learningCount = list.stream()
                            .filter(km -> km.getMasteryStatus() != null && km.getMasteryStatus() == 1)
                            .count();
                    long notMasteredCount = list.stream()
                            .filter(km -> km.getMasteryStatus() != null && km.getMasteryStatus() == 0)
                            .count();
                    
                    KnowledgePoint kp = kpMap.get(kpId);
                    Map<String, Object> item = new HashMap<>();
                    item.put("knowledgePointId", kpId);
                    item.put("knowledgePointName", kp != null ? kp.getName() : "");
                    item.put("avgMasteryLevel", avgMastery);
                    item.put("masteredCount", masteredCount);
                    item.put("learningCount", learningCount);
                    item.put("notMasteredCount", notMasteredCount);
                    item.put("studentCount", list.size());
                    return item;
                })
                .sorted((a, b) -> ((BigDecimal) a.get("avgMasteryLevel")).compareTo((BigDecimal) b.get("avgMasteryLevel")))
                .collect(Collectors.toList());
        
        return Map.of("code", 200, "msg", "success", "data", masteryStats);
    }

    /**
     * 班级整体数据看板汇总
     */
    @GetMapping("/statistics/class/{classId}/overview")
    public Map<String, Object> classOverview(@PathVariable Long classId) {
        // 1. 获取班级信息
        ClassInfo classInfo = classService.getById(classId);
        if (classInfo == null) {
            return Map.of("code", 404, "msg", "班级不存在");
        }
        
        // 2. 获取班级学生列表
        List<User> students = userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getClassId, classId)
                .eq(User::getRole, "STUDENT"));
        int studentCount = students.size();
        List<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toList());
        
        Map<String, Object> data = new HashMap<>();
        data.put("classId", classId);
        data.put("className", classInfo.getName());
        data.put("studentCount", studentCount);
        
        if (studentCount == 0) {
            data.put("totalWrongQuestions", 0);
            data.put("totalPracticeQuestions", 0);
            data.put("avgMasteryLevel", BigDecimal.ZERO);
            data.put("weakKnowledgeCount", 0);
            data.put("questionTypeStats", List.of());
            data.put("knowledgeMasteryStats", List.of());
            return Map.of("code", 200, "msg", "success", "data", data);
        }
        
        // 3. 错题总数
        long totalWrongQuestions = wrongQuestionService.count(
                new LambdaQueryWrapper<WrongQuestion>()
                        .in(WrongQuestion::getStudentId, studentIds));
        data.put("totalWrongQuestions", totalWrongQuestions);
        
        // 4. 练习记录总数
        data.put("totalPracticeQuestions", 0); // StudyRecord暂不统计
        
        // 5. 平均掌握度
        List<KnowledgeMastery> masteryList = knowledgeMasteryService.list(
                new LambdaQueryWrapper<KnowledgeMastery>()
                        .in(KnowledgeMastery::getStudentId, studentIds));
        
        BigDecimal avgMastery = BigDecimal.ZERO;
        if (!masteryList.isEmpty()) {
            avgMastery = masteryList.stream()
                    .map(KnowledgeMastery::getMasteryLevel)
                    .filter(ml -> ml != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(masteryList.size()), 2, RoundingMode.HALF_UP);
        }
        data.put("avgMasteryLevel", avgMastery);
        
        // 6. 薄弱知识点数量（错误率>50%）
        List<WrongQuestion> wrongQuestions = wrongQuestionService.list(
                new LambdaQueryWrapper<WrongQuestion>()
                        .in(WrongQuestion::getStudentId, studentIds));
        
        List<Long> questionIds = wrongQuestions.stream()
                .map(WrongQuestion::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, Question> questionMap;
        if (!questionIds.isEmpty()) {
            questionMap = questionService.listByIds(questionIds).stream()
                    .collect(Collectors.toMap(Question::getId, q -> q));
        } else {
            questionMap = new HashMap<>();
        }
        
        Map<Long, Set<Long>> kpStudentWrongMap = new HashMap<>();
        for (WrongQuestion wq : wrongQuestions) {
            Question q = questionMap.get(wq.getQuestionId());
            if (q == null || q.getKnowledgePointIds() == null || q.getKnowledgePointIds().isEmpty()) {
                continue;
            }
            String[] kpIdStrs = q.getKnowledgePointIds().split(",");
            for (String kpIdStr : kpIdStrs) {
                try {
                    Long kpId = Long.parseLong(kpIdStr.trim());
                    kpStudentWrongMap.computeIfAbsent(kpId, k -> new HashSet<>()).add(wq.getStudentId());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        
        long weakKnowledgeCount = kpStudentWrongMap.entrySet().stream()
                .filter(entry -> {
                    BigDecimal rate = BigDecimal.valueOf(entry.getValue().size())
                            .divide(BigDecimal.valueOf(studentCount), 2, RoundingMode.HALF_UP);
                    return rate.compareTo(BigDecimal.valueOf(0.5)) > 0;
                })
                .count();
        data.put("weakKnowledgeCount", weakKnowledgeCount);
        
        // 7. 题型错误率统计
        Map<String, Set<Long>> typeStudentWrongMap = new HashMap<>();
        for (WrongQuestion wq : wrongQuestions) {
            Question q = questionMap.get(wq.getQuestionId());
            if (q == null || q.getType() == null) {
                continue;
            }
            typeStudentWrongMap.computeIfAbsent(q.getType(), k -> new HashSet<>()).add(wq.getStudentId());
        }
        
        List<Map<String, Object>> questionTypeStats = typeStudentWrongMap.entrySet().stream()
                .map(entry -> {
                    String type = entry.getKey();
                    int wrongStudentCount = entry.getValue().size();
                    BigDecimal wrongRate = BigDecimal.valueOf(wrongStudentCount)
                            .divide(BigDecimal.valueOf(studentCount), 2, RoundingMode.HALF_UP);
                    Map<String, Object> item = new HashMap<>();
                    item.put("questionType", type);
                    item.put("wrongStudentCount", wrongStudentCount);
                    item.put("wrongRate", wrongRate);
                    return item;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("wrongRate")).compareTo((BigDecimal) a.get("wrongRate")))
                .collect(Collectors.toList());
        data.put("questionTypeStats", questionTypeStats);
        
        // 8. 知识点掌握度TOP10（最弱的）
        List<Long> kpIds = new ArrayList<>(kpStudentWrongMap.keySet());
        Map<Long, KnowledgePoint> kpMap;
        if (!kpIds.isEmpty()) {
            kpMap = knowledgePointService.listByIds(kpIds).stream()
                    .collect(Collectors.toMap(KnowledgePoint::getId, kp -> kp));
        } else {
            kpMap = new HashMap<>();
        }
        
        Map<Long, List<KnowledgeMastery>> kpMasteryMap = masteryList.stream()
                .collect(Collectors.groupingBy(KnowledgeMastery::getKnowledgePointId));
        
        List<Map<String, Object>> knowledgeMasteryStats = kpMasteryMap.entrySet().stream()
                .map(entry -> {
                    Long kpId = entry.getKey();
                    List<KnowledgeMastery> list = entry.getValue();
                    BigDecimal avgMl = list.stream()
                            .map(KnowledgeMastery::getMasteryLevel)
                            .filter(ml -> ml != null)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(list.size()), 2, RoundingMode.HALF_UP);
                    KnowledgePoint kp = kpMap.get(kpId);
                    Map<String, Object> item = new HashMap<>();
                    item.put("knowledgePointId", kpId);
                    item.put("knowledgePointName", kp != null ? kp.getName() : "");
                    item.put("avgMasteryLevel", avgMl);
                    item.put("studentCount", list.size());
                    return item;
                })
                .sorted((a, b) -> ((BigDecimal) a.get("avgMasteryLevel")).compareTo((BigDecimal) b.get("avgMasteryLevel")))
                .limit(10)
                .collect(Collectors.toList());
        data.put("knowledgeMasteryStats", knowledgeMasteryStats);
        
        return Map.of("code", 200, "msg", "success", "data", data);
    }
}
