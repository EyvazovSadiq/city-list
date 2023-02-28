import {updateCity} from "../utils/apiRequests";
import {useState} from "react";
import Button from "react-bootstrap/Button";
import {Modal} from "react-bootstrap";
import Form from 'react-bootstrap/Form';

const EditModal = ({city, onModalClose, onModalUpdate}) => {

    const [updatedName, setUpdatedName] = useState(city.name);
    const [updatedImage, setUpdatedImage] = useState();

    const onFileUploaded = (event) => {
        let tgt = event.target || window.event.srcElement,
            files = tgt.files;

        // FileReader support
        if (FileReader && files && files.length) {
            let fr = new FileReader();
            fr.onload = function () {
                document.getElementById('test-img').src = fr.result;
                setUpdatedImage(event.target.files[0]);
            }
            fr.readAsDataURL(files[0]);
        }
    }

    const nameChangeHandler = (event) => {
        setUpdatedName(event.target.value);
    }

    const updateDataById = async () => {
        await updateCity({
            id: city.id,
            name: updatedName,
            image: updatedImage
        })
        onModalUpdate();
    }

    return (
        <>
            <Modal
                show={true}
                onHide={onModalClose}
                size="lg"
                aria-labelledby="contained-modal-title-vcenter"
                centered
            >
                <Modal.Header
                    closeButton
                >
                    <Modal.Title id="contained-modal-title-vcenter">
                        Edit
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <div className="col-md-6 offset-md-3 text-center">
                        <img
                            id="test-img"
                            src={`http://localhost:8080/city-list/images/${city.id}`}
                            width="100%"
                            alt="city_image"
                        />
                        <br/>
                        <label htmlFor="uploadPhoto"><u>Upload new photo</u></label>
                        <input type="file" name="" id="uploadPhoto" onChange={onFileUploaded}/>
                    </div>
                    <div className="content-form-element col-md-6 offset-md-3">
                        <br/>
                        <Form.Control
                            value={updatedName}
                            size="lg"
                            type="text"
                            placeholder="Large text"
                            onChange={nameChangeHandler}
                        />
                    </div>
                </Modal.Body>
                <Modal.Footer className="justify-content-center">
                    <Button onClick={onModalClose} variant="danger">Close</Button>
                    <Button onClick={updateDataById} variant="primary">Update</Button>
                </Modal.Footer>
            </Modal>
        </>

    )
}

export default EditModal;