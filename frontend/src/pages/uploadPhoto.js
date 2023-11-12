import '../css/uploadPhoto.css';
import { MapContainer, Marker, Popup, TileLayer } from "react-leaflet";
import 'leaflet/dist/leaflet.css';
import {useRef, useState} from 'react';
import markerIcon from '../marker-icon.png'
import * as L from "leaflet";
import EXIF from 'exif-js'
import axios from "axios";

const base_url = "http://localhost:8080"

function UploadPhoto() {

    const inputRef = useRef(null);
    const bolognaCoords = [44.494887, 11.3426163]
    let [fileMarker, setFileMarker] = useState({lat: 0.0, lon: 0.0})
    let [selectedFile, setSelectedFile] = useState(null)
    let [isPublic, setPublic] = useState(false)
    let [place, setPlace] = useState(null)

    // TODO Allow user to select more files at once, to upload them as a collection.
    async function uploadOnDB() {
        let reader = new FileReader();

        let imageNameInput = document.getElementById("file-metadata").querySelector("#imageNameInput");
        let placeInput = document.getElementById("file-metadata").querySelector("#placeInput");
        let collectionNameInput = document.getElementById("file-metadata").querySelector("#collectionNameInput");
        let typeInput = document.getElementById("file-metadata").querySelector("#typeInput");
        let queryType = '';
        
        await axios.post(`${base_url}/checkcollectionexists`, {
            logged_user: localStorage.getItem("user"),
            collection_name: collectionNameInput.value
        })
        .then(response => {
            // Collection already exists
            if (response.data.status === 200) {
                queryType = 'addtoexisting'
            } else {
                queryType = 'imageupload'
            }
        })

        reader.readAsDataURL(selectedFile);
        reader.onload = function () {
            axios.post(`${base_url}/${queryType}`, {
                image_name: imageNameInput.value,
                collection_name: collectionNameInput.value,
                image: [reader.result.split(',')[1]],
                username: localStorage.getItem("user"),
                lat: fileMarker.lat,
                lon: fileMarker.lon,
                length: 1,
                tagged_people: [],
                public: isPublic.checked, // Default
                type: typeInput.value, // Default
                place: placeInput.value 

            })
            .then((response) => {
                if(response.status === 200) {
                    alert("Image uploaded successfully!")
                }
                })
            .catch((error) => {
                console.log(error);
                if(error.response.status === 401) {
                    alert("Error during image uploading...")
                }
            });
        };
        reader.onerror = function (error) {
            console.log('Error: ', error);
        };
    }

    const handleFileChange = event => {
        const fileObj = event.target.files && event.target.files[0];
        if (!fileObj)
            return;

        setSelectedFile(fileObj)

        let fileLat;
        let fileLon;
        const RGC_API_KEY = '01114512c1ce49018d40d94d6aab3d68'

        EXIF.getData(fileObj, function(){
            fileLat = EXIF.getTag(this, "GPSLatitude")
            fileLon = EXIF.getTag(this, "GPSLongitude")

            setFileMarker({
                lat: (fileLat[0].numerator/fileLat[0].denominator) +
                     ((fileLat[1].numerator/fileLat[1].denominator) / 60) +
                     ((fileLat[2].numerator/fileLat[2].denominator) / 3600),
    
                lon: (fileLon[0].numerator/fileLon[0].denominator) +
                     (fileLon[1].numerator/fileLon[1].denominator) / 60 +
                     (fileLon[2].numerator/fileLon[2].denominator) / 3600
            })

        })

        axios.get(`https://api.geoapify.com/v1/geocode/reverse?lat=${fileMarker.lat}&lon=${fileMarker.lon}&apiKey=${RGC_API_KEY}`)
            .then(response => {
                let imageNameInput = document.getElementById("file-metadata").querySelector("#imageNameInput");
                let placeInput = document.getElementById("file-metadata").querySelector("#placeInput");
                imageNameInput.value = `${fileObj.name}`
                placeInput.value = `${response.data.features[0].properties.city}`
                
                setPlace(response.data.features[0].properties.city)
            })
            .catch(error => console.log(error));
        
        Object.entries(document.getElementsByClassName("labelMetadata")).map(element => {
            element[1].style.visibility = "visible"
        })

        Object.entries(document.getElementsByClassName("metadataInput")).map(element => {
            element[1].style.visibility = "visible"
        })

        document.getElementById("confirm-upload").style.visibility = "visible"
        document.getElementById("publicSpan").style.visibility = "visible"
        document.getElementById("publicPhotoCheckBox").style.visibility = "visible"
        
        event.target.value = null;
    }

    const mIcon = new L.Icon({
        iconUrl: markerIcon,
        iconRetinaUrl: markerIcon,
        iconSize: [45, 48],
        shadowSize: [50, 64],
        iconAnchor: [22, 94],
        shadowAnchor: [4, 62],
        popupAnchor: [-3, -76],
    })

    return (
        <div id='uploadContent'>
            <div id="outer-rectangle">
                <div id="inner-rectangle">
                    <h1 id="inner-text">
                        Drag here your media or click the button below to select from your files...
                    </h1>
                    <input
                        style={{display: 'none'}}
                        ref={inputRef}
                        type="file"
                        accept=".png, .jpg, .zip"
                        onChange={handleFileChange}
                    />
                    <button id="chooseFileBtn" onClick={() => {inputRef.current.click()}}>
                        <h1 id="chooseFileText">
                            Choose file...
                        </h1>
                    </button>
                    <div id="inner-inner-rectangle">
                        <div id='labelsMetadata'>
                            <h5 className='labelMetadata'>Collection Name</h5>
                            <h5 className='labelMetadata'>Image Name</h5>
                            <h5 className='labelMetadata'>Type</h5>
                            <h5 className='labelMetadata'>Place</h5>
                        </div>

                        <div id='file-metadata'>
                            <input className='metadataInput' id="collectionNameInput" type='text' />
                            <input className='metadataInput' id="imageNameInput" type='text' />
                            <input className='metadataInput' id="typeInput" type='text' />
                            <input className='metadataInput' id="placeInput" type='text' />
                        </div>

                        <div id='buttons'>
                            <div className="form-check">
                                <input className="form-check-input"
                                    ref={setPublic}
                                    type="checkbox"
                                    id="publicPhotoCheckBox"
                                />
                                <span id='publicSpan'>Public</span>
                            </div>
                            <button id="confirm-upload" onClick={uploadOnDB}>
                                Confirm
                            </button>
                        </div>
                        
                    </div>
                </div>
            </div>

            <div id="LeafletMap">
                <MapContainer id="mapContainer" center={bolognaCoords} zoom={6} scrollWheelZoom={true} zoomControl={false} attributionControl={false}>
                    <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                    {
                        <Marker position={[fileMarker.lat, fileMarker.lon]} icon={mIcon}>
                            <Popup>Photo taken here.</Popup>
                        </Marker>
                    }
                </MapContainer>
            </div>
        </div>
    );
}

export default UploadPhoto;