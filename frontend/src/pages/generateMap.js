import '../css/generateMap.css'
import 'leaflet/dist/leaflet.css';
import { useState, useRef } from 'react';
import Button from 'react-bootstrap/Button';
import { MapContainer, Marker, Popup, useMapEvents, MapConsumer, TileLayer, GeoJSON, useMap } from "react-leaflet";
import * as L from 'leaflet';
import us from '../us.json'
import markerIcon from '../marker-icon.png'
import * as d3 from "d3";

const GenerateMap = () => {
  localStorage.removeItem("buttonClicked") // Prevent old session saves

    function showFeatureMarkers(area) {

    }

    function featureContainer(json, point) {
        for (let index = 0; index < json.features.length; index++) {
          if (d3.geoContains(json.features[index], point)) {
            console.log(json.features[index].properties.feature_name)
            showFeatureMarkers(json)
          }
        }
    }

    function AddGeoJSON() {
      const map = useMap();
      var layerPostalcodes = L.geoJSON()
        .addData(Object(JSON.parse(localStorage.getItem("geojson"))))
        .addTo(map);
    }

    function PhotoPerArea() {
      useMapEvents({
        click(e) {
          if (localStorage.getItem("buttonClicked") == "ppa") {
            featureContainer(
              Object(JSON.parse(localStorage.getItem("geojson"))),
              [e.latlng.lng, e.latlng.lat]
            )
          }
        }
      })
    }

    const bolognaCoords = [44.494887, 11.3426163]

    return (
        <div id="LeafletMap2">
            <div id="menuOptions">
                <Button className="menuItem">
                    Choose map type
                </Button>
                <Button className="menuItem">Cluster</Button>
                <Button className="menuItem" onClick={
                  () => {
                    localStorage.setItem("buttonClicked", "ppa")
                    alert("Select the country you're concerned in");
                    }}>
                    Photo per area
                </Button>
                <Button className="menuItem">Heatmap</Button>
            </div>
            
            <MapContainer id="mapContainer2" center={bolognaCoords} zoom={5} scrollWheelZoom={true} zoomControl={false} attributionControl={false}>
                <AddGeoJSON />
                <PhotoPerArea />
                <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
            </MapContainer>
            
        </div>
    )
}

export default GenerateMap;