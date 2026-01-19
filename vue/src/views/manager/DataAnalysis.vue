<template>
  <div>
    <el-row :gutter="10">
      <el-col :span="6">
        <div class="card" style="padding: 20px; display: flex; align-items: center">
          <div style="flex: 1; text-align: center">
            <img src="@/assets/imgs/1.jpeg" alt="" style="width: 70px;  border-radius: 50%;transition: transform 0.3s ease-in-out; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);">
          </div>
          <div style="flex: 1; font-size: 20px;">
            <div style="margin-bottom: 10px">体检收入</div>
            <div style="font-weight: bold">￥{{ data.countData.physicalExaminationMoney }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="card" style="padding: 20px; display: flex; align-items: center">
          <div style="flex: 1; text-align: center">
            <img src="@/assets/imgs/2.jpeg" alt="" style="width: 70px;  border-radius: 50%;transition: transform 0.3s ease-in-out; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);">
          </div>
          <div style="flex: 1; font-size: 20px;">
            <div style="margin-bottom: 10px">套餐体检收入</div>
            <div style="font-weight: bold">￥{{ data.countData.examinationPackageMoney }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="card" style="padding: 20px; display: flex; align-items: center">
          <div style="flex: 1; text-align: center; ">
            <img src="@/assets/imgs/3.jpeg" alt="" style="width: 70px;  border-radius: 50%;transition: transform 0.3s ease-in-out; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);">
          </div>
          <div style="flex: 1; font-size: 20px;">
            <div style="margin-bottom: 10px">体检项目</div>
            <div style="font-weight: bold">{{ data.countData.physicalExaminationCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="card" style="padding: 20px; display: flex; align-items: center">
          <div style="flex: 1; text-align: center">
            <img src="@/assets/imgs/4.jpeg" alt="" style="width: 70px;  border-radius: 50%;transition: transform 0.3s ease-in-out; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);">
          </div>
          <div style="flex: 1; font-size: 20px;">
            <div style="margin-bottom: 10px">套餐体检项目</div>
            <div style="font-weight: bold">{{ data.countData.examinationPackageCount }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="card" style="margin: 10px 0; padding: 20px">
      <div id="line" style="height: 400px"></div>
    </div>

    <div style="margin: 10px 0; background-color: #2c334c">
      <el-row :gutter="10">
        <el-col :span="12"  >
          <div style="height: 400px; padding: 20px" class="card" id="pie"></div>
        </el-col>
        <el-col :span="12" >
          <div style="height: 400px; padding: 20px" class="card" id="bar"></div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import request from "@/utils/request.js";
import * as echarts from 'echarts'
const lineOption = {
  title: {
    text: '30天内体检销售趋势图',
    left: 'center',
    textStyle: {
      color: '#333', // 设置标题颜色
      fontSize: 18,  // 设置标题字体大小
      fontWeight: 'bold' // 设置标题字体粗细
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow' // 设置鼠标悬停时的指示器类型
    },
    backgroundColor: 'rgba(50,50,50,0.7)', // 设置提示框的背景色
    textStyle: {
      color: '#fff' // 设置提示框文字颜色
    }
  },
  legend: {
    left: 'left',
    textStyle: {
      color: '#444', // 设置图例文本颜色
      fontSize: 14 // 设置图例字体大小
    }
  },
  grid: {
    left: '10%',
    right: '10%',
    bottom: '10%',
    top: '20%',
    containLabel: true // 防止图表标题或坐标轴标签被遮挡
  },
  xAxis: {
    type: 'category',
    data: [],
    axisLabel: {
      color: '#666', // 设置X轴标签的颜色
      fontSize: 12,  // 设置X轴标签的字体大小
    },
    axisLine: {
      lineStyle: {
        color: '#ccc' // 设置X轴线条颜色
      }
    },
    axisTick: {
      show: false // 隐藏X轴刻度线
    }
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      color: '#666', // 设置Y轴标签的颜色
      fontSize: 12 // 设置Y轴标签字体大小
    },
    axisLine: {
      lineStyle: {
        color: '#ccc' // 设置Y轴线条颜色
      }
    },
    axisTick: {
      show: false // 隐藏Y轴刻度线
    }
  },
  series: [
    {
      name: '体检销售',
      data: [],
      type: 'line',
      smooth: true,
      lineStyle: {
        color: '#409EFF', // 设置线条颜色
        width: 3, // 设置线条宽度
        type: 'solid' // 设置线条类型（可选：solid, dashed, dotted）
      },
      itemStyle: {
        color: '#409EFF' // 设置数据点的颜色
      },
      label: {
        show: true, // 显示数据标签
        position: 'top', // 设置数据标签的位置
        color: '#409EFF', // 设置数据标签颜色
        fontSize: 12 // 设置数据标签的字体大小
      },
      areaStyle: {
        color: 'rgba(64,158,255,0.2)' // 设置折线图区域填充颜色
      }
    }
  ]
};

const pieOption = {
  title: {
    text: '医生职称分布图表',
    left: 'center',
    top: '5%',
    textStyle: {
      color: '#333',
      fontSize: 24,
      fontWeight: 'bold',
      fontFamily: 'Arial, sans-serif',
    },
    subtextStyle: {
      color: '#777',
      fontSize: 16,
      fontFamily: 'Arial, sans-serif',
    }
  },
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
    textStyle: {
      color: '#fff'
    },
    formatter: '{b}: {c} 个 ({d}%)',
    extraCssText: 'box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);'
  },
  legend: {
    orient: 'vertical',
    left: 'right',  // 将图例放到右侧，避免和饼图重叠
    top: 'middle',
    textStyle: {
      color: '#555',
      fontSize: 15,
      fontFamily: 'Arial, sans-serif',
    },
    itemHeight: 14,
    itemWidth: 10,
    icon: 'circle',
    padding: [20, 20]  // 增加图例的间距
  },
  series: [
    {
      type: 'pie',
      radius: ['40%', '75%'],  // 调整内外半径，避免遮挡
      center: ['50%', '55%'],  // 更细致地调整饼图的位置，避免遮挡
      data: [
        { value: 335, name: '主任医生' },
        { value: 310, name: '副主任医生' },
        { value: 234, name: '主治医生' },
        { value: 135, name: '住院医生' },
        { value: 1548, name: '实习医生' }
      ],
      itemStyle: {
        normal: {
          borderColor: '#fff',
          borderWidth: 2,
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowOffsetY: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)',  // 为每个扇区添加阴影
          opacity: 0.8  // 设置透明度，增加层次感
        },
        emphasis: {
          shadowBlur: 20,  // 增强选中时的阴影效果
          shadowColor: 'rgba(0, 0, 0, 0.8)',
          color: '#FF6347',  // 选中时改变颜色
        }
      },
      label: {
        show: true,
        position: 'outside',  // 标签位于外部，避免遮挡
        formatter: '{b}: {c}个 ({d}%)',
        fontSize: 16,
        fontWeight: 'bold',
        color: '#333',
        backgroundColor: '#fff',
        borderRadius: 5,
        padding: [5, 10],
        shadowColor: 'rgba(0, 0, 0, 0.3)',
        shadowBlur: 5
      },
      labelLine: {
        show: true,
        length: 20,
        lineStyle: {
          width: 3,
          type: 'solid',
          color: '#bbb'
        }
      },
      animationType: 'expand',  // 使用展开动画
      animationEasing: 'elasticOut',  // 缓动效果
      animationDuration: 1500,  // 动画时长
      animationDelay: 500,
      startAngle: 90,  // 让饼图从顶部开始
      itemStyle: {
        normal: {
          color: function(params) {
            // 使用渐变色来增强效果
            const colorList = [
              new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                { offset: 0, color: '#ff7f50' },
                { offset: 1, color: '#ff4500' }
              ]),
              new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                { offset: 0, color: '#87cefa' },
                { offset: 1, color: '#4682b4' }
              ]),
              new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                { offset: 0, color: '#98fb98' },
                { offset: 1, color: '#32cd32' }
              ]),
              new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                { offset: 0, color: '#dda0dd' },
                { offset: 1, color: '#8a2be2' }
              ]),
              new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                { offset: 0, color: '#f0e68c' },
                { offset: 1, color: '#ffd700' }
              ])
            ];
            return colorList[params.dataIndex];
          }
        }
      }
    }
  ]
};


