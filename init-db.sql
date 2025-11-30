-- Create schemas for microservices
CREATE SCHEMA IF NOT EXISTS customer_schema;
CREATE SCHEMA IF NOT EXISTS inventory_schema;
CREATE SCHEMA IF NOT EXISTS billing_schema;
CREATE SCHEMA IF NOT EXISTS chatbot_schema;

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA customer_schema TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA inventory_schema TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA billing_schema TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA chatbot_schema TO postgres;