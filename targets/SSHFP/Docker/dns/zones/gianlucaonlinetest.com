$TTL 86400
@   IN  SOA ns1.gianlucaonline.com. admin.gianlucaonline.com. (
        2024031104 ; Serial
        3600       ; Refresh
        1800       ; Retry
        604800     ; Expire
        86400 )    ; Minimum TTL

; Nameserver-Einträge
@   IN  NS  ns1.gianlucaonline.com.
@   IN  NS  ns2.gianlucaonline.com.
; A-Einträge für Nameserver
ns1 IN  A   138.199.222.42
ns2 IN  A   195.201.43.124

; RR-Einträge für SSH-Server
ssh IN  A   138.199.222.42
ssh IN SSHFP 4 2 ed270ed53a6ceee16aa6280093e1d573887aff00be5b614d02d8d0b6023a00b0
