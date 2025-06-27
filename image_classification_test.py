import os
import requests

# Config
API_URL = "http://localhost:8111/recycling/classify/"
IMAGE_FOLDER = "test_images"

# Check if folder exists
if not os.path.exists(IMAGE_FOLDER):
    print(f"Folder '{IMAGE_FOLDER}' not found.")
    exit()

# Iterate through images in the folder
for image_name in os.listdir(IMAGE_FOLDER):
    image_path = os.path.join(IMAGE_FOLDER, image_name)
    if os.path.isfile(image_path):
        try:
            # Open the image and send it to the API
            with open(image_path, "rb") as image_file:
                files = {"file": image_file}
                response = requests.post(API_URL, files=files)
                
                # Check the response
                if response.status_code == 200:
                    result = response.json()
                    print(f"Image: {image_name} - Classified as: {result['bin']}")
                else:
                    print(f"Error for {image_name}: {response.status_code} - {response.text}")
        except Exception as e:
            print(f"Failed to process {image_name}: {e}")
    else:
        print(f"Skipping {image_name}, not a file.")
