USE daily_finance;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE transaction_record;
TRUNCATE TABLE monthly_budget;
TRUNCATE TABLE category;
TRUNCATE TABLE user_profile;
TRUNCATE TABLE account;
TRUNCATE TABLE `user`;
SET FOREIGN_KEY_CHECKS = 1;

SOURCE database/init-data.sql;
