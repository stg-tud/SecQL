#!/bin/bash

SERVICE_ID=$(aws servicediscovery list-services \
	--filters "[{
		\"Name\": \"NAMESPACE_ID\",
		\"Values\": [\"$1\"],
		\"Condition\": \"EQ\"
	}]" \
	| tr '\n' ' ' \
	| sed -nE "s/.*\{([^}]*\"Name\":\s*\"$2\"[^}]*)\}.*/\1/p" \
	| sed -nE 's/.*"Id":\s*"([^"]*)".*/\1/p')

if [ -z $SERVICE_ID ]; then
	echo "No service with the name $2 exists in namespace $1" >&2
	exit 1
fi
echo $SERVICE_ID