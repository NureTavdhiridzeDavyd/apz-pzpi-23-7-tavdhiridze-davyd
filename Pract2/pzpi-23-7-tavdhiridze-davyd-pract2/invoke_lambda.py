import boto3
import json

lambda_client = boto3.client(
    'lambda',
    region_name='eu-west-1'
)

response = lambda_client.invoke(
    FunctionName='process-order',
    InvocationType='RequestResponse',
    Payload=json.dumps({
        'order_id': 'ORD-42',
        'user_id': 'USR-007'
    })
)

result = json.loads(
    response['Payload'].read()
)

print(result)
