package org.example.classpai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.classpai.common.Result;
import org.example.classpai.common.exception.BusinessException;
import org.example.classpai.entity.*;
import org.example.classpai.mapper.HomeworkMapper;
import org.example.classpai.mapper.MessageMapper;
import org.example.classpai.service.MessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageServiceImpl extends BaseCourseServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final HomeworkMapper homeworkMapper;

    public MessageServiceImpl(MessageMapper messageMapper,
                              HomeworkMapper homeworkMapper) {
        this.messageMapper = messageMapper;
        this.homeworkMapper = homeworkMapper;
    }

    @Override
    @Transactional
    public Result<?> urge(Long hwId, Long studentId, User teacher) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null) {
            throw new BusinessException(404, "作业不存在");
        }
        checkTeacher(hw.getCourseId(), teacher.getUserId());
        Course course = courseMapper.selectById(hw.getCourseId());
        Message msg = buildUrgeMessage(hw, course, studentId, teacher);
        messageMapper.insert(msg);
        return Result.success("催交已发送");
    }

    @Override
    @Transactional
    public Result<?> urgeAll(Long hwId, User teacher) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null) {
            throw new BusinessException(404, "作业不存在");
        }
        checkTeacher(hw.getCourseId(), teacher.getUserId());
        List<Long> unsubmittedIds = messageMapper.findUnsubmittedStudentIds(hwId);
        if (unsubmittedIds.isEmpty()) {
            return Result.success("所有学生均已提交");
        }
        Course course = courseMapper.selectById(hw.getCourseId());
        for (Long studentId : unsubmittedIds) {
            Message msg = buildUrgeMessage(hw, course, studentId, teacher);
            messageMapper.insert(msg);
        }
        return Result.success("已向" + unsubmittedIds.size() + "位学生发送催交通知");
    }

    private Message buildUrgeMessage(Homework hw, Course course, Long studentId, User teacher) {
        String courseName = course != null ? course.getCourseName() : "未知课程";
        String content = "你" + courseName + "的" + hw.getTitle() + "即将结束，请尽快提交";
        Message msg = new Message();
        msg.setSenderId(teacher.getUserId());
        msg.setReceiverId(studentId);
        msg.setCourseId(hw.getCourseId());
        msg.setContent(content);
        msg.setIsRead(false);
        return msg;
    }

    @Override
    @Transactional
    public Result<?> notifyNewHomework(Long hwId, User teacher) {
        Homework hw = homeworkMapper.selectById(hwId);
        if (hw == null) {
            throw new BusinessException(404, "作业不存在");
        }
        checkTeacher(hw.getCourseId(), teacher.getUserId());
        Course course = courseMapper.selectById(hw.getCourseId());
        List<UserCourse> students = userCourseMapper.selectList(
                new LambdaQueryWrapper<UserCourse>()
                        .eq(UserCourse::getCourseId, hw.getCourseId())
                        .eq(UserCourse::getRole, "student"));
        if (students.isEmpty()) {
            return Result.success("课程暂无学生");
        }
        String courseName = course != null ? course.getCourseName() : "未知课程";
        for (UserCourse uc : students) {
            Message msg = new Message();
            msg.setSenderId(teacher.getUserId());
            msg.setReceiverId(uc.getUserId());
            msg.setCourseId(hw.getCourseId());
            msg.setContent(courseName + "发布了新作业：" + hw.getTitle() + "，请及时完成并提交");
            msg.setIsRead(false);
            messageMapper.insert(msg);
        }
        return Result.success("已通知" + students.size() + "位学生");
    }

    @Override
    public Result<List<Message>> getMessages(User user) {
        List<Message> messages = messageMapper.findMessagesByUserId(user.getUserId());
        return Result.success(messages);
    }

    @Override
    public Result<Integer> getUnreadCount(User user) {
        long count = messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getReceiverId, user.getUserId())
                        .eq(Message::getIsRead, false));
        return Result.success((int) count);
    }

    @Override
    @Transactional
    public Result<?> markAsRead(User user) {
        Message update = new Message();
        update.setIsRead(true);
        messageMapper.update(update,
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getReceiverId, user.getUserId())
                        .eq(Message::getIsRead, false));
        return Result.success("ok");
    }
}
