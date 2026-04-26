import pymongo
from pymongo import MongoClient
import numpy as np

from ai_fn.config import mongodb_cfg as c


uri = f"mongodb+srv://{c.MONGO_USER}:{c.MONGO_PASSWORD}@{c.MONGO_CLUSTER}/{c.MONGO_DATABASE}?retryWrites=true&w=majority"
client = MongoClient(uri)
db = client[c.MONGO_DATABASE]

collection = db['asd_ex']

def push_to_db(data):
    collection.insert_one(data)


def numpy_data_formatter(data):
    if isinstance(data, np.ndarray):
        data = data.tolist() if data.ndim > 0 else data.item()
    return data