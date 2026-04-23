provider "aws" {
  region = "eu-west-1"
}

resource "aws_instance" "web" {
  ami           = "ami-0c55b159cbfafe1f0"
  instance_type = "t3.micro"
  tags = {
    Name = "WebServer"
    Env  = "production"
  }
}

resource "aws_s3_bucket" "assets" {
  bucket = "my-app-assets-bucket"
}
