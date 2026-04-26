import boto3
from botocore.exceptions import NoCredentialsError

def download_file_from_space(space_name, file_name, local_file_name, region, access_key, secret_key):
    try:
        # Connect to the DigitalOcean Space
        session = boto3.session.Session()
        client = session.client('s3',
                                region_name=region,
                                endpoint_url=f'https://{region}.digitaloceanspaces.com',
                                aws_access_key_id=access_key,
                                aws_secret_access_key=secret_key)

        client.download_file(space_name, file_name, local_file_name)
        return True
    
    except NoCredentialsError:
        print("Credentials not available")
        return False
    except Exception as e:
        print(f"Error occurred: {e}")
        return False