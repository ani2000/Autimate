import os
import modal
import modal.gpu
import numpy as np
from http import HTTPStatus
from datetime import datetime

from ai_fn.do import download_file_from_space
from ai_fn.video_inference import video_autism_prediction
from ai_fn.db import push_to_db, numpy_data_formatter
from ai_fn.q10_manager import q10_data_proc, q10_autism_prediction
from ai_fn.config import env_cfg as e, SECRET_TOKEN



image = modal.Image.debian_slim(python_version="3.11").pip_install(
        "onnx", "onnxruntime", "boto3", 
        "opencv-python", "requests", "modal", "pymongo", "python-dotenv").apt_install("libglu1-mesa-dev", "libglib2.0-0")

app = modal.App(image=image, name="autism_video_analysis_fn")


@app.function(gpu=modal.gpu.T4(count=1), timeout=120, volumes={"/storage": modal.Volume.from_name("wwvolume")})
@modal.web_endpoint(method="POST")
def main(args: dict) -> str:
        # Collect all the passed data.
        q10_data_array = args['arrq10']
        request_id = args['video_name'].split(".mp4")[0]
        video_name = args['video_name']
        secret_token = args['secret_token']
        username = args['childId']

        
        if secret_token != SECRET_TOKEN:
                return {"statusCode" : HTTPStatus.FORBIDDEN}

        # Download video.
        video_download_status = download_file_from_space(e.SPACE_NAME,video_name, e.LOCAL_FILE_NAME, 
                                                   e.REGION, e.ACCESS_KEY, e.SECRET_KEY)
        if video_download_status:   
                # Q10 data processing.
                proc_q10_array = q10_data_proc(q10_data_array)
                q10_res = q10_autism_prediction(e.Q_MODEL_PATH, proc_q10_array)
                
                #Video inferencing
                video_res_status, video_res_confidence = video_autism_prediction(e.MODEL_PATH, e.LOCAL_FILE_NAME)

                # Ask GPT for therapy suggestions.
                suggested_therapies = "Speech Therapy"
                suggested_games = "Flashcard"

                print(q10_res)
                print(video_res_status)
                print(video_res_confidence)

                # Write info to db.    
                db_data = {
                        "username": username, "requestID": request_id, "testDate": datetime.now(),
                        "q10": str(q10_res[0]),
                        "vid_res": video_res_status, 
                        "vid_confid": video_res_confidence,
                        "suggested_therapies": suggested_therapies,
                        "suggested_games": suggested_games
                }

                push_to_db(db_data)

                return {"statusCode" : HTTPStatus.OK}

        else:
                return {"statusCode" : HTTPStatus.BAD_REQUEST, "error": "File download error"}