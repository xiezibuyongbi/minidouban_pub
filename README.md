`*.json` in `data source` is the data fetched from douban book. `Parser.java` can be used to parse them into proper format to insert into mysql.
`Webserver` contains the program. Remember to fill in your redis, mysql, smtp server configuration in `application.properties`.

`cachemgr` is used to manage cache in redis, receving messages from canal through kafka, determining when the cache should be expired and be deleted.

`fullsync2es` is used to take a full amount of synchronization of Book data from mysql to elasticsearch.

`dsl` and `sql` in the `data souce `is the meta data of elasticsearch and mysql.
