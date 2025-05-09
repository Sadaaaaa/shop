-- Insert test user (password is 'password' encoded with BCrypt)
INSERT INTO users (username, password, role)
VALUES ('user', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'USER')
ON CONFLICT (username) DO NOTHING;

INSERT INTO products (name, description, price, image) VALUES
('Смартфон Galaxy S21', 'Флагманский смартфон Samsung с отличной камерой', 799.99, null),
('Ноутбук MacBook Pro', '13-дюймовый ноутбук с M1 чипом', 1299.99, null),
('Наушники AirPods Pro', 'Беспроводные наушники с шумоподавлением', 249.99, null),
('Планшет iPad Air', '10.9-дюймовый планшет с чипом M1', 599.99, null),
('Умные часы Apple Watch', 'Series 7 с постоянно включенным дисплеем', 399.99, null),
('Игровая консоль PS5', 'Консоль нового поколения от Sony', 499.99, null),
('4K Телевизор LG OLED', '55-дюймовый OLED телевизор', 1499.99, null),
('Фотокамера Canon EOS', 'Профессиональная зеркальная камера', 1999.99, null),
('Робот-пылесос Roomba', 'Умный пылесос с автоматической зарядкой', 299.99, null),
('Кофемашина Delonghi', 'Автоматическая кофемашина с капучинатором', 699.99, null),
('Умная колонка Echo', 'Amazon Echo с голосовым помощником Alexa', 99.99, null),
('Фитнес-браслет Mi Band', 'Браслет для отслеживания активности', 49.99, null),
('Электросамокат Xiaomi', 'Складной электросамокат с большим запасом хода', 599.99, null),
('Монитор Dell UltraSharp', '27-дюймовый 4K монитор для работы', 699.99, null),
('Клавиатура Logitech', 'Механическая игровая клавиатура', 149.99, null),
('Мышь Razer', 'Беспроводная игровая мышь', 79.99, null),
('Портативная колонка JBL', 'Водонепроницаемая Bluetooth колонка', 129.99, null),
('Внешний SSD Samsung', 'Портативный SSD на 1TB', 199.99, null),
('Графический планшет Wacom', 'Планшет для цифрового рисования', 299.99, null),
('Веб-камера Logitech', 'HD веб-камера для стриминга', 89.99, null);