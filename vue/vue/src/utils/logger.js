/**
 * 前端日志工具
 * 统一管理前端日志输出，便于生产环境控制
 */
class Logger {
  constructor() {
    this.isDevelopment = import.meta.env.DEV;
  }

  /**
   * 信息日志
   * @param {string} message - 日志消息
   * @param {...any} args - 额外参数
   */
  info(message, ...args) {
    if (this.isDevelopment) {
      console.log(`[INFO] ${message}`, ...args);
    }
  }

  /**
   * 警告日志
   * @param {string} message - 日志消息
   * @param {...any} args - 额外参数
   */
  warn(message, ...args) {
    if (this.isDevelopment) {
      console.warn(`[WARN] ${message}`, ...args);
    }
  }

  /**
   * 错误日志
   * @param {string} message - 日志消息
   * @param {...any} args - 额外参数
   */
  error(message, ...args) {
    if (this.isDevelopment) {
      console.error(`[ERROR] ${message}`, ...args);
    }
    // 错误日志在生产环境也可以保留，便于问题排查
    if (!this.isDevelopment) {
      // 可以在这里添加错误上报逻辑
      console.error(`[ERROR] ${message}`, ...args);
    }
  }

  /**
   * 调试日志
   * @param {string} message - 日志消息
   * @param {...any} args - 额外参数
   */
  debug(message, ...args) {
    if (this.isDevelopment) {
      console.log(`[DEBUG] ${message}`, ...args);
    }
  }
}

// 创建全局日志实例
const logger = new Logger();

export default logger;