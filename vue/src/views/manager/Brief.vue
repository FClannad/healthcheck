<template>
  <div class="hospital-intro">
    <!-- 轮播图部分 -->
    <el-card class="carousel-card">
      <el-carousel interval="4000" height="600px" width="400px"  >
        <el-carousel-item v-for="item in carouselList" :key="item.id">
          <img :src="item.image" alt="轮播图" style="width: 100%; height: 100%; opacity:1;">
        </el-carousel-item>
      </el-carousel>
    </el-card>

    <!-- 医院概况 -->
    <section class="hospital-overview">
      <h2>医院概况</h2>
      <p>成立于2005年的XX医院，是一所综合性的三级甲等医院，拥有超过1000张床位，涵盖了内科、外科、妇产科、儿科等多个学科领域。医院致力于为患者提供高质量的医疗服务，已经成为本地区领先的医疗机构。</p>
    </section>

    <!-- 医疗团队 -->
    <section class="medical-team">
      <h2 ><a>我们的医疗团队</a></h2>
      <p>XX医院拥有一支由国内外知名专家组成的医疗团队，医院的主治医生和专科医师具有丰富的临床经验，并且多次获得国内外医疗奖项。我们的医疗团队致力于为患者提供科学、专业和人性化的医疗服务。</p>

    </section>

    <!-- 技术设备 -->
    <section class="technology-equipment">
      <h2>先进的医疗设备</h2>
      <p>为了确保诊疗的准确性和高效性，XX医院配备了多台世界顶级医疗设备，包括MRI、CT、超声波检查仪、机器人手术系统等。我们不断引进和升级设备，以满足日益增长的医疗需求。</p>
      <img src="@/assets/imgs/equipent.png" style="width: 80%" alt="Medical Equipment">
    </section>

    <!-- 服务项目 -->
    <section class="services">
      <h2>我们提供的服务</h2>
      <div class="service-list">
        <div class="service-item"  >
          <h3  :to="{ path: '/manager/doctor'}" >先进的医疗团队</h3>
          <p>我们提供各类门诊服务，涵盖多个科室的医生，包括内科、外科、妇产科、牙科等。</p>
        </div>
        <div class="service-item">
          <h3>体检预约服务</h3>
          <p>医院设有多个病区，提供舒适、专业的住院环境，全天候护理服务。可以随时进行体检预约</p>
        </div>
        <div class="service-item">
          <h3>体检套餐服务</h3>
          <p>提供各类的体检套餐。</p>
        </div>
        <div class="service-item">
          <h3>健康科普服务</h3>
          <p>为市民提供全方位的健康检查服务，早期筛查，预防疾病。</p>
        </div>
      </div>
    </section>

    <!-- 医院文化 -->
    <section class="hospital-culture">
      <img src="@/assets/imgs/cu.jpeg" style="width: 80%; height: 700px" >
      <h2>医院文化</h2>
      <p>“关爱每一位患者，打造最专业的医疗服务”，这是我们始终秉承的服务理念。我们注重患者的体验，致力于营造温馨、舒适的就医环境，所有工作人员都以高度的责任心和同理心为患者提供最好的医疗服务。</p>
    </section>

    <!-- 患者体验 -->
    <section class="patient-experience">
      <img src="@/assets/imgs/listen.jpeg" style="width: 80%; height: 700px" >
      <h2>患者的声音</h2>
      <p>我们的患者常常表示，在XX医院的就医体验非常好，服务细致、医疗质量高，医院环境温馨舒适。许多患者选择我们作为长期的就医基地，并且通过口碑推荐给身边的朋友。</p>
    </section>

    <!-- 科研与教育 -->
    <section class="research-education">
      <img src="@/assets/imgs/edu.jpeg" style="width: 80%; height: 700px" >
      <h2>科研与教育</h2>
      <p>XX医院不仅注重医疗服务，还积极推动医学科研和临床实践的结合。我们与多所知名高校和科研机构合作，推动医学研究，培养医学人才。</p>
    </section>

    <!-- 社区服务与公益 -->
    <section class="community-service">
      <img src="@/assets/imgs/shequ.jpeg" style="width: 80%; height: 700px" >
      <h2>社区服务与公益</h2>
      <p>作为本地区的医疗龙头企业，XX医院积极开展公益活动，如免费义诊、健康讲座等。我们致力于提升社区居民的健康水平，打造更和谐的社会。</p>
    </section>
  </div>

</template>

<script>
import Carousel from '@chenfengyuan/vue-carousel';
import Slide from '@chenfengyuan/vue-carousel';

import image1 from '@/assets/imgs/haibao1.png'
import image2 from '@/assets/imgs/haibao2.png'
import image3 from '@/assets/imgs/jing.jpg'

import router from "@/router/index.js";

export default {
  components: {
    Carousel,
    Slide
  },
  data() {
    return {
      carouselList: [
        { id: 1, image: image1 },
        { id: 2, image: image2 },
        { id: 3, image: image3 }
      ],

    };
}

};

const ToDoctor = () => {
  router.push({ path: '/doctor' });
}


</script>

<style scoped>

p{
  width: 100%;
}

.hospital-intro {
  background: url("@/assets/imgs/background3.jpg") no-repeat center center; /* 设置背景图 */
  background-size: cover; /* 背景图像覆盖整个区域 */
  position: relative; /* 设置相对定位，以便给子元素提供定位参考 */
  z-index: 0; /* 确保背景图位于底层 */
  font-family: 'Arial', sans-serif;
  margin: 0;
  padding: 0;
}

.hospital-intro::before {
  content: ''; /* 使用伪元素创建遮罩 */
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4); /* 半透明的黑色遮罩层 */
  z-index: -1; /* 确保遮罩层位于背景图下方但在内容上方 */
}

h2, p {
  color: #fff; /* 文字颜色为白色，以提高对比度 */
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.6); /* 添加文字阴影增强可读性 */
}

.carousel-card {
  height: 100%;
  margin-bottom: 0px;
}

.carousel-container {
  max-width: 800px;
  margin: 0 auto;
}

.carousel-container img {
  width: 100%;
  height: auto;
}

section {
  padding: 40px 20px;
  text-align: center;
  background-color: transparent; /* 确保文字框背景透明 */
  margin-bottom: 20px;
  border-radius: 8px;
  position: relative;
  z-index: 1; /* 确保文字内容位于背景图之上 */
}

h2 {
  font-size: 28px;
  color: #fff; /* 文字颜色为白色 */
  margin-bottom: 20px;
}

p {
  left: 100px;
  font-size: 18px;
  color: #fff; /* 文字颜色为白色 */
  line-height: 1.6;
}

.service-list {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
}

.service-item {
  width: 22%;
  margin: 10px;
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.service-item:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.service-item h3 {
  font-size: 20px;
  color: #333;
  margin-bottom: 10px;
}

.service-item p {
  font-size: 16px;
  color: #777;
}

img {
  max-width: 100%;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .service-item {
    width: 45%; /* 在小屏幕上调整为45% */
    margin: 10px 0;
  }
}

@media (max-width: 480px) {
  .service-item {
    width: 100%; /* 在更小的屏幕上调整为100% */
  }
}

</style>
