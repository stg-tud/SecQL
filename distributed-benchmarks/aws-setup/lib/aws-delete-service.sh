#!/bin/bash

echo "Deleting service $2 in cluster $1 and namespace $3"

echo ">> Getting service description"
DESCRIPTION=$(aws ecs describe-services \
	--cluster $1 \
	--services $2)

if [[ $DESCRIPTION =~ "\"status\": \"ACTIVE\"," ]]; then

	echo ">> Scaling service down to 0 tasks"
	aws ecs update-service \
		--cluster $1 \
		--service $2 \
		--desired-count 0

	while true; do
		echo ">> Checking whether service task containers are down"
		RUNNING_TASKS=$(aws ecs describe-services --cluster $1 --services $2 | sed -nE '/"runningCount": /{s/.*:\s*(.*),/\1/p;q}')
		PENDING_TASKS=$(aws ecs describe-services --cluster $1 --services $2 | sed -nE '/"pendingCount": /{s/.*:\s*(.*),/\1/p;q}')
		echo ">> $RUNNING_TASKS running and $PENDING_TASKS pending containers"

		if [ $RUNNING_TASKS -eq 0 ] && [ $PENDING_TASKS -eq 0 ]; then
			break
		else
			echo ">> Going to re-check in 2 seconds..."
			sleep 2
		fi
	done

	echo ">> Deleting service"
	aws ecs delete-service \
		--cluster $1 \
		--service $2

else
	echo ">> Service is not active. Most probably it was not created or is already deleted..."
fi

echo ">> Getting namespace id"
NAMESPACE_ID=$(aws-sd-get-namespaceid.sh $3)
if [ -z $NAMESPACE_ID ]; then
	echo ">> Namespace seems not to exist, skipping servicediscovery deregistraion"
else

	echo ">> Getting service id in servicediscovery"
	DISCOVERY_SERVICE_ID=$(echo $0 | sed "s/delete-service.sh/sd-get-serviceid.sh $NAMESPACE_ID $2/e")

	if [ -z $DISCOVERY_SERVICE_ID ]; then
		echo ">> Discovery service seems not to exist, skipping servicediscovery deregistraion"
	else
		echo ">> Deleting service from servicediscovery"
		aws servicediscovery delete-service \
			--id $DISCOVERY_SERVICE_ID
	fi
fi