CREATE DATABASE  IF NOT EXISTS `cashless-payment` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `cashless-payment`;
-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: localhost    Database: cashless-payment
-- ------------------------------------------------------
-- Server version	8.0.19

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
-- Table structure for table `cards`
--

DROP TABLE IF EXISTS `cards`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cards` (
  `id` int NOT NULL AUTO_INCREMENT,
  `unique_id` varchar(45) NOT NULL,
  `amount` decimal(5,2) NOT NULL,
  `user_id` int NOT NULL,
  `passcode` varchar(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_id_UNIQUE` (`unique_id`),
  KEY `user_to_card_idx` (`user_id`),
  CONSTRAINT `user_to_card` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cards`
--

LOCK TABLES `cards` WRITE;
/*!40000 ALTER TABLE `cards` DISABLE KEYS */;
INSERT INTO `cards` VALUES (1,'01010701A916831C',55.26,1,'1234'),(3,'01010701A916851C',10.00,1,'1234');
/*!40000 ALTER TABLE `cards` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `offline_transactions`
--

DROP TABLE IF EXISTS `offline_transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `offline_transactions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `card_id` int NOT NULL,
  `vm_id` int NOT NULL,
  `remaining_amount` decimal(5,2) NOT NULL,
  `timestamp` bigint NOT NULL,
  `prev_vm_id` int NOT NULL,
  `prev_remaining_amount` decimal(5,2) NOT NULL,
  `prev_timestamp` bigint NOT NULL,
  `complete` tinyint NOT NULL,
  `transaction_sequence` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `offline_transaction_to_card_idx` (`card_id`),
  KEY `offline_transaction_to_vm_idx` (`vm_id`),
  CONSTRAINT `offline_transaction_to_card` FOREIGN KEY (`card_id`) REFERENCES `cards` (`id`),
  CONSTRAINT `offline_transaction_to_vm` FOREIGN KEY (`vm_id`) REFERENCES `vending_machines` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=212 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `offline_transactions`
--

LOCK TABLES `offline_transactions` WRITE;
/*!40000 ALTER TABLE `offline_transactions` DISABLE KEYS */;
INSERT INTO `offline_transactions` VALUES (210,1,1,0.00,1599251052537,1,0.00,1599251052537,1,0),(211,1,2,50.00,1599251130569,1,0.00,1599251052537,1,1);
/*!40000 ALTER TABLE `offline_transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transaction_types`
--

DROP TABLE IF EXISTS `transaction_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transaction_types` (
  `id` tinyint NOT NULL AUTO_INCREMENT,
  `type` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transaction_types`
--

