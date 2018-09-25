#!/bin/bash

echo "Registering task $1 from repo $2 with $3 CPUs and $4 memory"

echo ">> Getting repo URI"
REPO=$(aws ecr describe-repositories --repository-names $2 | sed -nE '/"repositoryUri": "/{s/.*:\s*"(.*)",/\1/p;q}')

echo ">> Getting execution role ARN"
EXECUTION_ROLE_NAME=ecsTaskExecutionRole
EXECUTION_ROLE=$(aws iam get-role --role-name $EXECUTION_ROLE_NAME | sed -nE '/"Arn": "/{s/.*:\s*"(.*)",/\1/p;q}')

echo ">> Registering task definition"
aws ecs register-task-definition \
	--family $1 \
	--network-mode awsvpc \
	--container-definitions "[{
			\"name\": \"client\",
			\"image\": \"$REPO:latest\",
			\"portMappings\": [{
				\"containerPort\": 22,
				\"hostPort\": 22,
				\"protocol\": \"tcp\"
			}],
			\"essential\": true
		}]" \
	--cpu $3 \
	--memory $4 \
	--requires-compatibilities FARGATE \
	--execution-role-arn $EXECUTION_ROLE