from transformers import AutoImageProcessor, SiglipForImageClassification
import torch
from PIL import Image
import numpy as np

class ClassificationService:
    def init(self, model_name: str = "prithivMLmods/Trash-Net"):
        # loads weights (92.9M params) and processor from HF Hub
        self.model = SiglipForImageClassification.from_pretrained(model_name)
        self.processor = AutoImageProcessor.from_pretrained(model_name)
        self.labels = ["cardboard","glass","metal","paper","plastic","trash"]

    def preprocess(self, image_bytes: bytes) -> dict:
        img = Image.open(io.BytesIO(image_bytes)).convert("RGB")
        return self.processor(images=img, return_tensors="pt")

    def get_classification(self, image_bytes: bytes) -> str:
        inputs = self.preprocess(image_bytes)
        with torch.no_grad():
            outputs = self.model(**inputs)
        probs = torch.nn.functional.softmax(outputs.logits, dim=-1).squeeze().tolist()
        idx = int(np.argmax(probs))
        return self.labels[idx]
    
svc = ClassificationService()