#!/bin/bash

echo "Deleting namespace $1"

echo ">> Getting namespace id"
NAMESPACE_ID=$(echo $0 | sed "s/delete-namespace.sh/get-namespaceid.sh $1/e")

if [ -z $NAMESPACE_ID ]; then
	echo ">> Namespace seems not to exist, skipping deletion"
else
	echo ">> Deleting namespace"
	aws servicediscovery delete-namespace \
		--id $NAMESPACE_ID

	echo ">> Deleted successfully"
fi