LOCK TABLES `transaction_types` WRITE;
/*!40000 ALTER TABLE `transaction_types` DISABLE KEYS */;
INSERT INTO `transaction_types` VALUES (1,'credit'),(2,'debit');
/*!40000 ALTER TABLE `transaction_types` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transactions`
--

DROP TABLE IF EXISTS `transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transactions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `card_id` int NOT NULL,
  `vending_machine_id` int NOT NULL,
  `amount` decimal(5,2) NOT NULL,
  `type` tinyint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `transactions_to_card_idx` (`card_id`),
  KEY `transaction_to_vending_machine_idx` (`vending_machine_id`),
  KEY `transaction_to_type_idx` (`type`),
  CONSTRAINT `transaction_to_card` FOREIGN KEY (`card_id`) REFERENCES `cards` (`id`),
  CONSTRAINT `transaction_to_type` FOREIGN KEY (`type`) REFERENCES `transaction_types` (`id`),
  CONSTRAINT `transaction_to_vending_machine` FOREIGN KEY (`vending_machine_id`) REFERENCES `vending_machines` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=77 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions`
--

LOCK TABLES `transactions` WRITE;
/*!40000 ALTER TABLE `transactions` DISABLE KEYS */;
INSERT INTO `transactions` VALUES (1,1,1,3.00,2),(2,1,1,3.00,1),(3,1,1,3.00,1),(4,1,1,3.00,1),(5,1,1,3.00,1),(6,1,1,3.00,1),(7,1,1,3.42,1),(8,1,1,3.42,1),(9,1,1,3.42,1),(10,1,1,3.42,1),(11,1,1,3.42,1),(12,1,1,3.42,1),(13,1,1,3.42,1),(14,1,1,3.42,1),(15,1,1,3.42,1),(16,1,1,3.42,1),(17,1,1,3.42,1),(18,1,1,3.42,1),(19,1,1,3.42,1),(20,1,1,3.42,1),(21,1,1,3.42,1),(22,1,1,3.42,1),(23,1,1,3.42,1),(24,1,1,3.42,1),(25,1,1,3.42,1),(26,1,1,3.42,1),(27,1,1,3.42,1),(28,1,1,3.42,2),(29,1,1,3.42,2),(30,1,1,3.42,2),(31,1,1,3.42,2),(32,1,1,3.42,2),(33,1,1,3.42,2),(34,1,1,3.42,2),(35,1,1,3.42,2),(36,1,1,3.42,2),(37,1,1,3.42,2),(38,1,1,3.42,2),(39,1,1,3.42,2),(40,1,1,3.42,2),(41,1,1,3.42,2),(42,1,1,3.42,2),(43,1,1,0.20,2),(44,1,1,0.20,2),(45,1,1,0.20,2),(46,1,1,0.20,2),(47,1,1,0.20,2),(48,1,1,0.20,2),(49,1,1,0.25,2),(50,1,1,2.00,2),(51,1,1,0.25,2),(52,1,1,25.00,2),(53,1,1,2.00,2),(54,1,1,2.00,2),(55,1,1,2.00,2),(56,1,1,2.00,2),(57,1,1,70.00,1),(58,1,1,0.20,2),(59,1,1,0.20,2),(60,1,1,0.20,2),(61,1,1,0.20,2),(62,1,1,0.20,2),(63,1,1,0.20,2),(64,1,1,0.20,2),(65,1,1,2.00,2),(66,1,1,2.00,2),(67,1,1,0.20,2),(68,1,1,0.20,2),(69,1,1,0.20,2),(70,1,1,0.20,2),(71,1,1,60.00,2),(72,1,1,2.00,2),(73,1,1,2.00,2),(74,1,1,50.00,1),(75,1,1,0.10,2),(76,1,1,0.10,2);
/*!40000 ALTER TABLE `transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `email` varchar(45) NOT NULL,
  `contact` varchar(15) NOT NULL,
  `passhash` varchar(255) NOT NULL,
  `is_admin` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (2,'Admin','admin@cp.com','980990000','$2b$10$djSuDaOOL7.JxXORK8LCzumMhUt9ZOZP.PiQCQ5r/kFirWMflquyq',1),(3,'User','user@cp.com','1234568','$2b$10$ykZxCJPJZKgls8kKmRaUQ.ZGEIfqV4dBRkVd9VCgMFQTkh.sryvcm',0),(4,'user2','user2@cp.com','292929','$2b$10$T7FeVa9B/k3D1UCAgnfLx.T5vGSMGfgKiuC0J73Z4MuQouAM9J0yC',0),(5,'user3','user3@gmail.com','93423545','$2b$10$//xhRmWXC1bfdKwkAMYVV.brDlhhFjdvwjtf3P57LfA1fFF4yptY6',0),(6,'user4','user4@cp.com','user4','$2b$10$/hFnXfjybyGbjdF..fMNS.7vdOw/xwir/yKaLhgiIF3wx82Y0LLNi',0),(7,'user5','user5@cp.com','user6','$2b$10$9vY/i/IdZDAxm65TcBGMbus.YYPG0WJQ.BAtBSjeBUwvpLaU59qbW',0),(8,'user5','user6@cp.com','user6','$2b$10$.aKh0jbuKDPf1A5kPt17legzTEcfb5v.8OqH8XW2rvZhPfw3t312S',0),(9,'Test user','test@cp.com','39535345348','$2b$10$uDBnj/iVZ6TKztZ2oK55bOH3ooT7T/CYUUqlTOXkxhMCphopTYP1a',0),(10,'test','ttest@cp.com','test','$2b$10$WfWzwzSXAdI.gX5iHoEXt.VLwA5.nIszOb1MUHQd6WXG4E6RCLLVi',0);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vending_machines`
--

DROP TABLE IF EXISTS `vending_machines`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vending_machines` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `unique_id` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vending_machines`
--

LOCK TABLES `vending_machines` WRITE;
/*!40000 ALTER TABLE `vending_machines` DISABLE KEYS */;
INSERT INTO `vending_machines` VALUES (1,'vm123456','wtf1'),(2,'bcd','edg'),(3,'evil','mal1');
/*!40000 ALTER TABLE `vending_machines` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vendor_transactions`
--

DROP TABLE IF EXISTS `vendor_transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendor_transactions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `vendor_id` int DEFAULT NULL,
  `card_id` int DEFAULT NULL,
  `collected_amount` decimal(5,2) DEFAULT NULL,
  `timestamp` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `vendor_id` (`vendor_id`),
  KEY `card_id` (`card_id`),
  CONSTRAINT `vendor_transactions_ibfk_1` FOREIGN KEY (`vendor_id`) REFERENCES `users` (`id`),
  CONSTRAINT `vendor_transactions_ibfk_2` FOREIGN KEY (`card_id`) REFERENCES `cards` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vendor_transactions`
--

LOCK TABLES `vendor_transactions` WRITE;
/*!40000 ALTER TABLE `vendor_transactions` DISABLE KEYS */;
INSERT INTO `vendor_transactions` VALUES (6,10,1,50.00,1599251130569);
/*!40000 ALTER TABLE `vendor_transactions` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-09-04 15:31:59
