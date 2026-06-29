package org.example.classpai.service;

import org.example.classpai.common.Result;
import org.example.classpai.entity.Message;
import org.example.classpai.entity.User;

import java.util.List;

public interface MessageService {
    /** 催交单个学生 */
    Result<?> urge(Long hwId, Long studentId, User teacher);

    /** 一键催交所有未提交学生 */
    Result<?> urgeAll(Long hwId, User teacher);

    /** 获取当前用户的私信列表 */
    Result<List<Message>> getMessages(User user);

    /** 获取未读消息数量 */
    Result<Integer> getUnreadCount(User user);

    /** 标记所有消息为已读 */
    Result<?> markAsRead(User user);
}
