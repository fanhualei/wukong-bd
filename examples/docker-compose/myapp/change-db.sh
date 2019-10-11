

docker-compose exec -T mysql sh -c 'exec mysql -uroot -pmysql@root ' < ./00-sql-init/init.sql

