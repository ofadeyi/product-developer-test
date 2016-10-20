#!/usr/bin/env bash
set -x

# Enables job control
set -m

# Enables error propagation
set -e

/entrypoint.sh couchbase-server &

sleep 15

couchbase-cli  cluster-init -c 127.0.0.1:8091 --cluster-username=admin  --cluster-password=password --cluster-port=8091 --cluster-ramsize=384 --cluster-index-ramsize=384 --services=data,index,query

couchbase-cli bucket-create -c 127.0.0.1:8091 --bucket=default --bucket-type=couchbase --bucket-port=11211  --bucket-ramsize=100  --bucket-replica=0 -u admin  -p password


# Setup index and memory quota
curl -v -X POST http://127.0.0.1:8091/pools/default -d memoryQuota=300 -d indexMemoryQuota=300

# Setup Memory Optimized Indexes
curl -i -u admin:password -X POST http://127.0.0.1:8091/settings/indexes -d 'storageMode=memory_optimized'

sleep 15

cbq -engine=http://127.0.0.1:8093  --script="create primary index on default using gsi;"


fg 1