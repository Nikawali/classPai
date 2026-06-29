package org.example.classpai.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.classpai.common.Result;
import org.example.classpai.entity.Message;
import org.example.classpai.entity.User;
import org.example.classpai.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /** 获取当前用户私信列表 */
    @GetMapping
    public Result<List<Message>> list(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return messageService.getMessages(user);
    }

    /** 获取未读消息数量 */
    @GetMapping("/unread-count")
    public Result<Integer> unreadCount(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return messageService.getUnreadCount(user);
    }

    /** 标记所有消息为已读 */
    @PostMapping("/read")
    public Result<?> markRead(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return messageService.markAsRead(user);
    }
}
