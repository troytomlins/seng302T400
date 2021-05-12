/* Addresses for Users (20) */
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('109', 'Blue Street', 'Upper Hutt', 'Wellington', 'New Zealand', '5018');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('333', 'Ilam Road', 'Christchurch', 'Canterbury', 'New Zealand', '90210');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('57', 'Sydney Highway', 'Shire of Cocos Islands', 'West Island', 'Cocos (Keeling) Islands', '9732');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('47993', 'Norwood Garden', 'Mambere-Kadei', 'Central African Republic', 'Africa', '3428');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('129', 'Mastic Trail', 'Frank Sound Cayman Islands', 'Caribbean', 'North America', '3442');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('80416', 'Rodney Street', 'Jon Loop', 'Shaanxi', 'China', '2113');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('9205', 'Monique Vista', 'Bururi', 'Bigomogomo', 'Africa', '1000');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('240', 'Newson Street', 'Bernhard Run', 'Southland', 'New Zealand', '2839');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('16', 'Presidente Peron', 'Funes', 'Santa Fe', 'Argentina', 'S2132');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('190', 'Fort Washington Avenue', 'New York', 'New York', 'United States', '10040');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('32', 'Hunter Avenue', 'Fairview Shores', 'Florida', 'United States', '32804');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('32', 'Via Giuseppe Di Vittorio', 'Rovereto', 'Trentino-Alto Adige/Südtirol', 'Italy', '38068');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('3434', 'Russell Street', 'Detroit', 'Michigan', 'United States', '48207');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('16', 'Barnards Road', 'Scarborough', 'England', 'United Kingdom', 'YO21 1UX');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('199', 'Kwa Jongo Street', 'Dar es Salaam', 'Coastal Zone', 'Tanzania', '78570');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('76', 'Milltown Road', 'Strabane', 'Northern Ireland', 'United Kingdom', 'BT82 8AS');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('87', 'Ansancheonnam-ro', 'Ansan-si', 'Gyeonggi-do', 'South Korea', '15483');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('98', 'Sqaq il-Qenċ', 'Zebbug', 'Southern Region', 'Malta', 'ZBG9019');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('19', 'Heping Road', 'Zebbug', 'Tianjin', 'China', '300010');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('19b', 'IGIS Road', 'Awan Town', 'Islamabad Capital Territory', 'Pakistan', '44110');

