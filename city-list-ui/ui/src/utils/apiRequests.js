import {getListByNameAndPage, getListByPage, updateData} from "./apiClient";

export const getCitiesByPage = async (page) => {
    let response;
    try {
        response = await getListByPage('/city-list/get', page);
    } catch (error) {
        console.error(error);
    }
    return response;
};

export const getCitiesByName = async (key, page) => {
    let response;
    try {
        response = await getListByNameAndPage('/city-list/search', key, page);
    } catch (error) {
        console.error(error);
    }
    return response;
};

export const updateCity = async (city) => {
    let response;
    try {
        response = await updateData('/city-list/update', city);
    } catch (error) {
        console.error(error);
    }
    return response;
};


