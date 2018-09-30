#!/bin/bash

VPC=$(aws ec2 describe-vpcs \
	--filters "[
		{
			\"Name\": \"tag:Name\",
			\"Values\": [\"i3ql\"]
		},
		{
			\"Name\": \"cidr\",
			\"Values\": [\"10.0.0.0/16\"]
		},
		{
			\"Name\": \"state\",
			\"Values\": [\"available\"]
		}
	]" \
| sed -nE '/"VpcId": "/{s/.*:\s*"(.*)",/\1/p;q}')
if [ -z $VPC ]; then
	echo "Please create a vpc with cidr 10.0.0.0/16 and Name Tag i3ql!!! Exiting..." >&2
	exit 1
fi
echo $VPC