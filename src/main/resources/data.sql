
INSERT INTO user (id, user_name, first_name, surname, password, role) VALUES (1, "Pavel", "Pavel", "Novak", "12345", "");

INSERT INTO car (id, user_id, name, icon) VALUES (1, 1, "Trabant", "");
INSERT INTO car (id, user_id, name, icon) VALUES (2, 1, "Favorit", "");

INSERT INTO route (id, length, car_id) VALUES (1, 0, 1);
INSERT INTO route (id, length, car_id) VALUES (2, 0, 1);
INSERT INTO route (id, length, car_id) VALUES (3, 0, 2);
INSERT INTO route (id, length, car_id) VALUES (4, 0, 1);
INSERT INTO route (id, length, car_id) VALUES (5, 0, 2);