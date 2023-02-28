import axios from "axios";
import {SERVICE_HOST} from "./const";

export const getListByPage = async (url, page) => {
    try {
        const response = await axios.get(
            SERVICE_HOST + url,
            {params: {page: page}});
        return response.data;
    } catch (err) {
        console.error(err);
    }
};

export const getListByNameAndPage = async (url, key, page) => {
    try {
        const response = await axios.get(
            SERVICE_HOST + url,
            {params: {page: page, name: key}});
        return response.data;
    } catch (err) {
        console.error(err);
    }
};

export const updateData = async (url, reqBody) => {
    const formData = new FormData();

    const cityProperties = {
        "name": reqBody.name
    }
    formData.append('cityProperties',
        new Blob([JSON.stringify(cityProperties)], {
            type: 'application/json'
        }));

    if(reqBody.image) {
        formData.append("image", reqBody.image, reqBody.name);
    }

    try {
        const response = await axios.put(
            `${SERVICE_HOST}${url}/${reqBody.id}`,
            formData,
            {
                headers: {
                    "Content-Type": "multipart/form-data"
                }
            }
        );
        return response.data;
    } catch (err) {
        console.error(err);
    }
};
