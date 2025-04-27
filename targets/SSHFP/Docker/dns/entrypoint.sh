#!/bin/bash
envsubst < /etc/bind/named.conf.template > /etc/bind/named.conf
envsubst < /etc/bind/dnssec/$named_conf_options_file > /etc/bind/named.conf.options
exec named -g -c /etc/bind/named.conf
