package org.example.classpai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.classpai.common.Result;
import org.example.classpai.common.exception.BusinessException;
import org.example.classpai.dto.CourseDTO;
import org.example.classpai.dto.CourseGroupDTO;
import org.example.classpai.dto.MemberDTO;
import org.example.classpai.dto.UserAllCoursesDTO;
import org.example.classpai.entity.*;
import org.example.classpai.mapper.*;
import org.example.classpai.service.CourseService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final UserCourseMapper userCourseMapper;

    public CourseServiceImpl(CourseMapper courseMapper,
            UserCourseMapper userCourseMapper) {
        this.courseMapper = courseMapper;
        this.userCourseMapper = userCourseMapper;
    }

    /** 【用户首页】一次性获取用户所有课程数据（置顶课程 + 按学期分组） */
    @Override
    public Result<UserAllCoursesDTO> getAllCourses(User user) {
        // 1. 获取该用户的所有课程关联
        List<UserCourse> allUc = userCourseMapper.selectList(
                new LambdaQueryWrapper<UserCourse>().eq(UserCourse::getUserId, user.getUserId()));

        if (allUc.isEmpty()) {
            UserAllCoursesDTO empty = new UserAllCoursesDTO();
            empty.setPinnedCourses(List.of());
            empty.setGroups(List.of());
            return Result.success(empty);
        }

        // courseId → UserCourse 映射
        Map<Long, UserCourse> ucMap = allUc.stream()
                .collect(Collectors.toMap(UserCourse::getCourseId, uc -> uc, (a, b) -> a));

        // 2. 获取所有关联的课程
        List<Long> courseIds = new ArrayList<>(ucMap.keySet());
        List<Course> courses = courseMapper.selectList(
                new LambdaQueryWrapper<Course>().in(Course::getCourseId, courseIds));

        // 3. 填充瞬态字段
        Map<Long, Course> courseMap = new HashMap<>();
        for (Course c : courses) {
            UserCourse uc = ucMap.get(c.getCourseId());
            c.setUserRole(uc.getRole());
            c.setPinned(Boolean.TRUE.equals(uc.getPinned()));
            c.setSortOrder(uc.getSortOrder());
            courseMap.put(c.getCourseId(), c);
        }

        // 4. 批量填充学生人数（一次 GROUP BY 查询，避免 N+1）
        Map<Long, Integer> studentCountMap = new HashMap<>();
        if (!courseIds.isEmpty()) {
            studentCountMap = userCourseMapper.countStudentsByCourseIds(courseIds).stream()
                    .collect(Collectors.toMap(
                            m -> ((Number) m.get("course_id")).longValue(),
                            m -> ((Number) m.get("total")).intValue()));
        }
        for (Course c : courses) {
            c.setStudentCount(studentCountMap.getOrDefault(c.getCourseId(), 0));
        }

        // 5. 提取置顶课程（按 sortOrder 排序）
        List<Course> pinnedCourses = courses.stream()
                .filter(c -> Boolean.TRUE.equals(c.getPinned()))
                .sorted(Comparator.comparing(Course::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        // 6. 按学年学期分组
        Map<String, List<Course>> groupMap = new LinkedHashMap<>();
        for (Course c : courses) {
            String key = buildSemesterKey(c);
            groupMap.computeIfAbsent(key, k -> new ArrayList<>()).add(c);
        }

        List<CourseGroupDTO> groups = groupMap.entrySet().stream()
                .sorted((a, b) -> b.getKey().compareTo(a.getKey()))
                .map(entry -> {
                    CourseGroupDTO vo = new CourseGroupDTO();
                    vo.setGroupName(entry.getKey());
                    vo.setCourses(entry.getValue());
                    return vo;
                })
                .collect(Collectors.toList());

        UserAllCoursesDTO result = new UserAllCoursesDTO();
        result.setPinnedCourses(pinnedCourses);
        result.setGroups(groups);
        return Result.success(result);
    }

    /** 【教师创建课程】生成6位选课码，创建者自动成为该课程教师 */
    @Override
    @Transactional
    public Result<Course> createCourse(CourseDTO dto, User user) {
        Course course = new Course();
        BeanUtils.copyProperties(dto, course);
        course.setCourseCode(generateCourseCode());
        courseMapper.insert(course);

        // 创建者默认为该课程教师
        UserCourse uc = new UserCourse();
        uc.setUserId(user.getUserId());
        uc.setCourseId(course.getCourseId());
        uc.setRole("teacher");
        uc.setJoinTime(LocalDateTime.now());
        userCourseMapper.insert(uc);

        return Result.success(course);
    }

    /** 【成员管理页】获取课程所有成员（教师+学生），每人带 userId、userName、角色 */
    @Override
    public Result<List<MemberDTO>> getCourseMembers(Long courseId, User user) {
        checkMembership(courseId, user);
        List<MemberDTO> members = userCourseMapper.findMembersByCourseId(courseId);
        return Result.success(members);
    }

    /** 【学生加入课程】通过选课码加入，默认以学生身份 */
    @Override
    @Transactional
    public Result<?> joinCourse(String courseCode, User user) {
        LambdaQueryWrapper<Course> cw = new LambdaQueryWrapper<>();
        cw.eq(Course::getCourseCode, courseCode);
        Course course = courseMapper.selectOne(cw);
        if (course == null) {
            throw new BusinessException(400, "选课码无效");
        }

        // 检查是否已加入
        LambdaQueryWrapper<UserCourse> ucWrapper = new LambdaQueryWrapper<>();
        ucWrapper.eq(UserCourse::getUserId, user.getUserId())
                .eq(UserCourse::getCourseId, course.getCourseId());
        if (userCourseMapper.selectCount(ucWrapper) > 0) {
            throw new BusinessException(400, "已加入该课程");
        }

        // 加入者默认为学生
        UserCourse uc = new UserCourse();
        uc.setUserId(user.getUserId());
        uc.setCourseId(course.getCourseId());
        uc.setRole("student");
        uc.setJoinTime(LocalDateTime.now());
        userCourseMapper.insert(uc);

        return Result.success("加入成功");
    }

    /** 【课程详情页】获取单个课程信息，含当前用户在该课程的角色和学生总人数 */
    @Override
    public Result<Course> getCourseDetail(Long courseId, User user) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        UserCourse uc = checkMembership(courseId, user);
        course.setUserRole(uc.getRole());

        // 填充学生人数
        Long studentCount = userCourseMapper.selectCount(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getCourseId, courseId)
                        .eq(UserCourse::getRole, "student"));
        course.setStudentCount(studentCount.intValue());

        return Result.success(course);
    }

    /** 【课程置顶】切换课程的置顶/取消置顶状态，置顶时自动分配排序号 */
    @Override
    @Transactional
    public Result<?> togglePin(Long courseId, User user) {
        LambdaQueryWrapper<UserCourse> ucWrapper = new LambdaQueryWrapper<>();
        ucWrapper.eq(UserCourse::getUserId, user.getUserId())
                .eq(UserCourse::getCourseId, courseId);
        UserCourse uc = userCourseMapper.selectOne(ucWrapper);
        if (uc == null) {
            throw new BusinessException(404, "未加入该课程");
        }

        boolean newPinned = !Boolean.TRUE.equals(uc.getPinned());
        uc.setPinned(newPinned);
        if (newPinned) {
            // 置顶时放到同用户已有置顶的最大 sortOrder + 1（即排最后）
            Integer maxOrder = userCourseMapper.selectList(
                    new LambdaQueryWrapper<UserCourse>()
                            .select(UserCourse::getSortOrder)
                            .eq(UserCourse::getUserId, user.getUserId())
                            .eq(UserCourse::getPinned, true)
                            .orderByDesc(UserCourse::getSortOrder)
                            .last("LIMIT 1"))
                    .stream().map(UserCourse::getSortOrder).findFirst().orElse(0);
            uc.setSortOrder(maxOrder == null ? 1 : maxOrder + 1);
        } else {
            uc.setSortOrder(null);
        }
        userCourseMapper.updateById(uc);

        return Result.success(newPinned ? "已置顶" : "已取消置顶");
    }

    /** 【课程排序】拖拽调整置顶课程的显示顺序，按传入的 courseId 列表顺序更新 sortOrder */
    @Override
    @Transactional
    public Result<?> updatePinnedOrder(List<Long> courseIds, User user) {
        for (int i = 0; i < courseIds.size(); i++) {
            LambdaQueryWrapper<UserCourse> ucWrapper = new LambdaQueryWrapper<>();
            ucWrapper.eq(UserCourse::getUserId, user.getUserId())
                    .eq(UserCourse::getCourseId, courseIds.get(i));
            UserCourse uc = userCourseMapper.selectOne(ucWrapper);
            if (uc != null) {
                uc.setSortOrder(i + 1);
                userCourseMapper.updateById(uc);
            }
        }
        return Result.success();
    }

    /** 校验用户是否为课程成员，是则返回关联记录，否则抛异常 */
    private UserCourse checkMembership(Long courseId, User user) {
        UserCourse uc = userCourseMapper.selectOne(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getCourseId, courseId)
                        .eq(UserCourse::getUserId, user.getUserId()));
        if (uc == null) {
            throw new BusinessException(403, "非课程成员，无权查看");
        }
        return uc;
    }

    /** 根据课程的 startDate/endDate/semester 构建分组键，如 "2025-2026第一学期" */
    private String buildSemesterKey(Course c) {
        String yearRange = c.getStartDate().getYear() + "-" + c.getEndDate().getYear();
        return yearRange + c.getSemester();
    }

    /** 生成6位不重复选课码 */
    private String generateCourseCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random r = new Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(r.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (courseMapper.selectCount(
                new LambdaQueryWrapper<Course>().eq(Course::getCourseCode, code)) > 0);
        return code;
    }
}
