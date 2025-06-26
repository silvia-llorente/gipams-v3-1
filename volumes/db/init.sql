DROP DATABASE IF EXISTS gipams_db;
CREATE DATABASE gipams_db;
CREATE USER 'WORKFLOW_user'@'%' IDENTIFIED BY 'password';
CREATE USER 'GCS_user'@'%' IDENTIFIED BY 'password';
CREATE USER 'SEARCH_user'@'%' IDENTIFIED BY 'password';
CREATE USER 'PS_user'@'%' IDENTIFIED BY 'password';
CREATE USER 'AM_user'@'%' IDENTIFIED BY 'password';
GRANT SELECT ON gipams_db.* TO 'WORKFLOW_user'@'%';
GRANT ALL ON gipams_db.* TO 'GCS_user'@'%';
GRANT ALL ON gipams_db.DGKeys TO 'PS_user'@'%';
GRANT ALL ON gipams_db.DTKeys TO 'PS_user'@'%';
GRANT SELECT ON gipams_db.* TO 'SEARCH_user'@'%';
GRANT ALL ON gipams_db.App TO 'AM_user'@'%';
GRANT ALL ON gipams_db.User TO 'AM_user'@'%';
GRANT ALL ON gipams_db.Role TO 'AM_user'@'%';
GRANT ALL ON gipams_db.UserRole TO 'AM_user'@'%';

use gipams_db;
CREATE TABLE App(
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    app VARCHAR(50) UNIQUE
);

CREATE TABLE User(
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    app INT NOT NULL,
    FOREIGN KEY (app) REFERENCES App(id) ON DELETE NO ACTION
);

CREATE TABLE Role(
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    role VARCHAR(50) UNIQUE
);

CREATE TABLE UserRole(
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES User(id),
    FOREIGN KEY (role_id) REFERENCES Role(id)
);

INSERT INTO App(app) VALUES ('GOOGLE'), ('FACEBOOK');
INSERT INTO Role(role) VALUES ('researcher'), ('practitioner');

create table MPEGFile (
	id bigint NOT NULL AUTO_INCREMENT, 
	name varchar(128) NOT NULL, 
	path varchar(512) NOT NULL, 
	owner varchar(128) NOT NULL, 
	primary key(id));

create table DatasetGroup (
	dg_id int NOT NULL AUTO_INCREMENT, 
	title varchar(128) NOT NULL, 
	type varchar(128), 
	abstract varchar(1024), 
	projectCentre varchar(128), 
	description varchar(1024), 
	path varchar(256) NOT NULL, 
	owner varchar(128) NOT NULL, 
	protection text, 
	hasMetadata bool default false, 
	mpegfile bigint NOT NULL, 
	foreign key (mpegfile) references MPEGFile(id), 
	primary key(dg_id,mpegfile));

create table Dataset (
	dt_id int NOT NULL AUTO_INCREMENT, 
	title varchar(128) NOT NULL, 
	type varchar(128), 
	abstract varchar(1024), 
	projectCentre varchar(1024), 
	description varchar(1024), 
	path varchar(128) NOT NULL, 
	owner varchar(128) NOT NULL, 
	protection text, 
	hasMetadata bool default false, 
	dg_id int NOT NULL, 
	mpegfile bigint NOT NULL, 
	foreign key (mpegfile) references MPEGFile(id), 
	foreign key (dg_id,mpegfile) references DatasetGroup(dg_id,mpegfile),
	primary key(dt_id,dg_id,mpegfile));

create table AccessUnit (
	au_id int NOT NULL AUTO_INCREMENT, 
	type varchar(128), 
	path varchar(128) NOT NULL, 
	owner varchar(128) NOT NULL, 
	protection text, 
	dt_id int NOT NULL, 
	dg_id INT NOT NULL,
	mpegfile bigint NOT NULL, 
	FOREIGN KEY (dt_id, dg_id, mpegfile) REFERENCES Dataset(dt_id, dg_id, mpegfile), 
    FOREIGN KEY (dg_id, mpegfile) REFERENCES DatasetGroup(dg_id, mpegfile), 
    FOREIGN KEY (mpegfile) REFERENCES MPEGFile(id), 
    PRIMARY KEY (au_id, dt_id, dg_id, mpegfile));

create table Block (
	block_id int NOT NULL AUTO_INCREMENT, 
	size int,
	path varchar(128) NOT NULL, 
	owner varchar(128) NOT NULL, 
	au_id int NOT NULL, 
	dt_id int NOT NULL, 
	dg_id INT NOT NULL,
	mpegfile bigint NOT NULL, 
	FOREIGN KEY (au_id, dt_id, dg_id, mpegfile) references AccessUnit(au_id, dt_id, dg_id, mpegfile), 
	FOREIGN KEY (dt_id, dg_id, mpegfile) REFERENCES Dataset(dt_id, dg_id, mpegfile), 
    FOREIGN KEY (dg_id, mpegfile) REFERENCES DatasetGroup(dg_id, mpegfile), 
    FOREIGN KEY (mpegfile) REFERENCES MPEGFile(id), 
	primary key(block_id,au_id,dt_id,dg_id,mpegfile));

CREATE TABLE DGSample (
    dgs_id INT NOT NULL, 
    taxon_id INT, 
    title VARCHAR(128), 
    dg_id INT NOT NULL, 
    mpegfile BIGINT NOT NULL, 
    FOREIGN KEY (dg_id, mpegfile) REFERENCES DatasetGroup(dg_id, mpegfile), 
    FOREIGN KEY (mpegfile) REFERENCES MPEGFile(id)
);

CREATE TABLE DTSample (
    dts_id INT NOT NULL, 
    taxon_id INT, 
    title VARCHAR(128), 
    dt_id INT NOT NULL, 
    dg_id INT NOT NULL, 
    mpegfile BIGINT NOT NULL, 
    FOREIGN KEY (dt_id, dg_id, mpegfile) REFERENCES Dataset(dt_id, dg_id, mpegfile), 
    FOREIGN KEY (mpegfile) REFERENCES MPEGFile(id)
);

CREATE TABLE DGKeys (
    mpegfile BIGINT NOT NULL,
    dg_id INT NOT NULL,
    name VARCHAR(128) NOT NULL,
    symKey TEXT,
    privKey TEXT,
    pubKey TEXT,
    FOREIGN KEY (mpegfile) REFERENCES MPEGFile(id),
    FOREIGN KEY (dg_id, mpegfile) REFERENCES DatasetGroup(dg_id, mpegfile),
    PRIMARY KEY (mpegfile, dg_id, name)
);

CREATE TABLE DTKeys (
    mpegfile BIGINT NOT NULL,
    dg_id INT NOT NULL,
    dt_id INT NOT NULL,
    name VARCHAR(128) NOT NULL,
    symKey TEXT,
    privKey TEXT,
    pubKey TEXT,
    FOREIGN KEY (mpegfile) REFERENCES MPEGFile(id),
    FOREIGN KEY (dg_id, mpegfile) REFERENCES DatasetGroup(dg_id, mpegfile),
    FOREIGN KEY (dt_id, dg_id, mpegfile) REFERENCES Dataset(dt_id, dg_id, mpegfile),
    PRIMARY KEY (mpegfile, dg_id, dt_id, name)
);
