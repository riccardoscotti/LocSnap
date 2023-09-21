import '../css/uploadPhoto.css';
import { MapContainer, Marker, Popup, TileLayer } from "react-leaflet";
import 'leaflet/dist/leaflet.css';
import {useRef} from 'react';

function UploadPhoto() {

    const inputRef = useRef(null);
    const bolognaCoords = [44.494887, 11.3426163]

    const handleFileChange = event => {
        const fileObj = event.target.files && event.target.files[0];
        if (!fileObj)
            return;
        
        event.target.value = null;
    }

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
                        onChange={handleFileChange}
                    />
                    <button id="chooseFileBtn" onClick={() => {inputRef.current.click()}}>
                        <h1 id="chooseFileText">
                            Choose file...
                        </h1>
                    </button>
                </div>
            </div>

            <div id="LeafletMap">
                <MapContainer id="mapContainer" center={bolognaCoords} zoom={14} scrollWheelZoom={true} zoomControl={false} attributionControl={false}>
                    <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                </MapContainer>
            </div>
        </div>
    );
}

export default UploadPhoto;