#!/bin/bash
set -e

CLUSTER_NAME=i3ql-company-benchmark
NAMESPACE_NAME=company.i3ql

../lib/aws-delete-service.sh $CLUSTER_NAME server $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME public $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME production $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME purchasing $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME employees $NAMESPACE_NAME
../lib/aws-delete-service.sh $CLUSTER_NAME client $NAMESPACE_NAME

../lib/aws-ecs-deregister-task-definitions.sh i3ql-company-server
../lib/aws-ecs-deregister-task-definitions.sh i3ql-company-public
../lib/aws-ecs-deregister-task-definitions.sh i3ql-company-production
../lib/aws-ecs-deregister-task-definitions.sh i3ql-company-purchasing
../lib/aws-ecs-deregister-task-definitions.sh i3ql-company-employees
../lib/aws-ecs-deregister-task-definitions.sh i3ql-company-client

../lib/aws-ecs-delete-cluster.sh $CLUSTER_NAME
../lib/aws-ec2-delete-subnet.sh $CLUSTER_NAME
../lib/aws-sd-delete-namespace.sh $NAMESPACE_NAME