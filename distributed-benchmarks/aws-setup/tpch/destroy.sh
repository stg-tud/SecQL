#!/bin/bash
set -e

CLUSTER_NAME=i3ql-tpch-benchmark
NAMESPACE_NAME=tpch.i3ql

../lib/aws-delete-service.sh $CLUSTER_NAME server $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME data-customer $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME data-nation $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME data-orders $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME data-part $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME data-partsupp $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME data-region $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME data-supplies $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME process-finance $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME process-purchasing $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME process-shipping $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME process-geographical $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME process-private $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME client $NAMESPACE_NAME

../lib/aws-ecs-deregister-task-definitions.sh i3ql-company-server
../lib/aws-ecs-deregister-task-definitions.sh i3ql-tpch-data-customer
../lib/aws-ecs-deregister-task-definitions.sh i3ql-tpch-data-nation
../lib/aws-ecs-deregister-task-definitions.sh i3ql-tpch-data-orders
../lib/aws-ecs-deregister-task-definitions.sh i3ql-tpch-data-part
../lib/aws-ecs-deregister-task-definitions.sh i3ql-tpch-data-partsupp
../lib/aws-ecs-deregister-task-definitions.sh i3ql-tpch-data-region
../lib/aws-ecs-deregister-task-definitions.sh i3ql-tpch-data-supplies
../lib/aws-ecs-deregister-task-definitions.sh i3ql-tpch-process-finance
../lib/aws-ecs-deregister-task-definitions.sh i3ql-tpch-process-purchasing
../lib/aws-ecs-deregister-task-definitions.sh i3ql-tpch-process-shipping
../lib/aws-ecs-deregister-task-definitions.sh i3ql-tpch-process-geographical
../lib/aws-ecs-deregister-task-definitions.sh i3ql-tpch-process-private
../lib/aws-ecs-deregister-task-definitions.sh i3ql-tpch-client

../lib/aws-ecs-delete-cluster.sh $CLUSTER_NAME
../lib/aws-ec2-delete-subnet.sh $CLUSTER_NAME
../lib/aws-sd-delete-namespace.sh $NAMESPACE_NAME