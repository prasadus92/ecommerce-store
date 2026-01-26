-- Database initialization script for E-commerce Store
-- This script runs automatically when the PostgreSQL container starts for the first time

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE ecommerce_db TO ecommerce_user;

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(256) NOT NULL,
    description VARCHAR(1024),
    price DECIMAL(19, 4) NOT NULL CHECK (price > 0),
    sku VARCHAR(64) UNIQUE,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    buyer_email VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Order items table
CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(19, 4) NOT NULL CHECK (unit_price > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_products_active ON products(active);
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
CREATE INDEX IF NOT EXISTS idx_orders_buyer_email ON orders(buyer_email);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items(product_id);

-- Insert sample data for development
INSERT INTO products (name, description, price, sku, active) VALUES
    ('iPhone 15 Pro', 'Latest flagship smartphone from Apple with A17 Pro chip', 999.00, 'IPHONE-15-PRO', true),
    ('MacBook Pro 14"', 'Professional laptop with M3 Pro chip', 1999.00, 'MBP-14-M3', true),
    ('AirPods Pro', 'Wireless earbuds with active noise cancellation', 249.00, 'AIRPODS-PRO-2', true),
    ('iPad Air', 'Versatile tablet with M1 chip', 599.00, 'IPAD-AIR-M1', true),
    ('Apple Watch Ultra', 'Premium smartwatch for extreme sports', 799.00, 'WATCH-ULTRA-2', true)
ON CONFLICT (sku) DO NOTHING;
