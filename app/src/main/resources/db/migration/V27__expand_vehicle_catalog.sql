INSERT INTO work.w_vehicle_make(name) VALUES
('OMODA'),('JAECOO'),('Jetour'),('Tank'),('Belgee'),('Voyah'),('Li Auto'),('Hongqi'),('GAC'),('GAC Motor'),('Kaiyi'),('JAC'),('FAW'),('Dongfeng'),('Москвич'),('Sollers'),('Infiniti'),('Volvo'),('Subaru'),('Honda'),('Suzuki'),('Cadillac'),('Genesis'),('Porsche'),('Peugeot'),('Citroen'),('Opel'),('Isuzu')
ON CONFLICT (name) DO NOTHING;

INSERT INTO work.w_vehicle_model(make_id, name)
SELECT id, model FROM work.w_vehicle_make CROSS JOIN (VALUES
('Haval','Jolion'),('Haval','Jolion рестайлинг'),('Haval','F7'),('Haval','F7x'),('Haval','F7 рестайлинг'),('Haval','F7x рестайлинг'),('Haval','H2'),('Haval','H5'),('Haval','H6'),('Haval','H6 третьего поколения'),('Haval','H8'),('Haval','H9'),('Haval','M6'),('Haval','Dargo'),('Haval','Dargo X'),('Haval','Ruge'),
('Toyota','Land Cruiser'),('Toyota','Land Cruiser Prado'),('Toyota','Highlander'),('Toyota','Fortuner'),('Toyota','Hilux'),('Toyota','C-HR'),('Toyota','Yaris'),('Toyota','Avensis'),('Toyota','Prius'),('Toyota','Venza'),
('Lada','Niva Legend'),('Lada','Niva Travel'),('Lada','Niva Bronto'),('Lada','Niva Sport'),('Lada','Largus'),('Lada','XRAY'),('Lada','Kalina'),('Lada','Priora'),('Lada','2107'),
('Kia','Ceed'),('Kia','Cerato'),('Kia','Seltos'),('Kia','Sorento'),('Kia','Sorento Prime'),('Kia','Mohave'),('Kia','Soul'),('Kia','K900'),('Kia','Carnival'),
('Hyundai','i30'),('Hyundai','Elantra'),('Hyundai','Sonata'),('Hyundai','Santa Fe'),('Hyundai','Palisade'),('Hyundai','Kona'),('Hyundai','ix35'),('Hyundai','Staria'),
('Geely','Emgrand'),('Geely','Emgrand X7'),('Geely','Atlas Pro'),('Geely','Monjaro'),('Geely','Tugella'),('Geely','Okavango'),('Geely','Geometry C'),
('Chery','Tiggo 3'),('Chery','Tiggo 4 Pro'),('Chery','Tiggo 7'),('Chery','Tiggo 7 Pro Max'),('Chery','Tiggo 8'),('Chery','Tiggo 8 Pro Max'),('Chery','Arrizo 8'),
('Exeed','VX'),('Exeed','RX'),('Exeed','EXLANTIX ET'),('Exeed','EXLANTIX ES'),
('Changan','CS35'),('Changan','CS55'),('Changan','CS55 Plus'),('Changan','CS75'),('Changan','CS75 Plus'),('Changan','UNI-T'),('Changan','UNI-K'),('Changan','UNI-V'),('Changan','Alsvin'),
('Volkswagen','Golf'),('Volkswagen','Jetta'),('Volkswagen','Passat'),('Volkswagen','Touareg'),('Volkswagen','Teramont'),('Volkswagen','Transporter'),
('Skoda','Octavia'),('Skoda','Superb'),('Skoda','Karoq'),('Skoda','Yeti'),('Skoda','Fabia'),
('Renault','Sandero'),('Renault','Kaptur'),('Renault','Arkana'),('Renault','Koleos'),('Renault','Master'),('Renault','Trafic'),
('Nissan','Almera'),('Nissan','Teana'),('Nissan','Murano'),('Nissan','Pathfinder'),('Nissan','Terrano'),('Nissan','Patrol'),('Nissan','Navara'),
('Mitsubishi','Outlander'),('Mitsubishi','Pajero'),('Mitsubishi','Pajero Sport'),('Mitsubishi','ASX'),('Mitsubishi','L200'),
('BMW','1 Series'),('BMW','2 Series'),('BMW','4 Series'),('BMW','7 Series'),('BMW','X1'),('BMW','X3'),('BMW','X5'),('BMW','X6'),
('Mercedes-Benz','A-Class'),('Mercedes-Benz','C-Class'),('Mercedes-Benz','S-Class'),('Mercedes-Benz','GLA'),('Mercedes-Benz','GLC'),('Mercedes-Benz','GLE'),('Mercedes-Benz','Sprinter'),
('Audi','A3'),('Audi','A5'),('Audi','A6'),('Audi','A8'),('Audi','Q3'),('Audi','Q5'),('Audi','Q7'),('Audi','Q8'),
('Lexus','ES'),('Lexus','IS'),('Lexus','NX'),('Lexus','GX'),('Lexus','LX'),('Lexus','UX'),
('OMODA','C5'),('OMODA','S5'),('OMODA','S5 GT'),('OMODA','C7'),('JAECOO','J7'),('JAECOO','J8'),('Jetour','X70'),('Jetour','X90'),('Jetour','Dashing'),('Jetour','T2'),
('Tank','300'),('Tank','500'),('Tank','700'),('Belgee','X50'),('Belgee','X70'),('Voyah','Free'),('Voyah','Dream'),('Voyah','Passion'),('Li Auto','L7'),('Li Auto','L8'),('Li Auto','L9'),('Hongqi','HS5'),('Hongqi','HS7'),('Hongqi','E-HS9'),
('GAC','GS3'),('GAC','GS8'),('GAC','M8'),('Kaiyi','E5'),('Kaiyi','X3'),('JAC','JS4'),('JAC','JS6'),('FAW','Bestune T77'),('FAW','Bestune T99'),('Dongfeng','T5 EVO'),('Dongfeng','580'),('Москвич','3'),('Москвич','3е'),('Москвич','6'),('Москвич','8'),
('Volvo','S60'),('Volvo','S90'),('Volvo','XC40'),('Volvo','XC60'),('Volvo','XC90'),('Subaru','Forester'),('Subaru','Outback'),('Honda','Civic'),('Honda','CR-V'),('Honda','Pilot'),('Suzuki','Vitara'),('Suzuki','SX4'),('Suzuki','Jimny'),('Cadillac','XT4'),('Cadillac','XT5'),('Genesis','G70'),('Genesis','GV80'),('Porsche','Cayenne'),('Porsche','Macan'),('Peugeot','3008'),('Peugeot','5008'),('Citroen','C4'),('Opel','Astra'),('Opel','Grandland'),('Isuzu','D-Max')) AS models(make_name, model)
WHERE work.w_vehicle_make.name = models.make_name ON CONFLICT DO NOTHING;
