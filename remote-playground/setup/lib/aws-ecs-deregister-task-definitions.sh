#!/bin/bash

echo "Deregistering all tasks $1"

aws ecs list-task-definitions \
	--family-prefix $1 \
	--status ACTIVE \
| sed -nE '/arn:aws:ecs:/{s/^.*"(.*)".*$/echo ">> Deregistering \1"; aws ecs deregister-task-definition --task-definition \1/ep}'