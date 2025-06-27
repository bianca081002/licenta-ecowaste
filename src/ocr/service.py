import io
from transformers import AutoImageProcessor, SiglipForImageClassification
import torch
from PIL import Image
import numpy as np
import datetime

class ClassificationService:
    def __init__(self, model_name: str = "prithivMLmods/Trash-Net"):
        # loads weights (92.9M params) and processor from HF Hub
        self.model = SiglipForImageClassification.from_pretrained(model_name)
        self.processor = AutoImageProcessor.from_pretrained(model_name)
        self.labels = ["cardboard","glass","metal","paper","plastic","trash"]

    def preprocess(self, image_bytes: bytes) -> dict:
        img = Image.open(io.BytesIO(image_bytes)).convert("RGB")
        return self.processor(images=img, return_tensors="pt")

    

    def get_classification(self, image_bytes: bytes) -> str:
        try:
            inputs = self.preprocess(image_bytes)
            with torch.no_grad():
                outputs = self.model(**inputs)
            probs = torch.nn.functional.softmax(outputs.logits, dim=-1).squeeze().tolist()

            idx = int(np.argmax(probs))
            predicted_label = self.labels[idx]

            # Logăm clasificarea și probabilitățile
            timestamp = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            log_entry = f"[{timestamp}] Predicted: {predicted_label} | Probabilities: {probs}"
            print(log_entry)
            with open("classification_log.txt", "a") as log_file:
                log_file.write(log_entry + "\n")

            return predicted_label

        except Exception as e:
            print(f"Error during classification: {e}")
            raise e

    
svc = ClassificationService()