INSERT INTO products (name, description, price, image)
VALUES ('Test Product 1', 'Test description 1', 10.99, NULL),
       ('Test Product 2', 'Test description 2', 20.49, NULL),
       ('Test Product 3', 'Test description 3', 15.75, NULL);

INSERT INTO carts (user_id) VALUES (1);
INSERT INTO orders (user_id, order_date, total_price) VALUES (1, CURRENT_TIMESTAMP(), 10.99);