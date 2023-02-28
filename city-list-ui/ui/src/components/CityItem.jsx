const CityItem = ({data, onEdit}) => {
    return (

        <div className="cities__item col-md-2 col-6">
            <img src={`http://localhost:8080/city-list/images/${data.id}?${Date.now()}`} alt="city_iamge"/>
            <div className="content">
                <h4>{data.name}</h4>
                <button
                    className="btn btn-outline-danger"
                    onClick={() => {
                        onEdit(data)}
                    }
                >
                    Edit
                </button>
            </div>
        </div>
    )
}

export default CityItem;