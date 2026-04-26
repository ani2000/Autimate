import onnxruntime as ort
import numpy as np


def q10_data_proc(data_array):
    new_list = []
    for idx, itm in enumerate(data_array): 
        if idx < 10: # 0-9 accessed
            itm = 1 if itm < 3 else 0
        new_list.append(itm)

    return new_list


def q10_autism_prediction(model_path, data_array):
    # Data preperation
    data_array = np.array(data_array).astype(np.float32)
    data_array = np.expand_dims(data_array, 0)
    data_array = {'input': data_array}

    # Model building.
    ort_session = ort.InferenceSession(model_path)
    output = ort_session.run(None, data_array)
    return output[0][0]



# def q10_inference(data_array):
#     print(q10_autism_prediction("q10_autism_detection_model.onnx", data_array))


# main([ 1., 0., 1., 1., 1., 0., 1., 1., 1., 1., 0., 8., 26., 0.])