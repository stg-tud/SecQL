#!/bin/bash

NAMESPACE_ID=$(aws servicediscovery list-namespaces \
	| tr '\n' ' ' \
	| sed -nE "s/.*\{([^}]*\"Name\":\s*\"$1\"[^}]*)\}.*/\1/p" \
	| sed -nE 's/.*"Id":\s*"([^"]*)".*/\1/p')

if [ -z $NAMESPACE_ID ]; then
	echo "No namespace with the name $1 exists" >&2
	exit 1
fi
echo $NAMESPACE_ID