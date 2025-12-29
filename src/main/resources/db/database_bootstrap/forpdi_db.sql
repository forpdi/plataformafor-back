-- MySQL dump 10.13  Distrib 5.7.35, for Linux (x86_64)
--
-- Host: localhost    Database: forpdi_db_2
-- ------------------------------------------------------
-- Server version	5.7.35

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `DATABASECHANGELOG`
--

DROP TABLE IF EXISTS `DATABASECHANGELOG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DATABASECHANGELOG` (
  `ID` varchar(255) NOT NULL,
  `AUTHOR` varchar(255) NOT NULL,
  `FILENAME` varchar(255) NOT NULL,
  `DATEEXECUTED` datetime NOT NULL,
  `ORDEREXECUTED` int(11) NOT NULL,
  `EXECTYPE` varchar(10) NOT NULL,
  `MD5SUM` varchar(35) DEFAULT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `COMMENTS` varchar(255) DEFAULT NULL,
  `TAG` varchar(255) DEFAULT NULL,
  `LIQUIBASE` varchar(20) DEFAULT NULL,
  `CONTEXTS` varchar(255) DEFAULT NULL,
  `LABELS` varchar(255) DEFAULT NULL,
  `DEPLOYMENT_ID` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DATABASECHANGELOG`
--

LOCK TABLES `DATABASECHANGELOG` WRITE;
/*!40000 ALTER TABLE `DATABASECHANGELOG` DISABLE KEYS */;
/*!40000 ALTER TABLE `DATABASECHANGELOG` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DATABASECHANGELOGLOCK`
--

DROP TABLE IF EXISTS `DATABASECHANGELOGLOCK`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DATABASECHANGELOGLOCK` (
  `ID` int(11) NOT NULL,
  `LOCKED` bit(1) NOT NULL,
  `LOCKGRANTED` datetime DEFAULT NULL,
  `LOCKEDBY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DATABASECHANGELOGLOCK`
--

LOCK TABLES `DATABASECHANGELOGLOCK` WRITE;
/*!40000 ALTER TABLE `DATABASECHANGELOGLOCK` DISABLE KEYS */;
INSERT INTO `DATABASECHANGELOGLOCK` VALUES (1,_binary '\0',NULL,NULL);
/*!40000 ALTER TABLE `DATABASECHANGELOGLOCK` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_action_plan`
--

DROP TABLE IF EXISTS `fpdi_action_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_action_plan` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `begin` date NOT NULL,
  `checked` bit(1) NOT NULL,
  `creation` datetime NOT NULL,
  `description` varchar(4000) DEFAULT NULL,
  `end` date NOT NULL,
  `responsible` varchar(4000) NOT NULL,
  `levelInstance_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_ec9iffsprjnfdwuvdxl9lfm2` (`levelInstance_id`),
  CONSTRAINT `FK_ec9iffsprjnfdwuvdxl9lfm2` FOREIGN KEY (`levelInstance_id`) REFERENCES `fpdi_structure_level_instance` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_action_plan`
--

LOCK TABLES `fpdi_action_plan` WRITE;
/*!40000 ALTER TABLE `fpdi_action_plan` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_action_plan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_aggregate_indicator`
--

DROP TABLE IF EXISTS `fpdi_aggregate_indicator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_aggregate_indicator` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `percentage` double NOT NULL,
  `aggregate_id` bigint(20) NOT NULL,
  `indicator_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `UK_5cnly1q29798iwl20qmxkmoa` (`aggregate_id`,`indicator_id`),
  KEY `FK_8fwe22eis5unn1hcq4neuswox` (`indicator_id`),
  CONSTRAINT `FK_8fwe22eis5unn1hcq4neuswox` FOREIGN KEY (`indicator_id`) REFERENCES `fpdi_structure_level_instance` (`id`),
  CONSTRAINT `FK_rqfbwt0c9t753re1t15r64ptn` FOREIGN KEY (`aggregate_id`) REFERENCES `fpdi_structure_level_instance` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_aggregate_indicator`
--

LOCK TABLES `fpdi_aggregate_indicator` WRITE;
/*!40000 ALTER TABLE `fpdi_aggregate_indicator` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_aggregate_indicator` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_archive`
--

DROP TABLE IF EXISTS `fpdi_archive`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_archive` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `company_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_cfyynlsiy39fmc53bb94g616t` (`company_id`),
  CONSTRAINT `FK_cfyynlsiy39fmc53bb94g616t` FOREIGN KEY (`company_id`) REFERENCES `fpdi_company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_archive`
--

LOCK TABLES `fpdi_archive` WRITE;
/*!40000 ALTER TABLE `fpdi_archive` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_archive` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_attachment`
--

DROP TABLE IF EXISTS `fpdi_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_attachment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `creation` datetime NOT NULL,
  `description` varchar(250) DEFAULT NULL,
  `fileLink` varchar(255) NOT NULL,
  `name` varchar(250) NOT NULL,
  `author_id` bigint(20) DEFAULT NULL,
  `levelInstance_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_5qvd4bwb8ohvccr92v5iujtpv` (`author_id`),
  KEY `FK_1yn3cwl1hsg2qxmcjrlxpk336` (`levelInstance_id`),
  CONSTRAINT `FK_1yn3cwl1hsg2qxmcjrlxpk336` FOREIGN KEY (`levelInstance_id`) REFERENCES `fpdi_structure_level_instance` (`id`),
  CONSTRAINT `FK_5qvd4bwb8ohvccr92v5iujtpv` FOREIGN KEY (`author_id`) REFERENCES `fpdi_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_attachment`
--

LOCK TABLES `fpdi_attachment` WRITE;
/*!40000 ALTER TABLE `fpdi_attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_attachment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_attribute`
--

DROP TABLE IF EXISTS `fpdi_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_attribute` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `beginField` bit(1) NOT NULL,
  `bscField` bit(1) NOT NULL,
  `description` varchar(10000) DEFAULT NULL,
  `endField` bit(1) NOT NULL,
  `expectedField` bit(1) NOT NULL,
  `finishDate` bit(1) NOT NULL,
  `formatField` bit(1) NOT NULL,
  `justificationField` bit(1) NOT NULL,
  `label` varchar(255) NOT NULL,
  `maximumField` bit(1) NOT NULL,
  `minimumField` bit(1) NOT NULL,
  `periodicityField` bit(1) NOT NULL,
  `polarityField` bit(1) NOT NULL,
  `reachedField` bit(1) NOT NULL,
  `required` bit(1) NOT NULL,
  `type` varchar(255) NOT NULL,
  `visibleInTables` bit(1) NOT NULL,
  `level_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_hiidy41ra43oemhoohxbb4x71` (`level_id`),
  CONSTRAINT `FK_hiidy41ra43oemhoohxbb4x71` FOREIGN KEY (`level_id`) REFERENCES `fpdi_structure_level` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_attribute`
--

LOCK TABLES `fpdi_attribute` WRITE;
/*!40000 ALTER TABLE `fpdi_attribute` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_attribute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_attribute_instance`
--

DROP TABLE IF EXISTS `fpdi_attribute_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_attribute_instance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `creation` datetime NOT NULL,
  `value` varchar(4000) DEFAULT NULL,
  `valueAsDate` datetime DEFAULT NULL,
  `valueAsNumber` double DEFAULT NULL,
  `attribute_id` bigint(20) NOT NULL,
  `levelInstance_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `UK_qhd1t0t1oko2t31xetjl67jgf` (`attribute_id`,`levelInstance_id`),
  KEY `FK_be6vvgp7qb1wplvbs8esq9bkb` (`levelInstance_id`),
  CONSTRAINT `FK_be6vvgp7qb1wplvbs8esq9bkb` FOREIGN KEY (`levelInstance_id`) REFERENCES `fpdi_structure_level_instance` (`id`),
  CONSTRAINT `FK_qhl548pqsfnyowr1tb4qdtny5` FOREIGN KEY (`attribute_id`) REFERENCES `fpdi_attribute` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_attribute_instance`
--

LOCK TABLES `fpdi_attribute_instance` WRITE;
/*!40000 ALTER TABLE `fpdi_attribute_instance` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_attribute_instance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_budget`
--

DROP TABLE IF EXISTS `fpdi_budget`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_budget` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `committed` double DEFAULT NULL,
  `creation` datetime DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `realized` double DEFAULT NULL,
  `subAction` varchar(255) NOT NULL,
  `budgetElement_id` bigint(20) NOT NULL,
  `levelInstance_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_qth3jk3faci4207ysmsqnn9hc` (`budgetElement_id`),
  KEY `FK_nowg8ate4fxres1s7vcmp32i6` (`levelInstance_id`),
  CONSTRAINT `FK_nowg8ate4fxres1s7vcmp32i6` FOREIGN KEY (`levelInstance_id`) REFERENCES `fpdi_structure_level_instance` (`id`),
  CONSTRAINT `FK_qth3jk3faci4207ysmsqnn9hc` FOREIGN KEY (`budgetElement_id`) REFERENCES `fpdi_budget_element` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_budget`
--

LOCK TABLES `fpdi_budget` WRITE;
/*!40000 ALTER TABLE `fpdi_budget` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_budget` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_budget_element`
--

DROP TABLE IF EXISTS `fpdi_budget_element`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_budget_element` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `balanceAvailable` double NOT NULL,
  `budgetLoa` double NOT NULL,
  `creation` datetime DEFAULT NULL,
  `linkedObjects` bigint(20) NOT NULL,
  `subAction` varchar(255) NOT NULL,
  `company_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_5ll850amxvmflescsv63i0fsh` (`company_id`),
  CONSTRAINT `FK_5ll850amxvmflescsv63i0fsh` FOREIGN KEY (`company_id`) REFERENCES `fpdi_company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_budget_element`
--

LOCK TABLES `fpdi_budget_element` WRITE;
/*!40000 ALTER TABLE `fpdi_budget_element` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_budget_element` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_company`
--

DROP TABLE IF EXISTS `fpdi_company`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_company` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `description` varchar(11000) DEFAULT NULL,
  `enableForrisco` bit(1) NOT NULL,
  `localization` varchar(255) DEFAULT NULL,
  `logo` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `showBudgetElement` bit(1) NOT NULL,
  `showDashboard` bit(1) NOT NULL,
  `showMaturity` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_company`
--

LOCK TABLES `fpdi_company` WRITE;
/*!40000 ALTER TABLE `fpdi_company` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_company` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_company_domain`
--

DROP TABLE IF EXISTS `fpdi_company_domain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_company_domain` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `baseUrl` varchar(255) NOT NULL,
  `creation` datetime NOT NULL,
  `host` varchar(128) NOT NULL,
  `theme` varchar(128) NOT NULL,
  `company_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_5o8uobe9iui9gb3r848mik0rj` (`host`),
  KEY `FK_p2vckjo91deulxh3ihkqoeef5` (`company_id`),
  CONSTRAINT `FK_p2vckjo91deulxh3ihkqoeef5` FOREIGN KEY (`company_id`) REFERENCES `fpdi_company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_company_domain`
--

LOCK TABLES `fpdi_company_domain` WRITE;
/*!40000 ALTER TABLE `fpdi_company_domain` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_company_domain` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_company_message`
--

DROP TABLE IF EXISTS `fpdi_company_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_company_message` (
  `messageKey` varchar(128) NOT NULL,
  `lastUpdated` datetime NOT NULL,
  `messageValue` varchar(4000) NOT NULL,
  `company_id` bigint(20) NOT NULL,
  PRIMARY KEY (`messageKey`,`company_id`),
  KEY `FK_9w7kpmns3bi3r3l9g3oc4d8we` (`company_id`),
  CONSTRAINT `FK_9w7kpmns3bi3r3l9g3oc4d8we` FOREIGN KEY (`company_id`) REFERENCES `fpdi_company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_company_message`
--

LOCK TABLES `fpdi_company_message` WRITE;
/*!40000 ALTER TABLE `fpdi_company_message` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_company_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_company_user`
--

DROP TABLE IF EXISTS `fpdi_company_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_company_user` (
  `accessLevel` int(11) NOT NULL,
  `blocked` bit(1) NOT NULL,
  `notificationSetting` int(11) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `company_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`company_id`),
  KEY `FK_no9x3bjpv7ny791l6h645pwvq` (`company_id`),
  CONSTRAINT `FK_5k71nr4s17tng3lhk6tmkkh8d` FOREIGN KEY (`user_id`) REFERENCES `fpdi_user` (`id`),
  CONSTRAINT `FK_no9x3bjpv7ny791l6h645pwvq` FOREIGN KEY (`company_id`) REFERENCES `fpdi_company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_company_user`
--

LOCK TABLES `fpdi_company_user` WRITE;
/*!40000 ALTER TABLE `fpdi_company_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_company_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_document`
--

DROP TABLE IF EXISTS `fpdi_document`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_document` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `creation` datetime NOT NULL,
  `description` varchar(4000) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `plan_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_l5lmntakhbbj1slyeqt1qx1lg` (`plan_id`),
  CONSTRAINT `FK_l5lmntakhbbj1slyeqt1qx1lg` FOREIGN KEY (`plan_id`) REFERENCES `fpdi_plan_macro` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_document`
--

LOCK TABLES `fpdi_document` WRITE;
/*!40000 ALTER TABLE `fpdi_document` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_document` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_document_attribute`
--

DROP TABLE IF EXISTS `fpdi_document_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_document_attribute` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `required` bit(1) NOT NULL,
  `sequence` int(11) NOT NULL,
  `type` varchar(255) NOT NULL,
  `value` longtext,
  `valueAsDate` datetime DEFAULT NULL,
  `valueAsNumber` double DEFAULT NULL,
  `section_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `UK_t6gq04490gchu8u19fu7bh9jx` (`sequence`),
  KEY `FK_duqbdv2o8jwtq7fxctdgx7lbu` (`section_id`),
  CONSTRAINT `FK_duqbdv2o8jwtq7fxctdgx7lbu` FOREIGN KEY (`section_id`) REFERENCES `fpdi_document_section` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_document_attribute`
--

LOCK TABLES `fpdi_document_attribute` WRITE;
/*!40000 ALTER TABLE `fpdi_document_attribute` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_document_attribute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_document_section`
--

DROP TABLE IF EXISTS `fpdi_document_section`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_document_section` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `leaf` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `preTextSection` bit(1) NOT NULL,
  `sequence` int(11) NOT NULL,
  `document_id` bigint(20) NOT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `UK_jpw9fe41m2gv1pl63n8d4mckc` (`sequence`),
  KEY `FK_3ck4anw3vk08teonit1pj9hh` (`document_id`),
  KEY `FK_l2hbcm004g024o13ai5jmkkww` (`parent_id`),
  CONSTRAINT `FK_3ck4anw3vk08teonit1pj9hh` FOREIGN KEY (`document_id`) REFERENCES `fpdi_document` (`id`),
  CONSTRAINT `FK_l2hbcm004g024o13ai5jmkkww` FOREIGN KEY (`parent_id`) REFERENCES `fpdi_document_section` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_document_section`
--

LOCK TABLES `fpdi_document_section` WRITE;
/*!40000 ALTER TABLE `fpdi_document_section` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_document_section` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_favorite_level_instance`
--

DROP TABLE IF EXISTS `fpdi_favorite_level_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_favorite_level_instance` (
  `deleted` bit(1) NOT NULL,
  `levelInstance_id` bigint(20) NOT NULL,
  `companyUser_user_id` bigint(20) NOT NULL,
  `companyUser_company_id` bigint(20) NOT NULL,
  PRIMARY KEY (`levelInstance_id`,`companyUser_user_id`,`companyUser_company_id`),
  KEY `FK_pws60es59puqbdpysnn6p75vc` (`companyUser_user_id`,`companyUser_company_id`),
  CONSTRAINT `FK_81mvpgkwtcmhxv3namm95virl` FOREIGN KEY (`levelInstance_id`) REFERENCES `fpdi_structure_level_instance` (`id`),
  CONSTRAINT `FK_pws60es59puqbdpysnn6p75vc` FOREIGN KEY (`companyUser_user_id`, `companyUser_company_id`) REFERENCES `fpdi_company_user` (`user_id`, `company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_favorite_level_instance`
--

LOCK TABLES `fpdi_favorite_level_instance` WRITE;
/*!40000 ALTER TABLE `fpdi_favorite_level_instance` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_favorite_level_instance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_level_instance_history`
--

DROP TABLE IF EXISTS `fpdi_level_instance_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_level_instance_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `creation` datetime NOT NULL,
  `value` double NOT NULL,
  `levelInstance_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_s27u0kyuwo69kaue322lafy9b` (`levelInstance_id`),
  CONSTRAINT `FK_s27u0kyuwo69kaue322lafy9b` FOREIGN KEY (`levelInstance_id`) REFERENCES `fpdi_structure_level_instance` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_level_instance_history`
--

LOCK TABLES `fpdi_level_instance_history` WRITE;
/*!40000 ALTER TABLE `fpdi_level_instance_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_level_instance_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_message_history`
--

DROP TABLE IF EXISTS `fpdi_message_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_message_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `creation` datetime NOT NULL,
  `message` longtext NOT NULL,
  `subject` varchar(70) NOT NULL,
  `company_id` bigint(20) NOT NULL,
  `notification_id` bigint(20) DEFAULT NULL,
  `userReceiver_id` bigint(20) NOT NULL,
  `userSender_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_irb1q3954befjbgy3kxrwcob5` (`company_id`),
  KEY `FK_4rehrw0c3o2o9myeb56nan40x` (`notification_id`),
  KEY `FK_14q8gb80d2lbr6lkstc6scbkl` (`userReceiver_id`),
  KEY `FK_s5wq7dcswrc5279xr7wo80hwr` (`userSender_id`),
  CONSTRAINT `FK_14q8gb80d2lbr6lkstc6scbkl` FOREIGN KEY (`userReceiver_id`) REFERENCES `fpdi_user` (`id`),
  CONSTRAINT `FK_4rehrw0c3o2o9myeb56nan40x` FOREIGN KEY (`notification_id`) REFERENCES `fpdi_notification` (`id`),
  CONSTRAINT `FK_irb1q3954befjbgy3kxrwcob5` FOREIGN KEY (`company_id`) REFERENCES `fpdi_company` (`id`),
  CONSTRAINT `FK_s5wq7dcswrc5279xr7wo80hwr` FOREIGN KEY (`userSender_id`) REFERENCES `fpdi_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_message_history`
--

LOCK TABLES `fpdi_message_history` WRITE;
/*!40000 ALTER TABLE `fpdi_message_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_message_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_notification`
--

DROP TABLE IF EXISTS `fpdi_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `creation` datetime NOT NULL,
  `description` varchar(4000) NOT NULL,
  `onlyEmail` bit(1) NOT NULL,
  `picture` varchar(255) DEFAULT NULL,
  `responded` bit(1) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `vizualized` bit(1) NOT NULL,
  `company_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_86cvci26gum8dglvx7lctuc42` (`company_id`),
  KEY `FK_8o7kfsvjq674pjob0s9rki5m1` (`user_id`),
  CONSTRAINT `FK_86cvci26gum8dglvx7lctuc42` FOREIGN KEY (`company_id`) REFERENCES `fpdi_company` (`id`),
  CONSTRAINT `FK_8o7kfsvjq674pjob0s9rki5m1` FOREIGN KEY (`user_id`) REFERENCES `fpdi_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_notification`
--

LOCK TABLES `fpdi_notification` WRITE;
/*!40000 ALTER TABLE `fpdi_notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_options_field`
--

DROP TABLE IF EXISTS `fpdi_options_field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_options_field` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `attributeId` bigint(20) NOT NULL,
  `columnId` bigint(20) DEFAULT NULL,
  `creation` datetime NOT NULL,
  `isDocument` bit(1) DEFAULT NULL,
  `label` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_options_field`
--

LOCK TABLES `fpdi_options_field` WRITE;
/*!40000 ALTER TABLE `fpdi_options_field` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_options_field` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_plan_detailed`
--

DROP TABLE IF EXISTS `fpdi_plan_detailed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_plan_detailed` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `maximumAverage` double DEFAULT NULL,
  `minimumAverage` double DEFAULT NULL,
  `month` int(11) NOT NULL,
  `performance` double DEFAULT NULL,
  `year` int(11) NOT NULL,
  `plan_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_sxw505d54b4pada1hjwfhcbio` (`plan_id`),
  CONSTRAINT `FK_sxw505d54b4pada1hjwfhcbio` FOREIGN KEY (`plan_id`) REFERENCES `fpdi_plans` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_plan_detailed`
--

LOCK TABLES `fpdi_plan_detailed` WRITE;
/*!40000 ALTER TABLE `fpdi_plan_detailed` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_plan_detailed` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_plan_macro`
--

DROP TABLE IF EXISTS `fpdi_plan_macro`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_plan_macro` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `archived` bit(1) NOT NULL,
  `begin` date NOT NULL,
  `description` varchar(10000) DEFAULT NULL,
  `documented` bit(1) NOT NULL,
  `end` date NOT NULL,
  `name` varchar(255) NOT NULL,
  `company_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_6x701xhgquyy75u28ayfh026r` (`company_id`),
  CONSTRAINT `FK_6x701xhgquyy75u28ayfh026r` FOREIGN KEY (`company_id`) REFERENCES `fpdi_company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_plan_macro`
--

LOCK TABLES `fpdi_plan_macro` WRITE;
/*!40000 ALTER TABLE `fpdi_plan_macro` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_plan_macro` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_plans`
--

DROP TABLE IF EXISTS `fpdi_plans`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_plans` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `archived` bit(1) NOT NULL,
  `begin` date NOT NULL,
  `creation` datetime NOT NULL,
  `description` varchar(10000) DEFAULT NULL,
  `end` date NOT NULL,
  `maximumAverage` double DEFAULT NULL,
  `minimumAverage` double DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `performance` double DEFAULT NULL,
  `parent_id` bigint(20) NOT NULL,
  `structure_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_hn29cq1wddb02y1o7ydw5fhhd` (`parent_id`),
  KEY `FK_5tyigisrnwlk7uynd9lmvs888` (`structure_id`),
  CONSTRAINT `FK_5tyigisrnwlk7uynd9lmvs888` FOREIGN KEY (`structure_id`) REFERENCES `fpdi_structure` (`id`),
  CONSTRAINT `FK_hn29cq1wddb02y1o7ydw5fhhd` FOREIGN KEY (`parent_id`) REFERENCES `fpdi_plan_macro` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_plans`
--

LOCK TABLES `fpdi_plans` WRITE;
/*!40000 ALTER TABLE `fpdi_plans` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_plans` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_role`
--

DROP TABLE IF EXISTS `fpdi_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `label` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_role`
--

LOCK TABLES `fpdi_role` WRITE;
/*!40000 ALTER TABLE `fpdi_role` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_role_permission`
--

DROP TABLE IF EXISTS `fpdi_role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_role_permission` (
  `permission` varchar(255) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`role_id`,`permission`),
  CONSTRAINT `FK_inks5n7cibc8dj8uk8s67najn` FOREIGN KEY (`role_id`) REFERENCES `fpdi_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_role_permission`
--

LOCK TABLES `fpdi_role_permission` WRITE;
/*!40000 ALTER TABLE `fpdi_role_permission` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_role_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_schedule`
--

DROP TABLE IF EXISTS `fpdi_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `attributeId` bigint(20) NOT NULL,
  `isDocument` bit(1) DEFAULT NULL,
  `periodicityEnable` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_schedule`
--

LOCK TABLES `fpdi_schedule` WRITE;
/*!40000 ALTER TABLE `fpdi_schedule` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_schedule_instance`
--

DROP TABLE IF EXISTS `fpdi_schedule_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_schedule_instance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `begin` date NOT NULL,
  `creation` datetime NOT NULL,
  `description` varchar(4000) NOT NULL,
  `end` date NOT NULL,
  `number` bigint(20) NOT NULL,
  `periodicity` varchar(255) DEFAULT NULL,
  `schedule_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_3fydc0sjxmhibchssjfn9p2uk` (`schedule_id`),
  CONSTRAINT `FK_3fydc0sjxmhibchssjfn9p2uk` FOREIGN KEY (`schedule_id`) REFERENCES `fpdi_schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_schedule_instance`
--

LOCK TABLES `fpdi_schedule_instance` WRITE;
/*!40000 ALTER TABLE `fpdi_schedule_instance` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_schedule_instance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_schedule_structure`
--

DROP TABLE IF EXISTS `fpdi_schedule_structure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_schedule_structure` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `label` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `schedule_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_slgxpny1xnyjm01nyutbk86qp` (`schedule_id`),
  CONSTRAINT `FK_slgxpny1xnyjm01nyutbk86qp` FOREIGN KEY (`schedule_id`) REFERENCES `fpdi_schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_schedule_structure`
--

LOCK TABLES `fpdi_schedule_structure` WRITE;
/*!40000 ALTER TABLE `fpdi_schedule_structure` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_schedule_structure` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_schedule_values`
--

DROP TABLE IF EXISTS `fpdi_schedule_values`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_schedule_values` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `value` varchar(4000) DEFAULT NULL,
  `valueAsDate` datetime DEFAULT NULL,
  `valueAsNumber` double DEFAULT NULL,
  `scheduleInstance_id` bigint(20) NOT NULL,
  `scheduleStructure_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_mf63cfor9ognt8cj47aemrxn3` (`scheduleInstance_id`),
  KEY `FK_cshuvyqrn6ke3swfyig2q89o9` (`scheduleStructure_id`),
  CONSTRAINT `FK_cshuvyqrn6ke3swfyig2q89o9` FOREIGN KEY (`scheduleStructure_id`) REFERENCES `fpdi_schedule_structure` (`id`),
  CONSTRAINT `FK_mf63cfor9ognt8cj47aemrxn3` FOREIGN KEY (`scheduleInstance_id`) REFERENCES `fpdi_schedule_instance` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_schedule_values`
--

LOCK TABLES `fpdi_schedule_values` WRITE;
/*!40000 ALTER TABLE `fpdi_schedule_values` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_schedule_values` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_structure`
--

DROP TABLE IF EXISTS `fpdi_structure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_structure` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `description` varchar(10000) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `company_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_cayko9b64ree1s11sbx398g23` (`company_id`),
  CONSTRAINT `FK_cayko9b64ree1s11sbx398g23` FOREIGN KEY (`company_id`) REFERENCES `fpdi_company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_structure`
--

LOCK TABLES `fpdi_structure` WRITE;
/*!40000 ALTER TABLE `fpdi_structure` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_structure` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_structure_level`
--

DROP TABLE IF EXISTS `fpdi_structure_level`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_structure_level` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `description` varchar(10000) DEFAULT NULL,
  `goal` bit(1) NOT NULL,
  `indicator` bit(1) NOT NULL,
  `leaf` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `objective` bit(1) NOT NULL,
  `sequence` int(11) NOT NULL,
  `structure_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_gpcayxufp00m9jsdwjnv4wcfb` (`id`,`sequence`),
  KEY `UK_sg0xp81puol3ms5ra60n36nm7` (`sequence`),
  KEY `UK_6ieyqgnn6bldim3nnfrw081wa` (`structure_id`,`sequence`),
  CONSTRAINT `FK_2sjtndavp4tneur2w0idi03dg` FOREIGN KEY (`structure_id`) REFERENCES `fpdi_structure` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_structure_level`
--

LOCK TABLES `fpdi_structure_level` WRITE;
/*!40000 ALTER TABLE `fpdi_structure_level` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_structure_level` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_structure_level_instance`
--

DROP TABLE IF EXISTS `fpdi_structure_level_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_structure_level_instance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `aggregate` bit(1) NOT NULL,
  `calculation` int(11) DEFAULT NULL,
  `closed` bit(1) NOT NULL,
  `closedDate` date DEFAULT NULL,
  `creation` datetime NOT NULL,
  `levelMaximum` double DEFAULT NULL,
  `levelMinimum` double DEFAULT NULL,
  `levelValue` double DEFAULT NULL,
  `modification` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `nextSave` datetime DEFAULT NULL,
  `parent` bigint(20) DEFAULT NULL,
  `level_id` bigint(20) NOT NULL,
  `plan_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `UK_92va0qovt9x52fslweplxosly` (`level_id`,`plan_id`),
  KEY `FK_ddhnv83ogr3lhit27gng34kan` (`plan_id`),
  CONSTRAINT `FK_ddhnv83ogr3lhit27gng34kan` FOREIGN KEY (`plan_id`) REFERENCES `fpdi_plans` (`id`),
  CONSTRAINT `FK_ocjjphp6le3666fjibda6s7qw` FOREIGN KEY (`level_id`) REFERENCES `fpdi_structure_level` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_structure_level_instance`
--

LOCK TABLES `fpdi_structure_level_instance` WRITE;
/*!40000 ALTER TABLE `fpdi_structure_level_instance` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_structure_level_instance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_structure_level_instance_detailed`
--

DROP TABLE IF EXISTS `fpdi_structure_level_instance_detailed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_structure_level_instance_detailed` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `levelMaximum` double DEFAULT NULL,
  `levelMinimum` double DEFAULT NULL,
  `levelValue` double DEFAULT NULL,
  `month` int(11) NOT NULL,
  `year` int(11) NOT NULL,
  `levelInstance_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_cpe98gx6s84cuqacoxgka67pa` (`levelInstance_id`),
  CONSTRAINT `FK_cpe98gx6s84cuqacoxgka67pa` FOREIGN KEY (`levelInstance_id`) REFERENCES `fpdi_structure_level_instance` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_structure_level_instance_detailed`
--

LOCK TABLES `fpdi_structure_level_instance_detailed` WRITE;
/*!40000 ALTER TABLE `fpdi_structure_level_instance_detailed` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_structure_level_instance_detailed` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_table_field`
--

DROP TABLE IF EXISTS `fpdi_table_field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_table_field` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `attributeId` bigint(20) NOT NULL,
  `isDocument` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_table_field`
--

LOCK TABLES `fpdi_table_field` WRITE;
/*!40000 ALTER TABLE `fpdi_table_field` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_table_field` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_table_instance`
--

DROP TABLE IF EXISTS `fpdi_table_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_table_instance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `creation` datetime NOT NULL,
  `tableFields_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_c65jfwg07t0gdm146jtkhws50` (`tableFields_id`),
  CONSTRAINT `FK_c65jfwg07t0gdm146jtkhws50` FOREIGN KEY (`tableFields_id`) REFERENCES `fpdi_table_field` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_table_instance`
--

LOCK TABLES `fpdi_table_instance` WRITE;
/*!40000 ALTER TABLE `fpdi_table_instance` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_table_instance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_table_structure`
--

DROP TABLE IF EXISTS `fpdi_table_structure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_table_structure` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `isInTotal` bit(1) DEFAULT NULL,
  `label` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `tableFields_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_jbtl3k0ea1h0qb0fl89kgtshq` (`tableFields_id`),
  CONSTRAINT `FK_jbtl3k0ea1h0qb0fl89kgtshq` FOREIGN KEY (`tableFields_id`) REFERENCES `fpdi_table_field` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_table_structure`
--

LOCK TABLES `fpdi_table_structure` WRITE;
/*!40000 ALTER TABLE `fpdi_table_structure` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_table_structure` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_table_values`
--

DROP TABLE IF EXISTS `fpdi_table_values`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_table_values` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `value` varchar(4000) DEFAULT NULL,
  `valueAsDate` datetime DEFAULT NULL,
  `valueAsNumber` double DEFAULT NULL,
  `tableInstance_id` bigint(20) NOT NULL,
  `tableStructure_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_3bir2ebw7f08kmtix09f7mfcx` (`tableInstance_id`),
  KEY `FK_hpu578slahxqu0fe09bdnra1c` (`tableStructure_id`),
  CONSTRAINT `FK_3bir2ebw7f08kmtix09f7mfcx` FOREIGN KEY (`tableInstance_id`) REFERENCES `fpdi_table_instance` (`id`),
  CONSTRAINT `FK_hpu578slahxqu0fe09bdnra1c` FOREIGN KEY (`tableStructure_id`) REFERENCES `fpdi_table_structure` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_table_values`
--

LOCK TABLES `fpdi_table_values` WRITE;
/*!40000 ALTER TABLE `fpdi_table_values` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_table_values` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_user`
--

DROP TABLE IF EXISTS `fpdi_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `accessLevel` int(11) NOT NULL,
  `active` bit(1) NOT NULL,
  `birthdate` date DEFAULT NULL,
  `cellphone` varchar(255) DEFAULT NULL,
  `cpf` varchar(255) DEFAULT NULL,
  `creation` datetime NOT NULL,
  `department` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `inviteToken` varchar(128) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `picture` varchar(255) DEFAULT NULL,
  `termsAcceptance` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_rh4wkodr0haqdmnk9a2vfb2nx` (`email`),
  UNIQUE KEY `UK_ljkggk1aoaiihr131fjfj4t2h` (`cellphone`),
  UNIQUE KEY `UK_2dsxxkyyuvp5b3voia0h2l664` (`cpf`),
  UNIQUE KEY `UK_cue2hm5uyl4gmp0ie4h2tqkxf` (`inviteToken`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_user`
--

LOCK TABLES `fpdi_user` WRITE;
/*!40000 ALTER TABLE `fpdi_user` DISABLE KEYS */;
INSERT INTO `fpdi_user` VALUES (1,_binary '\0',100,_binary '\0',NULL,NULL,'00000000000','2022-01-21 18:08:02',NULL,'admin@forpdi.org',NULL,'Administrador ForPDI','$2a$10$YaWLoTwRzg6AOmX5flgdfes4iqUZOz70KgHALO/IRxz3iqsHi1PWi',NULL,NULL,NULL);
/*!40000 ALTER TABLE `fpdi_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_user_access_token`
--

DROP TABLE IF EXISTS `fpdi_user_access_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_user_access_token` (
  `token` varchar(128) NOT NULL,
  `creation` datetime NOT NULL,
  `creationIp` varchar(64) NOT NULL,
  `expiration` datetime NOT NULL,
  `ttl` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`token`),
  KEY `FK_ke4v1b2xbsmxjsm5yhqdir2jg` (`user_id`),
  CONSTRAINT `FK_ke4v1b2xbsmxjsm5yhqdir2jg` FOREIGN KEY (`user_id`) REFERENCES `fpdi_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_user_access_token`
--

LOCK TABLES `fpdi_user_access_token` WRITE;
/*!40000 ALTER TABLE `fpdi_user_access_token` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_user_access_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_user_password_used`
--

DROP TABLE IF EXISTS `fpdi_user_password_used`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_user_password_used` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `creation` datetime NOT NULL,
  `password` varchar(255) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_cot3x75k2qcc5cvru5k6dlkkd` (`user_id`),
  CONSTRAINT `FK_cot3x75k2qcc5cvru5k6dlkkd` FOREIGN KEY (`user_id`) REFERENCES `fpdi_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_user_password_used`
--

LOCK TABLES `fpdi_user_password_used` WRITE;
/*!40000 ALTER TABLE `fpdi_user_password_used` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_user_password_used` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_user_permission`
--

DROP TABLE IF EXISTS `fpdi_user_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_user_permission` (
  `permission` varchar(255) NOT NULL,
  `revoked` bit(1) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `company_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`permission`,`company_id`),
  KEY `FK_oflqiup203rspqe0nocjcpi10` (`company_id`),
  CONSTRAINT `FK_604p0vb2rapss1ymf1t0wb4` FOREIGN KEY (`user_id`) REFERENCES `fpdi_user` (`id`),
  CONSTRAINT `FK_oflqiup203rspqe0nocjcpi10` FOREIGN KEY (`company_id`) REFERENCES `fpdi_company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_user_permission`
--

LOCK TABLES `fpdi_user_permission` WRITE;
/*!40000 ALTER TABLE `fpdi_user_permission` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_user_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fpdi_user_recover_req`
--

DROP TABLE IF EXISTS `fpdi_user_recover_req`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fpdi_user_recover_req` (
  `token` varchar(128) NOT NULL,
  `creation` datetime NOT NULL,
  `creationIp` varchar(64) NOT NULL,
  `expiration` datetime NOT NULL,
  `recover` datetime DEFAULT NULL,
  `recoverIp` varchar(64) DEFAULT NULL,
  `used` bit(1) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`token`),
  KEY `FK_rw717vs0pslucixbbc4s5nldh` (`user_id`),
  CONSTRAINT `FK_rw717vs0pslucixbbc4s5nldh` FOREIGN KEY (`user_id`) REFERENCES `fpdi_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fpdi_user_recover_req`
--

LOCK TABLES `fpdi_user_recover_req` WRITE;
/*!40000 ALTER TABLE `fpdi_user_recover_req` DISABLE KEYS */;
/*!40000 ALTER TABLE `fpdi_user_recover_req` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_contingency`
--

DROP TABLE IF EXISTS `frisco_contingency`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_contingency` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `action` varchar(4000) NOT NULL,
  `risk_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_d98w337mo4mqky7cyly8840m6` (`risk_id`),
  KEY `FK_7q9rx395cymxf6w631ayjr493` (`user_id`),
  CONSTRAINT `FK_7q9rx395cymxf6w631ayjr493` FOREIGN KEY (`user_id`) REFERENCES `fpdi_user` (`id`),
  CONSTRAINT `FK_d98w337mo4mqky7cyly8840m6` FOREIGN KEY (`risk_id`) REFERENCES `frisco_risk` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_contingency`
--

LOCK TABLES `frisco_contingency` WRITE;
/*!40000 ALTER TABLE `frisco_contingency` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_contingency` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_incident`
--

DROP TABLE IF EXISTS `frisco_incident`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_incident` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `action` varchar(4000) NOT NULL,
  `begin` datetime NOT NULL,
  `description` varchar(4000) NOT NULL,
  `type` int(11) NOT NULL,
  `risk_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_i7ldgxird114g7gb89cvsi9va` (`risk_id`),
  KEY `FK_563hrdj87txcm2two7m6xlad7` (`user_id`),
  CONSTRAINT `FK_563hrdj87txcm2two7m6xlad7` FOREIGN KEY (`user_id`) REFERENCES `fpdi_user` (`id`),
  CONSTRAINT `FK_i7ldgxird114g7gb89cvsi9va` FOREIGN KEY (`risk_id`) REFERENCES `frisco_risk` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_incident`
--

LOCK TABLES `frisco_incident` WRITE;
/*!40000 ALTER TABLE `frisco_incident` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_incident` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_item`
--

DROP TABLE IF EXISTS `frisco_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `description` varchar(10000) DEFAULT NULL,
  `name` varchar(400) NOT NULL,
  `policy_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_a0dldwj5kcs5ap7hvyleokjcy` (`policy_id`),
  CONSTRAINT `FK_a0dldwj5kcs5ap7hvyleokjcy` FOREIGN KEY (`policy_id`) REFERENCES `frisco_policy` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_item`
--

LOCK TABLES `frisco_item` WRITE;
/*!40000 ALTER TABLE `frisco_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_item_field`
--

DROP TABLE IF EXISTS `frisco_item_field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_item_field` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `description` longtext,
  `fileLink` varchar(400) DEFAULT NULL,
  `isText` bit(1) NOT NULL,
  `name` varchar(400) NOT NULL,
  `item_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_ra42uawlgo53s50nneahvqvq` (`item_id`),
  CONSTRAINT `FK_ra42uawlgo53s50nneahvqvq` FOREIGN KEY (`item_id`) REFERENCES `frisco_item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_item_field`
--

LOCK TABLES `frisco_item_field` WRITE;
/*!40000 ALTER TABLE `frisco_item_field` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_item_field` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_monitor`
--

DROP TABLE IF EXISTS `frisco_monitor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_monitor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `begin` datetime NOT NULL,
  `impact` varchar(40) NOT NULL,
  `probability` varchar(40) NOT NULL,
  `report` varchar(4000) NOT NULL,
  `risk_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_mwg36wct7jvhhsnju67lf7f59` (`risk_id`),
  KEY `FK_5emfmdtkhxrnoxljvfmfirl8h` (`user_id`),
  CONSTRAINT `FK_5emfmdtkhxrnoxljvfmfirl8h` FOREIGN KEY (`user_id`) REFERENCES `fpdi_user` (`id`),
  CONSTRAINT `FK_mwg36wct7jvhhsnju67lf7f59` FOREIGN KEY (`risk_id`) REFERENCES `frisco_risk` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_monitor`
--

LOCK TABLES `frisco_monitor` WRITE;
/*!40000 ALTER TABLE `frisco_monitor` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_monitor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_monitor_history`
--

DROP TABLE IF EXISTS `frisco_monitor_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_monitor_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `estado` varchar(200) NOT NULL,
  `month` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `year` int(11) NOT NULL,
  `unit_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_pa4kilm2wxi19oyojrsfucra3` (`unit_id`),
  CONSTRAINT `FK_pa4kilm2wxi19oyojrsfucra3` FOREIGN KEY (`unit_id`) REFERENCES `frisco_unit` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_monitor_history`
--

LOCK TABLES `frisco_monitor_history` WRITE;
/*!40000 ALTER TABLE `frisco_monitor_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_monitor_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_plan_risk`
--

DROP TABLE IF EXISTS `frisco_plan_risk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_plan_risk` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `archived` bit(1) NOT NULL,
  `description` longtext,
  `name` varchar(400) NOT NULL,
  `validity_begin` date DEFAULT NULL,
  `validity_end` date DEFAULT NULL,
  `policy_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m8i5bu0c3yj9oyulky1o7rbih` (`policy_id`),
  CONSTRAINT `FK_m8i5bu0c3yj9oyulky1o7rbih` FOREIGN KEY (`policy_id`) REFERENCES `frisco_policy` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_plan_risk`
--

LOCK TABLES `frisco_plan_risk` WRITE;
/*!40000 ALTER TABLE `frisco_plan_risk` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_plan_risk` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_plan_risk_item`
--

DROP TABLE IF EXISTS `frisco_plan_risk_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_plan_risk_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `description` varchar(10000) DEFAULT NULL,
  `name` varchar(400) NOT NULL,
  `planRisk_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_7wrsnce2eqwqabqggwq6jf2a5` (`planRisk_id`),
  CONSTRAINT `FK_7wrsnce2eqwqabqggwq6jf2a5` FOREIGN KEY (`planRisk_id`) REFERENCES `frisco_plan_risk` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_plan_risk_item`
--

LOCK TABLES `frisco_plan_risk_item` WRITE;
/*!40000 ALTER TABLE `frisco_plan_risk_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_plan_risk_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_plan_risk_item_field`
--

DROP TABLE IF EXISTS `frisco_plan_risk_item_field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_plan_risk_item_field` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `description` longtext,
  `fileLink` varchar(400) DEFAULT NULL,
  `isText` bit(1) NOT NULL,
  `name` varchar(400) NOT NULL,
  `planRiskItem_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_5bfbkhdtbevf78bmr2djnw3js` (`planRiskItem_id`),
  CONSTRAINT `FK_5bfbkhdtbevf78bmr2djnw3js` FOREIGN KEY (`planRiskItem_id`) REFERENCES `frisco_plan_risk_item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_plan_risk_item_field`
--

LOCK TABLES `frisco_plan_risk_item_field` WRITE;
/*!40000 ALTER TABLE `frisco_plan_risk_item_field` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_plan_risk_item_field` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_plan_risk_sub_item`
--

DROP TABLE IF EXISTS `frisco_plan_risk_sub_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_plan_risk_sub_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `description` varchar(10000) DEFAULT NULL,
  `name` varchar(400) NOT NULL,
  `planRiskItem_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_7jirmu4sroyf235qtbgsqg3hb` (`planRiskItem_id`),
  CONSTRAINT `FK_7jirmu4sroyf235qtbgsqg3hb` FOREIGN KEY (`planRiskItem_id`) REFERENCES `frisco_plan_risk_item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_plan_risk_sub_item`
--

LOCK TABLES `frisco_plan_risk_sub_item` WRITE;
/*!40000 ALTER TABLE `frisco_plan_risk_sub_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_plan_risk_sub_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_plan_risk_sub_item_field`
--

DROP TABLE IF EXISTS `frisco_plan_risk_sub_item_field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_plan_risk_sub_item_field` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `description` longtext,
  `fileLink` varchar(400) DEFAULT NULL,
  `isText` bit(1) NOT NULL,
  `name` varchar(400) NOT NULL,
  `planRiskSubItem_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_rw0ie0qtfktsxlsffritaqtsp` (`planRiskSubItem_id`),
  CONSTRAINT `FK_rw0ie0qtfktsxlsffritaqtsp` FOREIGN KEY (`planRiskSubItem_id`) REFERENCES `frisco_plan_risk_sub_item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_plan_risk_sub_item_field`
--

LOCK TABLES `frisco_plan_risk_sub_item_field` WRITE;
/*!40000 ALTER TABLE `frisco_plan_risk_sub_item_field` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_plan_risk_sub_item_field` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_policy`
--

DROP TABLE IF EXISTS `frisco_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_policy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `PIDescriptions` text,
  `archived` bit(1) NOT NULL,
  `description` longtext,
  `impact` varchar(4000) NOT NULL,
  `matrix` varchar(4000) NOT NULL,
  `name` varchar(400) NOT NULL,
  `ncolumn` int(11) NOT NULL,
  `nline` int(11) NOT NULL,
  `probability` varchar(4000) NOT NULL,
  `validity_begin` date DEFAULT NULL,
  `validity_end` date DEFAULT NULL,
  `company_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_iwnyuy3le7dls4hcxvahk3ye3` (`company_id`),
  CONSTRAINT `FK_iwnyuy3le7dls4hcxvahk3ye3` FOREIGN KEY (`company_id`) REFERENCES `fpdi_company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_policy`
--

LOCK TABLES `frisco_policy` WRITE;
/*!40000 ALTER TABLE `frisco_policy` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_policy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_preventive_action`
--

DROP TABLE IF EXISTS `frisco_preventive_action`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_preventive_action` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `accomplished` bit(1) NOT NULL,
  `action` varchar(4000) NOT NULL,
  `risk_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_1ma34k35up7f54e92j7nnbpwh` (`risk_id`),
  KEY `FK_7uro8aphy8g431vw6aagpgs81` (`user_id`),
  CONSTRAINT `FK_1ma34k35up7f54e92j7nnbpwh` FOREIGN KEY (`risk_id`) REFERENCES `frisco_risk` (`id`),
  CONSTRAINT `FK_7uro8aphy8g431vw6aagpgs81` FOREIGN KEY (`user_id`) REFERENCES `fpdi_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_preventive_action`
--

LOCK TABLES `frisco_preventive_action` WRITE;
/*!40000 ALTER TABLE `frisco_preventive_action` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_preventive_action` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_process`
--

DROP TABLE IF EXISTS `frisco_process`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_process` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `fileLink` varchar(4000) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `objective` varchar(4000) NOT NULL,
  `company_id` bigint(20) DEFAULT NULL,
  `file_id` bigint(20) DEFAULT NULL,
  `unitCreator_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_9hh552e5h1g5ghbmeuhf1x56c` (`company_id`),
  KEY `FK_mbutjy8hwsfo3rqpowab4v3gj` (`file_id`),
  KEY `FK_sqmqb4g5bsftwdhb0qjwpn6nm` (`unitCreator_id`),
  CONSTRAINT `FK_9hh552e5h1g5ghbmeuhf1x56c` FOREIGN KEY (`company_id`) REFERENCES `fpdi_company` (`id`),
  CONSTRAINT `FK_mbutjy8hwsfo3rqpowab4v3gj` FOREIGN KEY (`file_id`) REFERENCES `fpdi_archive` (`id`),
  CONSTRAINT `FK_sqmqb4g5bsftwdhb0qjwpn6nm` FOREIGN KEY (`unitCreator_id`) REFERENCES `frisco_unit` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_process`
--

LOCK TABLES `frisco_process` WRITE;
/*!40000 ALTER TABLE `frisco_process` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_process` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_process_unit`
--

DROP TABLE IF EXISTS `frisco_process_unit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_process_unit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `process_id` bigint(20) DEFAULT NULL,
  `unit_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_9senwca0ss86k70i3lt38a5x1` (`process_id`),
  KEY `FK_gcy9fimli9fxtxqcj2xq00k5g` (`unit_id`),
  CONSTRAINT `FK_9senwca0ss86k70i3lt38a5x1` FOREIGN KEY (`process_id`) REFERENCES `frisco_process` (`id`),
  CONSTRAINT `FK_gcy9fimli9fxtxqcj2xq00k5g` FOREIGN KEY (`unit_id`) REFERENCES `frisco_unit` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_process_unit`
--

LOCK TABLES `frisco_process_unit` WRITE;
/*!40000 ALTER TABLE `frisco_process_unit` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_process_unit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_risk`
--

DROP TABLE IF EXISTS `frisco_risk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_risk` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `begin` datetime NOT NULL,
  `code` varchar(255) NOT NULL,
  `impact` varchar(400) NOT NULL,
  `linkFPDI` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `periodicity` varchar(400) NOT NULL,
  `probability` varchar(400) NOT NULL,
  `reason` varchar(4000) NOT NULL,
  `result` varchar(4000) NOT NULL,
  `risk_act_process` bit(1) NOT NULL,
  `risk_obj_process` bit(1) NOT NULL,
  `risk_pdi` bit(1) NOT NULL,
  `tipology` varchar(400) NOT NULL,
  `type` varchar(4010) NOT NULL,
  `riskLevel_id` bigint(20) NOT NULL,
  `unit_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_k7lmlbtprj4iouygk0lxyp2vr` (`riskLevel_id`),
  KEY `FK_13gojlpeuu4b4xuv88t2tf3y6` (`unit_id`),
  KEY `FK_7ttqp7tbygkj9px26577qad9h` (`user_id`),
  CONSTRAINT `FK_13gojlpeuu4b4xuv88t2tf3y6` FOREIGN KEY (`unit_id`) REFERENCES `frisco_unit` (`id`),
  CONSTRAINT `FK_7ttqp7tbygkj9px26577qad9h` FOREIGN KEY (`user_id`) REFERENCES `fpdi_user` (`id`),
  CONSTRAINT `FK_k7lmlbtprj4iouygk0lxyp2vr` FOREIGN KEY (`riskLevel_id`) REFERENCES `frisco_risk_level` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_risk`
--

LOCK TABLES `frisco_risk` WRITE;
/*!40000 ALTER TABLE `frisco_risk` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_risk` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_risk_activity`
--

DROP TABLE IF EXISTS `frisco_risk_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_risk_activity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `linkFPDI` varchar(1000) NOT NULL,
  `name` varchar(400) NOT NULL,
  `process_id` bigint(20) NOT NULL,
  `risk_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_oafiv78951vdep49gq1ljmd9u` (`process_id`),
  KEY `FK_nxbawrj9i2asb9rgnds8jt1n2` (`risk_id`),
  CONSTRAINT `FK_nxbawrj9i2asb9rgnds8jt1n2` FOREIGN KEY (`risk_id`) REFERENCES `frisco_risk` (`id`),
  CONSTRAINT `FK_oafiv78951vdep49gq1ljmd9u` FOREIGN KEY (`process_id`) REFERENCES `frisco_process` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_risk_activity`
--

LOCK TABLES `frisco_risk_activity` WRITE;
/*!40000 ALTER TABLE `frisco_risk_activity` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_risk_activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_risk_history`
--

DROP TABLE IF EXISTS `frisco_risk_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_risk_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `month` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `threat` bit(1) NOT NULL,
  `year` int(11) NOT NULL,
  `riskLevel_id` bigint(20) NOT NULL,
  `unit_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_qgfmkmn1a3qev6lc4id304064` (`riskLevel_id`),
  KEY `FK_p34p1wxt3smes1udxxoqr981h` (`unit_id`),
  CONSTRAINT `FK_p34p1wxt3smes1udxxoqr981h` FOREIGN KEY (`unit_id`) REFERENCES `frisco_unit` (`id`),
  CONSTRAINT `FK_qgfmkmn1a3qev6lc4id304064` FOREIGN KEY (`riskLevel_id`) REFERENCES `frisco_risk_level` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_risk_history`
--

LOCK TABLES `frisco_risk_history` WRITE;
/*!40000 ALTER TABLE `frisco_risk_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_risk_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_risk_level`
--

DROP TABLE IF EXISTS `frisco_risk_level`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_risk_level` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `color` int(11) NOT NULL,
  `level` varchar(255) NOT NULL,
  `policy_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_edlyijsb66hel0okueoqn3x0q` (`policy_id`),
  CONSTRAINT `FK_edlyijsb66hel0okueoqn3x0q` FOREIGN KEY (`policy_id`) REFERENCES `frisco_policy` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_risk_level`
--

LOCK TABLES `frisco_risk_level` WRITE;
/*!40000 ALTER TABLE `frisco_risk_level` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_risk_level` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_risk_process`
--

DROP TABLE IF EXISTS `frisco_risk_process`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_risk_process` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `linkFPDI` varchar(1000) NOT NULL,
  `process_id` bigint(20) NOT NULL,
  `risk_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_mg5rwcyrier8lpim1vak3s8mc` (`process_id`),
  KEY `FK_btvgjwujwk5pknc50d6s79d3f` (`risk_id`),
  CONSTRAINT `FK_btvgjwujwk5pknc50d6s79d3f` FOREIGN KEY (`risk_id`) REFERENCES `frisco_risk` (`id`),
  CONSTRAINT `FK_mg5rwcyrier8lpim1vak3s8mc` FOREIGN KEY (`process_id`) REFERENCES `frisco_process` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_risk_process`
--

LOCK TABLES `frisco_risk_process` WRITE;
/*!40000 ALTER TABLE `frisco_risk_process` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_risk_process` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_risk_strategy`
--

DROP TABLE IF EXISTS `frisco_risk_strategy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_risk_strategy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `linkFPDI` varchar(1000) NOT NULL,
  `name` varchar(1000) NOT NULL,
  `risk_id` bigint(20) NOT NULL,
  `structure_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_iwgppbujays7bm87o8y05822o` (`risk_id`),
  KEY `FK_aryrn2pf2c4dkukafryfw15h` (`structure_id`),
  CONSTRAINT `FK_aryrn2pf2c4dkukafryfw15h` FOREIGN KEY (`structure_id`) REFERENCES `fpdi_structure_level_instance` (`id`),
  CONSTRAINT `FK_iwgppbujays7bm87o8y05822o` FOREIGN KEY (`risk_id`) REFERENCES `frisco_risk` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_risk_strategy`
--

LOCK TABLES `frisco_risk_strategy` WRITE;
/*!40000 ALTER TABLE `frisco_risk_strategy` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_risk_strategy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_risk_tipology`
--

DROP TABLE IF EXISTS `frisco_risk_tipology`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_risk_tipology` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `name` varchar(4000) NOT NULL,
  `company_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_9q35ujia51kdbe1wvoc1u5l64` (`company_id`),
  CONSTRAINT `FK_9q35ujia51kdbe1wvoc1u5l64` FOREIGN KEY (`company_id`) REFERENCES `fpdi_company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_risk_tipology`
--

LOCK TABLES `frisco_risk_tipology` WRITE;
/*!40000 ALTER TABLE `frisco_risk_tipology` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_risk_tipology` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_subitem`
--

DROP TABLE IF EXISTS `frisco_subitem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_subitem` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `description` varchar(4000) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `item_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_w3draatouj8t3bbd4wl1goma` (`item_id`),
  CONSTRAINT `FK_w3draatouj8t3bbd4wl1goma` FOREIGN KEY (`item_id`) REFERENCES `frisco_item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_subitem`
--

LOCK TABLES `frisco_subitem` WRITE;
/*!40000 ALTER TABLE `frisco_subitem` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_subitem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_subitem_field`
--

DROP TABLE IF EXISTS `frisco_subitem_field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_subitem_field` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `description` longtext,
  `fileLink` varchar(400) DEFAULT NULL,
  `isText` bit(1) NOT NULL,
  `name` varchar(400) NOT NULL,
  `subitem_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m135nutpofhtyes04lp6pbxua` (`subitem_id`),
  CONSTRAINT `FK_m135nutpofhtyes04lp6pbxua` FOREIGN KEY (`subitem_id`) REFERENCES `frisco_subitem` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_subitem_field`
--

LOCK TABLES `frisco_subitem_field` WRITE;
/*!40000 ALTER TABLE `frisco_subitem_field` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_subitem_field` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frisco_unit`
--

DROP TABLE IF EXISTS `frisco_unit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `frisco_unit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `abbreviation` varchar(255) NOT NULL,
  `description` varchar(4000) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `planRisk_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_9gte6qy64gw87xda6qbk3xqy` (`parent_id`),
  KEY `FK_dwpc5yoktgq2vud9jlnawnubq` (`planRisk_id`),
  KEY `FK_p7kjojnbr1g650rv3ltrg6vdf` (`user_id`),
  CONSTRAINT `FK_9gte6qy64gw87xda6qbk3xqy` FOREIGN KEY (`parent_id`) REFERENCES `frisco_unit` (`id`),
  CONSTRAINT `FK_dwpc5yoktgq2vud9jlnawnubq` FOREIGN KEY (`planRisk_id`) REFERENCES `frisco_plan_risk` (`id`),
  CONSTRAINT `FK_p7kjojnbr1g650rv3ltrg6vdf` FOREIGN KEY (`user_id`) REFERENCES `fpdi_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frisco_unit`
--

LOCK TABLES `frisco_unit` WRITE;
/*!40000 ALTER TABLE `frisco_unit` DISABLE KEYS */;
/*!40000 ALTER TABLE `frisco_unit` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-01-21 18:12:25
