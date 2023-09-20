import '../css/uploadPhoto.css';
import { MapContainer, Marker, Popup, TileLayer } from "react-leaflet";
import 'leaflet/dist/leaflet.css';

function UploadPhoto() {
    const bolognaCoords = [44.494887, 11.3426163]
  return (

    <div id='uploadContent'>
        <div id ="outer-rectangle">
            <div id ="inner-rectangle">
                <h1 id="inner-text">
                    Drag here your media or click the button below to select from your files...
                </h1>
                <button id="chooseFileBtn">
                    <h1 id="chooseFileText">
                        Choose file...
                    </h1>
                </button>
            </div>
        </div>

        <div id="LeafletMap">
            <MapContainer center={bolognaCoords} zoom={14} scrollWheelZoom={true} zoomControl={false} attributionControl={false}>
                <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
            </MapContainer>
      </div>
    </div>
  );
}

export default UploadPhoto;