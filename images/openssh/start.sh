#!/bin/bash

/usr/bin/expect -c

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
      user="sshattacker"
fi

if [ -z "$password" ]
then
      password="secret"
fi


if [ -z "$command" ]
then
      command="pwd"
fi

parameter="--host $host -P $port -u $user -p $password -c $command"

if [ "$print_output" == "true"  ]
then
      parameter+=" -o"
fi

if [ "$print_error" == "true"  ]
then
      parameter+=" -e"
fi


expect login.exp $host $ip $port $user $password $command

exit 0
