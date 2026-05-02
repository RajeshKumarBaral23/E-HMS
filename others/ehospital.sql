-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: ehealth
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admission`
--

DROP TABLE IF EXISTS `admission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admission_date` date DEFAULT NULL,
  `discharge_date` date DEFAULT NULL,
  `room_charge_per_day` double DEFAULT NULL,
  `bed_id` bigint DEFAULT NULL,
  `patient_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmj5ink9eeapgq55k6yrabtato` (`bed_id`),
  KEY `FKlr4biw9gturjj6tmgrbhcvj9d` (`patient_id`),
  CONSTRAINT `FKlr4biw9gturjj6tmgrbhcvj9d` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`id`),
  CONSTRAINT `FKmj5ink9eeapgq55k6yrabtato` FOREIGN KEY (`bed_id`) REFERENCES `bed` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admission`
--

LOCK TABLES `admission` WRITE;
/*!40000 ALTER TABLE `admission` DISABLE KEYS */;
/*!40000 ALTER TABLE `admission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admissions`
--

DROP TABLE IF EXISTS `admissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admit_date` datetime(6) DEFAULT NULL,
  `bed_number` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `discharge_date` datetime(6) DEFAULT NULL,
  `room_charge_per_day` double DEFAULT NULL,
  `status` enum('ACTIVE','DISCHARGED') DEFAULT NULL,
  `appointment_id` bigint DEFAULT NULL,
  `doctor_id` bigint DEFAULT NULL,
  `patient_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKie3dc2501jf0uf650423sbcll` (`appointment_id`),
  KEY `FK55c5lbeov6h3wtv6gkc25arm` (`doctor_id`),
  KEY `FKtlufssykm7ejvgusfmo813iq` (`patient_id`),
  CONSTRAINT `FK54kby34kohmlfscpini97t0tv` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`id`),
  CONSTRAINT `FK55c5lbeov6h3wtv6gkc25arm` FOREIGN KEY (`doctor_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKtlufssykm7ejvgusfmo813iq` FOREIGN KEY (`patient_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admissions`
--

LOCK TABLES `admissions` WRITE;
/*!40000 ALTER TABLE `admissions` DISABLE KEYS */;
/*!40000 ALTER TABLE `admissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `appointments`
--

DROP TABLE IF EXISTS `appointments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `appointments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `appointment_time` datetime(6) DEFAULT NULL,
  `duration_minutes` int DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `doctor_id` bigint DEFAULT NULL,
  `patient_id` bigint DEFAULT NULL,
  `check_in_time` datetime(6) DEFAULT NULL,
  `consultation_end_time` datetime(6) DEFAULT NULL,
  `consultation_start_time` datetime(6) DEFAULT NULL,
  `follow_up_date` date DEFAULT NULL,
  `queue_number` int DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'PENDING',
  PRIMARY KEY (`id`),
  KEY `FK6u6s6egu60m2cbdjno44jbipa` (`doctor_id`),
  KEY `FKopb2h9yhin1rb4dqote8bws6w` (`patient_id`),
  CONSTRAINT `FK6u6s6egu60m2cbdjno44jbipa` FOREIGN KEY (`doctor_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKopb2h9yhin1rb4dqote8bws6w` FOREIGN KEY (`patient_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appointments`
--

LOCK TABLES `appointments` WRITE;
/*!40000 ALTER TABLE `appointments` DISABLE KEYS */;
INSERT INTO `appointments` VALUES (1,'2026-04-25 10:00:00.000000',30,'Test initial booking',3,4,NULL,NULL,NULL,NULL,1,'CANCELLED'),(2,'2026-04-26 11:00:00.000000',30,'Test rescheduled booking',3,4,'2026-04-28 22:47:04.462175','2026-04-28 22:47:24.405852','2026-04-28 22:47:13.915091',NULL,1,'COMPLETED'),(3,'2026-04-25 10:00:00.000000',30,'Test initial booking',3,4,NULL,NULL,NULL,NULL,2,'CANCELLED'),(4,'2026-04-26 11:00:00.000000',30,'Test rescheduled booking',3,4,NULL,NULL,NULL,NULL,2,'CONFIRMED'),(5,'2026-04-23 14:49:00.000000',30,'check up',3,4,NULL,NULL,NULL,NULL,1,'CONFIRMED'),(6,'2026-04-25 01:40:00.000000',30,'check',18,16,NULL,NULL,NULL,NULL,1,'CONFIRMED'),(7,'2026-04-26 08:08:00.000000',15,'sdf',18,29,NULL,NULL,NULL,NULL,1,'PENDING'),(8,'2026-04-25 20:08:00.000000',30,'fb',18,29,NULL,NULL,NULL,NULL,2,'PENDING'),(9,'2026-04-28 10:30:00.000000',30,'Automated test booking',18,33,'2026-04-27 08:28:43.463148',NULL,'2026-04-27 08:28:43.542935',NULL,1,'IN_PROGRESS');
/*!40000 ALTER TABLE `appointments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `availability_slots`
--

DROP TABLE IF EXISTS `availability_slots`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `availability_slots` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `end_date_time` datetime(6) DEFAULT NULL,
  `start_date_time` datetime(6) DEFAULT NULL,
  `doctor_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5v4ab9hx2i24g3q6bw7q1494f` (`doctor_id`),
  CONSTRAINT `FK5v4ab9hx2i24g3q6bw7q1494f` FOREIGN KEY (`doctor_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `availability_slots`
--

LOCK TABLES `availability_slots` WRITE;
/*!40000 ALTER TABLE `availability_slots` DISABLE KEYS */;
INSERT INTO `availability_slots` VALUES (1,_binary '','2026-04-24 21:35:30.991118','2026-04-26 21:35:00.000000','2026-04-24 21:35:00.000000',18),(2,_binary '','2026-04-27 08:27:45.114722','2026-04-28 16:00:00.000000','2026-04-28 10:00:00.000000',18);
/*!40000 ALTER TABLE `availability_slots` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bed`
--

DROP TABLE IF EXISTS `bed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bed` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `bed_number` varchar(255) DEFAULT NULL,
  `occupied` bit(1) NOT NULL,
  `ward` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bed`
--

LOCK TABLES `bed` WRITE;
/*!40000 ALTER TABLE `bed` DISABLE KEYS */;
/*!40000 ALTER TABLE `bed` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `billings`
--

DROP TABLE IF EXISTS `billings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `billings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `appointment_id` bigint DEFAULT NULL,
  `consultation_fee` double DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `medicine_cost` double DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `total_amount` double DEFAULT NULL,
  `payment_status` enum('PAID','UNPAID') DEFAULT NULL,
  `lab_cost` double DEFAULT NULL,
  `paid_at` datetime(6) DEFAULT NULL,
  `payment_method` varchar(255) DEFAULT NULL,
  `payment_reference` varchar(255) DEFAULT NULL,
  `room_charge` double DEFAULT NULL,
  `billing_status` enum('CANCELLED','DRAFT','PAID','REFUNDED','UNPAID') DEFAULT NULL,
  `created_by_id` bigint DEFAULT NULL,
  `finalized_at` datetime(6) DEFAULT NULL,
  `finalized_by_id` bigint DEFAULT NULL,
  `notes` longtext,
  `admission_id` bigint DEFAULT NULL,
  `lab_charges` double DEFAULT NULL,
  `patient_id` bigint DEFAULT NULL,
  `room_charges` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `billings`
--

LOCK TABLES `billings` WRITE;
/*!40000 ALTER TABLE `billings` DISABLE KEYS */;
INSERT INTO `billings` VALUES (1,9,100,'2026-04-27 08:30:44.477891',0,'PAID',100,'PAID',0,'2026-04-27 08:31:00.080441','CARD','TEST123',0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `billings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `departments`
--

DROP TABLE IF EXISTS `departments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `departments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `departments`
--

LOCK TABLES `departments` WRITE;
/*!40000 ALTER TABLE `departments` DISABLE KEYS */;
INSERT INTO `departments` VALUES (1,'Heart and cardiovascular disease treatment','Cardiology','555-2001'),(2,'Child health and development specialists','Pediatrics','555-2002'),(3,'Bone and joint disorder specialists','Orthopedics','555-2003'),(4,'Skin condition and disorder treatment','Dermatology','555-2004'),(5,'Surgical procedures and interventions','General Surgery','555-2005');
/*!40000 ALTER TABLE `departments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `discharge_summaries`
--

DROP TABLE IF EXISTS `discharge_summaries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `discharge_summaries` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `discharge_date` datetime(6) DEFAULT NULL,
  `follow_up_date` date DEFAULT NULL,
  `instructions` longtext,
  `summary` longtext,
  `appointment_id` bigint DEFAULT NULL,
  `doctor_id` bigint DEFAULT NULL,
  `patient_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbsem5hv93jwe36y5dns139rtg` (`appointment_id`),
  KEY `FKog969iptfb9ea49nb77dn0xx5` (`doctor_id`),
  KEY `FKano60sfjjmw3a9hraj4wv71mq` (`patient_id`),
  CONSTRAINT `FKano60sfjjmw3a9hraj4wv71mq` FOREIGN KEY (`patient_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKbsem5hv93jwe36y5dns139rtg` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`id`),
  CONSTRAINT `FKog969iptfb9ea49nb77dn0xx5` FOREIGN KEY (`doctor_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `discharge_summaries`
--

LOCK TABLES `discharge_summaries` WRITE;
/*!40000 ALTER TABLE `discharge_summaries` DISABLE KEYS */;
/*!40000 ALTER TABLE `discharge_summaries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `doctors`
--

DROP TABLE IF EXISTS `doctors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doctors` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `bio` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `specialization` varchar(255) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `department_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKt1f6cueqyjwx5ghew9ar1exe3` (`user_id`),
  KEY `FKl2mro81neln9topymd898urh1` (`department_id`),
  CONSTRAINT `FKe9pf5qtxxkdyrwibaevo9frtk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKl2mro81neln9topymd898urh1` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doctors`
--

LOCK TABLES `doctors` WRITE;
/*!40000 ALTER TABLE `doctors` DISABLE KEYS */;
INSERT INTO `doctors` VALUES (1,'Experienced cardiologist with 10+ years of practice','+1-555-0100','Cardiology',3,NULL),(12,'','123456789','skin',18,5),(13,'Experienced interventional cardiologist with 15 years of practice','555-0101','Interventional Cardiology',19,1),(14,'Specialist in congenital heart diseases','555-0102','Pediatric Cardiology',20,1),(16,'Specializes in newborn care','555-0104','Neonatology',22,2),(17,'Expert in bone and joint disorders','555-0105','Orthopedic Surgery',23,3),(18,'Specializes in sports-related injuries','555-0106','Sports Medicine',24,3),(19,'Treats skin conditions and disorders','555-0107','Medical Dermatology',25,4),(20,'Specialist in aesthetic skin procedures','555-0108','Cosmetic Dermatology',26,4),(21,'Experienced surgical specialist','555-0109','General Surgery',27,5),(23,'Expert in child health and development','555-0103','General Pediatrics',34,2),(24,'Expert in minimally invasive procedures','555-0110','Laparoscopic Surgery',35,5);
/*!40000 ALTER TABLE `doctors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file_attachment`
--

DROP TABLE IF EXISTS `file_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `file_attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file_name` varchar(255) DEFAULT NULL,
  `file_type` varchar(255) DEFAULT NULL,
  `file_url` varchar(255) DEFAULT NULL,
  `related_id` bigint DEFAULT NULL,
  `related_type` varchar(255) DEFAULT NULL,
  `uploaded_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_attachment`
--

LOCK TABLES `file_attachment` WRITE;
/*!40000 ALTER TABLE `file_attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `file_attachment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lab_results`
--

DROP TABLE IF EXISTS `lab_results`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lab_results` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `result` longtext,
  `test_type` enum('BLOOD','OTHER','ULTRASOUND','XRAY') DEFAULT NULL,
  `appointment_id` bigint DEFAULT NULL,
  `doctor_id` bigint DEFAULT NULL,
  `patient_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKeup09xlfgas3rj5onn3nkos7m` (`appointment_id`),
  KEY `FKe1vlfktvlprqiwungd14se93u` (`doctor_id`),
  KEY `FK55q0exk2el524taomffmsd0nr` (`patient_id`),
  CONSTRAINT `FK55q0exk2el524taomffmsd0nr` FOREIGN KEY (`patient_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKe1vlfktvlprqiwungd14se93u` FOREIGN KEY (`doctor_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKeup09xlfgas3rj5onn3nkos7m` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lab_results`
--

LOCK TABLES `lab_results` WRITE;
/*!40000 ALTER TABLE `lab_results` DISABLE KEYS */;
/*!40000 ALTER TABLE `lab_results` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medical_records`
--

DROP TABLE IF EXISTS `medical_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medical_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `diagnosis` varchar(255) DEFAULT NULL,
  `notes` longtext,
  `treatment` varchar(255) DEFAULT NULL,
  `visit_date` datetime(6) DEFAULT NULL,
  `doctor_id` bigint DEFAULT NULL,
  `patient_id` bigint DEFAULT NULL,
  `appointment_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkew56covm00kqia866qlo6n4` (`doctor_id`),
  KEY `FKe3g9v0pbec2843wd1rxeb0is3` (`patient_id`),
  KEY `FKifeec8p5v06rt258odelw8s7j` (`appointment_id`),
  CONSTRAINT `FKe3g9v0pbec2843wd1rxeb0is3` FOREIGN KEY (`patient_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKifeec8p5v06rt258odelw8s7j` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`id`),
  CONSTRAINT `FKkew56covm00kqia866qlo6n4` FOREIGN KEY (`doctor_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medical_records`
--

LOCK TABLES `medical_records` WRITE;
/*!40000 ALTER TABLE `medical_records` DISABLE KEYS */;
INSERT INTO `medical_records` VALUES (1,'2026-04-27 08:28:18.715400','Automated test diagnosis','Completed via automated test','Rest, fluids, and follow-up','2026-04-27 08:28:18.706959',18,33,9),(2,'2026-04-27 08:28:43.618731','Automated final diagnosis','Finalized by automated test','Rest and prescribed meds','2026-04-27 08:28:43.604772',18,33,9);
/*!40000 ALTER TABLE `medical_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medicines`
--

DROP TABLE IF EXISTS `medicines`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medicines` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` longtext,
  `name` varchar(255) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medicines`
--

LOCK TABLES `medicines` WRITE;
/*!40000 ALTER TABLE `medicines` DISABLE KEYS */;
INSERT INTO `medicines` VALUES (1,'','sgsf4',23,55);
/*!40000 ALTER TABLE `medicines` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `patients`
--

DROP TABLE IF EXISTS `patients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patients` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK9tbsl3fmey0eofbm2xj69v4qs` (`user_id`),
  CONSTRAINT `FKuwca24wcd1tg6pjex8lmc0y7` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patients`
--

LOCK TABLES `patients` WRITE;
/*!40000 ALTER TABLE `patients` DISABLE KEYS */;
INSERT INTO `patients` VALUES (1,'123 Main St, Springfield',NULL,'+1-555-0200',4);
/*!40000 ALTER TABLE `patients` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pharmacy_records`
--

DROP TABLE IF EXISTS `pharmacy_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pharmacy_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dispensed_at` datetime(6) DEFAULT NULL,
  `medicine_id` bigint DEFAULT NULL,
  `prescription_id` bigint DEFAULT NULL,
  `quantity_dispensed` int DEFAULT NULL,
  `appointment_id` bigint DEFAULT NULL,
  `patient_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pharmacy_records`
--

LOCK TABLES `pharmacy_records` WRITE;
/*!40000 ALTER TABLE `pharmacy_records` DISABLE KEYS */;
/*!40000 ALTER TABLE `pharmacy_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prescription_medicines`
--

DROP TABLE IF EXISTS `prescription_medicines`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prescription_medicines` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dosage` varchar(255) DEFAULT NULL,
  `duration_days` int DEFAULT NULL,
  `instructions` longtext,
  `medicine_id` bigint DEFAULT NULL,
  `prescription_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkljsa0768eycowt9le7nb215q` (`medicine_id`),
  KEY `FK14vcxytfp7rr81y9fdhl37bbw` (`prescription_id`),
  CONSTRAINT `FK14vcxytfp7rr81y9fdhl37bbw` FOREIGN KEY (`prescription_id`) REFERENCES `prescriptions` (`id`),
  CONSTRAINT `FKkljsa0768eycowt9le7nb215q` FOREIGN KEY (`medicine_id`) REFERENCES `medicines` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prescription_medicines`
--

LOCK TABLES `prescription_medicines` WRITE;
/*!40000 ALTER TABLE `prescription_medicines` DISABLE KEYS */;
/*!40000 ALTER TABLE `prescription_medicines` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prescriptions`
--

DROP TABLE IF EXISTS `prescriptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prescriptions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `medications` longtext,
  `notes` longtext,
  `appointment_id` bigint DEFAULT NULL,
  `doctor_id` bigint DEFAULT NULL,
  `patient_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr0yn695qg51gn0iskc8p0h2ii` (`appointment_id`),
  KEY `FK2hdpvkpjjx3plf21194oxjskt` (`doctor_id`),
  KEY `FK7sia9wnwh9j5hwrta9k8q0rbq` (`patient_id`),
  CONSTRAINT `FK2hdpvkpjjx3plf21194oxjskt` FOREIGN KEY (`doctor_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FK7sia9wnwh9j5hwrta9k8q0rbq` FOREIGN KEY (`patient_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKe2fpvlkkcgcd40k4ufyyju2al` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prescriptions`
--

LOCK TABLES `prescriptions` WRITE;
/*!40000 ALTER TABLE `prescriptions` DISABLE KEYS */;
INSERT INTO `prescriptions` VALUES (1,'2026-04-23 14:51:28.872920','55f\nhh\njj\n','',5,3,4),(2,'2026-04-27 08:28:18.753821','Paracetamol 500mg; 1 tablet every 6 hours as needed','Take with food',9,18,33);
/*!40000 ALTER TABLE `prescriptions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` enum('ADMIN','DOCTOR','PATIENT') DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin@ehealth.com','Admin User','$2a$10$LSf7yzii.YERiCQ638Rz5unpFSDum.UZNf2a6UcYi8YDb4Zl6uQz2','ADMIN'),(3,'doctor@ehealth.com','Dr. John Smith','$2a$10$4oyWC03e.ggIy7T7n0739emOF19m7aGhPWc1idkFOuW6FEgcgrQ1W','DOCTOR'),(4,'patient@ehealth.com','Jane Doe','$2a$10$yxAjtAuWTVmceRe2hx6hPeH5hbA17FuzEc0dTw0.hAkoI4amxDDgG','PATIENT'),(5,'ram@gmail.com','ram','$2a$10$Kf/9HsUHI31dER6R8ZQ.S.fhnr4IKcqQzcs2srRzP5ZrHhYjNbHuC','PATIENT'),(16,'rjbaral19@gmail.com','rajesh','$2a$10$QyIxbDfLQWrVQetwotMP/.ujYqBw3zklik74jP6Uf1ZGaCNbU3Nia','PATIENT'),(17,'doc2@gmail.com','doc2','$2a$10$ID/aN0gpwouARwUMHn7oWubyg4Z8pFe6Jjl.4ZTvjHhnfgRC9q1qq','PATIENT'),(18,'ramesh@gmail.com','ramesh sahoo','$2a$10$kTFdHrvODWDXynwCVuEsAuqJQXk6qNEcePJyz7AU0fggyziyYw1Pi','DOCTOR'),(19,'john.smith@hospital.com','Dr. John Smith','$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K','DOCTOR'),(20,'sarah.johnson@hospital.com','Dr. Sarah Johnson','$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K','DOCTOR'),(22,'emma.davis@hospital.com','Dr. Emma Davis','$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K','DOCTOR'),(23,'robert.wilson@hospital.com','Dr. Robert Wilson','$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K','DOCTOR'),(24,'lisa.anderson@hospital.com','Dr. Lisa Anderson','$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K','DOCTOR'),(25,'james.martinez@hospital.com','Dr. James Martinez','$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K','DOCTOR'),(26,'patricia.taylor@hospital.com','Dr. Patricia Taylor','$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K','DOCTOR'),(27,'david.lee@hospital.com','Dr. David Lee','$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K','DOCTOR'),(29,'sam@gmail.com','sam','$2a$10$WVcC/TvfyumJJ45cD61L2u2iw5mP3Kc95uLk.SjGF8MpQNwZ8zAvS','PATIENT'),(30,'qatest.patient@hospital.com','QA Test Patient','$2a$10$bIl.QHhfYlWeH3kkqQnhxeP35bURcxGtRwfLQQbSwwGnXBr30jZDO','PATIENT'),(31,'admin@hospital.com','QA Test Admin','$2a$10$nMGHtS5hCC9WgEBxhf/s/eIur1DCUwziiaumtjZu96eVyTx5ZJgd.','PATIENT'),(32,'qatest.doctor@hospital.com','QA Test Doctor','$2a$10$lG6f43z9eXJrk/mStfBeJuKcWe6pjjwY.YMa4qQvS8AG9mJum103G','PATIENT'),(33,'testpatient1777258473346@example.com','Test Patient','$2a$10$V9Lh/45gYohwFZ7QD3ih/OMLyzCnXmZiTUqImz4Co75Xv89iUoPKu','PATIENT'),(34,'michael.chen@hospital.com','Dr. Michael Chen','$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K','DOCTOR'),(35,'maria.garcia@hospital.com','Dr. Maria Garcia','$2a$10$slYQmyNdGzin7olVN3p5Be7DWRvEJiHf7/N8qF8nK2H5E8F9m5W1K','DOCTOR'),(36,'testpat1777397025231@test.com','Test Patient','$2a$10$ew2DV8RE9tAB9Ju878odt.Ivvl/adNxHhFrhavxBhzSERJg3OeQIq','PATIENT');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'ehealth'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-30 14:20:39
