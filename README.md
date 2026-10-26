# AI 面试 (AI Interview Simulation)

[English](#english) | [中文](#chinese)

---

<a id="english"></a>

### Project Introduction
The **AI Interview Simulation App** is a native Android frontend application designed to provide users with an immersive and interactive AI-driven mock interview experience. By utilizing advanced Speech-to-Text (STT) and Text-to-Speech (TTS) technologies, users can practice their interview skills in a simulated, conversational environment. The app seamlessly integrates with a backend service to track interview history, analyze performance, and provide constructive feedback.

### Project Architecture
The project strictly follows modern Android development guidelines, adopting the **MVVM (Model-View-ViewModel)** architectural pattern.

- **UI Layer**: Built entirely with **Jetpack Compose** for a modern, declarative, and responsive user interface. Page routing is managed by `Navigation Compose`.
- **Presentation Layer**: **ViewModels** manage UI states and handle business logic leveraging **Kotlin Coroutines** and **Flows**.
- **Data Layer**:
  - **Remote**: RESTful API communication is handled by **Retrofit** and **OkHttp**.
  - **Local**: Token management and local configurations are persisted using **Jetpack DataStore (Preferences)**.
- **Speech Module**: Integrates **Sherpa-ONNX** for efficient on-device offline speech recognition (STT) and text-to-speech synthesis (TTS).

### How to Use
1. Clone this repository to your local machine.
2. Open the project in **Android Studio**.
3. Connect a physical Android device or start an Android Emulator (Minimum API Level: 24).
4. Run the `app` configuration to compile and install the application on your device.
5. **Important**: The app requires a connection to the backend server to function completely (login, fetch interview questions, save history, etc.). Please ensure the backend is properly deployed and running.

### Backend Project
The backend service required for this application is hosted in a separate repository. You must run the backend project alongside this frontend app:
👉 **[https://github.com/locherli/ai-interview-simulation-backend](https://github.com/locherli/ai-interview-simulation-backend)**

---

<a id="chinese"></a>

### 项目简介
**AI 面试应用**是一个原生 Android 前端应用程序，旨在为用户提供沉浸式、交互式的 AI 模拟面试体验。通过端侧语音识别（STT）和语音合成（TTS）技术，用户可以在模拟对话的环境中进行真实的面试练习。应用与后端服务无缝对接，实现面试流程控制、历史记录保存以及面试表现反馈分析。

### 项目架构
本项目遵循现代 Android 开发规范，采用了清晰的 **MVVM (Model-View-ViewModel)** 架构模式。

- **UI 层**: 完全采用 **Jetpack Compose** 构建，实现声明式、响应式的现代用户界面。页面路由由 `Navigation Compose` 统一管理。
- **表现层**: 使用 **ViewModel** 结合 **Kotlin 协程 (Coroutines)** 与 **Flow** 来管理视图状态与核心业务逻辑。
- **数据层**:
  - **网络访问**: 使用 **Retrofit** 配合 **OkHttp** 处理所有 RESTful API 请求。
  - **本地存储**: 采用 **Jetpack DataStore (Preferences)** 进行用户 Token 等配置信息的本地持久化。
- **语音引擎**: 集成了 **Sherpa-ONNX**，实现了高效的端侧离线语音识别与语音合成功能。

### 如何使用
1. 将此代码仓库克隆到您的本地计算机。
2. 使用 **Android Studio** 打开该项目。
3. 连接您的 Android 测试设备或启动 Android 模拟器 (要求最低 API Level 为 24)。
4. 点击运行 `app` 配置，将应用程序编译并部署到您的设备上。
5. **注意**：本应用的功能（如登录鉴权、获取题目、提交记录等）强依赖于后端服务，请务必保证后端服务已正常启动并配置好 API 地址。

### 对接的后端项目地址
本应用对应的后端服务源码存放在独立的仓库中。如需完整体验，请获取并运行以下后端项目：
👉 **[https://github.com/locherli/ai-interview-simulation-backend](https://github.com/locherli/ai-interview-simulation-backend)**
