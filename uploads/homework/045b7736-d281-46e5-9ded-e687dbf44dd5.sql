-- S表
INSERT INTO S (Sno,Sname,Ssex,Sdept,Sbirth)
VALUES ('S1','张三','男','计算机学院','2000-01-01');

INSERT INTO S (Sno,Sname,Ssex,Sdept,Sbirth)
VALUES ('S2','李四','女','两江人工智能','2001-02-02');

INSERT INTO S (Sno,Sname,Ssex,Sdept,Sbirth)
VALUES ('S3','王浩','男','两江人工智能','2003-02-02');

INSERT INTO S (Sno,Sname,Ssex,Sdept,Sbirth)
VALUES ('S4','王其','男','两江人工智能','2004-02-12');

INSERT INTO S (Sno,Sname,Ssex,Sdept,Sbirth)
VALUES ('S5','李其','女','计算机学院','2005-02-02');

-- C表
INSERT INTO C VALUES ('C1','数据库',NULL,4);
INSERT INTO C VALUES ('C2','数据结构','C1',3);

-- SC表
INSERT INTO SC VALUES ('S1','C1',85);
INSERT INTO SC VALUES ('S1','C2',90);
INSERT INTO SC VALUES ('S2','C2',88);
INSERT INTO SC VALUES ('S3','C2',98);
INSERT INTO SC VALUES ('S3','C1',68);
INSERT INTO SC VALUES ('S4','C2',80);
INSERT INTO SC VALUES ('S5','C1',90);
INSERT INTO SC VALUES ('S5','C2',88);
INSERT INTO SC VALUES ('S2','C2',88);