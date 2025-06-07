# generate_proto.py
"""
Script to generate Python gRPC code from proto file
Run this after creating the .proto file to generate the Python stubs
"""

import os
import sys
import subprocess

try:
    import grpc_tools.protoc
    print("grpc_tools is installed and importable!")
except ImportError:
    print("grpc_tools is NOT importable!")

def generate_python_grpc():
    """Generate Python gRPC stubs from proto file"""
    print("Generating Python gRPC code from proto file...")
    
    # Path to the proto file
    proto_file = "smart_home.proto"
    
    # Check if the proto file exists
    if not os.path.exists(proto_file):
        print(f"Error: Proto file '{proto_file}' not found!")
        print("Make sure to run this script from the directory containing the .proto file")
        return False
    else:
        print(f"Proto file '{proto_file}' found!")
    # Generate Python code
    try:
        subprocess.check_call([
            sys.executable, "-m", "grpc_tools.protoc",
            "--proto_path=.",
            "--python_out=.",
            "--grpc_python_out=.",
            proto_file
        ])
        print("Python gRPC code generated successfully!")
        return True
    except subprocess.CalledProcessError as e:
        print(f"Error generating Python code: {e}")
        print("\nMake sure you have the grpcio-tools package installed:")
        print("pip install grpcio-tools")
        return False
    except FileNotFoundError as e:
        print(f"Error: {e}")
        print("\nMake sure you have the grpcio-tools package installed:")
        print("pip install grpcio-tools")
        return False

if __name__ == "__main__":
    generate_python_grpc()