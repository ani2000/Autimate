import cv2, math, random, os
import numpy as np


seed = 2021
random.seed(seed)
np.random.seed(seed)
CLIP_LEN, RESIZE_HEIGHT, CROP_SIZE, size2 = 16, 128, 108, 200 


"""
    Read video files and extract frames.
"""
def frame_extractor(video_path):
    cap = cv2.VideoCapture(video_path)
    frames = []
    while cap.isOpened():
        ret, frame = cap.read()
        if ret:
            frames.append(frame)
        else:
            break
    cap.release()

    return frames


def frame_tensoring(frames, clip_len, crop_size, size2):
    buffer = load_frames(frames)
    buffer = crop(buffer, clip_len, crop_size, size2)
    buffer = resize(buffer)
    buffer = normalize(buffer)
    buffer = to_tensor(buffer)
    
    return buffer

"""
    Given a video path, this function return frames as tensor shape.
"""
def get_video_tensor(video_path):
    frames = frame_extractor(video_path) # Extract frames.
    return frame_tensoring(frames, clip_len=CLIP_LEN, crop_size=CROP_SIZE, size2=size2)

"""
    From given frames, load 1 frame after 16 frames.
"""
def load_frames(frames):
    buffer = []
    total = len(frames)
    skip = max(int(total / 16), 1)  # uniform frame loading for HGD dataset
    for i, frame_data in enumerate(frames):
        if i % skip == 0:
            buffer.append(frame_data)
    return np.array(buffer).astype(np.uint8)


def resize(buffer):
    n=buffer.shape[0]
    new_buffer=np.empty((n,112,128,3))
    for i, frame in enumerate(buffer):
        frame=cv2.resize(buffer[i], (128,112), interpolation= cv2.INTER_LINEAR)
        new_buffer[i]=frame
    return new_buffer

def normalize(buffer):
    buffer = buffer.astype(np.float32)
    for i, frame in enumerate(buffer):
        buffer[i] = frame / 255.0
    return buffer

def to_tensor(buffer):
    return buffer.transpose((3, 0, 1, 2))


def crop(buffer, clip_len, crop_size, crop_size2):
    time_index = max(0, math.floor((buffer.shape[0] - clip_len) / 2))
    height_index = max(0, math.floor((buffer.shape[1] - crop_size) / 2))
    width_index = max(0, math.floor((buffer.shape[2] - crop_size2) / 2))
    
    buffer = buffer[time_index:time_index + clip_len,
                    height_index:height_index + crop_size,
                    width_index:width_index + crop_size2, :]
    
    if buffer.shape[0] < clip_len:
        buffered = buffer
        reverse = True
        repeated = clip_len // buffer.shape[0] - 1
        remainder = clip_len % buffer.shape[0]
        
        if repeated > 0:
            padded = []
            for _ in range(repeated):
                pad = buffer[::-1, :, :, :] if reverse else buffer
                padded.append(pad)
                reverse = not reverse
            buffer = np.concatenate((buffer, *padded), axis=0)
        
        pad = buffered[::-1, :, :, :][:remainder] if reverse else buffered[:remainder]
        buffer = np.concatenate((buffer, pad), axis=0)
    
    return buffer