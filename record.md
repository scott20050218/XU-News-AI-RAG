lsof -i :3000

mvn test -Dtest=ClassName

git add .

export MAIL_USERNAME=ruili20250518@gmail.com
export MAIL_PASSWORD=
export MAIL_FROM=noreply@ha72.com
export OPENAI_API_KEY=

难点
1，中文提示词还是英文提示词？我们肯定是用中文与 AI 交付比较顺畅，如果用英文，很多我们的想法不能完全表达出来，AI 返回的英文，我们可能不能完全了解。这个要如何平衡呢？
解决：把英语学好，先使用中文，后面过度到英文

2，AI 对提示词的了解和了解程度需要对齐，需要我们去检查，如何避免，如何预防呢？比如我的提示词为：请在 langchain 工程中帮我增加 AI 智能知识检索功能，AI 第 1 版给我写了一个最简单比较的，第 2 版我提示他用 langchain，他才用 langchain 写了一版
解决：熟悉业务，熟悉技术框架，明确提示词

3，AI 写的代码，不是自己写的，不好得调试，修改
解决：使用自己的框架，让 AI 提供功能，验证后，把新增功能放到自己的框架中

4.Cursor 会钻牛角尖，比如 163 发邮件，服务器端不允许国外 ip 发邮件，Cursor 改半天；使用 hotmail 发邮件，cursor 一直测试 456 超时端口；后来使用 gmail，马上就测试成功了
解决：熟悉业务

5.Cursor 的 token 使用是有限的（虽然 20 美元一个月）

6.依赖 AI 编程，结构很乱，AI 一下增加一个增强性功能，一下增加一个功能，代码结构混乱，不好维护
解决：使用自己的框架，让 AI 提供功能，验证后，把新增功能放到自己的框架中
