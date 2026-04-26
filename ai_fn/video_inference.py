import numpy as np
import onnxruntime as ort
from http import HTTPStatus

from ai_fn.video_onnx_proc import get_video_tensor


def video_autism_prediction(model_path, video_path):
    # Data preperation
    video_tensor = get_video_tensor(video_path)
    video_tensor = np.expand_dims(video_tensor, 0)
    video_tensor = {'input': video_tensor}

    # Model building.
    ort_session = ort.InferenceSession(model_path)
    index, confidence = ort_session.run(None, video_tensor)
    
    index_str = str(index.tolist()) 
    confidence_str = str(confidence[index].tolist())

    return index_str, confidence_str