const barOption = {
  title: {
    text: '科室数量分析',
    subtext: '数据统计图',
    left: 'center',
    top: '10%',
    textStyle: {
      color: '#4A4A4A',
      fontSize: 26,
      fontWeight: 'bold',
      fontFamily: 'Helvetica, Arial, sans-serif',
    },
    subtextStyle: {
      color: '#888',
      fontSize: 16,
      fontFamily: 'Helvetica, Arial, sans-serif',
    }
  },
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(0, 0, 0, 0.6)',
    textStyle: {
      color: '#fff',
    },
    formatter: function (params) {
      const name = params[0].name;
      const value = params[0].value;
      return `${name}<br/>科室数量: ${value}`;
    },
    extraCssText: 'border-radius: 8px; padding: 10px; box-shadow: 0 0 15px rgba(0, 0, 0, 0.3);',
  },
  legend: {
    data: ['科室数量'],
    left: 'center',
    top: '15%',
    textStyle: {
      color: '#555',
      fontSize: 14,
      fontFamily: 'Helvetica, Arial, sans-serif',
    },
    itemWidth: 18,
    itemHeight: 18,
    icon: 'circle',
  },
  grid: {
    left: '10%',
    right: '10%',
    bottom: '15%',
    top: '25%',
    containLabel: true,
  },
  xAxis: {
    type: 'category',
    data: ['内科', '外科', '儿科', '妇产科', '皮肤科'],  // 替换为实际数据
    axisLine: {
      show: true,
      lineStyle: {
        color: '#DCDCDC',
        width: 2,
      }
    },
    axisTick: {
      show: false,
    },
    axisLabel: {
      color: '#555',
      fontSize: 14,
      fontFamily: 'Helvetica, Arial, sans-serif',
      interval: 0,
      rotate: 30,
    }
  },
  yAxis: {
    type: 'value',
    axisLine: {
      show: false,
    },
    axisTick: {
      show: false,
    },
    axisLabel: {
      color: '#555',
      fontSize: 14,
      fontFamily: 'Helvetica, Arial, sans-serif',
    },
    splitLine: {
      show: true,
      lineStyle: {
        color: '#E0E0E0',
        type: 'solid',
      }
    }
  },
  series: [
    {
      data: [20, 35, 50, 65, 90],  // 替换为实际数据
      type: 'bar',
      barWidth: '35%',  // 更宽的柱子
      itemStyle: {
        normal: {
          color: function (params) {
            const colorList = [
              '#6A5ACD', '#FF8C00', '#00BFFF', '#3CB371', '#FFD700'
            ];  // 精美的配色
            return colorList[params.dataIndex % colorList.length];
          },
          borderColor: '#fff',
          borderWidth: 3,
          borderRadius: [12, 12, 0, 0],  // 圆角效果
          shadowColor: 'rgba(0, 0, 0, 0.15)',
          shadowBlur: 10,
          shadowOffsetX: 3,
          shadowOffsetY: 3,
        }
      },
      label: {
        show: true,
        position: 'top',
        color: '#333',
        fontSize: 16,
        fontWeight: 'bold',
        formatter: '{c}',
      },
      emphasis: {
        itemStyle: {
          color: '#FF6347',
          shadowColor: 'rgba(0, 0, 0, 0.4)',
          shadowBlur: 15,
        }
      },
      animationType: 'scale',
      animationEasing: 'elasticOut',
      animationDuration: 1200,
    }
  ]
};


const data = reactive({
  countData: {}
})

request.get('/getCountData').then(res => {
  data.countData = res.data
})

// 等页面所有元素加载完成后再设置 echarts图表
onMounted(() => {
  // 请求数据  初始化图表
  // 折线图
  let lineDom = document.getElementById('line');
  let lineChart = echarts.init(lineDom);

  request.get('/lineData').then(res => {
    lineOption.xAxis.data = res.data.dateList
    lineOption.series[0].data = res.data.moneyList
    lineChart.setOption(lineOption)
  })

  // 饼图
  let pieDom = document.getElementById('pie');
  let pieChart = echarts.init(pieDom);

  request.get('/pieData').then(res => {
    pieOption.series[0].data = res.data
    pieChart.setOption(pieOption)
  })

  // 柱状图
  let barDom = document.getElementById('bar');
  let barChart = echarts.init(barDom);

  request.get('/barData').then(res => {
    barOption.xAxis.data = res.data.officeList
    barOption.series[0].data = res.data.countList
    barChart.setOption(barOption)
  })
})
</script>

<style>
img:hover {
  transform: scale(1.1); /* 放大图片 */
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.3); /* 增强阴影 */
}
</style>