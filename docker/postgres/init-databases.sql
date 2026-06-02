-- Tworzone przy pierwszym starcie kontenera PostgreSQL.
-- Użytkownik 'deliveryflow' (z POSTGRES_USER) jest właścicielem wszystkich baz.

CREATE DATABASE deliveryflow_orders;
CREATE DATABASE deliveryflow_tracking;
CREATE DATABASE deliveryflow_notifications;

GRANT ALL PRIVILEGES ON DATABASE deliveryflow_orders       TO deliveryflow;
GRANT ALL PRIVILEGES ON DATABASE deliveryflow_tracking     TO deliveryflow;
GRANT ALL PRIVILEGES ON DATABASE deliveryflow_notifications TO deliveryflow;
