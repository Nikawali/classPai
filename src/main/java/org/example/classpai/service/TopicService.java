package org.example.classpai.service;

import org.example.classpai.common.Result;
import org.example.classpai.entity.Topic;
import org.example.classpai.entity.User;

public interface TopicService {
    /** 获取课程话题列表 */
    Result<?> listTopics(Long courseId, User user);
    /** 发帖 */
    Result<?> createTopic(Long courseId, String title, String content, User user);
    /** 编辑话题（教师可编辑任意，学生仅编辑自己的） */
    Result<?> updateTopic(Long topicId, String title, String content, User user);
    /** 删除话题（教师可删任意，学生仅删自己的） */
    Result<?> deleteTopic(Long topicId, User user);
    /** 置顶/取消置顶（仅教师） */
    Result<?> togglePin(Long topicId, User user);
    /** 回复话题 */
    Result<?> reply(Long topicId, String content, Boolean isAnonymous, User user);
    /** 获取话题的回复列表 */
    Result<?> listReplies(Long topicId, User user);
    /** 教师切换讨论开关 */
    Result<?> toggleDiscussion(Long courseId, User user);
}
