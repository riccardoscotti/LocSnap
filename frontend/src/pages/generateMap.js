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

    function GetCoords() {
        useMapEvents({
          click(e) {
            // featureContainer(europe, [e.latlng.lng, e.latlng.lat])
          }
        })
      }

      function showMarkersOfFeature(feature) {
        console.log(feature.properties.feature_name);
      }
    
      function featureContainer(json, point) {
          for (let index = 0; index < json.features.length; index++) {
            if (d3.geoContains(json.features[index], point)) {
              showMarkersOfFeature(json.features[index])
            }
          }
      }

    const bolognaCoords = [44.494887, 11.3426163]

    return (
        <div id="LeafletMap2">
            <div id="menuOptions">
                <Button className="menuItem">
                    Choose map type
                </Button>
                <Button className="menuItem">Cluster</Button>
                <Button className="menuItem">Photo per area</Button>
                <Button className="menuItem">Heatmap</Button>
            </div>
            
            <MapContainer id="mapContainer2" center={bolognaCoords} zoom={5} scrollWheelZoom={true} zoomControl={false} attributionControl={false}>
                <GeoJSON data={Object(JSON.parse(localStorage.getItem("geojson")))} />
                <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
            </MapContainer>
            
        </div>
    )
}

export default GenerateMap;