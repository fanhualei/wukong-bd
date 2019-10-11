

docker-compose exec mysql sh -c 'exec mysqldump --opt -uroot -pmysql@root wk' >wk-$(date +"%Y%m%d-%H%M%S").sql


