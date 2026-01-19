-- MySQL dump 10.13  Distrib 8.0.34, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: xm_health_check
-- ------------------------------------------------------
-- Server version	8.0.34

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `accounts`
--

DROP TABLE IF EXISTS `accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `accounts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `account` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `balance` double DEFAULT '1000',
  PRIMARY KEY (`id`),
  UNIQUE KEY `account` (`account`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accounts`
--

LOCK TABLES `accounts` WRITE;
/*!40000 ALTER TABLE `accounts` DISABLE KEYS */;
INSERT INTO `accounts` VALUES (1,'userA',1000),(2,'userB',1000);
/*!40000 ALTER TABLE `accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_consultation`
--

DROP TABLE IF EXISTS `ai_consultation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_consultation` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int DEFAULT NULL COMMENT '用户ID',
  `user_question` text COLLATE utf8mb4_unicode_ci COMMENT '用户问题',
  `ai_response` text COLLATE utf8mb4_unicode_ci COMMENT 'AI回复',
  `recommended_exams` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '推荐的体检项目',
  `session_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '会话ID',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'active' COMMENT '状态：active, archived, deleted',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI咨询记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '角色',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电话',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='管理员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'bin','123','bin','http://localhost:9090/files/download/1754882500639-2.jpg','ADMIN','15111095528','2692433610@qq.com'),(2,'cst','123','cst','http://localhost:9090/files/download/1731461039095-1.jpg','ADMIN','13988887766','limi@health.com');
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `doctor`
--

DROP TABLE IF EXISTS `doctor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doctor` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '角色',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电话',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `work_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '从业时长',
  `descr` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '简介',
  `office_id` int DEFAULT NULL COMMENT '科室的编号',
  `title_id` int DEFAULT NULL COMMENT '职称的编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='医生表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doctor`
--

LOCK TABLES `doctor` WRITE;
/*!40000 ALTER TABLE `doctor` DISABLE KEYS */;
INSERT INTO `doctor` VALUES (1,'zhangwei','123','张伟','http://localhost:9090/files/download/1733830768393-doctorn.jpeg','DOCTOR','13988887777','zhangwei@xm.com','20年','职务：主任医师，内科专家\n学历背景：毕业于复旦大学医学部，医学博士学位，曾赴德国柏林大学进行进修。\n专业特长：擅长呼吸系统疾病的诊断和治疗，尤其在肺癌、慢性阻塞性肺病（COPD）等领域有深厚的造诣。\n学术成就：发表核心期刊论文20余篇，主持过多项国家级科研课题，研究成果获国家奖。\n社会兼职：现任中国医师协会呼吸专委会委员。\n临床经验：有超过20年临床经验，精通呼吸科疾病的诊治，尤其注重患者个体化治疗方案的制定。',5,1),(2,'limi','321','李密','http://localhost:9090/files/download/1731595197687-1.jpg','DOCTOR','13988997766','limi@xm.com','10年','职务：主任医师，心血管内科专家\n学历背景：毕业于北京大学医学部，获医学博士学位，后在美国哈佛大学医学院完成心血管内科高级研修课程。\n专业特长：擅长高血压、冠心病、心力衰竭、心律失常等心血管疾病的诊断与治疗，特别是在介入心脏病学领域有着深厚造诣，成功实施了数千例心脏支架植入、心脏起搏器安装及复杂心血管介入手术。\n学术成就：发表国内外核心期刊论文30余篇，参与编写心血管疾病治疗指南2部，多次受邀在国际心血管病学术会议上做主题演讲。作为项目负责人，主持多项国家级科研课题，研究成果曾获国家科技进步奖二等奖。\n社会兼职：现任中华医学会心血管病分会常务委员，中国医师协会心血管内科医师分会副会长，同时担任《中华心血管病杂志》编委。\n临床经验：拥有超过20年的丰富临床经验，以其精湛的医术、严谨的学术态度和高度的责任心赢得了患者及同行的广泛赞誉。张伟医生始终坚持“以患者为中心”的服务理念，注重医患沟通，强调个性化治疗方案，致力于为每一位患者提供最适宜的治疗策略和最贴心的医疗服务。',2,3),(3,'wangfang','123','王芳','http://localhost:9090/files/download/1731468749818-bfe882bb062680cf97ef9ceabf976a97.jpeg','DOCTOR','13988776655','wangfang@xm.com','15年','职务：主任医师，妇科专家\n学历背景：毕业于上海交通大学医学部，医学博士学位，曾在英国剑桥大学进行妇科进修。\n专业特长：擅长治疗各种妇科疾病，特别是宫颈癌筛查、不孕不育症、妇科内分泌紊乱等领域。\n学术成就：参与多项国家级科研课题，发表核心期刊论文10篇。\n社会兼职：担任中国妇产科医师协会会员，上海市妇科专家委员会成员。\n临床经验：拥有15年丰富的临床经验，致力于为每一位女性患者提供温暖、专业的医疗服务，深受患者喜爱。',1,2),(4,'lihui','123','李辉','http://localhost:9090/files/download/1731468742638-f366dee48300d4afd31fca927b3a6f39.jpeg','DOCTOR','13988665544','lihui@xm.com','12年','职务：副主任医师，骨科专家\n学历背景：毕业于华中科技大学医学部，医学博士学位，后赴瑞士进行骨科临床进修。\n专业特长：擅长脊柱疾病、关节置换手术及骨折治疗。\n学术成就：多篇论文在国内外核心期刊发表，参与多项骨科领域的科研课题。\n社会兼职：中国骨科医师协会会员，长期参与国内外学术会议交流。\n临床经验：拥有12年骨科临床经验，尤其擅长复杂脊柱手术及关节置换术，得到患者及同行的广泛认可。',4,1),(5,'zhanghua','123','张华','http://localhost:9090/files/download/1731468187127-1.jpg','DOCTOR','13988554433','zhanghua@xm.com','18年','职务：主任医师，肿瘤科专家\n学历背景：毕业于中山大学医学部，医学博士学位，曾在美国MD安德森癌症中心进修。\n专业特长：擅长各类恶性肿瘤的早期诊断与治疗，尤其在肺癌、肝癌的个性化治疗方面有丰富经验。\n学术成就：主持和参与多个国家级肿瘤科研项目，发表学术论文30余篇。\n社会兼职：中国肿瘤学会会员，常年担任国际肿瘤学会议讲者。\n临床经验：有18年的临床经验，擅长各种肿瘤的综合治疗，致力于为患者提供最前沿、最适合的治疗方案。',3,1),(6,'wangliu','123','王六','http://localhost:9090/files/download/1731468736058-bfe882bb062680cf97ef9ceabf976a97.jpeg','DOCTOR','13988443322','wangliu@xm.com','9年','职务：副主任医师，儿科专家\n学历背景：毕业于南京医科大学，医学博士学位，曾在瑞士日内瓦大学医学院进修。\n专业特长：专注于儿童常见病、疑难病及疫苗接种方面，尤其擅长儿童过敏性疾病及哮喘治疗。\n学术成就：多篇学术论文在国内期刊上发表，参与儿童医疗指导手册的编写。\n社会兼职：中国儿科医师协会会员，长期参与国际儿科医疗学术交流。\n临床经验：拥有9年的儿科临床经验，具有丰富的儿童疾病诊治经验，尤其注重儿童的健康成长与发育。',5,2),(7,'wangqing','123','王清','http://localhost:9090/files/download/1733654025447-70eeb799062c5e70af6c43c886aa8c3d.jpeg','DOCTOR','13988332211','wangqing@xm.com','14年','职务：主任医师，眼科专家\n学历背景：毕业于中南大学医学部，医学博士学位，曾赴法国巴黎进行眼科临床学习。\n专业特长：专注于眼科疾病的诊断与治疗，特别是白内障、青光眼、眼底病等眼科疾病。\n学术成就：发表核心期刊论文20篇，参与多个国家级眼科科研项目。\n社会兼职：中国眼科协会会员，长期参与国际眼科学术会议。\n临床经验：拥有14年眼科临床经验，致力于为患者提供精准的眼科诊疗服务。',6,4),(8,'liyun','123','李云','http://localhost:9090/files/download/1733653972248-70eeb799062c5e70af6c43c886aa8c3d.jpeg','DOCTOR','13988221100','liyun@xm.com','16年','职务：主任医师，皮肤科专家\n学历背景：毕业于华东理工大学医学部，医学博士学位，曾赴日本东京大学皮肤科进修。\n专业特长：专注于皮肤病的早期诊断与治疗，尤其是牛皮癣、湿疹、痤疮等皮肤病。\n学术成就：发表学术论文25篇，参与皮肤病的临床治疗指南编写。\n社会兼职：中国皮肤病学会会员，长期参与国际皮肤病学术会议。\n临床经验：拥有16年临床经验，特别擅长各种皮肤病的综合治疗与管理，深受患者欢迎。',7,5);
/*!40000 ALTER TABLE `doctor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `examination_order`
--

