ALTER TABLE `share`
    MODIFY COLUMN `create_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL AFTER `expiry_date`;