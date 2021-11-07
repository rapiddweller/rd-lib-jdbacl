drop table table1 if exists;
drop index index1 if exists;
drop table table3 if exists;
drop table table4 if exists;
drop index index4 if exists;
drop sequence seq1 if exists;

create table table1 (
	id1 int,
	name1 int,
	constraint table1_pk primary key (id1),
	constraint table1_name1_uk unique (name1)
);
create unique index index1 on table1 (name1);
create table table2 (
	id2  int,
	ref2 int,
	constraint table2_pk primary key (id2),
	constraint table2_fk2 foreign key (ref2) references table1 (id1)
);
create table table3 (
	id3_1 int,
	id3_2 int,
	name3 VARCHAR(8),
	type3 CHAR,
	constraint table3_pk primary key (id3_1, id3_2),
	constraint table3_name3_uk unique (name3, type3)
);
create table table4 (
	id4 int,
	ref4_1 int,
	ref4_2 int,
	constraint table4_pk primary key (id4),
	constraint table4_fk2 foreign key (ref4_1, ref4_2) references table3 (id3_1, id3_2)
);
create index index4 on table4 (ref4_1, ref4_2);
create sequence seq1 start with 1000;