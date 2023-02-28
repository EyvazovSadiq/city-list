import CityItem from "./CityItem";
import Search from "./Search";
import {getCitiesByName} from "../utils/apiRequests";
import React, {useState} from "react";
import {Pagination} from "./Pagination";

const CityList = ({data, onEdit, loadData}) => {

    const [cityData, setCityData] = useState(data);
    const [searchKey, setSearchKey] = useState("");

    const searchCities = async (name, page = 1) => {
        if (searchKey !== name) {
            setSearchKey(name);
        }

        if (name === "RELOAD_DATA") {
            loadData(1);
            return;
        }
        const response = await getCitiesByName(name, page);
        setCityData(response)
    }

    return (
        <>
            <div className="cities">
                <Search onSearchCities={
                    searchCities
                }/>

                {cityData.cities.map(city => (
                    <CityItem data={city} key={city.id} onEdit={onEdit}/>
                ))
                }
            </div>

            {<Pagination currentPage={cityData.currentPage}
                         totalElements={cityData.totalElements}
                         onPageChange={(page) => {
                                 if (searchKey.length > 0) {
                                     searchCities(searchKey, page)
                                 } else {
                                     loadData(page);
                                 }
                             }}
            />}
        </>
    )
}

export default CityList;