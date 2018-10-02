#!/bin/bash
set -e

CLUSTER_NAME=i3ql-tpch-benchmark
NAMESPACE_NAME=tpch.i3ql
CIDR=10.0.2.0/24

VPC_ID=$(../lib/aws-ec2-get-vpcid.sh)
SERVER_REPO=i3ql-test-server
CLIENT_REPO=i3ql-test-client

echo "Creating cluster $CLUSTER_NAME"
aws ecs create-cluster\
	--cluster-name $CLUSTER_NAME

# Make sure cpu and memory combination exist in fargate
# https://docs.aws.amazon.com/de_de/cli/latest/reference/ecs/register-task-definition.html#options
../lib/aws-ecs-register-task-definition.sh i3ql-tpch-server $SERVER_REPO 1vcpu 2GB
../lib/aws-ecs-register-task-definition.sh i3ql-tpch-data-customer $CLIENT_REPO 1vcpu 2GB
../lib/aws-ecs-register-task-definition.sh i3ql-tpch-data-nation $CLIENT_REPO 1vcpu 2GB
../lib/aws-ecs-register-task-definition.sh i3ql-tpch-data-orders $CLIENT_REPO 1vcpu 2GB
../lib/aws-ecs-register-task-definition.sh i3ql-tpch-data-part $CLIENT_REPO 1vcpu 2GB
../lib/aws-ecs-register-task-definition.sh i3ql-tpch-data-partsupp $CLIENT_REPO 1vcpu 2GB
../lib/aws-ecs-register-task-definition.sh i3ql-tpch-data-region $CLIENT_REPO 1vcpu 2GB
../lib/aws-ecs-register-task-definition.sh i3ql-tpch-data-supplies $CLIENT_REPO 1vcpu 2GB
../lib/aws-ecs-register-task-definition.sh i3ql-tpch-process-finance $CLIENT_REPO 1vcpu 2GB
../lib/aws-ecs-register-task-definition.sh i3ql-tpch-process-purchasing $CLIENT_REPO 1vcpu 2GB
../lib/aws-ecs-register-task-definition.sh i3ql-tpch-process-shipping $CLIENT_REPO 1vcpu 2GB
../lib/aws-ecs-register-task-definition.sh i3ql-tpch-process-geographical $CLIENT_REPO 1vcpu 2GB
../lib/aws-ecs-register-task-definition.sh i3ql-tpch-process-private $CLIENT_REPO 1vcpu 2GB
../lib/aws-ecs-register-task-definition.sh i3ql-tpch-client $CLIENT_REPO 1vcpu 2GB


../lib/aws-ec2-create-subnet.sh $VPC_ID $CLUSTER_NAME $CIDR
SUBNET_ID=$(../lib/aws-ec2-get-subnetid.sh $CLUSTER_NAME)

../lib/aws-sd-create-namespace.sh $VPC_ID $NAMESPACE_NAME
NAMESPACE_ID=$(../lib/aws-sd-get-namespaceid.sh $NAMESPACE_NAME)

../lib/aws-create-service.sh $CLUSTER_NAME i3ql-tpch-server server $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-tpch-data-customer data-customer $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-tpch-data-nation data-nation $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-tpch-data-orders data-orders $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-tpch-data-part data-part $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-tpch-data-partsupp data-partsupp $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-tpch-data-region data-region $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-tpch-data-supplies data-supplies $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-tpch-process-finance process-finance $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-tpch-process-purchasing process-purchasing $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-tpch-process-shipping process-shipping $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-tpch-process-geographical process-geographical $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-tpch-process-private process-private $SUBNET_ID $NAMESPACE_ID
../lib/aws-create-service.sh $CLUSTER_NAME i3ql-tpch-client client $SUBNET_ID $NAMESPACE_ID