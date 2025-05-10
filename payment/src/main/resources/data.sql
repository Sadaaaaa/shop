INSERT INTO payment_account (user_id, amount)
VALUES (1, 2000.00),
       (2, 5000.00) ON CONFLICT (user_id) DO NOTHING;