DROP TABLE IF EXISTS `examination_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `examination_order` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(255) DEFAULT NULL COMMENT '订单号',
  `user_id` int DEFAULT NULL COMMENT '用户id',
  `doctor_id` int DEFAULT NULL COMMENT '医生id',
  `examination_id` int DEFAULT NULL COMMENT '体检项目id',
  `order_type` varchar(255) DEFAULT NULL COMMENT '订单类型',
  `reserve_date` varchar(255) DEFAULT NULL COMMENT '预约日期',
  `start_time` varchar(255) DEFAULT NULL COMMENT '开始时间',
  `end_time` varchar(255) DEFAULT NULL COMMENT '结束时间',
  `file` varchar(255) DEFAULT NULL COMMENT '检测报告地址',
  `comment` varchar(255) DEFAULT NULL COMMENT '评语',
  `money` int DEFAULT NULL COMMENT '费用',
  `status` varchar(255) DEFAULT NULL COMMENT '订单状态',
  `create_time` varchar(255) DEFAULT NULL COMMENT '创建时间',
  `check_time` varchar(255) DEFAULT NULL COMMENT '检查时间',
  `feedback` varchar(255) DEFAULT NULL COMMENT '审批反馈',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='体检预约订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `examination_order`
--

LOCK TABLES `examination_order` WRITE;
/*!40000 ALTER TABLE `examination_order` DISABLE KEYS */;
INSERT INTO `examination_order` VALUES (1,'202411181731932766841',1,1,10,'普通体检','2024-11-11','18:56','20:56','http://localhost:9090/files/download/1731933815733-1.jpg','很健康',150,'已完成','2024-11-18 20:26:06',NULL,'很好'),(2,'202411181731934701499',1,1,10,'普通体检','2024-11-15','18:58','20:58','http://localhost:9090/files/download/1731934759830-1.jpg','没救了',150,'已完成','2024-11-18 20:58:21','2024-11-12 20:59:14','ok\n'),(4,'202411181731934869734',1,4,6,'普通体检','2024-11-18','15:01','17:01',NULL,NULL,350,'已取消','2024-11-18 21:01:09',NULL,NULL),(5,'202411181731935999069',1,1,10,'普通体检','2024-11-19','19:19','21:19',NULL,NULL,150,'审批拒绝','2024-11-18 21:19:59',NULL,NULL),(9,'202411221732260069487',1,1,10,'普通体检','2024-11-25','14:21','17:21','http://localhost:9090/files/download/1732714158646-1.jpg',NULL,150,'已完成','2024-11-22 15:21:09','2024-11-27 21:28:56',NULL),(10,'202411221732270539568',1,1,10,'普通体检','2025-3-24','17:15','19:15','http://localhost:9090/files/download/1732714163294-1.jpg',NULL,150,'已完成','2024-11-22 18:15:39','2024-11-27 21:28:58',NULL),(11,'202411281732806687360',1,1,10,'普通体检','2025-3-26','23:12','23:15','http://localhost:9090/files/download/1732806713686-2022401575  --冯金彬_v1.0.docx','asd',150,'已完成','2024-11-28 23:11:27','2024-11-28 23:11:47',NULL),(12,'202411291732861253211',1,1,10,'普通体检','2025-3-25','11:20','14:20','http://localhost:9090/files/download/1732861830786-1.jpg','很好',150,'已完成','2024-11-29 14:20:53','2024-11-29 14:30:25',NULL),(13,'202412101733823086859',1,8,17,'套餐体检','2025-3-22','16:31','18:31','http://localhost:9090/files/download/1733823288818-zw.jpg','死期将近',566,'已完成','2024-12-10 17:31:26','2024-12-10 17:34:28','可以的，周伟帅哥'),(14,'202412101733823108830',2,8,17,'套餐体检','2025-3-21','16:31','18:31','http://localhost:9090/files/download/1733823398431-1.jpg','很好',566,'已完成','2024-12-10 17:31:48','2024-12-10 17:36:24','好的'),(15,'202412101733823161986',2,1,8,'普通体检','2025-3-31','18:32','20:32','http://localhost:9090/files/download/1733884107423-体检报告.docx','救不了了\n',1200,'已完成','2024-12-10 17:32:41','2024-12-11 10:28:15','通过'),(16,'202412101733823177127',2,4,5,'普通体检','2025-3-19','15:32','17:32','http://localhost:9090/files/download/1733884056280-体检报告.docx','很好',350,'已完成','2024-12-10 17:32:57','2024-12-11 10:27:30','好的\n'),(17,'202412101733831251482',1,1,10,'普通体检','2025-3-13','17:45','21:45','http://localhost:9090/files/download/1733831673060-体检报告.docx','很好身体',150,'已完成','2024-12-10 19:47:31','2024-12-10 19:52:09','好的'),(18,'202508281756344802330',1,1,10,'普通体检','2025-08-29','08:30','09:30','http://localhost:9090/files/download/1756344851111-QST学员学习总结-吉首大学-计算机科学与工程学院-冯金彬.doc','很好',150,'已完成','2025-08-28 09:33:22','2025-08-28 09:33:59','ok \n'),(19,'202508281756345090232',1,1,10,'普通体检','2025-08-30','09:30','10:30','http://localhost:9090/files/download/1756345154332-QST学员学习总结-吉首大学-计算机科学与工程学院-冯金彬.doc','1',150,'已完成','2025-08-28 09:38:10','2025-08-28 09:39:02','ok');
/*!40000 ALTER TABLE `examination_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `examination_package`
--

DROP TABLE IF EXISTS `examination_package`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `examination_package` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT '项目名称',
  `descr` varchar(255) DEFAULT NULL COMMENT '套餐简介',
  `cover` varchar(255) DEFAULT NULL COMMENT '项目封面',
  `money` int DEFAULT NULL COMMENT '费用',
  `doctor_id` int DEFAULT NULL COMMENT '负责医生',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `examinations` varchar(255) DEFAULT NULL COMMENT '体检项目列表',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='套餐体检项目信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `examination_package`
--

