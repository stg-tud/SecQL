#!/bin/bash

echo "Creating service $3 from task $2 in cluster $1 within subnet $4 and namespace $5"

echo ">> Creating service $3 in servicediscovery namespace $5$"
DISCOVERY_ARN=$(aws servicediscovery create-service \
	--name $3 \
	--dns-config "{
		\"NamespaceId\": \"$5\",
		\"DnsRecords\": [{
			\"Type\": \"A\",
			\"TTL\": 300
		}]
	}" \
	--health-check-custom-config FailureThreshold=1 \
	| sed -nE '/"Arn": "/{s/.*:\s*"(.*)",/\1/p;q}')

echo ">> Getting task definition $2"
TASK_DEFINITION=$(aws ecs list-task-definitions \
	--family-prefix $2 \
	--status ACTIVE \
	--max-items 1 \
	--sort DESC \
| grep "arn:aws:ecs:" \
| sed 's/^.*"\(.*\)".*$/echo \1/e')

echo ">> Creating service $3"
aws ecs create-service \
	--cluster $1 \
	--service-name $3 \
	--task-definition $TASK_DEFINITION \
	--launch-type FARGATE \
	--desired-count 1 \
	--network-configuration "{
		\"awsvpcConfiguration\": {
			\"subnets\": [\"$4\"],
			\"securityGroups\": [],
			\"assignPublicIp\": \"ENABLED\"
		}
	}" \
	--service-registries "[{
		\"registryArn\": \"$DISCOVERY_ARN\"
	}]"