#!/bin/bash

echo "Deleting subnet $1"

echo ">> Getting subnet description"
DESCRIPTION=$(aws ec2 describe-subnets \
	--filters "[{
		\"Name\": \"tag:Name\",
		\"Values\": [\"$1\"]
	}]")

if [[ $DESCRIPTION =~ "\"State\": \"available\"" ]]; then

	echo ">> Getting subnet id"
	SUBNET=$(echo $0 | sed "s/delete-subnet.sh/get-subnetid.sh $1/e")

	while true; do
		echo ">> Deleting subnet"
		if aws ec2 delete-subnet \
			--subnet-id $SUBNET
		then
			break
		else
			echo ">> Failed to delete subnet (usually that means dependencies are still registered)"
			echo ">> Going to retry in 2 seconds.."
			sleep 2
		fi
	done

	echo ">> Successfully deleted subnet $1"
else
	echo ">> Subnet is not available. Most probably it was not created or is already deleted..."
fi