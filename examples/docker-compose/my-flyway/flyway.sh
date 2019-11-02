
#!/bin/sh
path=$(pwd)

cmd=$*

if  [ ! -n "$cmd" ] ;then
  cmd=migrate
fi

echo $cmd

docker run --rm -v $path/flyway/sql:/flyway/sql -v $path/flyway/conf:/flyway/conf        flyway/flyway $cmd


