import {useState} from "react";
import Form from "react-bootstrap/Form";

const Search = (props) => {

    const [searchKey, setSearchKey] = useState("");

    const searchKeyHandler = (event) => {
        const searchKey = event.target.value
        setSearchKey(searchKey);
        if (searchKey.length === 0) {
            props.onSearchCities("RELOAD_DATA");
        }
    }

    const search = (event) => {
        event.preventDefault()
        if (searchKey.trim().length === 0) {
            return;
        }
        props.onSearchCities(searchKey);
    };

    return (
        <div className="cities__search col-12">
            <form action="" className="d-flex justify-content-center">
                <Form.Control
                    value={searchKey}
                    size="lg"
                    type="text"
                    placeholder="Search city by name..."
                    onChange={searchKeyHandler}
                />
                <button
                    className="btn btn-danger"
                    onClick={search}
                >
                    Search
                </button>
            </form>
        </div>
    )
}

export default Search;