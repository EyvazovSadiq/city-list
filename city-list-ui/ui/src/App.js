import "./assets/style.scss"
import React, {useEffect, useState} from "react";
import CityList from "./components/CityList";
import EditModal from "./components/EditModal";
import {getCitiesByPage} from "./utils/apiRequests";
import Spinner from 'react-bootstrap/Spinner';

function App() {

    const [cityData, setCityData] = useState(null);
    const [selectedCity, setSelectedCity] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [isModalOpen, setIsModalOpen] = useState(false);

    useEffect(() => {
        loadCities()
    }, []);

    const loadCities = async (page = 1) => {
        setIsLoading(true);
        setCityData([]);
        const cityList = await getCitiesByPage(page);
        setCityData(cityList);
        setIsLoading(false);
    }

    return (
        <div className="App">
            {isLoading && <Spinner className="text-danger position-fixed bottom-50 end-50"/>}

            {!isLoading && cityData?.cities.length > 0 &&
                <CityList data={cityData} onEdit={(city) => {
                    setIsModalOpen(true);
                    setSelectedCity({
                        "id": city.id,
                        "name": city.name,
                        "picturePath": city.imagePath
                    });
                }}
                          loadData={(page) => loadCities(page)}
                />}

            {isModalOpen && (
                <EditModal
                    isModalOpen={isModalOpen}
                    city={selectedCity}
                    onModalClose={() => {
                        setIsModalOpen(false);
                    }}
                    onModalUpdate={() => {
                        loadCities();
                        setIsModalOpen(false);
                    }}
                />
            )}
        </div>
    );
}

export default App;
