#!/bin/bash

echo "Deleting cluster $1"

while true; do
	if aws ecs delete-cluster --cluster $1; then
		break
	else
		RUNNING_TASKS=$(aws ecs describe-clusters --cluster $1 | sed -nE '/"runningTasksCount": /{s/.*:\s*(.*),/\1/p;q}')
		PENDING_TASKS=$(aws ecs describe-clusters --cluster $1 | sed -nE '/"pendingTasksCount": /{s/.*:\s*(.*),/\1/p;q}')

		if [ $RUNNING_TASKS -eq 0 ] && [ $PENDING_TASKS -eq 0 ]; then
			echo ">> Failed to delete ($RUNNING_TASKS running tasks and $PENDING_TASKS pending tasks)"
			exit 1
		else
			echo ">> Failed to delete cluster, because of $RUNNING_TASKS tasks are running and $PENDING_TASKS tasks are pending"
			echo ">> Going to retry in 2 seconds..."
			sleep 2
		fi
	fi
done

echo ">> Successfully deleted"