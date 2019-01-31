INSERT INTO public.audit("time", sessionid, "user", userid, rq, data) VALUES (
  'CURRDATE CURRHOUR:00:00.000','SESSION1','user1',2,'Q',cast('{"adress":"0:0:0:0:0:0:0:1","method":"GET","path":"path","params":{},"json":null}' as json));
INSERT INTO public.audit("time", sessionid, "user", userid, rq, data) VALUES (
  'CURRDATE CURRHOUR:00:00.001','SESSION1','user1',2,'R',cast('{"code":200,"json":[],"requestJson":{"time":"CURRDATE CURRHOUR:00:00.000","adress":"0:0:0:0:0:0:0:1","method":"GET","path":"path"}}' as json));
INSERT INTO public.audit("time", sessionid, "user", userid, rq, data) VALUES (
  'CURRDATE CURRHOUR:40:00.000','SESSION2','user2',3,'Q',cast('{"adress":"0:0:0:0:0:0:0:1","method":"GET","path":"path","params":{},"json":null}' as json));
INSERT INTO public.audit("time", sessionid, "user", userid, rq, data) VALUES (
  'CURRDATE CURRHOUR:40:00.001','SESSION2','user2',3,'R',cast('{"code":404,"json":{},"requestJson":{"time":"CURRDATE CURRHOUR:40:00.000","adress":"0:0:0:0:0:0:0:1","method":"GET","path":"path"}}' as json));
INSERT INTO public.audit("time", sessionid, "user", userid, rq, data) VALUES (
  'CURRDATE CURRHOUR:41:00.000','SESSION2','user2',3,'Q',cast('{"adress":"0:0:0:0:0:0:0:1","method":"GET","path":"path","params":{},"json":null}' as json));
INSERT INTO public.audit("time", sessionid, "user", userid, rq, data) VALUES (
  'CURRDATE CURRHOUR:41:00.001','SESSION2','user2',3,'R',cast('{"code":404,"json":{},"requestJson":{"time":"CURRDATE CURRHOUR:41:00.000","adress":"0:0:0:0:0:0:0:1","method":"GET","path":"path"}}' as json));
INSERT INTO public.audit("time", sessionid, "user", userid, rq, data) VALUES (
  'CURRDATE CURRHOUR:50:00.000','SESSION3','user3',4,'Q',cast('{"adress":"0:0:0:0:0:0:0:1","method":"GET","path":"path","params":{},"json":null}' as json));
INSERT INTO public.audit("time", sessionid, "user", userid, rq, data) VALUES (
  'CURRDATE CURRHOUR:50:00.001','SESSION3','user3',4,'R',cast('{"code":200,"json":[],"requestJson":{"time":"CURRDATE CURRHOUR:50:00.000","adress":"0:0:0:0:0:0:0:1","method":"GET","path":"path"}}' as json));
INSERT INTO public.audit("time", sessionid, "user", userid, rq, data) VALUES (
  'CURRDATE CURRHOUR:50:10.000','SESSION3','user3',4,'Q',cast('{"adress":"0:0:0:0:0:0:0:1","method":"GET","path":"path","params":{},"json":null}' as json));
INSERT INTO public.audit("time", sessionid, "user", userid, rq, data) VALUES (
  'CURRDATE CURRHOUR:50:10.001','SESSION3','user3',4,'R',cast('{"code":404,"json":{},"requestJson":{"time":"CURRDATE CURRHOUR:50:10.000","adress":"0:0:0:0:0:0:0:1","method":"GET","path":"path"}}' as json));
INSERT INTO public.audit("time", sessionid, "user", userid, rq, data) VALUES (
  'CURRDATE CURRHOUR:50:20.000','SESSION3','user3',4,'Q',cast('{"adress":"0:0:0:0:0:0:0:1","method":"GET","path":"path","params":{},"json":null}' as json));
INSERT INTO public.audit("time", sessionid, "user", userid, rq, data) VALUES (
  'CURRDATE CURRHOUR:50:20.001','SESSION3','user3',4,'R',cast('{"code":500,"json":{},"requestJson":{"time":"CURRDATE CURRHOUR:50:20.000","adress":"0:0:0:0:0:0:0:1","method":"GET","path":"path"}}' as json));