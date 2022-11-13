FROM metasploitframework/metasploit-framework AS metasploit-client
COPY --from=entrypoint /bin/client-entrypoint /usr/local/bin/
COPY start.sh /usr/local/bin/
COPY msfScript.rc /usr/local/bin/
LABEL "ssh_implementation"="metasploit"
LABEL "ssh_implementation_version"="latest"
LABEL "ssh_implementation_connectionRole"="client"
WORKDIR /usr/local/bin
ENTRYPOINT ["client-entrypoint", "start.sh"]