/* Users (20) */
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Alex', 'Doe', 'Joe', 'Johnny', 'Biography', 'emailUSER@email.com', DATE'2008-02-02', '0271317', 5, 'UGFzc3dvcmQxMjMh', DATE'2021-02-12', 'USER');
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Chad', 'Taylor', 'S', 'Chaddy', 'Biography', 'chad.taylor@example.com', DATE'2006-02-01', '0271316678', 3, 'UGFzc3dvcmQxMjMh', DATE'2021-03-14', 'USER');
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Naomi', 'Wilson', 'I', 'GM', 'Biography', 'naomi.wilson@example.com', DATE'2007-09-08', '54335522', 4, 'UGFzc3dvcmQxMjMh', DATE'2021-03-01', 'USER');
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Seth', 'Murphy', 'S', 'Sethy', 'Biography', 'seth.murphy@example.com', DATE'2003-06-19', '027188316', 5, 'UGFzc3dvcmQxMjMh', DATE'2021-03-03', 'USER');
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Minttu', 'Wainio', 'Anna', 'Minnie', 'Biography', 'minttu.wainio@example.com', DATE'2004-07-28', '0290316', 6, 'UGFzc3dvcmQxMjMh', DATE'2021-03-05', 'USER');
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Francisca', 'Benitez', 'Tina', 'Fran', 'Biography', 'francisca.benitez@example.com', DATE'2008-09-10', '12334456', 7, 'UGFzc3dvcmQxMjMh', DATE'2021-01-08', 'USER');
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Francisca', 'Bznitez', 'Tessa', 'Fran', 'Biography', 'francisca.bznitez@example.com', DATE'2001-07-17', '967352531', 8, 'UGFzc3dvcmQxMjMh', DATE'2021-01-01', 'USER');
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Frank', 'Smith', 'J', 'Frankie', 'Biography', 'frank.j.smith@email.com', DATE'2000-05-14', '0271316', 1, 'UGFzc3dvcmQxMjMh', DATE'2019-05-14', 'USER');
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Lina', 'Patterson', 'Jose Mari', 'Lina', 'Da', 'linap@email.com', DATE'2000-02-14', '0275431316', 9, 'UGFzc3dvcmQxMjMh', DATE'2010-05-14', 'USER');
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Evelia', 'Blanxart', 'Robert', 'Robby', 'I like art!', 'everblanxart@gmail.com', DATE'2007-04-13', '0272331323', 10, 'UGFzc3dvcmQxMjMh', DATE'2019-05-20', 'USER');
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Mirta', 'Lovel', 'Juan', 'Love', 'Pancakes', 'mjl25@uclive.ac.nz', DATE'1999-02-22', '0273321116', 11, 'UGFzc3dvcmQxMjMh', DATE'2021-01-20', 'USER');
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Jordan', 'Piper', 'Mervyn', 'Bag', 'I do not play the bagpipes!', 'jordanpiper@yahoo.com', DATE'2005-01-22', '0272121116', 12, 'UGFzc3dvcmQxMjMh', DATE'2019-05-22', 'USER');
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Ife', 'Weston', 'Missie', 'Missie', 'Miss me.', 'missie@gmail.com', DATE'2004-05-17', '0271316323', 13, 'UGFzc3dvcmQxMjMh', DATE'2014-02-14', 'USER');
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Pia', 'Kemp', 'Alex', 'Hemp', 'My cool bio.', 'piakemp13@email.com', DATE'2008-01-13', '0271316231', 14, 'UGFzc3dvcmQxMjMh', DATE'2020-01-12', 'USER');
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Alyce', 'Gibbs', 'Teddie', 'Teddie', 'Looking for cheap teddies.', 'alycegibbs@gmail.com', DATE'1965-02-19', '0271316943', 15, 'UGFzc3dvcmQxMjMh', DATE'2019-03-28', 'USER');
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Casandra', 'Dane', 'Fen', 'Cassie', 'I am not from Denmark!', 'cassie@dane.com', DATE'1982-01-17', '0271316226', 16, 'UGFzc3dvcmQxMjMh', DATE'2018-12-13', 'USER');
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Bennett', 'Garner', 'Finola', 'Garnish', 'I like apples.', 'garnish@yahoo.com', DATE'1989-09-12', '0271316096', 17, 'UGFzc3dvcmQxMjMh', DATE'2021-02-02', 'USER');
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Da', 'Meadows', 'Randolf', 'Da Baby', 'Looking for cheap bread.', 'dababy@email.com', DATE'2006-02-18', '0271316989', 18, 'UGFzc3dvcmQxMjMh', DATE'2019-12-12', 'USER');
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Tanner', 'Ilene', 'Levitt', 'Levy', 'Biography', 'tannerlevitt@gmail.com', DATE'1999-12-14', '0271316234', 19, 'UGFzc3dvcmQxMjMh', DATE'2020-01-12', 'USER');
INSERT INTO `user` (first_name, last_name, middle_name, nickname, bio, email, date_of_birth, phone_number, address_id, password, created, role) VALUES ('Juliana', 'Haden', 'Celeste', 'Jules', 'Biography', 'jules@email.com', DATE'2000-05-19', '0271316534', 20, 'UGFzc3dvcmQxMjMh', DATE'2021-02-03', 'USER');

/* Addresses for Businesses (3) */
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('86', 'High Street', 'Picton', 'Marlborough', 'New Zealand', '7220');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('1849', 'C Street Northwest', 'Washington', 'District of Columbia', 'United States', '20240');
INSERT INTO `address` (street_number, street_name, city, region, country, postcode) VALUES ('7', 'Wangjing Zhonghuan Nanlu', 'Chaoyang District', 'Beijing', 'China', '100102');


/* Businesses (3) */
INSERT INTO `business` (business_type, created, description, name, primary_administrator_id, address_id) VALUES ('RETAIL_TRADE', DATE'2021-02-12', 'Description', 'Brink Food', 1, 21);
INSERT INTO `business` (business_type, created, description, name, primary_administrator_id, address_id) VALUES ('CHARITABLE_ORGANISATION', DATE'2021-02-14', 'Description', 'Sunburst Waste', 10, 22);
INSERT INTO `business` (business_type, created, description, name, primary_administrator_id, address_id) VALUES ('RETAIL_TRADE', DATE'2021-02-01', 'Description', 'Fringe Wasteless', 6, 23);

/* Link Users to Businesses */
INSERT INTO `users_businesses` (user_id, businesses_id) VALUES (1, 1);
INSERT INTO `users_businesses` (user_id, businesses_id) VALUES (10, 2);
INSERT INTO `users_businesses` (user_id, businesses_id) VALUES (11, 2);
INSERT INTO `users_businesses` (user_id, businesses_id) VALUES (13, 2);
INSERT INTO `users_businesses` (user_id, businesses_id) VALUES (6, 3);