LOCK TABLES `examination_package` WRITE;
/*!40000 ALTER TABLE `examination_package` DISABLE KEYS */;
INSERT INTO `examination_package` VALUES (17,'眼睛全方位检查','对于眼睛检查','http://localhost:9090/files/download/1756344420757-3925fa6d29f419c7a378fa2575182531.jpeg',566,8,'医院二号楼','[5,4,1]'),(19,'全身体检','很贵','http://localhost:9090/files/download/1756344384262-76923b91680c4697814487247137542e.jpeg',5555,3,'医院一号楼','[10,9,8,7,6,5,4,3,2,1]');
/*!40000 ALTER TABLE `examination_package` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `examination_type`
--

DROP TABLE IF EXISTS `examination_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `examination_type` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='普通体检类型';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `examination_type`
--

LOCK TABLES `examination_type` WRITE;
/*!40000 ALTER TABLE `examination_type` DISABLE KEYS */;
INSERT INTO `examination_type` VALUES (4,'基础体检'),(5,'全面体检'),(6,'专项体检'),(7,'高端体检'),(8,' 老年体检'),(9,' 女性体检'),(10,'男性体检'),(11,'儿童体检');
/*!40000 ALTER TABLE `examination_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `feedback`
--

DROP TABLE IF EXISTS `feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `feedback` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int DEFAULT NULL COMMENT '用户id',
  `content` varchar(255) DEFAULT NULL COMMENT '反馈内容',
  `time` varchar(255) DEFAULT NULL COMMENT '反馈时间',
  `reply_content` varchar(255) DEFAULT NULL COMMENT '回复内容',
  `reply_time` varchar(255) DEFAULT NULL COMMENT '回复时间',
  `status` varchar(255) DEFAULT NULL COMMENT '回复状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='反馈信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feedback`
--

LOCK TABLES `feedback` WRITE;
/*!40000 ALTER TABLE `feedback` DISABLE KEYS */;
INSERT INTO `feedback` VALUES (2,2,'2','2024/11/21/8:33:27','14524','2024-11-21 20:26:46','已回复'),(4,1,'以后在来','2024-11-21 21:03:56','以后还是别来了','2024-11-21 21:04:48','已回复'),(5,1,'我不想体检了，我想吃饭','2024-11-21 21:04:25','先体检在吃饭','2024-11-21 21:04:59','已回复'),(8,1,'实打实','2024-11-28 20:57:58','hhh','2024-11-28 21:02:21','已回复'),(9,1,'我问问','2024-11-28 21:05:12','asd','2024-11-28 21:11:22','已回复'),(10,1,'阿萨德','2024-11-28 21:09:33','wdasad','2024-11-28 21:11:26','已回复'),(11,1,'阿萨德','2024-11-28 21:12:09',NULL,NULL,'待回复');
/*!40000 ALTER TABLE `feedback` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `information`
--

DROP TABLE IF EXISTS `information`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `information` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(255) DEFAULT NULL COMMENT '标题',
  `content` longtext COMMENT '内容',
  `cover` varchar(255) DEFAULT NULL COMMENT '封面图片',
  `publish_time` varchar(255) DEFAULT NULL COMMENT '发布时间',
  `author_id` int DEFAULT NULL COMMENT '发布者id',
  `view_count` int DEFAULT NULL COMMENT '浏览量',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='健康科普信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `information`
--

LOCK TABLES `information` WRITE;
/*!40000 ALTER TABLE `information` DISABLE KEYS */;
INSERT INTO `information` VALUES (9,'关爱健康，从预防做起','<p style=\"text-align: start;\">大家好！随着现代生活节奏的加快和环境的变化，人们的健康问题逐渐成为了社会关注的焦点。为了提高大家的健康意识，普及科学的健康知识，我们特此发布本次健康科普公告，希望大家能够从日常生活中入手，养成良好的健康习惯，预防常见疾病，保持身体和心理的最佳状态。</p><h3 style=\"text-align: start;\">一、保持良好的饮食习惯</h3><p style=\"text-align: start;\">饮食是健康的基石。正确的饮食不仅能提供身体所需的营养，还能帮助我们预防多种疾病。首先，均衡饮食非常重要。应确保每餐都包含适量的蛋白质、碳水化合物、脂肪、维生素和矿物质，避免偏食和暴饮暴食。</p><ul><li style=\"text-align: start;\"><strong>多样化饮食</strong>：每周应保证至少五种不同的蔬菜和水果，增加膳食纤维的摄入，促进肠道健康。</li><li style=\"text-align: start;\"><strong>少盐少油</strong>：尽量减少食物中的盐分和油脂摄入，过多的盐分容易引发高血压，油脂过多则可能导致肥胖和心血管疾病。</li><li style=\"text-align: start;\"><strong>适量饮水</strong>：每天饮用足够的水，成年人一般需要保持每天6-8杯水的摄入量，帮助维持身体的水分平衡，促进新陈代谢。</li></ul><h3 style=\"text-align: start;\">二、保持适度的运动量</h3><p style=\"text-align: start;\">运动不仅有助于增强体力，还能提高免疫力，预防心血管疾病、糖尿病等慢性病。根据世界卫生组织的建议，成年人每周应进行至少150分钟的中等强度运动或75分钟的高强度运动。</p><ul><li style=\"text-align: start;\"><strong>每天走路</strong>：如果条件允许，建议每天步行30分钟，这是最简便且效果显著的锻炼方式。</li><li style=\"text-align: start;\"><strong>参与集体运动</strong>：游泳、跑步、瑜伽、太极等都是非常适合大众的运动方式。找到自己喜欢的运动，保持长期坚持。</li><li style=\"text-align: start;\"><strong>避免久坐</strong>：长时间久坐对健康非常不利，建议每工作45分钟就起来活动5-10分钟，进行简单的伸展运动。</li></ul><h3 style=\"text-align: start;\">三、保持心理健康</h3><p style=\"text-align: start;\">心理健康对整体健康至关重要。随着工作和生活压力的增加，心理健康问题日益凸显。良好的心理状态有助于身体健康，反之，压力过大容易引发各种身体疾病。</p><ul><li style=\"text-align: start;\"><strong>缓解压力</strong>：通过冥想、呼吸练习、倾诉等方式减轻压力，有助于情绪的调节和心理的放松。</li><li style=\"text-align: start;\"><strong>保持社交</strong>：与家人、朋友保持良好的沟通和互动，分享自己的喜怒哀乐，能够有效减轻孤独感，增强情感支持。</li><li style=\"text-align: start;\"><strong>培养兴趣爱好</strong>：通过学习新技能、发展兴趣爱好等方式，让自己保持积极向上的心态，增强生活的幸福感。</li></ul><h3 style=\"text-align: start;\">四、定期体检，早预防早发现</h3><p style=\"text-align: start;\">健康体检是预防疾病的重要手段。定期的体检有助于我们及时发现潜在的健康问题，尤其是一些慢性疾病，如高血压、糖尿病、肝肾功能问题等，在早期往往没有明显的症状，通过体检能够及早发现并采取相应的治疗措施。</p><ul><li style=\"text-align: start;\"><strong>常见的体检项目</strong>：血压、血糖、血脂、肝肾功能、胸部X光等检查都是常见的健康体检项目。根据年龄、性别和健康状况，选择适合自己的体检项目。</li><li style=\"text-align: start;\"><strong>遵循医生建议</strong>：体检结果出来后，应根据医生的建议进行必要的治疗或生活方式调整，做到早预防、早干预。</li></ul><h3 style=\"text-align: start;\">五、良好的生活习惯</h3><p style=\"text-align: start;\">除了饮食、运动和心理健康，良好的生活习惯同样不可忽视。充足的睡眠、规律的作息、远离烟酒等不良习惯，都能显著提高我们的健康水平。</p><ul><li style=\"text-align: start;\"><strong>规律作息</strong>：尽量保持每天7-8小时的高质量睡眠，避免熬夜。睡眠对身体的恢复和免疫系统的正常运作至关重要。</li><li style=\"text-align: start;\"><strong>戒烟限酒</strong>：吸烟和过量饮酒是引发多种疾病的危险因素，尽量避免或减少这些不良习惯的影响。</li></ul><h3 style=\"text-align: start;\">结语</h3><p style=\"text-align: start;\">健康是人生最宝贵的财富，只有保持良好的生活习惯、合理的饮食和充足的运动，才能确保身体健康、心理愉快、生活幸福。希望大家在日常生活中时刻保持对健康的关注，从现在做起，从自我做起，创造一个更加健康、幸福的生活环境。</p><p style=\"text-align: start;\">让我们共同努力，关爱自己和他人的健康，营造更加美好的社区生活。</p>','http://localhost:9090/files/download/1756344668785-fa48be85310a1e4391a3ec10b0abf588.jpeg','2024-11-15 14:02:08',1,1),(10,'防风感，保护健康','<p style=\"text-align: start;\">尊敬的市民朋友们：</p><p style=\"text-align: start;\">随着季节交替，气温波动频繁，风感（风寒感冒）的发生率逐渐上升。风感，通常指由外界寒冷、干燥或湿气影响，导致人体免疫力下降、气道受到刺激，从而引发一系列上呼吸道症状的疾病。为帮助大家更好地理解风感的预防与治疗，特此发布本公告，旨在提醒大家注意季节性变化中的健康管理，增强抵抗力，减少风感的发生。</p><h3 style=\"text-align: start;\">一、风感的常见症状</h3><p style=\"text-align: start;\">风感通常由风寒、风热或风湿等外邪引起，表现症状有所不同，但多见以下几种情况：</p><ul><li style=\"text-align: start;\"><strong>鼻部症状</strong>：鼻塞、流涕、打喷嚏、喉咙干痒。</li><li style=\"text-align: start;\"><strong>全身症状</strong>：头痛、畏寒、全身乏力、轻度发热。</li><li style=\"text-align: start;\"><strong>咳嗽</strong>：常伴有干咳或痰少的情况。</li><li style=\"text-align: start;\"><strong>嗓音变化</strong>：喉咙红肿、声音沙哑。</li></ul><p style=\"text-align: start;\">需要注意的是，风感的症状与流感、普通感冒类似，但风感一般症状较轻，且多数患者可以通过自我管理缓解症状。</p><h3 style=\"text-align: start;\">二、风感的致病原因与传染途径</h3><p style=\"text-align: start;\">风感的发生通常与以下因素相关：</p><ul><li style=\"text-align: start;\"><strong>气候变化</strong>：气温突降、寒冷天气以及湿气重的季节，易诱发风感。特别是秋冬季节，天气干燥寒冷，人体容易受到风寒侵袭，导致免疫力下降。</li><li style=\"text-align: start;\"><strong>免疫力低下</strong>：身体抵抗力不足，容易感染外界的风寒之气。老年人、儿童、孕妇、慢性病患者等群体更容易受到影响。</li><li style=\"text-align: start;\"><strong>环境因素</strong>：长时间处于空调、风扇直吹的环境中，容易导致局部体温过低，进而引发风感症状。</li></ul><p style=\"text-align: start;\">风感主要通过空气传播，尤其在密闭空间中，飞沫传播较为常见。尤其在家庭、办公室、公共交通工具等人群聚集的地方，较易感染。</p><h3 style=\"text-align: start;\">三、风感的预防措施</h3><ol><li style=\"text-align: start;\">保持温暖，避免受寒 在寒冷天气中，尤其是在外出时，穿着适当的衣物，以保暖为主。避免穿着单薄衣物暴露于寒冷气候中，尤其是脖部、胸部和脚部需要额外注意保暖。</li><li style=\"text-align: start;\">增强体质，提升免疫力 均衡饮食、适量运动、充足睡眠等可以增强免疫力，帮助身体抵抗外界病原的侵袭。特别是高强度的有氧运动，如快走、跑步等，有助于改善心肺功能，增强抗病能力。均衡饮食：保持丰富的营养摄入，增加维生素C（如柑橘、草莓、绿叶蔬菜等）和维生素D（如鱼类、蛋类等）的摄入，以增强免疫功能。睡眠管理：保持规律作息，确保每晚7-8小时的高质量睡眠，有助于增强体内的免疫反应。</li><li style=\"text-align: start;\">避免长时间暴露在寒风中 如果天气寒冷且多风，应尽量减少在室外的活动时间，避免长时间待在寒风直吹的环境中，尤其是避免空调或风扇直接吹向身体。</li><li style=\"text-align: start;\">勤洗手、保持室内空气流通 勤洗手，尤其是在外出回家后，要使用肥皂和清水洗净手部，减少病原的传播。在家中应保持良好的空气流通，适当开窗通风，避免细菌、病毒积聚在室内。</li><li style=\"text-align: start;\">佩戴口罩，减少传播 在人群密集的场所，佩戴口罩有助于防止空气中传播的病菌进入呼吸道。特别是在冬季，佩戴口罩不仅可以抵御外界的寒风，还能有效防止病菌的传播。</li><li style=\"text-align: start;\">适时接种流感疫苗 虽然流感与风感有所不同，但许多防护措施相似。接种流感疫苗是预防流感及其他呼吸道疾病的有效手段，可以显著降低感染风险。</li></ol><h3 style=\"text-align: start;\">四、风感的治疗方法</h3><ol><li style=\"text-align: start;\">对症治疗 风感的治疗以缓解症状为主。常见药物如退烧药、抗病毒药物、咳嗽药物等可以帮助缓解不适。若伴随较明显的症状，如持续发热、持续咳嗽等，可适当使用药物进行缓解。</li><li style=\"text-align: start;\">饮食与生活护理温热饮品：喝一些温热的汤水或茶水，有助于舒缓喉咙不适，避免寒凉食物和冷饮，防止加重症状。保持室内湿润：使用加湿器或将湿毛巾挂在室内，有助于缓解干燥的空气对喉咙的刺激，减轻咳嗽和喉部不适。</li><li style=\"text-align: start;\">及时就医 如果症状较重或持续不见好转，尤其是出现高烧、咳痰或呼吸困难等症状时，应及时就医，排除其他严重疾病，接受专业医生的诊治。</li></ol><h3 style=\"text-align: start;\">五、总结与提醒</h3><p style=\"text-align: start;\">风感虽然多数情况下不会导致严重并发症，但若处理不当，仍可能影响生活质量，甚至诱发其他疾病。因此，预防风感的发生，保持健康的生活方式，提升身体免疫力是至关重要的。大家应根据气候变化，做好自我保护，合理穿着，保持室内温暖，增加运动，保持良好的作息和饮食习惯。</p><p style=\"text-align: start;\">如有任何健康问题，欢迎随时向专业医生咨询，我们将竭诚为您提供个性化的健康指导与支持。</p>','http://localhost:9090/files/download/1756344635538-91f7617d73dcfe2de0cd7cf207178b0a.jpeg','2024-11-15 14:15:43',1,1),(11,'正确认识与应对发烧','<p style=\"text-align: start;\">尊敬的市民朋友们：</p><p style=\"text-align: start;\">发烧是人体免疫系统在对抗外部病原（如病毒、细菌、真菌等）入侵时常见的自然反应。它通常作为一种症状，表明身体正在努力对抗感染或其他健康问题。为帮助大家更好地理解发烧的原因、预防措施以及处理方法，医院特发布此健康科普公告，提供科学的指导，帮助大家更有效地应对发烧问题，保障自身健康。</p><h3 style=\"text-align: start;\">一、发烧的定义与常见症状</h3><p style=\"text-align: start;\">发烧是指体温升高超过正常范围（通常为37.0°C），在大多数情况下，成人体温超过38.0°C可视为发热。发烧的症状通常包括：</p><ul><li style=\"text-align: start;\"><strong>体温升高</strong>：成人体温超过38.0°C，儿童体温超过38.5°C通常被视为发热。</li><li style=\"text-align: start;\"><strong>寒战与出汗</strong>：发烧时，可能会感到寒冷，并伴随明显的出汗。</li><li style=\"text-align: start;\"><strong>头痛、全身乏力</strong>：发烧常伴随头痛、肌肉酸痛、乏力等全身不适症状。</li><li style=\"text-align: start;\"><strong>食欲减退、嗓子干痒或咳嗽</strong>：一些发热的疾病还可能伴有上呼吸道症状。</li></ul><p style=\"text-align: start;\">发烧虽然通常是身体的免疫反应，但它也可能是多种疾病的表现，包括感冒、流感、感染性疾病（如肺炎、尿路感染等）、炎症性疾病、甚至癌症等。因此，发烧是一种非常普遍的症状，了解发烧的根本原因至关重要。</p><h3 style=\"text-align: start;\">二、发烧的常见原因</h3><p style=\"text-align: start;\">发烧的原因广泛，可以分为以下几类：</p><ol><li style=\"text-align: start;\"><strong>感染性疾病</strong>：最常见的引起发烧的原因是细菌、病毒、真菌等微生物感染。比如流感、感冒、肺炎、胃肠炎、尿路感染等。</li><li style=\"text-align: start;\"><strong>炎症性疾病</strong>：某些自身免疫性疾病，如类风湿关节炎、系统性红斑狼疮等，也可能引起体温升高。</li><li style=\"text-align: start;\"><strong>药物反应</strong>：某些药物（如抗生素、抗癫痫药等）可能导致药物过敏反应，从而引起发烧。</li><li style=\"text-align: start;\"><strong>肿瘤</strong>：某些类型的癌症，如淋巴瘤、白血病等，也可能引发持续性发热。</li><li style=\"text-align: start;\"><strong>其他原因</strong>：过度劳累、环境过热、脱水等因素也可能引起发热。</li></ol><h3 style=\"text-align: start;\">三、发烧的预防与应对措施</h3><ol><li style=\"text-align: start;\">保持良好的个人卫生 勤洗手是防止感染、减少发烧风险的最有效手段之一。特别是在进食前、使用厕所后、外出归来后，要彻底清洁双手，避免细菌和病毒通过手部传播进入体内。</li><li style=\"text-align: start;\">避免与患病者密切接触 在流感季节或者某些传染病高发期，尽量避免与发烧或有感染症状的人近距离接触。尤其在公共场所，佩戴口罩能够有效阻止空气中细菌或病毒传播。</li><li style=\"text-align: start;\">合理膳食，增强免疫力 均衡的饮食有助于提高身体的免疫功能，增强抗病能力。摄取丰富的维生素、矿物质和蛋白质，尤其是新鲜的水果和蔬菜，以帮助身体抵御外部病原的侵害。</li><li style=\"text-align: start;\">注意适当的休息和运动 保证足够的睡眠，增强体力，保持身心健康。适量的有氧运动（如散步、慢跑）有助于增强免疫力，但在生病期间，应避免过度运动。</li><li style=\"text-align: start;\">环境温湿度调节 保持室内空气流通，避免潮湿、过热或过冷的环境。特别是在冬季，室内温度过低或者过高都可能导致身体不适，甚至引发上呼吸道感染和发热。</li></ol><h3 style=\"text-align: start;\">四、如何处理发烧</h3><ol><li style=\"text-align: start;\">自我监测体温 当出现发烧症状时，应首先测量体温，记录发热的时间和温度变化，观察是否伴随其他症状（如咳嗽、流感症状、皮疹等）。持续性的高烧或体温升高至39.0°C以上时，应及时就医。</li><li style=\"text-align: start;\">适当的退烧处理 对于轻度的发热（38.0°C以下），一般无需立刻服用退烧药。可以通过多喝水、保持环境通风、穿着轻便衣物来帮助身体降温。对于体温较高（38.5°C以上）的发热者，尤其是小孩、老年人或免疫力较弱的人群，适当使用退烧药（如对乙酰氨基酚、布洛芬等）可以帮助缓解不适并降低体温。但应注意遵循药品说明，避免滥用。</li><li style=\"text-align: start;\">及时就医 如果发烧持续不退，或伴随严重症状，如呼吸急促、胸痛、持续咳嗽、皮疹等，应尽早就医。特别是免疫力低下的群体，如老年人、孕妇、儿童、慢性病患者等，应及时到医院就诊，避免延误病情。</li><li style=\"text-align: start;\">保持充足水分 发烧时，人体会大量出汗和排尿，容易导致脱水，因此应增加水分摄入，最好选择温水或电解质饮料，避免饮用过冷或过热的液体。</li></ol><h3 style=\"text-align: start;\">五、总结</h3><p style=\"text-align: start;\">发烧是身体对抗感染和炎症的自然反应，大多数情况下不必过于恐慌。但对于高烧不退、症状严重的患者，及时就医非常重要。保持良好的生活习惯、注意个人卫生，能够有效减少发烧和其他疾病的发生。如果出现发烧症状，应该根据具体情况采取合适的处理措施，必要时寻求专业医生的帮助。</p><p style=\"text-align: start;\">我们提醒大家，健康是幸福的基础，只有通过科学的预防和治疗，才能确保身体健康。若有任何疑问，欢迎随时咨询我们医院的专业医生。</p><p style=\"text-align: start;\"><strong>医院内科</strong><br><br>2024年11月15日</p>','http://localhost:9090/files/download/1756344605764-d8095b4b576f41a157873fe0bb742d9c.jpeg','2024-11-15 14:23:00',1,1),(12,'关注HIV，预防从了解开始','<p style=\"text-align: start;\">尊敬的市民朋友们：</p><p style=\"text-align: start;\">随着医学和科技的不断进步，HIV（人类免疫缺陷病毒）感染的防治工作取得了显著成效。然而，HIV及其引发的艾滋病（AIDS）依然是全球范围内影响人类健康的重要问题。为了增强大家对HIV的认识，预防HIV感染，我们特别发布此份健康科普公告，希望每位市民都能科学了解HIV，采取有效预防措施，共同保护自己和他人的健康。</p><h4 style=\"text-align: start;\">一、什么是HIV？</h4><p style=\"text-align: start;\">HIV是人类免疫缺陷病毒（Human Immunodeficiency Virus）的简称，能够攻击人体的免疫系统，特别是攻击CD4细胞（即T细胞）。这些细胞是帮助人体抵抗疾病的重要免疫细胞。当CD4细胞数量减少，人体免疫力下降，容易感染其他疾病。最终，HIV感染如果没有得到及时治疗，可能会发展为艾滋病（AIDS，获得性免疫缺陷综合症），此时免疫系统已严重受损，人体变得无法抵御各种感染。</p><h4 style=\"text-align: start;\">二、HIV如何传播？</h4><p style=\"text-align: start;\">HIV的传播途径主要有以下几种：</p><ol><li style=\"text-align: start;\"><strong>性传播</strong>：通过无保护的性行为，尤其是与HIV阳性者发生性接触时，病毒可能通过精液、阴道分泌物等体液传播。</li><li style=\"text-align: start;\"><strong>血液传播</strong>：通过血液接触传播，如共享针具、接受未经检测的血液或血液制品等。</li><li style=\"text-align: start;\"><strong>母婴传播</strong>：HIV阳性母亲在怀孕、分娩或哺乳过程中，病毒可通过胎盘、分娩或母乳传递给婴儿。</li></ol><p style=\"text-align: start;\">需要特别强调的是，HIV不会通过空气、食物、水、蚊虫叮咬等途径传播。因此，日常接触如握手、拥抱、共用餐具、同居等是完全安全的，不必过度恐慌。</p><h4 style=\"text-align: start;\">三、HIV的症状与检测</h4><p style=\"text-align: start;\">初期感染HIV时，很多人可能并没有明显的症状，甚至多年内没有任何不适。部分人在感染后的2至4周内，可能会出现类似流感的症状，如发热、喉咙痛、肌肉疼痛、头痛、淋巴结肿大等，称为急性HIV感染期。</p><p style=\"text-align: start;\">但是，如果没有及时治疗，HIV会逐渐破坏免疫系统，最终可能导致艾滋病的发生。艾滋病的症状包括持续性高热、体重急剧下降、长期腹泻、持续性咳嗽、反复感染等。</p><p style=\"text-align: start;\">要准确了解自己是否感染了HIV，最有效的方法是进行<strong>HIV检测</strong>。目前，HIV检测技术已经非常成熟，早期诊断可以帮助感染者及时采取抗病毒治疗，控制病毒复制，延缓疾病进展，提高生活质量。</p><h4 style=\"text-align: start;\">四、HIV的预防措施</h4><ol><li style=\"text-align: start;\"><strong>安全性行为</strong>：使用避孕套是预防HIV最有效的措施之一，尤其是在与不确定HIV感染状态的伴侣发生性关系时，务必使用避孕套。</li><li style=\"text-align: start;\"><strong>避免共用针具</strong>：不与他人共用注射针具，特别是在吸毒人群中，要严格避免此类高风险行为。</li><li style=\"text-align: start;\"><strong>血液安全</strong>：确保接受正规医疗机构提供的血液和血液制品，避免接受未经筛查的血液。</li><li style=\"text-align: start;\"><strong>母婴阻断</strong>：HIV阳性孕妇在医生指导下可以采取抗病毒治疗，减少母婴传播的风险。</li></ol><p style=\"text-align: start;\">此外，对于高风险人群，如性工作者、同性恋者、吸毒者等，建议定期进行HIV检测，及时发现问题，采取有效的预防措施。</p><h4 style=\"text-align: start;\">五、治疗与生活管理</h4><p style=\"text-align: start;\">虽然目前尚无根治HIV的疗法，但现代医学已可以通过**抗逆转录病毒治疗（ART）**有效控制病毒，帮助感染者维持良好的生活质量，延长寿命。HIV感染者通过规范的治疗，可以像普通人一样过上健康的生活。</p><p style=\"text-align: start;\">在治疗过程中，患者应遵医嘱坚持服药，定期复查，保持良好的心态和生活习惯，避免过度疲劳，合理饮食，保持足够的休息，增强身体免疫力。</p><h4 style=\"text-align: start;\">六、呼吁：关爱与支持</h4><p style=\"text-align: start;\">HIV感染者和艾滋病患者需要的不仅是治疗，更多的是社会的关爱与支持。我们呼吁全社会共同摒弃歧视和偏见，为感染者提供更多的关怀与帮助，创造一个友善、理解的生活环境。</p><h3 style=\"text-align: start;\">结语</h3><p style=\"text-align: start;\">预防HIV，人人有责。通过科学的了解、合理的预防措施及积极的治疗，我们可以有效减少HIV的传播，保护自己的健康，也为社会贡献力量。让我们携手共同关注HIV防治，建设更加健康和谐的社会。</p><p style=\"text-align: start;\">如有疑问或需要进一步了解HIV防治知识，欢迎前往我院健康咨询中心咨询，我们将为您提供专业的帮助。</p><p style=\"text-align: start;\">感谢大家的关注与支持！</p><p style=\"text-align: start;\"><strong>[医院名称]</strong><br><strong>健康科普办公室</strong><br><strong>发布日期：2024年11月15日</strong></p>','http://localhost:9090/files/download/1756344577789-d5e7223ee74ae4bae2ab5bbfd7ed1ba0.jpeg','2024-11-15 14:38:52',1,1),(13,'禽流感防控与预防知识','<p style=\"text-align: start;\">尊敬的市民朋友们：</p><p style=\"text-align: start;\">为了增强大家的防病意识，普及禽流感的基本知识和防控措施，我们特别发布此份健康科普公告，旨在帮助大家科学认识禽流感，做好预防工作，保护自己和家人的健康。</p><h4 style=\"text-align: start;\">一、什么是禽流感？</h4><p style=\"text-align: start;\">禽流感（Avian Influenza），又称鸟流感，是由禽流感病毒引发的一种急性呼吸道传染病。禽流感病毒通常存在于鸟类体内，尤其是野生鸟类和家禽群体中。禽流感有多种亚型，其中H5N1、H7N9等亚型是较为常见的高致病性禽流感病毒。</p><p style=\"text-align: start;\">禽流感的主要传播途径为鸟类之间的飞行迁徙和接触传播。禽类感染禽流感后，可能出现急性呼吸道症状、腹泻、体重减轻等症状，甚至死亡。而在人类中，禽流感通过与感染禽类的直接接触、食用未经充分加热的禽肉及禽类产品等途径传播。</p><h4 style=\"text-align: start;\">二、禽流感的传播途径</h4><ol><li style=\"text-align: start;\">人与禽类的直接接触：禽流感病毒可通过感染禽类的唾液、鼻涕、羽毛、粪便等体液传播。养殖场工作人员、禽类屠宰工人、农民等与家禽有较多接触的人群，暴露于禽流感病毒的风险较高。</li><li style=\"text-align: start;\">食物传播：未经充分加热的禽肉及禽蛋制品可能存在病毒风险，尤其是食用未煮熟或未彻底加热的禽肉及鸡蛋。</li><li style=\"text-align: start;\">空气传播：通过空气中的飞沫传播也是禽流感病毒在人与人之间传播的途径之一。特别是禽流感病毒在人与人之间传播的情况较少，但仍然不能忽视空气传播的可能性。</li><li style=\"text-align: start;\">间接传播：通过污染的环境、器具、运输工具等物品，病毒也可能传播至其他区域。</li></ol><h4 style=\"text-align: start;\">三、禽流感的症状与诊断</h4><p style=\"text-align: start;\">禽流感感染人类后，症状可能包括：</p><ul><li style=\"text-align: start;\">突发高热、头痛、肌肉疼痛</li><li style=\"text-align: start;\">呼吸困难、咳嗽、喉咙痛</li><li style=\"text-align: start;\">咳痰、流感样症状（如寒战、乏力、食欲减退）</li><li style=\"text-align: start;\">在一些重症病例中，可能出现急性呼吸衰竭、肺炎等并发症。</li></ul><p style=\"text-align: start;\">一旦出现疑似症状，尤其是近期有接触禽类或禽肉产品的历史，建议尽早就医，并进行相关检测。通过实验室检测（如PCR检测），可以确诊是否感染禽流感病毒。</p><h4 style=\"text-align: start;\">四、禽流感的预防措施</h4><ol><li style=\"text-align: start;\">避免与禽类密切接触：尽量避免与活禽、死禽的直接接触，特别是在家禽市场、养殖场等高风险区域。如必须接触禽类，应佩戴口罩、手套，并做好个人卫生防护。</li><li style=\"text-align: start;\">确保禽肉及禽蛋的充分加热：禽肉、禽蛋等产品一定要彻底煮熟或加热，确保肉类温度达到75℃以上，避免食用生禽肉或未煮熟的禽蛋。</li><li style=\"text-align: start;\">加强个人卫生：保持良好的个人卫生习惯，尤其是接触禽类后要及时洗手。避免用脏手触摸口、鼻、眼等部位。</li><li style=\"text-align: start;\">加强空气流通：在禽类养殖场、屠宰场等工作环境中，要注意加强通风和空气流通，减少病毒通过空气传播的机会。</li><li style=\"text-align: start;\">关注健康状况：出现感冒、发热、呼吸道症状时，要及时就医，尤其是有禽类接触史的人员，应告知医生可能的暴露史。</li><li style=\"text-align: start;\">疫苗接种与防护：对于高风险人群（如禽类养殖、屠宰、运输等工作人员），可考虑接种禽流感疫苗（如果有相应的疫苗供应）。在疫情爆发期间，及时关注官方健康部门发布的预防和控制措施。</li></ol><h4 style=\"text-align: start;\">五、禽流感的治疗</h4><p style=\"text-align: start;\">目前，禽流感病毒感染一旦确诊，可以通过抗病毒药物治疗（如奥司他韦、扎那米韦等药物）进行控制。早期诊断和及时治疗对于减轻症状、缩短病程、降低致死率至关重要。因此，一旦出现疑似症状，应尽早就医，避免延误病情。</p><h4 style=\"text-align: start;\">六、社会应对与防控措施</h4><p style=\"text-align: start;\">政府和相关部门会加强对高风险区域和行业的监控，及时处理禽类疫情，开展健康教育与宣传，提醒市民提高防护意识，避免高风险行为。同时，医疗机构也会加强流感监测与病例追踪，做好禽流感的诊断、治疗和防控工作。</p><h4 style=\"text-align: start;\">七、结语</h4><p style=\"text-align: start;\">预防禽流感，人人有责。通过科学的了解、合理的预防措施及及时的治疗，我们可以有效减少禽流感的传播，保护自己和他人的健康。希望大家保持警觉，做好个人防护，尽早识别和报告疑似症状，为防控禽流感贡献一份力量。</p><p style=\"text-align: start;\">如果您有任何问题或需要进一步了解禽流感防治知识，欢迎前往我院健康咨询中心，我们将为您提供专业的帮助与建议。</p><p style=\"text-align: start;\"><strong>[医院名称]</strong><br><strong>健康科普办公室</strong><br><strong>发布日期：2024年11月15日</strong></p>','http://localhost:9090/files/download/1756344546989-4aa6297ebdd917cb99511885a9eea3a6.jpeg','2024-11-15 14:40:05',1,6),(14,'孕期健康管理与注意事项','<p style=\"text-align: start;\">尊敬的准妈妈们：</p><p style=\"text-align: start;\">随着现代医学的发展，孕期健康管理越来越受到重视。为了确保母婴安全，帮助准妈妈们度过一个健康、顺利的孕期，我们特别发布本健康科普公告，旨在为您提供科学的孕期健康指导，让每一位准妈妈都能更好地了解和照顾自己和宝宝的健康。</p><h3 style=\"text-align: start;\">一、孕期的基本变化与注意事项</h3><p style=\"text-align: start;\">怀孕是一个充满期待和变化的过程。随着胎儿的成长，准妈妈的身体会发生一系列的生理变化。了解这些变化，并采取相应的措施，有助于保持孕期的健康。</p><h4 style=\"text-align: start;\">1. 孕期常见的生理变化</h4><ul><li style=\"text-align: start;\">体重变化：孕期体重的增加是正常的，但增加的速度和幅度需要因人而异。一般来说，怀孕的前三个月体重增加较少，后期随着胎儿的成长，体重会逐渐增加。每位准妈妈的体重增加量不同，建议遵循医生的建议，合理控制体重，避免过度增加或体重不足。</li><li style=\"text-align: start;\">荷尔蒙变化：怀孕期间，体内激素水平的变化会影响到各项身体功能。孕妇可能会出现一些常见的症状，如早孕反应（恶心、呕吐）、疲劳、乳房胀痛等。这些症状通常在孕期前三个月较为明显，随着孕期进展，症状会逐渐缓解。</li><li style=\"text-align: start;\">免疫力变化：孕期免疫系统发生了一定的改变，这使得孕妇对某些疾病的抵抗力可能下降。孕妇要特别注意保持良好的个人卫生，避免与传染病患者接触，定期进行产前检查，确保自己和胎儿的健康。</li></ul><h4 style=\"text-align: start;\">2. 孕期的饮食与营养</h4><p style=\"text-align: start;\">怀孕期间，合理的饮食和营养摄入至关重要。胎儿的生长发育、孕妇的健康状况都依赖于充足而均衡的营养。</p><ul><li style=\"text-align: start;\">增加蛋白质摄入：蛋白质是胎儿生长发育的重要营养成分。孕妇应适当增加优质蛋白的摄入，如鱼、禽肉、豆类、蛋类等。</li><li style=\"text-align: start;\">保证钙和铁的摄入：怀孕期间，母体对钙和铁的需求量增加，钙有助于胎儿骨骼和牙齿的发育，而铁则有助于防止贫血。孕妇应多食用富含钙和铁的食物，如奶制品、深绿色蔬菜、红肉、动物肝脏等。</li><li style=\"text-align: start;\">摄入叶酸：叶酸对于胎儿神经管的正常发育至关重要。建议怀孕前及怀孕初期开始补充叶酸，尤其是计划怀孕的女性，应提前进行叶酸的补充。</li><li style=\"text-align: start;\">避免过多的高糖、高脂食物：孕期饮食应尽量避免高糖、高脂肪的食物，避免体重过度增加或引发孕期糖尿病。</li></ul><h4 style=\"text-align: start;\">3. 孕期常见症状与应对</h4><ul><li style=\"text-align: start;\">晨吐：大约50-80%的孕妇在孕早期会出现恶心、呕吐的情况，这被称为“晨吐”。如果症状严重，可以适当调整饮食结构，选择清淡易消化的食物，避免过度疲劳。如果晨吐症状持续严重，建议及时就医，必要时可能需要药物干预。</li><li style=\"text-align: start;\">水肿：怀孕后期，部分准妈妈会出现下肢水肿，尤其是晚上。此时应尽量避免长时间站立，适当休息时抬高双腿，帮助血液循环。穿着舒适的鞋子，避免高跟鞋。</li><li style=\"text-align: start;\">腰背疼痛：随着孕期的推进，孕妇的腹部逐渐增大，可能会出现腰背疼痛。注意保持正确的姿势，避免长时间弯腰、站立。可以适当进行产前瑜伽等舒缓运动。</li></ul><h4 style=\"text-align: start;\">4. 孕期的运动与休息</h4><p style=\"text-align: start;\">适当的运动有助于保持孕妇的身体健康，提高产力，减轻孕期不适。但孕妇应选择温和的运动方式，如散步、孕妇瑜伽、游泳等。避免剧烈运动或有危险的运动项目。</p><p style=\"text-align: start;\">此外，孕期的休息同样重要。孕妇应保证充足的睡眠，避免熬夜，保持良好的作息习惯。如果出现失眠或睡眠质量差的情况，可以尝试调整睡姿或寻求医生的帮助。</p><h3 style=\"text-align: start;\">二、孕期的产前检查</h3><p style=\"text-align: start;\">产前检查是确保母婴健康的重要环节。孕妇应按照医生的建议，定期进行产前检查，及时发现和处理潜在的健康问题。常见的产前检查项目包括：</p><ul><li style=\"text-align: start;\"><strong>血压、体重监测</strong>：检查孕妇的血压、体重变化，确保在正常范围内。</li><li style=\"text-align: start;\"><strong>血常规、尿常规</strong>：检测贫血、尿蛋白等问题。</li><li style=\"text-align: start;\"><strong>胎儿生长发育监测</strong>：通过超声检查、胎心监测等手段，评估胎儿的发育状况。</li><li style=\"text-align: start;\"><strong>糖耐量测试</strong>：评估孕妇是否患有妊娠糖尿病。</li><li style=\"text-align: start;\"><strong>胎儿染色体筛查</strong>：了解胎儿是否有遗传性疾病。</li></ul><h3 style=\"text-align: start;\">三、孕期的心理健康</h3><p style=\"text-align: start;\">孕期的心理变化同样不可忽视。许多准妈妈可能会经历情绪波动、焦虑或压力。建议准妈妈们与家人保持沟通，适时寻求心理支持，保持愉快的心情。</p><h3 style=\"text-align: start;\">结语</h3><p style=\"text-align: start;\">怀孕是一个充满期待与挑战的特殊时期，健康的孕期管理不仅关乎准妈妈的身体健康，也直接影响到宝宝的发育。希望每一位准妈妈都能通过合理的饮食、适度的运动、定期的产检以及积极的心理调适，迎接一个健康、快乐的孕期。</p><p style=\"text-align: start;\">如果您有任何疑问或需要更多健康指导，欢迎随时咨询我们医院的产科专家，我们将竭诚为您提供专业的孕期健康管理服务。</p><p style=\"text-align: start;\"><strong>[医院名称]</strong><br><strong>健康科普办公室</strong><br><strong>发布日期：2024年11月15日</strong></p>','http://localhost:9090/files/download/1756344481629-d30c07a5266e55d4c7afc408434b61a5.jpeg','2024-11-15 14:43:49',1,2),(15,'关爱儿童健康，呵护成长每一步','<p style=\"text-align: start;\">尊敬的家长朋友们：</p><p style=\"text-align: start;\">儿童是祖国的未来，是家庭的希望。为了帮助家长们更好地了解儿童的生长发育特点，科学养育孩子，促进儿童的身心健康，我们特别发布本健康科普公告，旨在提供有关儿童健康的重要知识和指导，助力孩子们在健康、快乐的环境中茁壮成长。</p><h3 style=\"text-align: start;\">一、儿童生长发育的关键期</h3><p style=\"text-align: start;\">儿童的生长发育分为几个重要的阶段，每个阶段都有不同的生理特点和需求。了解这些特点，对于家长科学养育孩子至关重要。</p><h4 style=\"text-align: start;\">1. 婴幼儿期（0-3岁）</h4><p style=\"text-align: start;\">婴幼儿期是孩子身体和智力发展的关键时期。此时，孩子的生理机能尚在逐渐完善，免疫力较弱。家长要特别关注以下几个方面：</p><ul><li style=\"text-align: start;\"><strong>营养</strong>：婴儿期以母乳喂养为主，母乳中的营养成分有助于增强婴儿免疫力，促进大脑发育。如果母乳不足，可以选择适合的婴儿配方奶。辅食添加应从6个月开始，逐步引入多种营养成分，确保孩子的成长需要。</li><li style=\"text-align: start;\"><strong>疫苗接种</strong>：婴幼儿期是接种疫苗的高峰期，家长应按时带孩子接种国家规定的免疫疫苗，以预防常见传染病。</li><li style=\"text-align: start;\"><strong>睡眠与休息</strong>：婴儿期孩子的睡眠时间较长，家长要为孩子提供安静、舒适的睡眠环境，确保充足的休息，有助于身体和大脑的发育。</li></ul><h4 style=\"text-align: start;\">2. 学龄前期（3-6岁）</h4><p style=\"text-align: start;\">这个阶段是儿童自我意识初步建立的时期，也是运动和语言能力快速发展的时期。</p><ul><li style=\"text-align: start;\"><strong>饮食习惯</strong>：学龄前儿童的饮食要营养均衡，尤其要增加蔬菜、水果、蛋白质的摄入，减少糖分和高脂肪食物的摄入，培养孩子良好的饮食习惯。</li><li style=\"text-align: start;\"><strong>运动与锻炼</strong>：鼓励孩子进行户外活动，增强体质。此时孩子的运动能力和协调性发展较快，家长可以通过跑步、跳跃、踢球等活动提高孩子的运动能力。</li><li style=\"text-align: start;\"><strong>语言发展</strong>：学龄前孩子是语言学习的黄金期，家长应多与孩子进行语言交流，鼓励孩子表达自己的想法，增强语言能力。</li></ul><h4 style=\"text-align: start;\">3. 学龄期（6-12岁）</h4><p style=\"text-align: start;\">学龄期孩子进入学校学习阶段，身心发展趋向成熟，此时的成长重点主要在于智力、情感和社交能力的培养。</p><ul><li style=\"text-align: start;\"><strong>学习与认知发展</strong>：家长要关注孩子的学习兴趣，帮助他们养成良好的学习习惯。同时，要注重孩子的综合素质发展，如艺术、体育等方面的培养。</li><li style=\"text-align: start;\"><strong>心理健康</strong>：学龄期是孩子逐步接触社会的阶段，可能会面临学业压力和人际交往的问题。家长要关注孩子的情绪变化，及时给予关爱和支持，帮助孩子建立自信心和抗压能力。</li></ul><h3 style=\"text-align: start;\">二、儿童常见健康问题及预防</h3><p style=\"text-align: start;\">在孩子的成长过程中，家长需要时刻关注孩子的健康问题，及时发现并处理潜在的健康隐患。</p><h4 style=\"text-align: start;\">1. 呼吸道疾病</h4><p style=\"text-align: start;\">儿童的免疫系统尚未完全发育，容易受到病毒和细菌的侵袭。常见的呼吸道疾病包括感冒、咳嗽、流感等。</p><ul><li style=\"text-align: start;\"><strong>预防措施</strong>：保持室内空气流通，避免孩子长时间处于空气污染较严重的环境中；外出时，尽量避免让孩子与有呼吸道感染的人员接触；加强孩子的体质，适度运动，增强免疫力。</li></ul><h4 style=\"text-align: start;\">2. 过敏性疾病</h4><p style=\"text-align: start;\">过敏性鼻炎、哮喘、湿疹等过敏性疾病在儿童中较为常见，常由环境因素或遗传因素引起。</p><ul><li style=\"text-align: start;\"><strong>预防措施</strong>：避免接触过敏原，如花粉、尘螨、宠物等；家长应关注孩子是否有过敏症状，并及时就医，避免病情加重。</li></ul><h4 style=\"text-align: start;\">3. 近视</h4><p style=\"text-align: start;\">随着电子产品使用时间的增加，儿童近视问题日益严重，影响了孩子的视力和整体健康。</p><ul><li style=\"text-align: start;\"><strong>预防措施</strong>：家长应控制孩子使用电子产品的时间，鼓励孩子进行户外活动，增加眼睛的自然调节；同时，定期带孩子进行视力检查，确保及时发现并纠正近视问题。</li></ul><h4 style=\"text-align: start;\">4. 口腔健康</h4><p style=\"text-align: start;\">儿童口腔健康问题也不容忽视，蛀牙和牙齿排列不齐是常见问题。</p><ul><li style=\"text-align: start;\"><strong>预防措施</strong>：保持良好的口腔卫生习惯，鼓励孩子每天刷牙两次，避免过多食用糖分高的食物；定期带孩子进行口腔检查，及时发现口腔问题。</li></ul><h3 style=\"text-align: start;\">三、家长的陪伴与教育</h3><p style=\"text-align: start;\">除了身体健康，孩子的心理健康和教育也是至关重要的。家长在孩子成长过程中应做好榜样，给孩子提供一个温暖、支持的环境，鼓励他们独立思考，培养积极乐观的心态。通过亲子互动，家长可以帮助孩子更好地适应社会，提升情感智力。</p><h3 style=\"text-align: start;\">结语</h3><p style=\"text-align: start;\">孩子的健康是家庭幸福的基石，是社会未来的希望。希望家长们能通过科学的养育方式，关注孩子的身体和心理健康，让孩子在健康、快乐的环境中茁壮成长。如果您有任何关于儿童健康的问题或困惑，欢迎随时咨询我们的儿科专家，我们将竭诚为您提供专业的健康指导和帮助。</p><p style=\"text-align: start;\"><strong>[医院名称]</strong><br><strong>健康科普办公室</strong><br><strong>发布日期：2024年11月15日</strong></p>','http://localhost:9090/files/download/1756344453263-a5010a9d08543f56d5f2887ba4480806.jpeg','2024-11-15 14:46:19',1,10);
/*!40000 ALTER TABLE `information` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medical_literature`
--

DROP TABLE IF EXISTS `medical_literature`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medical_literature` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文献标题',
  `authors` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '作者',
  `journal` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '期刊名称',
  `publish_date` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发布日期',
  `abstract_content` text COLLATE utf8mb4_unicode_ci COMMENT '摘要内容',
  `keywords` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关键词',
  `category` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类',
  `source_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '原始链接',
  `pdf_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'PDF下载链接',
  `doi` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'DOI号',
  `view_count` int DEFAULT '0' COMMENT '浏览次数',
  `download_count` int DEFAULT '0' COMMENT '下载次数',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'active' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `crawl_source` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '爬取来源',
  `language` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '语言',
  `impact` double DEFAULT NULL COMMENT '影响因子',
  `tags` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_doi` (`doi`),
  KEY `idx_category` (`category`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_view_count` (`view_count`),
  KEY `idx_title` (`title`(191)),
  FULLTEXT KEY `idx_fulltext` (`title`,`abstract_content`,`keywords`)
) ENGINE=InnoDB AUTO_INCREMENT=110 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医疗文献表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medical_literature`
--

LOCK TABLES `medical_literature` WRITE;
/*!40000 ALTER TABLE `medical_literature` DISABLE KEYS */;
/*!40000 ALTER TABLE `medical_literature` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notice`
--

DROP TABLE IF EXISTS `notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notice` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '公告内容',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发布时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='系统公告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notice`
--

LOCK TABLES `notice` WRITE;
/*!40000 ALTER TABLE `notice` DISABLE KEYS */;
INSERT INTO `notice` VALUES (1,'不冰的彬彬','今天好开心呀!','2024-11-6 15:51:17'),(2,'吃饭了吗','吃饱了','2024-11-6 15:52:20'),(3,'你好你好','开始学习','2024-11-6 21:22:33'),(5,'考试加油','考试加油','2024-11-08 21:47:52'),(6,'知识分子','123','2024-11-13 14:57:55'),(7,'123','258','2024-11-27 22:27:25');
/*!40000 ALTER TABLE `notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `office`
--

DROP TABLE IF EXISTS `office`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `office` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '科室名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科室信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `office`
--

LOCK TABLES `office` WRITE;
/*!40000 ALTER TABLE `office` DISABLE KEYS */;
INSERT INTO `office` VALUES (1,'外科'),(2,'内科'),(3,'儿科'),(4,'妇产科'),(5,'眼科'),(6,'皮肤科'),(7,'耳鼻喉科');
/*!40000 ALTER TABLE `office` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `physical_examination`
--

DROP TABLE IF EXISTS `physical_examination`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `physical_examination` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT '项目名称',
  `english_name` varchar(255) DEFAULT NULL COMMENT '项目英文名称',
  `cover` varchar(255) DEFAULT NULL COMMENT '项目封面',
  `examination_type_id` int DEFAULT NULL COMMENT '项目类型id',
  `content` varchar(1000) DEFAULT NULL COMMENT '项目内容',
  `people` varchar(255) DEFAULT NULL COMMENT '适宜人群',
  `purpose` varchar(1000) DEFAULT NULL COMMENT '检测目的',
  `office_id` int DEFAULT NULL COMMENT '所属科室',
  `money` int DEFAULT NULL COMMENT '费用',
  `doctor_id` int DEFAULT NULL COMMENT '负责医生',
  `attention` varchar(1000) DEFAULT NULL COMMENT '注意事项',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='普通体检项目信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `physical_examination`
--

LOCK TABLES `physical_examination` WRITE;
/*!40000 ALTER TABLE `physical_examination` DISABLE KEYS */;
INSERT INTO `physical_examination` VALUES (1,'眼科检查','Ophthalmologic examination','http://localhost:9090/files/download/1756344347647-3925fa6d29f419c7a378fa2575182531.jpeg',6,'视力测试。\n眼底检查。\n眼压测量。\n色盲测试。\n角膜曲率测量。','成人','及早发现眼疾：通过系统的眼科检查，及早发现近视、远视、白内障、青光眼等常见眼病，避免病情恶化。\n提高视觉健康：通过定期检查，保持眼部健康，预防视力下降或其他眼科疾病。\n定期监测：对于已有眼病或有家族史的患者，定期眼科检查有助于病情的监测与控制。',5,300,4,'佩戴眼镜：如果您平时佩戴眼镜或隐形眼镜，请带上，便于检查时校正视力。\n禁食要求：部分眼部检查可能要求空腹，特别是如果需要做眼部影像学检查时。\n防止过度疲劳：检查前请避免长时间用眼，确保眼部处于放松状态。','中国广东省深圳市南山区科技大道88号\n'),(2,'牙科检查','Dental examination','http://localhost:9090/files/download/1756344301953-6ac774378c58cf22b3b1b0ca0d67253c.jpeg',6,'口腔检查。\n牙齿X光检查。\n牙周检查。\n牙齿清洁。','青少年及成人','口腔健康评估：通过检查口腔、牙齿和牙龈，评估牙齿的健康状况，及时发现龋齿、牙周炎等问题。\n牙齿清洁：定期的牙科检查有助于去除牙石和牙菌斑，减少口腔疾病的发生。\n预防疾病：通过及时发现问题，避免发展成更严重的牙齿和口腔疾病。',3,500,2,'刷牙记录：请在检查前保持良好的口腔卫生，定期刷牙，避免过度饮食糖分过多食物。\n特殊病史：如果有口腔溃疡、牙齿矫正等特殊情况，请提前告知牙科医生。\n检查前禁食：部分检查可能需要禁食，请与诊所确认是否有禁食要求。','中国广东省深圳市南山区科技大道88号\n'),(3,'妇科检查','Gynecological examination','http://localhost:9090/files/download/1756344272159-c6b4852f596b1a33415063526c59a315.jpeg',9,'乳腺检查。\n宫颈涂片（Pap smear）。\n盆腔超声波检查。\n子宫和卵巢检查。','女性','妇科疾病早期筛查：及时发现乳腺癌、宫颈癌等女性常见疾病，确保尽早治疗。\n了解女性生殖健康：评估子宫、卵巢及其他生殖器官的健康状况，指导相关治疗与预防措施。\n定期检查：定期进行妇科检查有助于监测生殖系统的健康，减少潜在疾病的发生。',4,800,5,'月经周期：建议选择月经期结束后几天进行检查，这时检查结果较为准确。\n穿着建议：建议穿着宽松衣物，方便进行盆腔检查。\n告知病史：如有妇科疾病史或怀孕史，请提前告知医生，以便进行针对性检查。','中国广东省深圳市南山区科技大道88号\n'),(4,'皮肤科检查','Dermatological examination','http://localhost:9090/files/download/1756344197308-aeb26253518f530eb1dd5fefb0affd50.jpeg',6,'皮肤检查。\n皮肤病史评估。\n皮肤病变切除检查。','所有人群','皮肤疾病早期筛查：通过检查皮肤上的斑点、痣、疣等，早期发现皮肤癌等重大疾病。\n皮肤健康评估：评估皮肤的整体健康状况，及时发现皮肤病，如湿疹、银屑病、皮肤过敏等。\n制定护理方案：根据检查结果，制定个性化的皮肤护理和防晒方案。',6,400,4,'日晒防护：如需进行皮肤检查，请避免在检查前两天日晒过度。\n保留症状记录：如有皮肤病变或不适，提前记录并带到检查现场。\n舒适衣物：穿着宽松、舒适的衣物，便于进行全身皮肤检查。','中国广东省深圳市南山区科技大道88号\n'),(5,'耳鼻喉科检查','ENT examination','http://localhost:9090/files/download/1756344127255-60a6383977500703ca92764ea426379e.jpeg',8,'耳朵听力检查。\n鼻部内窥镜检查。\n喉部检查。\n咽喉镜检查。','所有人群','耳鼻喉疾病筛查：检查耳朵、鼻腔、喉咙的健康状态，及时发现耳鸣、鼻炎、咽喉感染等问题。\n早期发现疾病：通过耳鼻喉检查，及早发现可能的耳聋、鼻窦炎、喉癌等疾病。\n改善生活质量：及时治疗耳鼻喉疾病，避免影响生活质量，减少不适症状。',7,350,4,'耳鼻喉科病史：如有耳鸣、耳聋、鼻塞、咳嗽等症状，建议详细记录并告知医生。\n检查时放松：检查过程中放松，避免紧张影响结果。\n禁食建议：部分检查可能需要空腹，检查前请与诊所确认。','中国广东省深圳市南山区科技大道88号\n'),(6,'心电图检查','Electrocardiogram','http://localhost:9090/files/download/1756344088100-6fca739e7d13a5a70d3c5344d356030e.jpeg',6,'心电图检查。\n心率、心律监测。\n心脏功能评估。','成人','检测心脏健康：通过心电图检查，及时发现心脏病、心律不齐等问题。\n早期诊断心血管疾病：通过监测心脏电活动，帮助医生早期识别心脏病的迹象。\n预防心血管疾病：定期检查可以帮助控制潜在的心血管风险因素，避免疾病发展。',2,350,4,'检查前休息：在检查前保持平静，避免剧烈运动，以确保检查结果准确。\n药物影响：若正在服用任何心脏药物，请告知医生。\n避免进食：部分检查可能要求空腹，请在检查前与诊所确认是否需要禁食。','中国广东省深圳市南山区科技大道88号\n'),(7,'肺功能检查','Pulmonary Function Test','http://localhost:9090/files/download/1756344052688-5b2ea3c6bfb973172b65044a9dd4e228.jpeg',7,'肺活量测定。\n呼气峰流速测定。\n气体交换能力测试。','所有人群','评估肺部健康：通过肺功能检查，评估肺部的气体交换能力及肺活量，及时发现肺部问题。\n早期检测呼吸系统疾病：通过检查，及早识别如哮喘、慢性阻塞性肺病等呼吸系统疾病。\n改善呼吸健康：为改善生活质量，尤其是呼吸系统疾病患者，制定有效的治疗和预防方案。',2,500,7,'穿着建议：请穿宽松衣物，方便进行肺活量测试。\n检查前禁烟：检查前两小时内避免吸烟。\n禁食要求：检查前保持空腹，确保结果的准确性。','中国广东省深圳市南山区科技大道88号\n'),(8,'胃肠镜检查','Gastrointestinal Endoscopy','http://localhost:9090/files/download/1756344010413-0cf1c611604d23cf006519ef53cf8534.jpeg',6,'胃镜检查。\n肠镜检查。\n胃肠道病变评估。','成人，特别是有胃肠病史的人群','筛查胃肠道疾病：通过胃肠镜检查，及时发现胃炎、胃溃疡、肠癌等疾病。\n早期发现癌症：胃肠镜检查有助于早期发现胃癌、食道癌、结肠癌等恶性肿瘤。\n改善消化系统健康：根据检查结果，提供个性化的治疗建议，改善胃肠功能。',2,1200,1,'检查前禁食：通常需要空腹进行检查，请确保检查前12小时内不进食。\n药物使用：如有使用抗凝药物等，请在检查前告知医生。\n术后护理：检查后可能需要休息，避免剧烈运动，遵照医生建议。','中国广东省深圳市南山区科技大道88号\n'),(9,'肝功能检查','Liver Function Test','http://localhost:9090/files/download/1756343944490-c00c46fc085115bd7415457c8409f838.jpeg',6,'肝酶检测。\n肝脏功能评估。\n病毒性肝炎筛查。','所有人群，特别是有肝病风险的人群','肝脏健康评估：通过肝功能检查，评估肝脏的健康状况，及时发现肝脏损伤或疾病。\n筛查病毒性肝炎：检测肝炎病毒感染，及早治疗避免病情恶化。\n预防肝病：通过定期检查，减少肝脏疾病的发生，保护肝脏功能。',2,400,6,'空腹要求：请在检查前至少8小时保持空腹状态。\n药物影响：如有正在使用药物，请提前告知医生。\n健康饮食：检查前尽量避免高脂肪、高糖分饮食，保持肝脏健康。','中国广东省深圳市南山区科技大道88号\n'),(10,'血糖检查','Blood Glucose Test','http://localhost:9090/files/download/1756343888569-ba225d1480499dc22db1c4990299684f.jpeg',6,'空腹血糖测试。\n餐后血糖测试。\n糖耐量测试。','成人，特别是有糖尿病风险的人群','早期筛查糖尿病：通过血糖检查，及时发现血糖异常，诊断糖尿病及其并发症。\n预防糖尿病：通过定期监测血糖水平，帮助控制血糖，预防糖尿病的发生。\n改善生活方式：根据血糖检查结果，制定个性化的饮食和运动方案，控制血糖水平。',2,150,1,'禁食要求：检查前需空腹8小时，确保测试结果准确。\n药物影响：如有使用降糖药物，请在检查前告知医生。\n避免剧烈运动：检查前一天避免剧烈运动，影响测试结果。','中国广东省深圳市南山区科技大道88号\n');
/*!40000 ALTER TABLE `physical_examination` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `title`
--

DROP TABLE IF EXISTS `title`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `title` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '职称名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职称信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `title`
--

LOCK TABLES `title` WRITE;
/*!40000 ALTER TABLE `title` DISABLE KEYS */;
INSERT INTO `title` VALUES (1,'主治医师'),(2,'副主任医师'),(3,'主任医师'),(4,'住院医师'),(5,'医师');
/*!40000 ALTER TABLE `title` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '角色',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电话',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='管理员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'bin','321','周伟帅哥','http://localhost:9090/files/download/1754882408467-1.jpg','USER','110','110@qq,com'),(2,'cst','123','250','http://localhost:9090/files/download/1733823129619-1.jpg','USER','250','250@qq.com'),(3,'blue','123','blue','http://localhost:9090/files/download/1732191289804-1.jpg','USER',NULL,NULL),(4,'binnn','123123123a','binbin',NULL,'USER','15111095528','213@qq.com');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-02 17:29:42
