import boto3

s3 = boto3.client('s3', region_name='eu-west-1')

s3.upload_file(
    Filename='report.pdf',
    Bucket='my-app-bucket',
    Key='reports/2024/report.pdf'
)

print("Файл успішно завантажено!")  
