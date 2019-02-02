
INSERT INTO user (id, user_name, first_name, surname, password, role) VALUES (1, "Pavel", "Pavel", "Novak", "12345", "");

INSERT INTO car (id, user_id, name, icon) VALUES (1, 1, "Trabant", "");
INSERT INTO car (id, user_id, name, icon) VALUES (2, 1, "Favorit", "");

INSERT INTO route (id, length, car_id, note) VALUES (1, 0, 1, "car 1");
INSERT INTO route (id, length, car_id, note) VALUES (2, 0, 1, "car 2");
INSERT INTO route (id, length, car_id, note) VALUES (3, 0, 2, "car 3");
INSERT INTO route (id, length, car_id, note) VALUES (4, 0, 1, "car 4");
INSERT INTO route (id, length, car_id, note) VALUES (5, 0, 2, "car 5");

INSERT INTO event_type (id, description, name) VALUES (0, "Unrecognize event", "Unknown");
INSERT INTO event_type (id, description, name) VALUES (1, "Util was turn on", "Util On");
INSERT INTO event_type (id, description, name) VALUES (2, "Util was turn off", "Util Off");
INSERT INTO event_type (id, description, name) VALUES (3, "Alarm was triggered", "Alarm");
INSERT INTO event_type (id, description, name) VALUES (4, "Alarm was deactivated", "Alarm Off");
INSERT INTO event_type (id, description, name) VALUES (5, "Battery information message", "Battery");
INSERT INTO event_type (id, description, name) VALUES (6, "Device was connected to external power source", "Power connected");
INSERT INTO event_type (id, description, name) VALUES (7, "Device was disconnected of external power source", "Power disconnected");


INSERT INTO event (id, note, time, car_id, event_type_id, position_id) VALUES (1, "jedna", "2019-01-02T10:15:30", 1, 1, null);
INSERT INTO event (id, note, time, car_id, event_type_id, position_id) VALUES (2, "dva", "2019-01-02T10:15:31", 1, 1, null);
INSERT INTO event (id, note, time, car_id, event_type_id, position_id) VALUES (3, "tri", "2019-01-02T10:15:32", 2, 1, null);
INSERT INTO event (id, note, time, car_id, event_type_id, position_id) VALUES (4, "ctyri", "2019-01-02T10:15:33", 1, 2, null);
INSERT INTO event (id, note, time, car_id, event_type_id, position_id) VALUES (5, "pet", "2019-01-02T11:25:30", 2, 2, null);
INSERT INTO event (id, note, time, car_id, event_type_id, position_id) VALUES (6, "sest", "2019-01-12T10:15:30", 1, 2, null);
INSERT INTO event (id, note, time, car_id, event_type_id, position_id) VALUES (7, "sedm", "2019-01-22T10:15:30", 2, 1, null);
INSERT INTO event (id, note, time, car_id, event_type_id, position_id) VALUES (8, "osm", "2019-01-23T10:15:30", 2, 1, null);

INSERT INTO position (id, accuracy, altitude, latitude, longitude, speed, time, route_id) VALUES (1, 0.1, 1.11, 2.22, 3.33, 4.44, "2019-01-02T10:15:31", null);
INSERT INTO position (id, accuracy, altitude, latitude, longitude, speed, time, route_id) VALUES (2, 0.1, 2.22, 3.33, 4.44, 5.55, "2019-01-02T10:15:32", 1);
INSERT INTO position (id, accuracy, altitude, latitude, longitude, speed, time, route_id) VALUES (3, 0.1, 3.33, 4.44, 5.55, 6.66, "2019-01-02T10:15:33", 1);
INSERT INTO position (id, accuracy, altitude, latitude, longitude, speed, time, route_id) VALUES (4, 0.1, 4.44, 5.55, 6.66, 7.77, "2019-01-02T10:15:34", 1);
INSERT INTO position (id, accuracy, altitude, latitude, longitude, speed, time, route_id) VALUES (5, 0.1, 5.55, 6.66, 7.77, 8.88, "2019-01-02T10:15:35", 1);
INSERT INTO position (id, accuracy, altitude, latitude, longitude, speed, time, route_id) VALUES (6, 0.1, 6.66, 7.77, 8.88, 9.99, "2019-01-02T10:15:36", 1);
INSERT INTO position (id, accuracy, altitude, latitude, longitude, speed, time, route_id) VALUES (7, 0.1, 7.77, 8.88, 9.99, 1.11, "2019-01-02T10:15:37", 2);
INSERT INTO position (id, accuracy, altitude, latitude, longitude, speed, time, route_id) VALUES (8, 0.1, 8.88, 9.99, 1.11, 2.22, "2019-01-02T10:15:38", 3);

UPDATE route SET start_position_id = 2, end_position_id = 6 WHERE id = 1;
UPDATE route SET start_position_id = 7, end_position_id = 7 WHERE id = 2;
UPDATE route SET start_position_id = 8, end_position_id = 8 WHERE id = 3;
UPDATE event SET position_id = 1 WHERE id = 1;