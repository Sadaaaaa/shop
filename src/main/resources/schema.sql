DROP TABLE IF EXISTS cart_items CASCADE;
DROP TABLE IF EXISTS carts CASCADE;
DROP TABLE IF EXISTS carts_items CASCADE;
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS products CASCADE;

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    image BYTEA
);

CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT
);

CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    quantity INTEGER NOT NULL,
    product_id BIGINT REFERENCES products(id)
);

CREATE TABLE carts_items (
    cart_id BIGINT REFERENCES carts(id),
    items_id BIGINT UNIQUE REFERENCES cart_items(id)
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_date TIMESTAMP,
    total_price DECIMAL(10,2)
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    product_id BIGINT REFERENCES products(id),
    quantity INTEGER,
    price DECIMAL(10,2)
);