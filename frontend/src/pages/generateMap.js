import '../css/generateMap.css'
import 'leaflet/dist/leaflet.css';
import { useState } from 'react';
import Button from 'react-bootstrap/Button';
import { MapContainer, Marker, Popup, useMapEvents, MapConsumer, TileLayer, GeoJSON, useMap } from "react-leaflet";
import regioni from '../regioniit.json'
import europe from '../europe.json'
import * as L from 'leaflet';
import markerIcon from '../marker-icon.png'
import * as d3 from "d3";

const GenerateMap = () => {

    function GetCoords() {
        useMapEvents({
          click(e) {
            featureContainer(europe, [e.latlng.lng, e.latlng.lat])
          }
        })
      }
    
      function UploadGeoJSON() {
        const map = useMap()
        useMapEvents({
          click(e) {
            return L.marker(e.latlng).addTo(map);
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

    // function toggleMenu() {
    //     const menu = document.getElementById("backgroundItems")
    //     if (menu.style.display === "none") 
    //         menu.style.display = "flex";
    //     else 
    //         menu.style.display = "none";
    // }

    const bolognaCoords = [44.494887, 11.3426163]

    return (
        <div id="LeafletMap">
            <div id="menuOptions">
                <Button className="menuItem">
                    Choose map type
                </Button>
                <Button className="menuItem">Cluster</Button>
                <Button className="menuItem">Photo per area</Button>
                <Button className="menuItem">Heatmap</Button>
            </div>
            
            <MapContainer id="mapContainer" center={bolognaCoords} zoom={14} scrollWheelZoom={true} zoomControl={false} attributionControl={false}>
                <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
            </MapContainer>
            
        </div>
    )
}

export default GenerateMap;