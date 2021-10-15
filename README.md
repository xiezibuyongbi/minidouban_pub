`*.json` in `data source` is the data fetched from douban book. `Parser.java` can be used to parse them into proper format to insert into mysql.
`Webserver` contains the application. Remember to fill in your redis, mysql, smtp server configuration in `application.properties`.
`cachemgr` is the redis cache manager. It subscribe the message published by canal from kafka, determining which key should by deleted.
