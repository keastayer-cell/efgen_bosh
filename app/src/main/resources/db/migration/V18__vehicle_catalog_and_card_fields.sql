ALTER TABLE work.w_car ADD COLUMN vehicle_make VARCHAR(120) NOT NULL DEFAULT '';
ALTER TABLE work.w_car ADD COLUMN vehicle_model VARCHAR(180) NOT NULL DEFAULT '';
ALTER TABLE work.w_car ADD COLUMN owner_name VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE work.w_car ADD COLUMN owner_phone VARCHAR(64) NOT NULL DEFAULT '';

UPDATE work.w_car SET vehicle_model = COALESCE(NULLIF(vehicle_name, ''), 'Не указано') WHERE vehicle_model = '';

CREATE TABLE work.w_vehicle_make (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(120) NOT NULL UNIQUE,
  active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE work.w_vehicle_model (
  id BIGSERIAL PRIMARY KEY,
  make_id BIGINT NOT NULL REFERENCES work.w_vehicle_make(id) ON DELETE CASCADE,
  name VARCHAR(180) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE(make_id, name)
);

INSERT INTO work.w_vehicle_make(name) VALUES
('Toyota'),('Lada'),('Kia'),('Hyundai'),('Geely'),('Haval'),('Chery'),('Exeed'),('Changan'),('Volkswagen'),('Skoda'),('Renault'),('Nissan'),('Mitsubishi'),('BMW'),('Mercedes-Benz'),('Audi'),('Lexus'),('Mazda'),('Ford'),('Chevrolet'),('УАЗ'),('ГАЗ')
ON CONFLICT (name) DO NOTHING;

INSERT INTO work.w_vehicle_model(make_id, name)
SELECT id, model FROM work.w_vehicle_make CROSS JOIN (VALUES
('Toyota','Camry'),('Toyota','Corolla'),('Toyota','RAV4'),('Lada','Vesta'),('Lada','Granta'),('Lada','Niva Travel'),('Kia','Rio'),('Kia','Sportage'),('Kia','K5'),('Hyundai','Solaris'),('Hyundai','Creta'),('Hyundai','Tucson'),('Geely','Coolray'),('Geely','Atlas'),('Haval','Jolion'),('Haval','F7'),('Chery','Tiggo 4'),('Chery','Tiggo 7 Pro'),('Exeed','LX'),('Exeed','TXL'),('Changan','CS35 Plus'),('Volkswagen','Polo'),('Volkswagen','Tiguan'),('Skoda','Rapid'),('Skoda','Kodiaq'),('Renault','Logan'),('Renault','Duster'),('Nissan','Qashqai'),('Nissan','X-Trail'),('BMW','3 Series'),('BMW','5 Series'),('Mercedes-Benz','E-Class'),('Audi','A4'),('Lexus','RX'),('Mazda','CX-5'),('Ford','Focus'),('Chevrolet','Niva'),('УАЗ','Патриот'),('ГАЗ','Газель')) AS models(make_name, model) WHERE work.w_vehicle_make.name = models.make_name ON CONFLICT DO NOTHING;
