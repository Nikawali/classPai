package org.example.classpai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.classpai.common.Result;
import org.example.classpai.common.exception.BusinessException;
import org.example.classpai.entity.*;
import org.example.classpai.mapper.*;
import org.example.classpai.service.TopicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TopicServiceImpl extends BaseCourseServiceImpl implements TopicService {

    private final TopicMapper topicMapper;
    private final ReplyMapper replyMapper;

    public TopicServiceImpl(TopicMapper topicMapper, ReplyMapper replyMapper) {
        this.topicMapper = topicMapper;
        this.replyMapper = replyMapper;
    }

    @Override
    public Result<?> listTopics(Long courseId, User user) {
        checkMembership(courseId, user.getUserId());
        List<Topic> topics = topicMapper.findTopicsByCourseId(courseId);
        return Result.success(topics);
    }

    @Override
    @Transactional
    public Result<?> createTopic(Long courseId, String title, String content, User user) {
        UserCourse uc = checkMembership(courseId, user.getUserId());
        // 学生发帖需检查是否被禁止讨论
        if (!"teacher".equals(uc.getRole())) {
            Course course = courseMapper.selectById(courseId);
            if (course != null && Boolean.TRUE.equals(course.getDiscussionLocked())) {
                throw new BusinessException(403, "教师已禁止讨论");
            }
        }
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException(400, "标题不能为空");
        }
        Topic topic = new Topic();
        topic.setCourseId(courseId);
        topic.setUserId(user.getUserId());
        topic.setTitle(title.trim());
        topic.setContent(content);
        topic.setIsPinned(false);
        topicMapper.insert(topic);
        return Result.success(topic);
    }

    @Override
    @Transactional
    public Result<?> updateTopic(Long topicId, String title, String content, User user) {
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) throw new BusinessException(404, "话题不存在");
        if (!topic.getUserId().equals(user.getUserId()) && !isTeacher(topic.getCourseId(), user.getUserId())) {
            throw new BusinessException(403, "无权编辑此话题");
        }
        if (title != null && !title.trim().isEmpty()) {
            topic.setTitle(title.trim());
        }
        topic.setContent(content);
        topicMapper.updateById(topic);
        return Result.success("编辑成功");
    }

    @Override
    @Transactional
    public Result<?> deleteTopic(Long topicId, User user) {
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) throw new BusinessException(404, "话题不存在");
        if (!topic.getUserId().equals(user.getUserId()) && !isTeacher(topic.getCourseId(), user.getUserId())) {
            throw new BusinessException(403, "无权删除此话题");
        }
        // 级联删除回复
        replyMapper.delete(new LambdaQueryWrapper<Reply>().eq(Reply::getTopicId, topicId));
        topicMapper.deleteById(topicId);
        return Result.success("删除成功");
    }

    @Override
    @Transactional
    public Result<?> togglePin(Long topicId, User user) {
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) throw new BusinessException(404, "话题不存在");
        if (!isTeacher(topic.getCourseId(), user.getUserId())) {
            throw new BusinessException(403, "仅教师可置顶话题");
        }
        topic.setIsPinned(!Boolean.TRUE.equals(topic.getIsPinned()));
        topicMapper.updateById(topic);
        return Result.success(Boolean.TRUE.equals(topic.getIsPinned()) ? "已置顶" : "已取消置顶");
    }

    @Override
    @Transactional
    public Result<?> reply(Long topicId, String content, Boolean isAnonymous, User user) {
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) throw new BusinessException(404, "话题不存在");
        UserCourse uc = checkMembership(topic.getCourseId(), user.getUserId());
        if (!"teacher".equals(uc.getRole())) {
            Course course = courseMapper.selectById(topic.getCourseId());
            if (course != null && Boolean.TRUE.equals(course.getDiscussionLocked())) {
                throw new BusinessException(403, "教师已禁止讨论");
            }
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(400, "回复内容不能为空");
        }
        Reply reply = new Reply();
        reply.setTopicId(topicId);
        reply.setContent(content.trim());
        reply.setIsAnonymous(isAnonymous != null && isAnonymous);
        reply.setUserId(isAnonymous != null && isAnonymous ? null : user.getUserId());
        replyMapper.insert(reply);
        return Result.success(reply);
    }

    @Override
    public Result<?> listReplies(Long topicId, User user) {
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) throw new BusinessException(404, "话题不存在");
        checkMembership(topic.getCourseId(), user.getUserId());
        List<Reply> replies = replyMapper.findRepliesByTopicId(topicId);
        // 匿名回复隐藏 userId 和 userName
        for (Reply r : replies) {
            if (Boolean.TRUE.equals(r.getIsAnonymous())) {
                r.setUserId(null);
                r.setUserName("匿名");
            }
        }
        return Result.success(replies);
    }

    @Override
    @Transactional
    public Result<?> toggleDiscussion(Long courseId, User user) {
        if (!isTeacher(courseId, user.getUserId())) {
            throw new BusinessException(403, "仅教师可操作");
        }
        Course course = courseMapper.selectById(courseId);
        if (course == null) throw new BusinessException(404, "课程不存在");
        course.setDiscussionLocked(!Boolean.TRUE.equals(course.getDiscussionLocked()));
        courseMapper.updateById(course);
        return Result.success(Boolean.TRUE.equals(course.getDiscussionLocked()) ? "已禁止讨论" : "已开放讨论");
    }
}
