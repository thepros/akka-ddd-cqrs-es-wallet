# Bank API CQRS Event Sourcing System on Akka-Cluster (Concept Model)

## How to run E2E tests

Terminal #1

```sh
$ sbt -DPORT=2551 -DHTTP_PORT=8080 'localMysql/run' 'api-server/run'
```

Terminal #2

```sh
$ sbt -DPORT=2552 -DHTTP_PORT=8081 'api-server/run'
```

Terminal #3

```sh
$ sbt -DPORT=2553 -DHTTP_PORT=8082 'api-server/run'
```

Terminal #4

```sh
$ sbt 'read-model-updater/run'
```

## How to test

```sh
# deposit to the bank account
$ curl -X POST \
  http://localhost:8080/bank-accounts/deposit \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{
	"datetime": "2022-06-05T14:04:47Z"
	"amount": 1000,
}'
{"errorMessage":null}%

```
