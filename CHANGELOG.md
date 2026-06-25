# ClassPai 项目会话变更汇总

> 分支: czl  
> 日期: 2026-06-23 ~ 2026-06-24  
> 范围: 个人中心信息展示 + 用户信息修改模块

---

## 一、新增文件

| 序号 | 文件路径 | 类型 | 功能说明 | 新增原因 |
|------|----------|------|----------|----------|
| 1 | `src/main/java/org/example/classpai/dto/UpdateProfileDTO.java` | DTO | 用户信息修改请求体，仅含 phone、password、college、major、smsCode 五个字段，实现字段白名单管控 | 注册模块已有 `RegisterDTO`，包含 `userId`、`userName`、`role` 等注册专属字段，无法复用于修改场景。新建 DTO 将请求体限定为四个可修改字段 + 验证码，实现字段白名单拦截，非法字段无法传入 |

---

## 二、修改文件

| 序号 | 文件路径 | 类型 | 修改内容 | 修改原因 |
|------|----------|------|----------|----------|
| 1 | `pom.xml` | 配置 | 新增 `maven-compiler-plugin` + `annotationProcessorPaths` 指向 Lombok | 前期从 main 分支拉取代码时该配置丢失。Lombok 依赖仍在，但 Maven 命令行编译需显式声明注解处理器路径，否则所有 `@Data` 注解的类无法生成 getter/setter，全项目报"找不到符号" |
| 2 | `src/main/java/org/example/classpai/auth/dto/LoginResponse.java` | DTO | 新增 `phone`、`createTime`、`school`、`college`、`major` 字段；添加 `@NoArgsConstructor` 支持无参构造 | `/auth/me` 接口需返回完整用户信息给个人页展示，原有四个字段（token, userId, userName, role）不足。扩展同名 DTO 复用，避免新建文件造成冗余。`@AllArgsConstructor` 因新增字段数量变化导致 `login()` 调用失败，补充 `@NoArgsConstructor` 后改用 setter 构建 |
| 3 | `src/main/java/org/example/classpai/auth/service/AuthService.java` | 接口 | 新增 `LoginResponse getProfile(String token)` 方法声明 | 接口契约扩展，与 `login()` 并列，统一由 `AuthController` 聚合调用 |
| 4 | `src/main/java/org/example/classpai/auth/service/impl/AuthServiceImpl.java` | 业务 | ① 实现 `getProfile()`：Token 校验 → 查库 → 组装完整用户信息返回；② 修复 `login()` 方法改为 setter 构建 LoginResponse | 前端 `Profile.vue` 的 `onMounted` 发起 `GET /auth/me` 请求，此前无对应接口实现，个人页无法展示用户信息；`login()` 因 DTO 字段变更导致全参构造编译失败，改用 setter 逐字段赋值更稳健 |
| 5 | `src/main/java/org/example/classpai/auth/controller/AuthController.java` | 控制器 | 新增 `GET /auth/me` 端点 + `extractToken()` 辅助方法 | 前端个人页需要获取当前登录用户的完整信息（含学校、学院、专业等扩展字段），需提供独立的认证态查询端点；`extractToken()` 从 `Authorization` 头提取 Bearer Token，复用拦截器已有的 Token 解析逻辑 |
| 6 | `src/main/java/org/example/classpai/service/UserService.java` | 接口 | 新增 `sendCodeForProfile(phone, userId)` 和 `updateProfile(dto, userId)` 方法声明 | 个人信息修改流程需两条独立链路：① 发送修改专用验证码（与注册验证码隔离，Redis Key 前缀不同）；② 执行修改操作（需校验验证码 + 字段白名单），接口层定义契约供 Controller 调用 |
| 7 | `src/main/java/org/example/classpai/service/impl/UserServiceImpl.java` | 业务 | ① 实现 `sendCodeForProfile()`：验证码生成/复用/Redis 存储（前缀 `sms:profile:`）；② 实现 `updateProfile()`：验证码校验 → 逐字段白名单校验 → BCrypt 加密 → 保存 | 个人中心需独立的验证码发送和修改逻辑，与注册模块逻辑一致（6位随机码、5分钟过期）但 Redis Key 前缀隔离避免冲突。修改操作需逐字段校验：手机号格式+唯一性、密码≥6位、学院/专业≤50字符，通过白名单机制防止非法字段入库 |
| 8 | `src/main/java/org/example/classpai/controller/UserController.java` | 控制器 | 新增 `GET /api/user/send-code-profile` 和 `PUT /api/user/profile` 端点 | 将个人信息修改功能暴露为 REST 接口，均需通过 LoginInterceptor 认证（从 `currentUser` 获取当前用户 ID），保证接口安全性 |
| 9 | `classPai-Web/src/api/request.js` | 前端 API | 新增 `sendCodeForProfile(token, phone)` 和 `updateProfile(token, body)` 两个 API 封装 | 前端个人中心页面需要调用后端修改验证码和修改个人信息接口，需在 API 层统一封装带 Token 的请求，与已有 `sendCode`/`register` 方法保持一致的调用风格 |
| 10 | `classPai-Web/src/views/Profile.vue` | 前端页面 | ① `onMounted` 从 API 真实数据填充 school/college/major；② `sendSmsForEdit` 替换模拟 alert 为真实 `api.sendCodeForProfile()` 调用；③ `handleEditSubmit` 替换模拟 `setTimeout` 为真实 `api.updateProfile()` 调用；④ `handleAuthSubmit` 替换模拟代码为真实 API 调用；⑤ 密码校验规则统一为"≥6位" | 原有 Profile.vue 使用模拟数据（空字符串、alert 弹窗、setTimeout 假提交），无法真正修改用户信息。需打通前后端数据链路，使个人中心具备真实的查看和编辑能力。密码校验从"≥8位+字母数字"对齐注册模块的"≥6位"规则，避免两处校验不一致导致用户困惑 |
| 11 | `classPai-Web/src/views/Login.vue` | 前端页面 | 移除登录成功后的 alert 弹窗，登录成功后直接无打扰跳转主界面 | 登录成功弹窗打断用户操作流程，体验较差。改为无声跳转，提升交互流畅度 |

---

## 三、数据流变更总览

### 个人页信息展示

```
GET /auth/me  →  AuthServiceImpl.getProfile()  →  MySQL
返回: userId, userName, role, phone, createTime, school, college, major
```

### 个人信息修改

```
① GET /api/user/send-code-profile  (发送验证码，Redis Key: sms:profile:{phone})
② PUT /api/user/profile            (修改，需验证码 + 字段白名单校验)

均通过 LoginInterceptor，从 request.getAttribute("currentUser") 获取当前用户。

可修改字段: phone, password, college, major
校验规则:
  - 手机号格式 / 唯一性
  - 密码 ≥ 6 位
  - 学院 ≤ 50 字符
  - 专业 ≤ 50 字符
```
