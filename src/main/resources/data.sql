
INSERT INTO user (id, user_name, first_name, surname, password, role) VALUES (1, "Pavel", "Pavel", "Novak", "12345", "");

INSERT INTO car (id, user_id, name, icon) VALUES (1, 1, "Trabant", "");
INSERT INTO car (id, user_id, name, icon) VALUES (2, 1, "Favorit", "");

INSERT INTO route (id, length, car_id, note) VALUES (1, 0, 1, "car 1");
INSERT INTO route (id, length, car_id, note) VALUES (2, 0, 1, "car 2");
INSERT INTO route (id, length, car_id, note) VALUES (3, 0, 2, "car 3");
INSERT INTO route (id, length, car_id, note) VALUES (4, 0, 1, "car 4");
INSERT INTO route (id, length, car_id, note) VALUES (5, 0, 2, "car 5");

INSERT INTO event_type (id, description, name) VALUES (1, "Car is moving", "Move");
INSERT INTO event_type (id, description, name) VALUES (2, "Engine start", "Engine");

INSERT INTO event (id, name, note, time, car_id, event_type_id, position_id) VALUES (1, "1", "jedna", null, 1, 1, null);
INSERT INTO event (id, name, note, time, car_id, event_type_id, position_id) VALUES (2, "2", "dva", null, 1, 1, null);
INSERT INTO event (id, name, note, time, car_id, event_type_id, position_id) VALUES (3, "3", "tri", null, 2, 1, null);
INSERT INTO event (id, name, note, time, car_id, event_type_id, position_id) VALUES (4, "4", "ctyri", null, 1, 2, null);
INSERT INTO event (id, name, note, time, car_id, event_type_id, position_id) VALUES (5, "5", "pet", null, 2, 2, null);
INSERT INTO event (id, name, note, time, car_id, event_type_id, position_id) VALUES (6, "6", "sest", null, 1, 2, null);
INSERT INTO event (id, name, note, time, car_id, event_type_id, position_id) VALUES (7, "7", "sedm", null, 2, 1, null);
INSERT INTO event (id, name, note, time, car_id, event_type_id, position_id) VALUES (8, "8", "osm", null, 2, 1, null);

INSERT INTO position (id, accuracy, altitude, latitude, longitude, speed, time, route_id) VALUES (1, 0.1, 1.11, 2.22, 3.33, 4.44, null, null);
INSERT INTO position (id, accuracy, altitude, latitude, longitude, speed, time, route_id) VALUES (2, 0.1, 2.22, 3.33, 4.44, 5.55, null, 1);
INSERT INTO position (id, accuracy, altitude, latitude, longitude, speed, time, route_id) VALUES (3, 0.1, 3.33, 4.44, 5.55, 6.66, null, 1);
INSERT INTO position (id, accuracy, altitude, latitude, longitude, speed, time, route_id) VALUES (4, 0.1, 4.44, 5.55, 6.66, 7.77, null, 1);
INSERT INTO position (id, accuracy, altitude, latitude, longitude, speed, time, route_id) VALUES (5, 0.1, 5.55, 6.66, 7.77, 8.88, null, 1);
INSERT INTO position (id, accuracy, altitude, latitude, longitude, speed, time, route_id) VALUES (6, 0.1, 6.66, 7.77, 8.88, 9.99, null, 1);
INSERT INTO position (id, accuracy, altitude, latitude, longitude, speed, time, route_id) VALUES (7, 0.1, 7.77, 8.88, 9.99, 1.11, null, 2);
INSERT INTO position (id, accuracy, altitude, latitude, longitude, speed, time, route_id) VALUES (8, 0.1, 8.88, 9.99, 1.11, 2.22, null, 3);

UPDATE route SET start_position_id = 2, end_position_id = 6 WHERE id = 1;
UPDATE route SET start_position_id = 7, end_position_id = 7 WHERE id = 2;
UPDATE route SET start_position_id = 8, end_position_id = 8 WHERE id = 3;
UPDATE event SET position_id = 1 WHERE id = 1;