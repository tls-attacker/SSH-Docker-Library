#! /bin/sh

for f in `docker image ls --format "{{.Repository}}:{{.Tag }}" | grep ^rub-nds/ | sed -e 's,^rub-nds/,,' `; do docker image tag rub-nds/"$f" hydrogen.cloud.nds.rub.de:443/ssh-attacker/docker-library/"$f"; done
for f in `docker image ls --format "{{.Repository}}" | grep ^rub-nds/ | sed -e 's,^rub-nds/,,' | sort -u `; do docker image push -a hydrogen.cloud.nds.rub.de:443/ssh-attacker/docker-library/"$f"; done
