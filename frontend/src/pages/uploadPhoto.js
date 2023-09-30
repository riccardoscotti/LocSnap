import '../css/uploadPhoto.css';
import { MapContainer, Marker, Popup, TileLayer } from "react-leaflet";
import 'leaflet/dist/leaflet.css';
import {useRef, useState} from 'react';
import markerIcon from '../marker-icon.png'
import * as L from "leaflet";
import EXIF from 'exif-js'

function UploadPhoto() {

    const inputRef = useRef(null);
    const bolognaCoords = [44.494887, 11.3426163]
    var [fileMarker, setFileMarker] = useState({lat: 0.0, lon: 0.0})

    const handleFileChange = event => {
        const fileObj = event.target.files && event.target.files[0];
        if (!fileObj)
            return;

        var fileLat;
        var fileLon;

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
                </div>
            </div>

            <div id="LeafletMap">
                <MapContainer id="mapContainer" center={bolognaCoords} zoom={14} scrollWheelZoom={true} zoomControl={false} attributionControl={false}>
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