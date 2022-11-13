#!/bin/bash

host=$1
port=$2
user=$3
password=$4
command=$5
print_output=$6
print_error=$7

if [ -z "$host" ]
then
      host="127.0.0.1"
fi

if [ -z "$port" ]
then
      port=3022
fi

if [ -z "$user" ]
then
      user="demo"
fi

if [ -z "$passtord" ]
then
      password="none"
fi


if [ -z "$command" ]
then
      command="pwd"
fi

sed -i "s/HOSTPLACEHOLDER/$host/g" input.txt
sed -i "s/PORTPLACEHOLDER/$port/g" multiple.ini

cat input.txt | zgrab2 multiple -c multiple.ini -o output.txt

exit 0
