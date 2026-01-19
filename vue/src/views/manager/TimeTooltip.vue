<template>
  <div
      class="time-tooltip"
      :style="{ left: position.x + 'px', top: position.y + 'px' }"
      @mousedown="startDrag"
      @mouseup="stopDrag"
      @mouseleave="stopDrag"
  >
    <div class="time-tooltip-header">兔子时间</div>  <!-- 标题部分 -->
    <div class="time-tooltip-content">
      <span class="centered-time">{{ time }}</span>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';

export default {
  setup() {
    // 时间和拖动位置的状态
    const time = ref('');
    const position = ref({ x: 100, y: 100 });
    let isDragging = ref(false);
    let offset = { x: 0, y: 0 };

    // 更新当前时间
    const updateTime = () => {
      const now = new Date();
      time.value = now.toLocaleTimeString();
    };

    // 每秒更新时间
    onMounted(() => {
      setInterval(updateTime, 1000);
      updateTime();
    });

    // 开始拖动
    const startDrag = (event) => {
      isDragging.value = true;
      offset.x = event.clientX - position.value.x;
      offset.y = event.clientY - position.value.y;
    };

    // 停止拖动
    const stopDrag = () => {
      isDragging.value = false;
    };

    // 更新拖动位置
    const updatePosition = (event) => {
      if (isDragging.value) {
        position.value.x = event.clientX - offset.x;
        position.value.y = event.clientY - offset.y;
      }
    };

    // 使用 requestAnimationFrame 优化性能
    const handleMouseMove = (event) => {
      if (isDragging.value) {
        requestAnimationFrame(() => updatePosition(event));
      }
    };

    // 监听鼠标移动事件
    window.addEventListener('mousemove', handleMouseMove);

    // 清理事件监听
    onMounted(() => {
      return () => {
        window.removeEventListener('mousemove', handleMouseMove);
      };
    });

    return { time, position, startDrag, stopDrag };
  }
};
</script>

<style scoped>

.centered-time {
  text-align: center;  /* 水平居中 */
  font-family: 'Helvetica', 'Arial', sans-serif;  /* 字体样式 */
  font-size: 15px;  /* 字体大小 */
  font-weight: bold;  /* 字体加粗 */
  padding-top: 5px;
  color: #333;  /* 字体颜色 */
}

.time-tooltip {
  position: fixed;  /* 固定在视口 */
  top: 100px;       /* 设置初始位置 */
  left: 100px;
  cursor: move;
  background-color: pink;  /* 设置粉色背景 */
  width: 80px;
  height: 80px;
  border-radius: 50%;  /* 圆形的主体部分 */
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  font-family: Arial, sans-serif;
  font-size: 14px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);  /* 添加阴影，增加立体感 */
}

.time-tooltip::before, .time-tooltip::after {
  content: '';
  position: absolute;
  background-color: pink;
  border-radius: 50%;
}

.time-tooltip::before {
  width: 20px;
  height: 40px;
  top: -15px;
  left: 10px;
  transform: rotate(-45deg);  /* 兔耳朵左 */
}

.time-tooltip::after {
  width: 20px;
  height: 40px;
  top: -15px;
  right: 10px;
  transform: rotate(45deg);  /* 兔耳朵右 */
}

.time-tooltip-content {
  display: flex;
  align-items: center;
  justify-content: center;
}

.time-tooltip span {
  margin: 0;
}
.time-tooltip {
  position: fixed;
  top: 100px;
  left: 100px;
  cursor: move;
  background-color: pink;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  font-family: Arial, sans-serif;
  font-size: 14px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.time-tooltip::before, .time-tooltip::after {
  content: '';
  position: absolute;
  background-color: pink;
  border-radius: 50%;
}

.time-tooltip::before {
  width: 20px;
  height: 40px;
  top: -15px;
  left: 10px;
  transform: rotate(-45deg);
}

.time-tooltip::after {
  width: 20px;
  height: 40px;
  top: -15px;
  right: 10px;
  transform: rotate(45deg);
}

.time-tooltip-content {
  display: flex;
  align-items: center;
  justify-content: center;
}

.time-tooltip span {
  margin: 0;
  user-select: none;  /* 禁用文本选择 */
}
.time-tooltip-header {
  position: absolute;
  top: 10px;  /* 将标题置于兔子上方 */
  font-size: 10px;
  font-weight: bold;
  color: #ff66b2;  /* 可自定义标题颜色 */
  text-align: center;
  z-index: 5;
}

</style>
