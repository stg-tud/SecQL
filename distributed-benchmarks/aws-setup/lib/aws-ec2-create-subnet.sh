#!/bin/bash

echo "Creating subnet $2 with cidr $3 in i3ql $1"

echo ">> Creating subnet"
SUBNET=$(aws ec2 create-subnet \
	--vpc-id $1\
	--cidr-block $3 \
| sed -nE '/"SubnetId": "/{s/.*:\s*"(.*)",/\1/p;q}')

echo ">> Adding name tag"
aws ec2 create-tags \
	--resources $SUBNET \
	--tags "[{
		\"Key\": \"Name\",
		\"Value\": \"$2\"
	}]"