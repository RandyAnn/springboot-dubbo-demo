# 贡献指南

感谢您对Spring Boot Dubbo饮食记录系统项目的关注！我们欢迎任何形式的贡献。

## 🤝 如何贡献

### 报告问题

如果您发现了bug或有功能建议，请：

1. 检查[现有Issues](https://github.com/your-username/spring-boot-dubbo-demo/issues)确保问题未被报告
2. 创建新的Issue，包含：
   - 清晰的标题和描述
   - 重现步骤（如果是bug）
   - 期望的行为
   - 实际的行为
   - 环境信息（操作系统、Java版本等）

### 提交代码

1. **Fork项目**
   ```bash
   git clone https://github.com/your-username/spring-boot-dubbo-demo.git
   cd spring-boot-dubbo-demo
   ```

2. **创建分支**
   ```bash
   git checkout -b feature/your-feature-name
   # 或
   git checkout -b bugfix/your-bugfix-name
   ```

3. **开发和测试**
   - 遵循现有的代码风格
   - 添加必要的测试
   - 确保所有测试通过
   ```bash
   mvn clean test
   ```

4. **提交更改**
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   # 或
   git commit -m "fix: fix your bug description"
   ```

5. **推送分支**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **创建Pull Request**
   - 提供清晰的PR标题和描述
   - 引用相关的Issue
   - 等待代码审查

## 📝 代码规范

### Java代码规范

- 遵循[阿里巴巴Java开发手册](https://github.com/alibaba/p3c)
- 使用4个空格缩进
- 类名使用PascalCase
- 方法名和变量名使用camelCase
- 常量使用UPPER_SNAKE_CASE

### 前端代码规范

- 遵循Vue.js官方风格指南
- 使用2个空格缩进
- 组件名使用PascalCase
- 方法名使用camelCase

### 提交信息规范

使用[Conventional Commits](https://www.conventionalcommits.org/)格式：

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

类型包括：
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动

示例：
```
feat(user): add user profile management
fix(auth): resolve JWT token expiration issue
docs: update API documentation
```

## 🧪 测试

### 运行测试

```bash
# 运行所有测试
mvn clean test

# 运行特定模块测试
cd user-service && mvn test

# 运行集成测试
mvn clean verify
```

### 编写测试

- 为新功能编写单元测试
- 为API编写集成测试
- 保持测试覆盖率在80%以上

## 📋 开发环境设置

### 必需工具

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.x+
- Zookeeper 3.7+
- Node.js 16+ (前端开发)

### IDE配置

推荐使用IntelliJ IDEA，并安装以下插件：
- Lombok Plugin
- MyBatis Plugin
- Vue.js Plugin

### 代码格式化

项目根目录包含IDE配置文件，导入后可自动格式化代码。

## 🔍 代码审查

所有PR都需要经过代码审查：

1. 至少一名维护者的批准
2. 所有CI检查通过
3. 没有合并冲突

### 审查要点

- 代码质量和可读性
- 测试覆盖率
- 性能影响
- 安全性考虑
- 文档完整性

## 📞 联系我们

如有疑问，可以通过以下方式联系：

- 创建Issue讨论
- 发送邮件至：[your-email@example.com]
- 加入我们的讨论群

## 🙏 致谢

感谢所有贡献者的努力！您的贡献让这个项目变得更好。

---

再次感谢您的贡献！🎉
