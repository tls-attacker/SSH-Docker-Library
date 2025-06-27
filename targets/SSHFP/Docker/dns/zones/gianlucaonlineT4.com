$TTL 86400
@   IN  SOA  ns1.gianlucaonline.com. admin.gianlucaonline.com. (
            20250320 ; Seriennummer (YYYYMMDDXX)
            3600       ; Refresh (1 Stunde)
            1800       ; Retry (30 Minuten)
            604800     ; Expire (7 Tage)
            86400 )    ; Minimum TTL (1 Tag)

; Nameserver-Einträge
@   IN  NS  ns1.gianlucaonline.com.
@   IN  NS  ns2.gianlucaonline.com.

; A-Records für die Nameserver
ns1 IN  A   138.199.222.42   ; IP-Adresse für ns1
ns2 IN  A   195.201.43.124   ; IP-Adresse für ns2

; A-Record für die Hauptdomain
ssh IN  A   138.199.222.42
ssh 30 IN SSHFP 4 2 ed270eb53a6ceee16aa6280093e1d573887aff00be5b614d02d8d0b6023a00b0
$INCLUDE /home/ugolinisshfp/Docker/dns/zones/Kgianlucaonline.com.+008+33529.key
$INCLUDE /home/ugolinisshfp/Docker/dns/zones/Kgianlucaonline.com.+008+53744.key
