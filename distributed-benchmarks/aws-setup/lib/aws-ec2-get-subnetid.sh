#!/bin/bash

SUBNET=$(aws ec2 describe-subnets \
	--filters "[
		{
			\"Name\": \"tag:Name\",
			\"Values\": [\"$1\"]
		}
	]" \
| sed -nE '/"SubnetId": "/{s/.*:\s*"(.*)",/\1/p;q}')
if [ -z $SUBNET ]; then
	echo "Subnet $1 does not exist!!! Exiting..." >&2
	exit 1
fi
echo $SUBNET