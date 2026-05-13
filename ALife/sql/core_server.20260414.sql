
use `mysql`;

DROP DATABASE IF EXISTS `core_server`;
CREATE DATABASE  `core_server` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

use `core_server`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for config
-- ----------------------------
DROP TABLE IF EXISTS `config`;
CREATE TABLE `config`  (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数键名',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '参数键值',
  `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 113 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '参数配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of config
-- ----------------------------
INSERT INTO `config` VALUES (101, '目标距离偏差（米）', 'sys.OFD.rangeDDeviation', '0', 'Y', 'admin', '2026-04-11 18:00:00', NULL, NULL, NULL);
INSERT INTO `config` VALUES (102, '目标方位角偏差（度）', 'sys.OFD.azimuthDeviation', '0', 'Y', 'admin', '2026-04-11 18:00:00', NULL, NULL, '0-360度');
INSERT INTO `config` VALUES (103, '目标俯仰角偏差（度）', 'sys.OFD.elevationDeviation', '0', 'Y', 'admin', '2026-04-11 18:00:00', NULL, NULL, '0-360度');
INSERT INTO `config` VALUES (104, '反制区坐标集合', 'sys.zone.countermeasure', '[\r\n      [\r\n        114.433145,\r\n        22.696913\r\n      ],\r\n      [\r\n        114.440037,\r\n        22.692064\r\n      ],\r\n      [\r\n        114.441155,\r\n        22.692233\r\n      ],\r\n      [\r\n        114.441505,\r\n        22.692664\r\n      ],\r\n      [\r\n        114.441522,\r\n        22.693111\r\n      ],\r\n      [\r\n        114.441422,\r\n        22.693757\r\n      ],\r\n      [\r\n        114.441105,\r\n        22.694604\r\n      ],\r\n      [\r\n        114.440871,\r\n        22.695251\r\n      ],\r\n      [\r\n        114.440671,\r\n        22.695528\r\n      ],\r\n      [\r\n        114.44042,\r\n        22.695743\r\n      ],\r\n      [\r\n        114.440053,\r\n        22.69599\r\n      ],\r\n      [\r\n        114.439803,\r\n        22.696051\r\n      ],\r\n      [\r\n        114.439369,\r\n        22.696082\r\n      ],\r\n      [\r\n        114.438985,\r\n        22.696128\r\n      ],\r\n      [\r\n        114.437867,\r\n        22.696405\r\n      ],\r\n      [\r\n        114.437316,\r\n        22.696744\r\n      ],\r\n      [\r\n        114.436682,\r\n        22.697222\r\n      ],\r\n      [\r\n        114.436314,\r\n        22.697576\r\n      ],\r\n      [\r\n        114.436014,\r\n        22.697822\r\n      ],\r\n      [\r\n        114.435513,\r\n        22.697946\r\n      ],\r\n      [\r\n        114.435297,\r\n        22.698038\r\n      ],\r\n      [\r\n        114.435113,\r\n        22.698115\r\n      ],\r\n      [\r\n        114.434295,\r\n        22.698238\r\n      ],\r\n      [\r\n        114.433628,\r\n        22.698084\r\n      ],\r\n      [\r\n        114.433361,\r\n        22.697884\r\n      ],\r\n      [\r\n        114.433194,\r\n        22.697653\r\n      ],\r\n      [\r\n        114.433044,\r\n        22.697391\r\n      ],\r\n      [\r\n        114.433053,\r\n        22.697176\r\n      ]\r\n    ]', 'N', 'admin', '2026-04-11 18:00:00', NULL, NULL, NULL);
INSERT INTO `config` VALUES (105, '预警区坐标集合', 'sys.zone.warning', '[\r\n      [\r\n        114.42870597146204,\r\n        22.69767823621861\r\n      ],\r\n      [\r\n        114.44239162393143,\r\n        22.688223907947368\r\n      ],\r\n      [\r\n        114.44421275277217,\r\n        22.68892532576947\r\n      ],\r\n      [\r\n        114.4448895847641,\r\n        22.689691602515513\r\n      ],\r\n      [\r\n        114.44510383980901,\r\n        22.690379519739444\r\n      ],\r\n      [\r\n        114.44525669825697,\r\n        22.691393601999433\r\n      ],\r\n      [\r\n        114.44523682233533,\r\n        22.692809952893523\r\n      ],\r\n      [\r\n        114.44521856422637,\r\n        22.69407233714371\r\n      ],\r\n      [\r\n        114.44508781574626,\r\n        22.69464352303893\r\n      ],\r\n      [\r\n        114.44487894858402,\r\n        22.695103986419383\r\n      ],\r\n      [\r\n        114.44454762171058,\r\n        22.695691777750774\r\n      ],\r\n      [\r\n        114.44430257158523,\r\n        22.69584024805929\r\n      ],\r\n      [\r\n        114.44386971500859,\r\n        22.6958972698113\r\n      ],\r\n      [\r\n        114.44348857614081,\r\n        22.696036551790034\r\n      ],\r\n      [\r\n        114.44150960790977,\r\n        22.69905489970504\r\n      ],\r\n      [\r\n        114.4357913427942,\r\n        22.700982629641278\r\n      ],\r\n      [\r\n        114.43388138891443,\r\n        22.70075005022335\r\n      ],\r\n      [\r\n        114.4333864562509,\r\n        22.700999455626736\r\n      ],\r\n      [\r\n        114.43298411325351,\r\n        22.701155218734876\r\n      ],\r\n      [\r\n        114.43214482528701,\r\n        22.700936979761536\r\n      ],\r\n      [\r\n        114.43185340134721,\r\n        22.700941823195308\r\n      ],\r\n      [\r\n        114.43161420905625,\r\n        22.700952079971216\r\n      ],\r\n      [\r\n        114.43050736083066,\r\n        22.70067610384395\r\n      ],\r\n      [\r\n        114.42958953520014,\r\n        22.700079335282954\r\n      ],\r\n      [\r\n        114.42919966615978,\r\n        22.69960848876518\r\n      ],\r\n      [\r\n        114.42893595081672,\r\n        22.699122550265848\r\n      ],\r\n      [\r\n        114.42870087801614,\r\n        22.698585927723528\r\n      ],\r\n      [\r\n        114.42866084743675,\r\n        22.698175778321556\r\n      ]\r\n    ]', 'N', 'admin', '2026-04-11 18:00:00', NULL, NULL, NULL);
INSERT INTO `config` VALUES (106, '探测区坐标集合', 'sys.zone.detection', '[\r\n      [\r\n        114.42426694292408,\r\n        22.698443472437223\r\n      ],\r\n      [\r\n        114.44474624786285,\r\n        22.684383815894737\r\n      ],\r\n      [\r\n        114.44727050554437,\r\n        22.68561765153894\r\n      ],\r\n      [\r\n        114.44827416952819,\r\n        22.686719205031025\r\n      ],\r\n      [\r\n        114.448685679618,\r\n        22.687648039478894\r\n      ],\r\n      [\r\n        114.44909139651394,\r\n        22.68903020399886\r\n      ],\r\n      [\r\n        114.44936864467068,\r\n        22.691015905787044\r\n      ],\r\n      [\r\n        114.44956612845272,\r\n        22.69289367428742\r\n      ],\r\n      [\r\n        114.44950463149252,\r\n        22.69375904607786\r\n      ],\r\n      [\r\n        114.44933789716804,\r\n        22.694464972838766\r\n      ],\r\n      [\r\n        114.44904224342116,\r\n        22.69539355550155\r\n      ],\r\n      [\r\n        114.44880214317045,\r\n        22.695629496118578\r\n      ],\r\n      [\r\n        114.44837043001716,\r\n        22.6957125396226\r\n      ],\r\n      [\r\n        114.4479921522816,\r\n        22.695945103580065\r\n      ],\r\n      [\r\n        114.44515221581953,\r\n        22.70170479941008\r\n      ],\r\n      [\r\n        114.4342666855884,\r\n        22.705221259282553\r\n      ],\r\n      [\r\n        114.43108077782885,\r\n        22.704278100446693\r\n      ],\r\n      [\r\n        114.4304589125018,\r\n        22.704422911253474\r\n      ],\r\n      [\r\n        114.42995422650702,\r\n        22.704488437469752\r\n      ],\r\n      [\r\n        114.42877665057404,\r\n        22.703927959523075\r\n      ],\r\n      [\r\n        114.42840980269443,\r\n        22.70384564639062\r\n      ],\r\n      [\r\n        114.42811541811251,\r\n        22.703789159942428\r\n      ],\r\n      [\r\n        114.4267197216613,\r\n        22.703114207687904\r\n      ],\r\n      [\r\n        114.42555107040027,\r\n        22.702074670565906\r\n      ],\r\n      [\r\n        114.42503833231955,\r\n        22.70133297753036\r\n      ],\r\n      [\r\n        114.42467790163344,\r\n        22.700592100531697\r\n      ],\r\n      [\r\n        114.42435775603228,\r\n        22.699780855447052\r\n      ],\r\n      [\r\n        114.4242686948735,\r\n        22.69917555664311\r\n      ]\r\n    ]', 'N', 'admin', '2026-04-11 18:00:00', NULL, NULL, NULL);
INSERT INTO `config` VALUES (107, '雷达校北角（度）', 'sys.radar.northAngle', '0', 'N', 'admin', '2026-04-11 18:00:00', NULL, NULL, '雷达角度修正偏差值，范围：0-360度');
INSERT INTO `config` VALUES (108, '光电引导时长默认值（秒）', 'sys.OFD.guidanceDuration', '3', 'N', 'admin', '2026-04-11 18:00:00', NULL, NULL, '单位：秒');
INSERT INTO `config` VALUES (109, '光电视频录制时长默认值（秒）', 'sys.OFD.videoRecordingDuration', '5', 'N', 'admin', '2026-04-11 18:00:00', NULL, NULL, '单位：秒');
INSERT INTO `config` VALUES (110, '地图中心点', 'sys.map.centerPoint', '114.427761, 22.700272', 'N', 'admin', '2026-04-11 18:00:00', NULL, NULL, '区域绘制及管理功能，地图中心点经纬度配置');
INSERT INTO `config` VALUES (111, '地图默认展示比例', 'sys.map.defaultScale', '15', 'N', 'admin', '2026-04-11 18:00:00', NULL, NULL, '区域配置管理功能，地图默认比例');

-- ----------------------------
-- Table structure for dict_type
-- ----------------------------
DROP TABLE IF EXISTS `dict_type`;
CREATE TABLE `dict_type`  (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`dict_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1024 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dict_type
-- ----------------------------
INSERT INTO `dict_type` VALUES (1001, '用户性别', 'user_sex', '0', 'admin', '2025-06-29 22:00:00', '', NULL, '用户性别列表');
INSERT INTO `dict_type` VALUES (1002, '菜单状态', 'show_hide', '0', 'admin', '2025-06-29 22:00:00', '', NULL, '菜单状态列表');
INSERT INTO `dict_type` VALUES (1003, '系统开关', 'normal_disable', '0', 'admin', '2025-06-29 22:00:00', '', NULL, '系统开关列表');
INSERT INTO `dict_type` VALUES (1004, '任务状态', 'job_status', '0', 'admin', '2025-06-29 22:00:00', '', NULL, '任务状态列表');
INSERT INTO `dict_type` VALUES (1005, '任务分组', 'job_group', '0', 'admin', '2025-06-29 22:00:00', '', NULL, '任务分组列表');
INSERT INTO `dict_type` VALUES (1006, '系统是否', 'yes_no', '0', 'admin', '2025-06-29 22:00:00', '', NULL, '系统是否列表');
INSERT INTO `dict_type` VALUES (1007, '威胁等级', 'threat_level', '0', 'admin', '2025-06-29 22:00:00', '', NULL, '威胁等级');
INSERT INTO `dict_type` VALUES (1017, '管制区域', 'control_zone', '0', 'admin', '2025-06-29 22:00:00', '', NULL, '管制区域');
INSERT INTO `dict_type` VALUES (1018, '设备类型', 'device_type', '0', 'admin', '2025-06-29 22:00:00', '', NULL, '设备类型');
INSERT INTO `dict_type` VALUES (1019, '融合目标类型', 'fusion_target_type', '0', 'admin', '2025-06-29 22:00:00', '', NULL, '融合目标类型');
INSERT INTO `dict_type` VALUES (1022, '雷达目标类型', 'radar_target_type', '0', 'admin', '2025-06-29 22:00:00', '', NULL, '雷达目标类型');
INSERT INTO `dict_type` VALUES (1023, 'TDOA目标类型', 'tdoa_target_type', '0', 'admin', '2025-06-29 22:00:00', '', NULL, 'TDOA目标类型');

-- ----------------------------
-- Table structure for dict_data
-- ----------------------------
DROP TABLE IF EXISTS `dict_data`;
CREATE TABLE `dict_data`  (
  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int NULL DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10064 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典数据表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dict_data
-- ----------------------------
INSERT INTO `dict_data` VALUES (10001, 1, '男', 'MEN', 'user_sex', NULL, NULL, 'Y', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '性别男');
INSERT INTO `dict_data` VALUES (10002, 2, '女', 'WOMEN', 'user_sex', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '性别女');
INSERT INTO `dict_data` VALUES (10004, 1, '显示', 'SHOW', 'show_hide', NULL, 'primary', 'Y', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '显示菜单');
INSERT INTO `dict_data` VALUES (10005, 2, '隐藏', 'HIDE', 'show_hide', NULL, 'danger', 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '隐藏菜单');
INSERT INTO `dict_data` VALUES (10006, 1, '正常', 'NORMAL', 'normal_disable', NULL, 'primary', 'Y', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '正常状态');
INSERT INTO `dict_data` VALUES (10007, 2, '停用', 'DISABLE', 'normal_disable', NULL, 'danger', 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '停用状态');
INSERT INTO `dict_data` VALUES (10008, 1, '运行', 'RUN', 'job_status', NULL, 'primary', 'Y', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '运行状态');
INSERT INTO `dict_data` VALUES (10009, 2, '停止', 'STOP', 'job_status', NULL, 'danger', 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '停止状态');
INSERT INTO `dict_data` VALUES (10010, 1, '默认', 'DEFAULT', 'job_group', NULL, NULL, 'Y', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '默认分组');
INSERT INTO `dict_data` VALUES (10011, 2, '系统', 'SYSTEM', 'job_group', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '系统分组');
INSERT INTO `dict_data` VALUES (10012, 1, '是', 'Y', 'yes_no', NULL, 'primary', 'Y', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '是');
INSERT INTO `dict_data` VALUES (10013, 2, '否', 'N', 'yes_no', NULL, 'danger', 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '否');
INSERT INTO `dict_data` VALUES (10020, 1, '光电', 'OFD', 'device_type', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '光电');
INSERT INTO `dict_data` VALUES (10021, 2, '雷达', 'RADAR', 'device_type', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '雷达');
INSERT INTO `dict_data` VALUES (10022, 3, '电侦', 'ELINT', 'device_type', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '电侦');
INSERT INTO `dict_data` VALUES (10023, 4, 'TDOA', 'TDOA', 'device_type', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, 'TDOA');
INSERT INTO `dict_data` VALUES (10024, 5, '导航诱骗', 'SPOOFING', 'device_type', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '导航诱骗');
INSERT INTO `dict_data` VALUES (10025, 6, '高能激光', 'ATP', 'device_type', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '高能激光');
INSERT INTO `dict_data` VALUES (10026, 7, '全景摄像头', 'CAMERA', 'device_type', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '全景摄像头');
INSERT INTO `dict_data` VALUES (10027, 0, '未知目标', '0', 'radar_target_type', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, NULL);
INSERT INTO `dict_data` VALUES (10028, 0, '人', '11', 'radar_target_type', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, NULL);
INSERT INTO `dict_data` VALUES (10029, 0, '车', '12', 'radar_target_type', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, NULL);
INSERT INTO `dict_data` VALUES (10030, 0, '无人机', '20', 'radar_target_type', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, NULL);
INSERT INTO `dict_data` VALUES (10031, 0, '鸟', '24', 'radar_target_type', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, NULL);
INSERT INTO `dict_data` VALUES (10032, 0, '船只', '31', 'radar_target_type', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, NULL);
INSERT INTO `dict_data` VALUES (10033, 0, '其他目标', '40', 'radar_target_type', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, NULL);
INSERT INTO `dict_data` VALUES (10034, 0, '未知目标', 'None', 'tdoa_target_type', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, NULL);
INSERT INTO `dict_data` VALUES (10035, 0, '无人机', 'Drone', 'tdoa_target_type', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, NULL);
INSERT INTO `dict_data` VALUES (10036, 0, '未知目标', 'None', 'fusion_target_type', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, NULL);
INSERT INTO `dict_data` VALUES (10037, 0, '无人机', 'Drone', 'fusion_target_type', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, NULL);
INSERT INTO `dict_data` VALUES (10041, 1, '高危', 'HIGH', 'threat_level', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '高危');
INSERT INTO `dict_data` VALUES (10042, 2, '中危', 'MEDIUM', 'threat_level', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '中危');
INSERT INTO `dict_data` VALUES (10043, 3, '低危', 'LOW', 'threat_level', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '低危');
INSERT INTO `dict_data` VALUES (10044, 4, '无威胁', 'NONE', 'threat_level', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '未定级');
INSERT INTO `dict_data` VALUES (10051, 1, '反制区', 'COUNTERMEASURE', 'control_zone', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '反制区');
INSERT INTO `dict_data` VALUES (10052, 2, '预警区', 'WARNING', 'control_zone', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '预警区');
INSERT INTO `dict_data` VALUES (10053, 3, '探测区', 'DETECTION', 'control_zone', NULL, NULL, 'N', '0', 'admin', '2025-05-24 23:00:00', NULL, NULL, '探测区');
INSERT INTO `dict_data` VALUES (10063, 4, '区域外', 'OUTSIDE', 'control_zone', NULL, NULL, 'N', '0', '', NULL, '', NULL, NULL);

-- ----------------------------
-- Table structure for target_monitor_stat
-- ----------------------------
DROP TABLE IF EXISTS `target_monitor_stat`;
CREATE TABLE `target_monitor_stat`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `stat_time` date NOT NULL COMMENT '统计时间点（每天一条）',
  `radar_target_count` int NOT NULL DEFAULT 0 COMMENT '雷达监控无人机目标个数',
  `tdoa_target_count` int NOT NULL DEFAULT 0 COMMENT 'TDOA监控无人机目标个数',
  `fusion_target_count` int NOT NULL DEFAULT 0 COMMENT '融合后无人机目标个数',
  `radar_illegal_count` int NOT NULL DEFAULT 0 COMMENT '雷达监控非法无人机个数',
  `tdoa_illegal_count` int NOT NULL DEFAULT 0 COMMENT 'TDOA监控非法无人机个数',
  `fusion_illegal_count` int NOT NULL DEFAULT 0 COMMENT '融合后非法无人机目标个数',
  `need_dispose_count` int NOT NULL DEFAULT 0 COMMENT '应处置数量',
  `effective_dispose_count` int NOT NULL DEFAULT 0 COMMENT '有效处置数量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stat_time`(`stat_time` ASC) USING BTREE COMMENT '每天一个时间，防止重复统计',
  INDEX `idx_create`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1000443 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '目标监控统计报表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of target_monitor_stat
-- ----------------------------
INSERT INTO `target_monitor_stat` VALUES (1000011, '2026-04-06', 0, 0, 0, 0, 0, 0, 0, 0, '2026-04-06 21:57:59', '2026-04-13 13:58:42');
INSERT INTO `target_monitor_stat` VALUES (1000012, '2026-04-07', 0, 0, 0, 0, 0, 0, 0, 0, '2026-04-07 21:57:59', '2026-04-13 13:58:45');
INSERT INTO `target_monitor_stat` VALUES (1000013, '2026-04-08', 0, 0, 0, 0, 0, 0, 0, 0, '2026-04-08 21:57:59', '2026-04-13 13:58:48');
INSERT INTO `target_monitor_stat` VALUES (1000014, '2026-04-09', 0, 0, 0, 0, 0, 0, 0, 0, '2026-04-09 21:57:59', '2026-04-13 13:58:51');
INSERT INTO `target_monitor_stat` VALUES (1000015, '2026-04-10', 0, 0, 0, 0, 0, 0, 0, 0, '2026-04-10 21:57:59', '2026-04-13 13:58:53');
INSERT INTO `target_monitor_stat` VALUES (1000016, '2026-04-11', 0, 0, 0, 0, 0, 0, 0, 0, '2026-04-11 21:57:59', '2026-04-13 13:58:56');
INSERT INTO `target_monitor_stat` VALUES (1000021, '2026-04-12', 0, 22, 0, 0, 0, 0, 0, 0, '2026-04-12 21:57:59', '2026-04-13 13:58:30');
INSERT INTO `target_monitor_stat` VALUES (1000026, '2026-04-13', 3, 24, 0, 3, 1, 0, 0, 0, '2026-04-13 00:02:59', '2026-04-13 23:57:59');
INSERT INTO `target_monitor_stat` VALUES (1000314, '2026-04-14', 14, 5, 0, 14, 0, 0, 0, 0, '2026-04-14 00:02:59', '2026-04-14 10:42:59');

-- ----------------------------
-- Table structure for data_ofd
-- ----------------------------
DROP TABLE IF EXISTS `data_ofd`;
CREATE TABLE `data_ofd`  (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'UUID主键，全局唯一标识符',
  `device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备ID',
  `timestamp` datetime NOT NULL COMMENT '时间戳',
  `visible_light_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '可见光RTSP地址',
  `visible_light_status` tinyint(1) NULL DEFAULT 0 COMMENT '可见光状态：0-关闭，1-开启',
  `width` int NULL DEFAULT 0 COMMENT '视频宽度',
  `height` int NULL DEFAULT 0 COMMENT '视频高度',
  `fps` int NULL DEFAULT 0 COMMENT '帧率',
  `codec` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '编码格式，如H264/H265',
  `resolution` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分辨率，如1920x1080',
  `current_stream_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'visible' COMMENT '当前流类型：visible-可见光，infrared-红外',
  `visible_light_frame_count` bigint NULL DEFAULT 0 COMMENT '可见光帧计数',
  `visible_light_frame` longblob NULL COMMENT '可见光帧数据',
  `laser_distance` double NULL DEFAULT 0 COMMENT '激光测距值，单位：米',
  `azimuth_angle` double NULL DEFAULT 0 COMMENT '方位角，范围：0-360度',
  `pitch_angle` double NULL DEFAULT 0 COMMENT '俯仰角，范围：-90到+90度',
  `azimuth_speed` double NULL DEFAULT 0 COMMENT '方位角速度，单位：度/秒',
  `pitch_speed` double NULL DEFAULT 0 COMMENT '俯仰角速度，单位：度/秒',
  `azimuth_error` double NULL DEFAULT 0 COMMENT '方位跟踪误差，单位：度',
  `pitch_error` double NULL DEFAULT 0 COMMENT '俯仰跟踪误差，单位：度',
  `auto_mode` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'MANUAL' COMMENT '自动模式状态：AUTO-自动，MANUAL-手动',
  `servo_mode` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'MANUAL' COMMENT '伺服模式：TRACK-跟踪，SCAN-扫描，MANUAL-手动',
  `tracking_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'SEARCHING' COMMENT '跟踪状态：SEARCHING-搜索中，TRACKING-跟踪中，LOST-丢失',
  `tracking_channel` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '跟踪通道：INFRARED-红外，TV-电视',
  `servo_power_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'OFF' COMMENT '伺服电源状态：ON-开启，OFF-关闭',
  `servo_ready_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'NOT_READY' COMMENT '伺服就绪状态：READY-就绪，NOT_READY-未就绪',
  `command_execute_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'SUCCESS' COMMENT '命令执行状态：SUCCESS-成功，FAIL-失败，PENDING-执行中',
  `command_response_message` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '命令响应消息',
  `is_correlation` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '跟踪算法：CORRELATION-相关，其他',
  `is_polarity_black` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'BLACK' COMMENT '目标极性：BLACK-黑目标，WHITE-白目标',
  `server_power_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '伺服电源状态（冗余字段）',
  `laser_energy` double NULL DEFAULT 0 COMMENT '激光能量',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`, `timestamp`) USING BTREE,
  INDEX `idx_device_id`(`device_id` ASC) USING BTREE,
  INDEX `idx_timestamp`(`timestamp` ASC) USING BTREE,
  INDEX `idx_device_timestamp`(`device_id` ASC, `timestamp` ASC) USING BTREE,
  INDEX `idx_tracking_status`(`tracking_status` ASC) USING BTREE,
  INDEX `idx_servo_mode`(`servo_mode` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '光电设备数据表' ROW_FORMAT = Dynamic 
PARTITION BY RANGE (to_days(`timestamp`))
(
PARTITION `p2026w17` VALUES LESS THAN (TO_DAYS('2026-04-20')) ENGINE = InnoDB COMMENT = '2026年第16周（周一2026-04-13至周日2026-04-19）',
PARTITION `p_future` VALUES LESS THAN (MAXVALUE) ENGINE = InnoDB COMMENT = '未来数据分区' MAX_ROWS = 0 MIN_ROWS = 0 )
;

-- ----------------------------
-- Records of data_ofd
-- ----------------------------

-- ----------------------------
-- Table structure for data_radar_target
-- ----------------------------
DROP TABLE IF EXISTS `data_radar_target`;
CREATE TABLE `data_radar_target`  (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'UUID主键，全局唯一标识符',
  `device_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '雷达设备编号（例：RADAR01）',
  `target_batch` bigint NOT NULL COMMENT '设备原生目标批次号',
  `target_id` int NOT NULL COMMENT '设备原生目标ID（例：53/55/79）',
  `timestamp` datetime(6) NOT NULL COMMENT '数据上报时间戳（微秒精度），表分区依据字段',
  `protocol_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '协议类型（例：Protocol1）',
  `frame_count` int NULL DEFAULT NULL COMMENT '数据帧计数',
  `search_direction` decimal(12, 6) NULL DEFAULT NULL COMMENT '雷达搜索方向角度',
  `search_cycle` int NULL DEFAULT NULL COMMENT '雷达搜索周期',
  `pulse_group_number` int NULL DEFAULT NULL COMMENT '脉冲组号',
  `total_target_count` int NULL DEFAULT NULL COMMENT '本次上报总目标数量',
  `valid_target_count` int NULL DEFAULT NULL COMMENT '本次上报有效目标数量',
  `is_active` tinyint NULL DEFAULT NULL COMMENT '设备激活状态：0-未激活 1-已激活',
  `snr` decimal(15, 6) NULL DEFAULT NULL COMMENT '信噪比',
  `range` decimal(10, 2) NULL DEFAULT NULL COMMENT '目标距离（米）',
  `azimuth2` decimal(15, 6) NULL DEFAULT NULL COMMENT '方位角',
  `pitch` decimal(15, 6) NULL DEFAULT NULL COMMENT '俯仰角',
  `speed` decimal(10, 6) NULL DEFAULT NULL COMMENT '目标速度',
  `altitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '目标高度',
  `target_lat` decimal(15, 12) NULL DEFAULT NULL COMMENT '目标纬度坐标',
  `target_lon` decimal(15, 12) NULL DEFAULT NULL COMMENT '目标经度坐标',
  `target_type` int NULL DEFAULT NULL COMMENT '目标类型编码',
  `selection_flag` int NULL DEFAULT NULL COMMENT '目标选择标志',
  `x_speed` decimal(10, 6) NULL DEFAULT NULL COMMENT 'X轴方向速度分量',
  `y_speed` decimal(10, 6) NULL DEFAULT NULL COMMENT 'Y轴方向速度分量',
  `z_speed` decimal(10, 6) NULL DEFAULT NULL COMMENT 'Z轴方向速度分量',
  `is_delete` tinyint NULL DEFAULT 0 COMMENT '数据状态标记：0-正常 1-已删除（仅标记不物理删除）',
  PRIMARY KEY (`id`, `timestamp`) USING BTREE,
  INDEX `idx_device_timestamp`(`device_id` ASC, `timestamp` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '雷达目标全量数据表' ROW_FORMAT = Dynamic 
PARTITION BY RANGE (to_days(`timestamp`))
(
PARTITION `p2026w17` VALUES LESS THAN (TO_DAYS('2026-04-20')) ENGINE = InnoDB COMMENT = '2026年第17周（周一2026-04-13至周日2026-04-19）',
PARTITION `p_future` VALUES LESS THAN (MAXVALUE) ENGINE = InnoDB COMMENT = '未来数据分区' MAX_ROWS = 0 MIN_ROWS = 0 )
;

-- ----------------------------
-- Records of data_radar_target
-- ----------------------------

-- ----------------------------
-- Table structure for data_tdoa_target
-- ----------------------------
DROP TABLE IF EXISTS `data_tdoa_target`;
CREATE TABLE `data_tdoa_target`  (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'UUID主键，全局唯一标识符',
  `target_batch` bigint NOT NULL COMMENT '设备原生目标批次号',
  `uav_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '无人机唯一标识（序列号）',
  `uav_model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '无人机型号（例：DJI Mavic 3 Pro）',
  `uav_model_no` int NULL DEFAULT NULL COMMENT '无人机型号编号',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '飞手注册编号（空表示未获取）',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '轨迹追踪ID',
  `uav_lon` decimal(15, 12) NULL DEFAULT NULL COMMENT '无人机经度坐标（WGS84坐标系）',
  `uav_lat` decimal(15, 12) NULL DEFAULT NULL COMMENT '无人机纬度坐标（WGS84坐标系）',
  `uav_alt` decimal(10, 2) NULL DEFAULT NULL COMMENT '无人机海拔高度（米）',
  `uav_height` decimal(10, 2) NULL DEFAULT NULL COMMENT '无人机相对高度（米）',
  `velocity` decimal(10, 2) NULL DEFAULT NULL COMMENT '无人机速度（米/秒）',
  `yaw` decimal(10, 2) NULL DEFAULT NULL COMMENT '无人机偏航角（度，正北为0°，顺时针360°）',
  `pilot_lon` decimal(15, 12) NULL DEFAULT NULL COMMENT '飞手经度坐标',
  `pilot_lat` decimal(15, 12) NULL DEFAULT NULL COMMENT '飞手纬度坐标',
  `home_lon` decimal(15, 12) NULL DEFAULT NULL COMMENT '返航点经度坐标',
  `home_lat` decimal(15, 12) NULL DEFAULT NULL COMMENT '返航点纬度坐标',
  `timestamp` datetime(6) NOT NULL COMMENT '数据上报时间戳（微秒精度），表分区依据字段',
  `start_from` bigint NULL DEFAULT NULL COMMENT '本次飞行开始时间（毫秒时间戳）',
  `duration` int NULL DEFAULT NULL COMMENT '飞行持续时间（秒）',
  `frequency` bigint NULL DEFAULT NULL COMMENT '数据上报频率（Hz）',
  `area_flag` tinyint NULL DEFAULT NULL COMMENT '区域标记（位掩码）：1=探测区 2=警戒区 4=反制区（可叠加）',
  `white_list_id` int NULL DEFAULT NULL COMMENT '白名单ID（在白名单中的无人机会标记此值）',
  `target_type` int NULL DEFAULT NULL COMMENT '目标类型编码',
  `sensor_topic` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'MQTT消息主题（传感器数据发送目标）',
  `sensor_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '传感器设备编号',
  `sensor_longitude` decimal(15, 12) NULL DEFAULT NULL COMMENT '传感器经度坐标（WGS84）',
  `sensor_latitude` decimal(15, 12) NULL DEFAULT NULL COMMENT '传感器纬度坐标（WGS84）',
  `sensor_altitude` decimal(10, 2) NULL DEFAULT NULL COMMENT '传感器海拔高度（米）',
  `uav_azimuth` decimal(10, 2) NULL DEFAULT NULL COMMENT '无人机相对传感器方位角（度，0°=正北 90°=正东 180°=正南 270°=正西）',
  `uav_distance` decimal(10, 2) NULL DEFAULT NULL COMMENT '无人机与传感器直线距离（米）',
  `device_uuid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备UUID（Extension.DeviceUUid）',
  `extension_device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '扩展设备ID（Extension.DeviceId）',
  PRIMARY KEY (`id`, `timestamp`) USING BTREE,
  INDEX `idx_uav_timestamp`(`uav_id` ASC, `timestamp` ASC) USING BTREE,
  INDEX `idx_trace_id`(`trace_id` ASC) USING BTREE,
  INDEX `idx_sensor_id`(`sensor_id` ASC) USING BTREE,
  INDEX `idx_area_flag`(`area_flag` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'TDOA目标全量数据表' ROW_FORMAT = Dynamic 
PARTITION BY RANGE (to_days(`timestamp`))
(
PARTITION `p2026w17` VALUES LESS THAN (TO_DAYS('2026-04-20')) ENGINE = InnoDB COMMENT = '2026年第17周（周一2026-04-13至周日2026-04-19）',
PARTITION `p_future` VALUES LESS THAN (MAXVALUE) ENGINE = InnoDB COMMENT = '未来数据分区' MAX_ROWS = 0 MIN_ROWS = 0 )
;

-- ----------------------------
-- Records of data_tdoa_target
-- ----------------------------

-- ----------------------------
-- Table structure for data_fusion_target
-- ----------------------------
DROP TABLE IF EXISTS `data_fusion_target`;
CREATE TABLE `data_fusion_target`  (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'UUID主键，全局唯一标识符',
  `target_batch` bigint NOT NULL COMMENT '设备原生目标批次号',
  `radar_target_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '雷达目标id',
  `tdoa_target_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'TDOA目标id',
  `timestamp` datetime(6) NOT NULL COMMENT '数据上报时间戳（微秒精度），表分区依据字段',
  `range` decimal(10, 2) NULL DEFAULT NULL COMMENT '目标距离（米）',
  `azimuth` decimal(15, 6) NULL DEFAULT NULL COMMENT '方位角',
  `pitch` decimal(15, 6) NULL DEFAULT NULL COMMENT '俯仰角',
  `speed` decimal(10, 6) NULL DEFAULT NULL COMMENT '目标速度',
  `altitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '目标高度',
  `target_lat` decimal(15, 12) NULL DEFAULT NULL COMMENT '目标纬度坐标',
  `target_lon` decimal(15, 12) NULL DEFAULT NULL COMMENT '目标经度坐标',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标类型',
  `frequency` bigint NULL DEFAULT NULL COMMENT '数据上报频率（Hz）',
  `start_from` bigint NULL DEFAULT NULL COMMENT '本次飞行开始时间（毫秒时间戳）',
  `duration` int NULL DEFAULT NULL COMMENT '飞行持续时间（秒）',
  `uav_model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '无人机型号（例：DJI Mavic 3 Pro）',
  `white_list_id` int NULL DEFAULT NULL COMMENT '白名单ID（在白名单中的无人机会标记此值）',
  PRIMARY KEY (`id`, `timestamp`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '融合目标全量数据表' ROW_FORMAT = Dynamic 
PARTITION BY RANGE (to_days(`timestamp`))
(
PARTITION `p2026w17` VALUES LESS THAN (TO_DAYS('2026-04-20')) ENGINE = InnoDB COMMENT = '2026年第17周（周一2026-04-13至周日2026-04-19）',
PARTITION `p_future` VALUES LESS THAN (MAXVALUE) ENGINE = InnoDB COMMENT = '未来数据分区' MAX_ROWS = 0 MIN_ROWS = 0 )
;

-- ----------------------------
-- Records of data_fusion_target
-- ----------------------------

-- ----------------------------
-- Table structure for exception_log
-- ----------------------------
DROP TABLE IF EXISTS `exception_log`;
CREATE TABLE `exception_log`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `opt_uri` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作 URI',
  `opt_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作方法',
  `request_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方法：Get，Post，Delete，Put',
  `request_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求参数',
  `opt_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作描述',
  `exception_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息',
  `ip_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ip 地址',
  `ip_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ip 来源',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '异常日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of exception_log
-- ----------------------------

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `opt_module` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作模块',
  `opt_uri` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作 URI',
  `opt_type` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作类型：新增、修改等',
  `opt_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作方法',
  `opt_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作描述',
  `request_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方法：GET，POST，DELETE，PUT',
  `request_param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数',
  `response_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '返回数据',
  `user_id` int NULL DEFAULT NULL COMMENT '操作用户 id',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作用户昵称',
  `ip_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作用户 id 地址',
  `ip_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作用户 ip 来源',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of operation_log
-- ----------------------------

-- ----------------------------
-- Table structure for permission
-- ----------------------------
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `permission_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `permission_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` tinyint(1) NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT NULL,
  `deleted` tinyint(1) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of permission
-- ----------------------------
INSERT INTO `permission` VALUES (1, '用户查看', 'USER_VIEW', 0, '2025-04-01 15:32:32', 0);
INSERT INTO `permission` VALUES (2, '用户添加', 'USER_ADD', 0, '2025-04-01 12:30:39', 0);
INSERT INTO `permission` VALUES (3, '用户删除', 'USER_DELETE', 0, '2025-04-02 21:34:50', 0);
INSERT INTO `permission` VALUES (4, '设备操控', 'DEVICE_MANIPULATE', 0, '2025-04-08 15:35:13', 0);

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色标识如ADMIN',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '0-正常 1-禁用',
  `create_time` datetime NULL DEFAULT NULL,
  `deleted` tinyint(1) NULL DEFAULT NULL COMMENT '0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, '超级管理员', 'ADMIN', 0, '2025-03-10 21:46:28', 0);
INSERT INTO `role` VALUES (2, '普通用户', 'NOMARL', 0, '2025-04-02 21:24:30', 0);
INSERT INTO `role` VALUES (3, '测试用户', 'TEST', 0, '2025-04-02 21:26:01', 0);
INSERT INTO `role` VALUES (4, '安全员', 'SECURITY', 0, '2025-04-02 21:46:40', 0);

-- ----------------------------
-- Table structure for role_permission
-- ----------------------------
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `role_id` int NOT NULL,
  `permission_id` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `role_permission_permission_fk2`(`permission_id` ASC) USING BTREE,
  INDEX `role_permission_role_fk`(`role_id` ASC) USING BTREE,
  CONSTRAINT `role_permission_permission_fk2` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `role_permission_role_fk` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of role_permission
-- ----------------------------
INSERT INTO `role_permission` VALUES (1, 1, 1);
INSERT INTO `role_permission` VALUES (2, 1, 2);
INSERT INTO `role_permission` VALUES (3, 1, 4);
INSERT INTO `role_permission` VALUES (4, 1, 3);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0-正常 1-禁用',
  `create_time` datetime NULL DEFAULT NULL,
  `deleted` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'test', '$2a$10$AiQ5VgTOWbAu4nGLaiMTO.ZGb0vaUWxgrm.y6GbgkwX6fvxrmXTVy', 0, '2025-03-10 21:46:43', 0);
INSERT INTO `user` VALUES (2, 'test1', '$2a$10$uTa.dZgFaiMUQGmiP2G8d.FDsOCZSwOWO53pJFttWnuBV9tep3bwu', 0, '2025-04-01 15:14:59', 0);
INSERT INTO `user` VALUES (3, 'test2', '$2a$10$yYWX6CSBevBBLLzP25WkseJhySgWtpRx33ftD5p.UwPo5YV43rPdC', 0, '2025-04-01 15:16:30', 0);
INSERT INTO `user` VALUES (4, 'test3', '$2a$10$UqJ9vZlIxCWRHMs/0jKVSePuU5/4xCQB6BtSdU5ZYRPq3yekLF8ii', 0, '2025-04-01 15:17:22', 1);

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `role_id` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_role_role_fk`(`role_id` ASC) USING BTREE,
  INDEX `user_role_user_fk`(`user_id` ASC) USING BTREE,
  CONSTRAINT `user_role_role_fk` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `user_role_user_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO `user_role` VALUES (1, 1, 1);
INSERT INTO `user_role` VALUES (2, 2, 1);



-- ----------------------------
-- 目标统计存储过程
-- ----------------------------

-- 删除旧存储过程（若存在），避免冲突
DROP PROCEDURE IF EXISTS p_target_stat;

-- 定义存储过程分隔符，避免与SQL语句分号冲突
DELIMITER //

-- 创建每天统计存储过程，无入参，自动统计前一天数据（确保当天数据完整）
CREATE PROCEDURE p_target_stat()
BEGIN
    -- 定义变量：统计日期（按天统计）、统计时间范围（当天00:00:00-23:59:59）
    DECLARE v_stat_date DATE;
    -- 定义8个指标变量，分别存储每个指标的统计结果（预留SQL查询位置）
    DECLARE v_radar_target INT DEFAULT 0;         -- 雷达监控无人机目标个数
    DECLARE v_tdoa_target INT DEFAULT 0;          -- TDOA监控无人机目标个数
    DECLARE v_fusion_target INT DEFAULT 0;        -- 融合后无人机目标个数
    DECLARE v_radar_illegal INT DEFAULT 0;        -- 雷达监控非法无人机个数
    DECLARE v_tdoa_illegal INT DEFAULT 0;         -- TDOA监控非法无人机个数
    DECLARE v_fusion_illegal INT DEFAULT 0;       -- 融合后非法无人机目标个数
    DECLARE v_need_dispose INT DEFAULT 0;         -- 应处置数量
    DECLARE v_effective_dispose INT DEFAULT 0;    -- 有效处置数量

    -- 1. 统计日期：默认统计前一天（确保当天所有数据已产生，避免统计不完整）
    SET v_stat_date = CURDATE();

    -- ==============================================
    -- 8个指标的独立SQL查询（已完善4个，预留4个），均统计当天（前一天）数据
    -- ==============================================
    -- 1. 雷达监控无人机目标个数（统计前一天数据）
    select count(1) INTO v_radar_target 
      from (select CONCAT(drt.target_batch,'_',drt.target_id) id 
            from data_radar_target drt 
            where drt.target_type = 20 
              and DATE_FORMAT(drt.`timestamp`, '%Y-%m-%d') = CURDATE() 
             group by CONCAT(drt.target_batch,'_',drt.target_id)) t;
    
    -- 2. TDOA监控无人机目标个数（统计前一天数据）
    select count(1) INTO v_tdoa_target 
      from (select CONCAT(dtt.uav_id,'_',dtt.trace_id) id 
            from data_tdoa_target dtt 
            where DATE_FORMAT(dtt.`timestamp`, '%Y-%m-%d') = CURDATE() 
             group by CONCAT(dtt.uav_id,'_',dtt.trace_id)) t;
    
    -- 3. 融合后无人机目标个数（预留SQL，自行完善，需统计前一天数据）
    -- SELECT COUNT(*) INTO v_fusion_target FROM 相关表 WHERE 条件 AND create_time BETWEEN v_start AND v_end;
    
    -- 4. 雷达监控非法无人机个数（统计前一天数据）
    select count(1) INTO v_radar_illegal 
      from (select CONCAT(drt.target_batch,'_',drt.target_id) id 
            from data_radar_target drt 
            where drt.target_type = 20 
              and DATE_FORMAT(drt.`timestamp`, '%Y-%m-%d') = CURDATE() 
             group by CONCAT(drt.target_batch,'_',drt.target_id)) t;
    
    -- 5. TDOA监控非法无人机个数（统计前一天数据）
    select count(1) INTO v_tdoa_illegal 
      from (select CONCAT(dtt.uav_id,'_',dtt.trace_id) id 
            from data_tdoa_target dtt 
            where dtt.uav_model = 'Drone' 
              and DATE_FORMAT(dtt.`timestamp`, '%Y-%m-%d') = CURDATE() 
             group by CONCAT(dtt.uav_id,'_',dtt.trace_id)) t;
    
    -- 6. 融合后非法无人机目标个数（预留SQL，自行完善，需统计前一天数据）
    -- SELECT COUNT(*) INTO v_fusion_illegal FROM 相关表 WHERE 条件 AND create_time BETWEEN v_start AND v_end;
    
    -- 7. 应处置数量（预留SQL，自行完善，需统计前一天数据）
    -- SELECT COUNT(*) INTO v_need_dispose FROM 相关表 WHERE 条件 AND create_time BETWEEN v_start AND v_end;
    
    -- 8. 有效处置数量（预留SQL，自行完善，需统计前一天数据）
    -- SELECT COUNT(*) INTO v_effective_dispose FROM 相关表 WHERE 条件 AND create_time BETWEEN v_start AND v_end;

    -- 4. 原子插入/更新统计数据（无锁操作，不阻塞主业务）
    -- 逻辑：当天统计记录存在则更新，不存在则新增（按stat_time唯一索引判断）
    INSERT INTO target_monitor_stat (
        stat_time,
        radar_target_count,
        tdoa_target_count,
        fusion_target_count,
        radar_illegal_count,
        tdoa_illegal_count,
        fusion_illegal_count,
        need_dispose_count,
        effective_dispose_count
    )
    VALUES (
        v_stat_date,  -- 按天统计，stat_time存储日期（格式：YYYY-MM-DD）
        v_radar_target,
        v_tdoa_target,
        v_fusion_target,
        v_radar_illegal,
        v_tdoa_illegal,
        v_fusion_illegal,
        v_need_dispose,
        v_effective_dispose
    )
    ON DUPLICATE KEY UPDATE
        -- 若该日期已存在统计记录，自动更新指标数据，确保幂等性
        radar_target_count = VALUES(radar_target_count),
        tdoa_target_count = VALUES(tdoa_target_count),
        fusion_target_count = VALUES(fusion_target_count),
        radar_illegal_count = VALUES(radar_illegal_count),
        tdoa_illegal_count = VALUES(tdoa_illegal_count),
        fusion_illegal_count = VALUES(fusion_illegal_count),
        need_dispose_count = VALUES(need_dispose_count),
        effective_dispose_count = VALUES(effective_dispose_count),
        update_time = NOW();  -- 更新记录的最后修改时间

END //

-- 恢复SQL语句默认分隔符
DELIMITER ;


-- ----------------------------
-- 创建分区存储过程 自动创建下一完整周的分区（幂等，可每日调用）
-- ----------------------------

DELIMITER $$

DROP PROCEDURE IF EXISTS `p_create_next_week_partitions`$$
CREATE PROCEDURE `p_create_next_week_partitions`()
BEGIN
    -- 变量定义
    DECLARE done INT DEFAULT FALSE;
    DECLARE tbl_name VARCHAR(64);
    DECLARE this_monday DATE;
    DECLARE boundary_monday DATE;          -- 分区边界（下下周一）
    DECLARE partition_week_start DATE;     -- 分区覆盖的起始周一
    DECLARE partition_week_end DATE;       -- 分区覆盖的结束周日
    DECLARE partition_name VARCHAR(32);
    DECLARE boundary_days INT;
    DECLARE comment_text VARCHAR(255);
    
    -- 游标：core_server 数据库中的四张表
    DECLARE table_cursor CURSOR FOR
        SELECT table_name FROM information_schema.tables
        WHERE table_schema = 'core_server'
          AND table_name IN ('data_ofd', 'data_radar_target', 'data_tdoa_target', 'data_fusion_target');
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- 1. 计算本周一的日期
    SET this_monday = DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY);
    
    -- 2. 分区边界 = 本周一 + 14天（即下下周一）
    SET boundary_monday = DATE_ADD(this_monday, INTERVAL 14 DAY);
    
    -- 3. 分区覆盖的周：从 boundary_monday 往前推一周（周一到周日）
    SET partition_week_start = DATE_SUB(boundary_monday, INTERVAL 7 DAY);
    SET partition_week_end = DATE_SUB(boundary_monday, INTERVAL 1 DAY);
    
    -- 4. 生成分区名：pYYYYwWW（使用 partition_week_start 的周数，符合 ISO 周一为一周起始）
    SET partition_name = CONCAT(
        'p', 
        YEAR(partition_week_start),
        'w',
        LPAD(WEEK(partition_week_start, 1), 2, '0')
    );
    
    -- 5. 分区边界值（TO_DAYS）
    SET boundary_days = TO_DAYS(boundary_monday);
    
    -- 6. 生成注释内容
    SET comment_text = CONCAT(
        YEAR(partition_week_start), '年第', LPAD(WEEK(partition_week_start, 1), 2, '0'), '周（周一',
        DATE_FORMAT(partition_week_start, '%Y-%m-%d'), '至周日',
        DATE_FORMAT(partition_week_end, '%Y-%m-%d'), '）'
    );
    
    -- 7. 遍历每张表
    OPEN table_cursor;
    read_loop: LOOP
        FETCH table_cursor INTO tbl_name;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 检查该表的下周分区是否已存在
        SET @check_sql = CONCAT(
            'SELECT COUNT(*) INTO @cnt FROM information_schema.partitions ',
            'WHERE table_schema = ''core_server'' AND table_name = ''', tbl_name, 
            ''' AND partition_name = ''', partition_name, ''''
        );
        PREPARE stmt_check FROM @check_sql;
        EXECUTE stmt_check;
        DEALLOCATE PREPARE stmt_check;
        
        -- 若不存在，则创建（重组 p_future 分区）
        IF @cnt = 0 THEN
            SET @alter_sql = CONCAT(
                'ALTER TABLE `core_server`.`', tbl_name, '` REORGANIZE PARTITION p_future INTO (',
                'PARTITION `', partition_name, '` VALUES LESS THAN (', boundary_days, ') ENGINE = InnoDB COMMENT ''', comment_text, ''',',
                'PARTITION `p_future` VALUES LESS THAN MAXVALUE ENGINE = InnoDB COMMENT ''未来数据分区''',
                ')'
            );
            PREPARE stmt_alter FROM @alter_sql;
            EXECUTE stmt_alter;
            DEALLOCATE PREPARE stmt_alter;
        END IF;
    END LOOP;
    
    CLOSE table_cursor;
END$$

DELIMITER ;

-- ----------------------------
-- 事件
-- ----------------------------

-- 开启事件调度
SET GLOBAL event_scheduler = ON;

-- 创建定时事件（每5分钟执行一次，调用统计目标数存储过程）
DROP EVENT IF EXISTS `e_target_stat`;
CREATE EVENT `e_target_stat`
ON SCHEDULE EVERY 5 MINUTE STARTS '2026-04-14 02:00:00'
ON COMPLETION PRESERVE
ENABLE
COMMENT '每5分钟统计一次目标数'
DO CALL p_target_stat();


-- 创建定时事件（每天凌晨 2:00 自动执行）调用创建表分区存储过程
DROP EVENT IF EXISTS `e_create_next_week_partitions`;
CREATE EVENT `e_create_next_week_partitions`
ON SCHEDULE EVERY 1 DAY STARTS '2026-04-14 02:00:00'
ON COMPLETION PRESERVE
ENABLE
COMMENT '每天检查并创建下一完整周的分区'
DO CALL p_create_next_week_partitions();



SET FOREIGN_KEY_CHECKS = 1;

-- 脚本执行完成




use `core_server`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 配置表
-- ----------------------------

-- ----------------------------
-- Table structure for geofence
-- ----------------------------
DROP TABLE IF EXISTS `geofence`;
CREATE TABLE `geofence`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'NU_禁飞区名称',
  `core_longitude` decimal(10, 7) NOT NULL COMMENT '核心区经度(-180.0~180.0)',
  `core_latitude` decimal(10, 7) NOT NULL COMMENT '核心区纬度(-90.0~90.0)',
  `core_radius` decimal(12, 2) NOT NULL COMMENT '核心区半径(>0)',
  `buffer_radius` decimal(12, 2) NOT NULL COMMENT '缓冲区半径(>core_radius)',
  `alert_radius` decimal(12, 2) NOT NULL COMMENT '告警区半径(>buffer_radius)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_coordinate`(`core_longitude` ASC, `core_latitude` ASC) USING BTREE,
  CONSTRAINT `chk_radius` CHECK ((`core_radius` > 0) and (`buffer_radius` > `core_radius`) and (`alert_radius` > `buffer_radius`))
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '禁飞区数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of geofence
-- ----------------------------
INSERT INTO `geofence` VALUES (1, '深圳坪山测试场', 114.4277000, 22.7002230, 600.00, 700.00, 800.00);

-- ----------------------------
-- 统计表
-- ----------------------------

-- ----------------------------
-- Table structure for daily_drone_data
-- ----------------------------
DROP TABLE IF EXISTS `daily_drone_data`;
CREATE TABLE `daily_drone_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `date` date NOT NULL,
  `drone_count` int NOT NULL DEFAULT 0,
  `illegal_drone_count` int NOT NULL DEFAULT 0,
  `disposed_drone_count` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `date`(`date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'NU_每日飞行物统计' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of daily_drone_data
-- ----------------------------

-- ----------------------------
-- Table structure for monthly_drone_data
-- ----------------------------
DROP TABLE IF EXISTS `monthly_drone_data`;
CREATE TABLE `monthly_drone_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `disposed_drone_count` int NOT NULL,
  `drone_count` int NOT NULL,
  `illegal_drone_count` int NOT NULL,
  `timestamp` datetime(6) NULL DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `year_month` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK95w2fe5ah738scevnpro67tdx`(`year_month` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'NU_每月飞行物统计' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of monthly_drone_data
-- ----------------------------

-- ----------------------------
-- 设备原始数据表
-- ----------------------------

-- ----------------------------
-- Table structure for device_electric_investigation_data
-- ----------------------------
DROP TABLE IF EXISTS `device_electric_investigation_data`;
CREATE TABLE `device_electric_investigation_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `original_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `timestamp` datetime(6) NULL DEFAULT NULL,
  `data_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'NU_电侦设备原始数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of device_electric_investigation_data
-- ----------------------------

-- ----------------------------
-- Table structure for device_photoelectric_data
-- ----------------------------
DROP TABLE IF EXISTS `device_photoelectric_data`;
CREATE TABLE `device_photoelectric_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `device_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `original_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `timestamp` datetime(6) NULL DEFAULT NULL,
  `data_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'NU_光电设备原始数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of device_photoelectric_data
-- ----------------------------

-- ----------------------------
-- Table structure for device_radar_data
-- ----------------------------
DROP TABLE IF EXISTS `device_radar_data`;
CREATE TABLE `device_radar_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `device_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `original_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `targets_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `timestamp` datetime(6) NULL DEFAULT NULL,
  `data_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'NU_雷达设备原始数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of device_radar_data
-- ----------------------------

-- ----------------------------
-- Table structure for device_video_frame
-- ----------------------------
DROP TABLE IF EXISTS `device_video_frame`;
CREATE TABLE `device_video_frame`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `device_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `frame_data` longblob NULL,
  `height` int NULL DEFAULT NULL,
  `source_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `timestamp` datetime(6) NULL DEFAULT NULL,
  `width` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'NU_全景设备原始数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of device_video_frame
-- ----------------------------

-- ----------------------------
-- 算法输出表
-- ----------------------------

-- ----------------------------
-- Table structure for algorithm_data_fusion
-- ----------------------------
DROP TABLE IF EXISTS `algorithm_data_fusion`;
CREATE TABLE `algorithm_data_fusion`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `target_id` int NOT NULL,
  `longitude` double NULL DEFAULT NULL,
  `latitude` double NULL DEFAULT NULL,
  `altitude` double NULL DEFAULT NULL,
  `velocity` double NULL DEFAULT NULL,
  `azimuth` double NULL DEFAULT NULL,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `distance` double NULL DEFAULT NULL,
  `pitch` double NULL DEFAULT NULL,
  `threat_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `pan_angle` double NULL DEFAULT NULL,
  `tilt_angle` double NULL DEFAULT NULL,
  `zoom_level` double NULL DEFAULT NULL,
  `color` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'NU_算法返回的融合数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of algorithm_data_fusion
-- ----------------------------

-- ----------------------------
-- Table structure for algorithm_geo_position_validator
-- ----------------------------
DROP TABLE IF EXISTS `algorithm_geo_position_validator`;
CREATE TABLE `algorithm_geo_position_validator`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `longitude` double NULL DEFAULT NULL,
  `latitude` double NULL DEFAULT NULL,
  `altitude` double NULL DEFAULT NULL,
  `warning_level` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'NU_算法返回的空间关系数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of algorithm_geo_position_validator
-- ----------------------------

-- ----------------------------
-- Table structure for algorithm_object_detection
-- ----------------------------
DROP TABLE IF EXISTS `algorithm_object_detection`;
CREATE TABLE `algorithm_object_detection`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `target_left` int NOT NULL,
  `target_top` int NOT NULL,
  `target_width` int NOT NULL,
  `target_height` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'NU_算法返回的目标识别数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of algorithm_object_detection
-- ----------------------------

-- ----------------------------
-- Table structure for algorithm_track_prediction
-- ----------------------------
DROP TABLE IF EXISTS `algorithm_track_prediction`;
CREATE TABLE `algorithm_track_prediction`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `target_id` bigint NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `altitude` double NOT NULL,
  `sequence_number` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_target_id`(`target_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'NU_算法返回的轨迹预测数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of algorithm_track_prediction
-- ----------------------------


SET FOREIGN_KEY_CHECKS = 1;

-- 脚本执行完成
