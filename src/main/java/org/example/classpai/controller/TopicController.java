package org.example.classpai.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.classpai.common.Result;
import org.example.classpai.entity.User;
import org.example.classpai.service.TopicService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topic")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    /** 课程话题列表 */
    @GetMapping("/course/{courseId}")
    public Result<?> list(@PathVariable Long courseId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return topicService.listTopics(courseId, user);
    }

    /** 发帖 */
    @PostMapping("/course/{courseId}")
    public Result<?> create(@PathVariable Long courseId,
            @RequestParam String title,
            @RequestParam(required = false) String content,
            HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return topicService.createTopic(courseId, title, content, user);
    }

    /** 编辑话题 */
    @PutMapping("/{topicId}")
    public Result<?> update(@PathVariable Long topicId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content,
            HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return topicService.updateTopic(topicId, title, content, user);
    }

    /** 删除话题 */
    @DeleteMapping("/{topicId}")
    public Result<?> delete(@PathVariable Long topicId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return topicService.deleteTopic(topicId, user);
    }

    /** 置顶/取消置顶 */
    @PutMapping("/{topicId}/pin")
    public Result<?> togglePin(@PathVariable Long topicId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return topicService.togglePin(topicId, user);
    }

    /** 回复话题 */
    @PostMapping("/{topicId}/reply")
    public Result<?> reply(@PathVariable Long topicId,
            @RequestParam String content,
            @RequestParam(defaultValue = "false") Boolean isAnonymous,
            HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return topicService.reply(topicId, content, isAnonymous, user);
    }

    /** 话题回复列表 */
    @GetMapping("/{topicId}/replies")
    public Result<?> replies(@PathVariable Long topicId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return topicService.listReplies(topicId, user);
    }

    /** 教师切换讨论开关 */
    @PutMapping("/course/{courseId}/toggle")
    public Result<?> toggleDiscussion(@PathVariable Long courseId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return topicService.toggleDiscussion(courseId, user);
    }
}
