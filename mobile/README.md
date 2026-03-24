# AIWPS Mobile

> AIWPS 移动端 Web 应用 - 教师端 & 学生端

## 项目介绍

AIWPS 智能辅导移动端 Web 应用，采用 Vue3 + Vite + Vant4 技术栈开发，提供科技感 UI 的教师端和学生端功能。

## 功能特性

### 教师端
- 登录页（支持手机号/账号密码）
- 首页：机构 → 班级 → 学生 层级导航，数据看板
- 学生错题查看页：按班级/按学生查看错题列表
- 扫描二维码：调用摄像头扫描试卷二维码
- 错题评价：为学生的试卷标注错题原因 + 老师评语
- 个人中心

### 学生端
- 登录页
- 首页：今日任务、错题提醒
- 错题本：按学科查看错题列表
- 知识点解析页
- 个人中心

## 技术栈

- Vue 3 + Vite
- Vant 4 (移动端 UI 组件库)
- Pinia (状态管理)
- Vue Router 4
- Axios (HTTP 请求)
- QRCode (二维码生成)
- Sass (CSS 预处理器)

## 开发

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview
```

## 项目结构

```
src/
├── api/              # API 请求封装
├── assets/           # 静态资源
├── components/       # 公共组件
├── router/           # 路由配置
├── stores/           # Pinia 状态管理
└── views/            # 页面组件
    ├── teacher/      # 教师端页面
    └── student/      # 学生端页面
```

## License

MIT
