
INSERT INTO user (id, user_name, first_name, surname, password, role) VALUES (1, "Pavel", "Pavel", "Novak", "12345", "");

INSERT INTO car (id, user_id, name, icon) VALUES (1, 1, "Trabant", "");
INSERT INTO car (id, user_id, name, icon) VALUES (2, 1, "Favorit", "");

INSERT INTO route (id, length, car_id) VALUES (1, 0, 1);
INSERT INTO route (id, length, car_id) VALUES (2, 0, 1);
INSERT INTO route (id, length, car_id) VALUES (3, 0, 2);
INSERT INTO route (id, length, car_id) VALUES (4, 0, 1);
INSERT INTO route (id, length, car_id) VALUES (5, 0, 2);

INSERT INTO event_type (id, description, name) VALUES (1, "Car is moving", "Move");
INSERT INTO event_type (id, description, name) VALUES (2, "Engine start", "Engine");

INSERT INTO event (id, time, car_id, event_type_id, position_id) VALUES (1, null, 1, 1, null);
INSERT INTO event (id, time, car_id, event_type_id, position_id) VALUES (2, null, 1, 1, null);
INSERT INTO event (id, time, car_id, event_type_id, position_id) VALUES (3, null, 2, 1, null);
INSERT INTO event (id, time, car_id, event_type_id, position_id) VALUES (4, null, 1, 2, null);
INSERT INTO event (id, time, car_id, event_type_id, position_id) VALUES (5, null, 2, 2, null);
INSERT INTO event (id, time, car_id, event_type_id, position_id) VALUES (6, null, 1, 2, null);
INSERT INTO event (id, time, car_id, event_type_id, position_id) VALUES (7, null, 2, 1, null);
INSERT INTO event (id, time, car_id, event_type_id, position_id) VALUES (8, null, 2, 1, null);