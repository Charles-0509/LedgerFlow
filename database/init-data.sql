USE daily_finance;

INSERT INTO `user` (id, username, password_hash, nickname, status)
VALUES
  (1, 'demo', '$2a$10$cYLM.qoXpeAzcZhJ3oXRLu9Slkb61LHyWW5qJ4QKvHEMhaxZ5qCPi', '演示用户', 1)
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname);

INSERT INTO account (id, user_id, name, type, balance, is_default)
VALUES
  (1, 1, '现金账户', 'CASH', 1000.00, 1),
  (2, 1, '银行卡', 'BANK_CARD', 5200.00, 0),
  (3, 1, '支付宝', 'ALIPAY', 800.00, 0)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO user_profile (user_id, default_monthly_budget, currency, default_account_id)
VALUES (1, 3500.00, 'CNY', 1)
ON DUPLICATE KEY UPDATE default_monthly_budget = VALUES(default_monthly_budget), currency = VALUES(currency);

INSERT INTO category (id, user_id, name, type, icon, color, is_system)
VALUES
  (1, 1, '工资', 'INCOME', 'Wallet', '#2f855a', 1),
  (2, 1, '奖金', 'INCOME', 'Money', '#38a169', 1),
  (3, 1, '兼职', 'INCOME', 'Briefcase', '#319795', 1),
  (4, 1, '理财收益', 'INCOME', 'TrendCharts', '#3182ce', 1),
  (5, 1, '餐饮', 'EXPENSE', 'Bowl', '#e53e3e', 1),
  (6, 1, '交通', 'EXPENSE', 'Van', '#dd6b20', 1),
  (7, 1, '购物', 'EXPENSE', 'ShoppingCart', '#805ad5', 1),
  (8, 1, '学习', 'EXPENSE', 'Reading', '#2b6cb0', 1),
  (9, 1, '医疗', 'EXPENSE', 'FirstAidKit', '#c53030', 1),
  (10, 1, '娱乐', 'EXPENSE', 'VideoPlay', '#d69e2e', 1),
  (11, 1, '住房', 'EXPENSE', 'House', '#4a5568', 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO monthly_budget (user_id, budget_month, amount)
VALUES
  (1, DATE_FORMAT(CURRENT_DATE(), '%Y-%m'), 3500.00)
ON DUPLICATE KEY UPDATE amount = VALUES(amount);

INSERT INTO transaction_record (id, user_id, type, amount, category_id, account_id, record_date, note)
VALUES
  (1, 1, 'INCOME', 8500.00, 1, 2, DATE_FORMAT(CURRENT_DATE(), '%Y-%m-05'), '本月工资'),
  (2, 1, 'EXPENSE', 36.50, 5, 3, CURRENT_DATE(), '午餐'),
  (3, 1, 'EXPENSE', 12.00, 6, 3, CURRENT_DATE(), '地铁'),
  (4, 1, 'EXPENSE', 199.00, 8, 2, DATE_SUB(CURRENT_DATE(), INTERVAL 2 DAY), '课程资料'),
  (5, 1, 'EXPENSE', 86.00, 7, 3, DATE_SUB(CURRENT_DATE(), INTERVAL 4 DAY), '日用品')
ON DUPLICATE KEY UPDATE
  amount = VALUES(amount),
  record_date = VALUES(record_date),
  note = VALUES(note);
