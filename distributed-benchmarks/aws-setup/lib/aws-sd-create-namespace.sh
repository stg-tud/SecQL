#!/bin/bash

echo "Creating namespace $2 in VPC $1"

echo ">> Request namespace creation"
OPID=$(aws servicediscovery create-private-dns-namespace \
	--name $2 \
	--vpc $1 \
| sed -nE '/"OperationId": "/{s/.*:\s*"(.*)"/\1/p;q}')

while [ "SUCCESS" != $(aws servicediscovery get-operation \
	--operation-id $OPID | sed -nE '/"Status": "/{s/.*:\s*"(.*)",/\1/p;q}') ]; do
	echo ">> Not created yet. Retrying in 2 seconds..."
	sleep 2
done

echo ">> Created successfully"