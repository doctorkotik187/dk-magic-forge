clean:
	rm -rf target

run:
	clj -M:dev

repl:
	clj -M:dev:nrepl

test:
	clj -M:test

uberjar:
	clj -T:build all

start_fresh_postgres:
    podman run --rm --replace -d \
        --name postgres \
        -e POSTGRES_USER=dk-magic-forge \
        -e POSTGRES_PASSWORD=dk-magic-forge \
        -e POSTGRES_DB=dk-magic-forge_dev \
        -p 5432:5432 \
        postgres:18

connect_to_db:
    usql postgres://dk-magic-forge:dk-magic-forge@localhost:5432/dk-magic-forge_dev
